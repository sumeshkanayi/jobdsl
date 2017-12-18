import jenkins.model.Jenkins
import hudson.model.Item
import hudson.model.Items

String projectRoot = 'global/us/bdx/dt8'

String gitHost = 'ssh://'
String FOLDER_CREDENTIALS_PROPERTY_NAME = 'com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider$FolderCredentialsProperty'

Node folderCredentialsPropertyNode
Item myFolder = Jenkins.instance.getItem(projectRoot)
if (myFolder) {
  def folderCredentialsProperty = myFolder.getProperties().getDynamic(FOLDER_CREDENTIALS_PROPERTY_NAME)
  if (folderCredentialsProperty) {
    String xml = Items.XSTREAM2.toXML(folderCredentialsProperty)
    folderCredentialsPropertyNode = new XmlParser().parseText(xml)
  }
}

else {

    println("not present")
    projectRootArray=projectRoot.split("/")
    print projectRootArray
    initialpath=""
    projectRootArray.each {
        initialpath=initialpath+"/"+it
        folderPresence=Jenkins.instance.getItem(initialpath)
        if (folderPresence==null){
            folder(initialpath){

                println(it)
                print(initialpath)
            }


        }



    }
}


folder(projectRoot) {
  if (folderCredentialsPropertyNode) {
    configure { project ->
      project / 'properties' << folderCredentialsPropertyNode
    }
    displayName projectRoot
    description 'Build jobs for building ' + projectRoot + 'artifacts' 

  }
}
[
    'com.rxcorp.dt4.web'
].eachWithIndex { repoName, index ->
    multibranchPipelineJob(projectRoot + '/' + repoName) {
        branchSources {
            git {
                remote(gitHost + '/' + projectRoot + '/' + repoName + '.git')
                credentialsId('gitlab_public_deploy_key')
            }
        }
        orphanedItemStrategy {
            discardOldItems {
                numToKeep(5)
            }
        }
    }
}
