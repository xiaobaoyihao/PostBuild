package com.fzm.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class PostBuildPlugin implements Plugin<Project> {

    void apply(Project project) {


//        project.extensions.create('postBuildConfig', PostBuildExtension)

        println('=== apply method =====')

        project.afterEvaluate {

            if (!project.android) {
                throw new IllegalStateException('Must apply \'com.android.application\' or \'com.android.library\' first !')
            }

//            if (project.postBuildConfig.name == null
//                    || project.postBuildConfig.email == null) {
//                project.logger.info('postBuildConfig must be set !')
//
//                return
//            }

//            def name = project.postBuildConfig.name
//            def email = project.postBuildConfig.email
//
//
//            println('=====name:'+name+" email:"+email)


            project.task('upload',{


                def jenkinsJobName = System.getenv('JOB_NAME') ?: "local_job"
                def jenkinsBuild = System.getenv('BUILD_NUMBER') ?: "0"

                def vName = project.getProperties().get('vName',project.rootProject.ext.configProps.DEFAULT_VERSION_NAME)
                def shouldUpload = project.getProperties().get('shouldUpload', false)

                //判断是否需要执行拷贝文件，如果不是jenkins上build直接返回
                if(jenkinsJobName != 'local_job'){
                    //do nothing
                    return
                }


                def fileBackPath = project.rootProject.ext.configProps.JENKINS_FILE_BACK_DIR+"/"+ jenkinsJobName + "/" + jenkinsBuild

                println("==========jenkinsJobName:"+jenkinsJobName+"===========")
                println("==========jenkinsBuild:"+jenkinsBuild+"===========")
                println("==========vName:"+vName+"===========")
                println("==========shouldUpload:"+shouldUpload+"===========")
                println("==========fileBackPath:"+fileBackPath+"===========")

                println("==========begain copy apk file===========")

                project.copy {
                    from('build/outputs/apk/release')
                    into(fileBackPath + '/apks')
                    include('*.apk', '*.txt')
                    exclude('**/*-unaligined.apk')
                }
//
                println("==========end copy apk file===========")
//
//
                println("==========begain copy mapping file===========")
                project.copy {
                    from('build/outputs/mapping/release')
                    into(fileBackPath + '/apks/mapping')
                    include('*.txt')
                }
                println("==========end copy mapping file===========")



                println("shouldUpload;"+shouldUpload)

                if (shouldUpload == "true") {
                    println('==========begin execute upload file task==========')

//                    String pythonScriptPath = "${project.rootDir.getAbsolutePath()}/app/upload.py"
                    String pythonScriptPath = "upload.py"
                    println('cmd:'+pythonScriptPath)
//                    return;
                    String[] cmd = new String[3];
                    cmd[0] = "python"; // check version of installed python: python -V
                    cmd[1] = pythonScriptPath
                    cmd[2] = vName

                    // create runtime to execute external command
                    Runtime rt = Runtime.getRuntime();
                    Process pr = rt.exec(cmd);

                    // retrieve output from python script
                    BufferedReader bfr = null;
                    try{
                        bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                        String line = ""
                        while((line = bfr.readLine()) != null) {
                            // display each output line form python script
                            println(line)
                        }
                    }finally {
                        bfr.close()
                    }

                } else {
                    println('==========no need upload file==========')
                }


            })

        }

    }
}