package com.rxcorp.devops.jenkins
import jenkins.model.Jenkins
import hudson.model.Item
import hudson.model.Items

/**
 * Created by Sumesh on 01/12/17.
 */
class SeedJob {

    public static Map set_naming_convention_right(def projectName){

        def splitProjectNameToPathAndRepo = projectName.tokenize("/")
        def repoName = splitProjectNameToPathAndRepo[-1]
        splitProjectNameToPathAndRepo.pop()
        def projectRoot = splitProjectNameToPathAndRepo.join("/")
        return [projectRoot:projectRoot,repoName:repoName,gitHost:'ssh://git@git-sb.rxcorp.com']


    }


    public static void setup_jekins_folder(def dslFactory,def projectRoot){

        def projectRootArray=projectRoot.split("/")
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
















    }

    public static void create_multibranch(def dslFactory,def projectName) {


       Map projectMap=this.set_naming_convention_right(projectName)
        def projectRoot=projectMap["projectRoot"]
        def repoName=projectMap["repoName"]
        def gitHost = projectMap["gitHost"]
        setup_jekins_folder(dslFactory,projectRoot)





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



    public static void create_pipeline(def dslFactory,def projectName) {




        def projectMap=this.set_naming_convention_right(projectName)
        def projectRoot=projectMap["projectRoot"]
        def gitHost = projectMap["gitHost"]
        def repoName=projectMap["repoName"]
        setup_jekins_folder(this,projectRoot)



        dslFactory.folder(projectRoot) {
            if (folderCredentialsPropertyNode) {
                configure { project ->
                    project / 'properties' << folderCredentialsPropertyNode
                }
                displayName projectRoot
                description 'Build jobs for building ' + projectRoot + 'artifacts'

            }
        }













        dslFactory.pipelineJob(projectName) {

            definition{
                cpsScm{

                    scm{
                        git{
                            remote{
                                url(gitHost + '/' + projectRoot + '/' + repoName + '.git')


                            }



                        }


                    }




                }





            }









        }




















    }



}


