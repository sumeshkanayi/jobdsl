package devops.seed
import jenkins.model.Jenkins
import hudson.model.Item
import hudson.model.Items

public class seedJob {

    public static void create_multibranch_job(def dslFactory) {

        def projectName = "global/us/bdf/com.rxcorp.sample"
        def splitProjectNameToPathAndRepo = projectName.tokenize("/")
        def repoName = splitProjectNameToPathAndRepo[-1]
        splitProjectNameToPathAndRepo.pop()
        def projectRoot = splitProjectNameToPathAndRepo.join("/")


        String gitHost = 'ssh://git@git-sb.sumesh.com'



        String FOLDER_CREDENTIALS_PROPERTY_NAME = 'com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider$FolderCredentialsProperty'

        Node folderCredentialsPropertyNode
        Item myFolder = Jenkins.instance.getItem(projectRoot)
        if (myFolder) {
            def folderCredentialsProperty = myFolder.getProperties().getDynamic(FOLDER_CREDENTIALS_PROPERTY_NAME)
            if (folderCredentialsProperty) {
                String xml = Items.XSTREAM2.toXML(folderCredentialsProperty)
                folderCredentialsPropertyNode = new XmlParser().parseText(xml)
            }
        } else {


            def projectRootArray = projectRoot.split("/")

            String folderWalk = ""
            projectRootArray.each {
                folderWalk = folderWalk + "/" + it
                def folderPresence = Jenkins.instance.getItem(folderWalk)
                if (folderPresence == null) {
                    dslFactory.folder(folderWalk) {


                    }


                }


            }
        }


        dslFactory.folder(projectRoot) {
            if (folderCredentialsPropertyNode) {
                configure { project ->
                    project / 'properties' << folderCredentialsPropertyNode
                }
                displayName projectRoot
                description 'Build jobs for building ' + projectRoot + 'artifacts'

            }
        }


        dslFactory.multibranchPipelineJob(projectRoot + '/' + repoName) {
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

}
