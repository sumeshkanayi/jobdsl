package com.rxcorp.devops.jenkins
import jenkins.model.Jenkins
import hudson.model.Item
import hudson.model.Items

/**
 * Created by Sumesh on 19/12/17.
 */
class sample {

    public static void createFolder(def justAnObject,def jobname){

        justAnObject.folder(jobname){


        }

    }

    public static void caller(def name){

        createFolder(this,name)
    }

}
