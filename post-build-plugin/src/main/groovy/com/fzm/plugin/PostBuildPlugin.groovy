package com.fzm.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class PostBuildPlugin implements Plugin<Project> {

    def backFileRootDir = "/home/qwe/shared/"
//    def backFileRootDir = "/Users/dingbaosheng/shared/"

    void apply(Project project) {


        project.tasks.whenTaskAdded { task ->


            if (task.getName() == "assembleDebug") {

                task.doLast {


                    def jenkinsJobName = System.getenv('JOB_NAME') ?: "local_job"
                    def jenkinsBuild = System.getenv('BUILD_NUMBER') ?: "0"

                    def vName = project.getProperties().get('vName', project.rootProject.ext.config.versionName)
                    def shouldUpload = project.getProperties().get('shouldUpload', false)

                    //判断是否需要执行拷贝文件，如果不是jenkins上build直接返回
                    if (jenkinsJobName == 'local_job') {
                        //do nothing
                        return
                    }


                    def fileBackPath = backFileRootDir + project.rootProject.ext.config.upload.jenkinsFileBackDir + File.separator + jenkinsJobName + File.separator + jenkinsBuild

                    println("==========jenkinsJobName" + jenkinsJobName + "===========")
                    println("==========jenkinsBuild:" + jenkinsBuild + "===========")
                    println("==========vName:" + vName + "===========")
                    println("==========shouldUpload:" + shouldUpload + "===========")
                    println("==========fileBackPath:" + fileBackPath + "===========")

                    println("==========begain copy apk file===========")

                    project.copy {
                        from('build/outputs/apk/debug')
                        into(fileBackPath + '/apks')
                        include('*.apk', '*.txt')
                        exclude('**/*-unaligined.apk')
                    }
//
                    println("==========end copy apk file===========")

                }
            } else if (task.getName() == "assembleRelease") {


                task.doLast {


                    println("=====assembleRelease.doLast======")

                    def jenkinsJobName = System.getenv('JOB_NAME') ?: "local_job"
                    def jenkinsBuild = System.getenv('BUILD_NUMBER') ?: "0"

                    def vName = project.getProperties().get('vName', project.rootProject.ext.config.versionName)
                    def shouldUpload = project.getProperties().get('shouldUpload', false)

                    //判断是否需要执行拷贝文件，如果不是jenkins上build直接返回
                    if (jenkinsJobName == 'local_job') {
                        //do nothing
                        return
                    }


                    def fileBackPath = backFileRootDir + project.rootProject.ext.config.upload.jenkinsFileBackDir + File.separator + jenkinsJobName + File.separator + jenkinsBuild

                    println("==========jenkinsJobName:" + jenkinsJobName + "===========")
                    println("==========jenkinsBuild:" + jenkinsBuild + "===========")
                    println("==========vName:" + vName + "===========")
                    println("==========shouldUpload:" + shouldUpload + "===========")
                    println("==========fileBackPath:" + fileBackPath + "===========")

                    println("==========begain copy apk file===========")

                    project.copy {
                        from('build/outputs/apk/release')
                        into(fileBackPath + '/apks')
                        include('*.apk', '*.txt')
                        exclude('**/*-unaligined.apk')
                    }
//
                    println("==========end copy apk file===========")


                    println("==========begain copy mapping file===========")
                    project.copy {
                        from('build/outputs/mapping/release')
                        into(fileBackPath + '/apks/mapping')
                        include('*.txt')
                    }
                    println("==========end copy mapping file===========")



                    println("shouldUpload:" + shouldUpload)

                    if (shouldUpload == "true") {
                        println('==========begin execute upload file task==========')

                        try {

                            Util.upload(project.rootDir)
                        }catch (Exception e) {
                            println("upload fails :" + e.getMessage())
                        }

                    } else {
                        println('==========no need upload file==========')
                    }


                }
            }

        }


    }
}