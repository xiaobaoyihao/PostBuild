package com.fzm.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class PostBuildPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {


        project.extensions.create('post_build', PostBuildExtension)

        project.task('post_build_task') {

            doLast {
                println "hello plugin"
            }
        }


        println('=== apply method =====')

        project.afterEvaluate {


            if (!project.android) {
                throw new IllegalStateException('Must apply \'com.android.application\' or \'com.android.library\' frist !')
            }

            if (project.postBuild.name == null
                    || project.postBuild.email == null) {
                project.logger.info('postBuild config should be set !')

                return
            }

            name = project.postBuild.name
            email = project.postBuild.emal


            println('=====name:'+name+" email:"+email)


//            assembleRelease.doLast{
//
//                print('assemble Release doLast')
//            }

        }

    }
}