import jenkins.model.Jenkins
import hudson.model.Item
import hudson.model.Items

String projectRoot = 'glob/dex/dt'

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


    projectRootArray=projectRoot.split("/")

    folderWalk=""
    projectRootArray.each {
        folderWalk=initialpath+"/"+it
        folderPresence=Jenkins.instance.getItem(folderWalk)
        if (folderPresence==null){
            folder(folderWalk){


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
    'com.rxcorp.dt4.web',
        'com.rxcorp.dt9.webs'
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
