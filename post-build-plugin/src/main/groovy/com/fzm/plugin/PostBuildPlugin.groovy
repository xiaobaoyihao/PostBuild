package com.fzm.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class PostBuildPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {


        project.extensions.create('postBuildConfig', PostBuildExtension)

        project.task('uploadAndSendEmail') {

            doLast {
                println "hello plugin"
            }
        }


        println('=== apply method =====')

        project.afterEvaluate {


            if (!project.android) {
                throw new IllegalStateException('Must apply \'com.android.application\' or \'com.android.library\' first !')
            }

            if (project.postBuildConfig.name == null
                    || project.postBuildConfig.email == null) {
                project.logger.info('postBuildConfig must be set !')

                return
            }

            name = project.postBuildConfig.name
            email = project.postBuildConfig.email


            println('=====name:'+name+" email:"+email)


            project.assembleRelease.doLast{

                print('assemble Release doLast')
            }

        }

    }
}