package com.fzm.plugin

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import javax.mail.*
import javax.mail.internet.*

class Util {

    private static String sRootDirPath

    public static boolean sIsTest = false


    static void upload(File rootDir) throws Exception {

        if (rootDir) {
            sRootDirPath = rootDir.getPath();
        }

        def jenkinsBuildNumber = System.getenv('BUILD_NUMBER')
        if (sIsTest) {
            jenkinsBuildNumber = '31'
        }

        def jenkinsWorkspace = System.getenv('WORKSPACE')
        if (sIsTest) {
            jenkinsWorkspace = '/Users/dingbaosheng/work/fzm/projects/android-test-demo'
        }

        def gitLog = System.getenv('SCM_CHANGELOG')
        println('=======gitLog:' + gitLog + "===========")
        if (gitLog) {
            gitLog = gitLog.replace(' ', '\n')
        } else {
            gitLog = ''
        }

        //load config.json file
        def configFile = new File(rootDir.getPath() + File.separator + 'config.json')
        def configJson = new JsonSlurper().parseText(configFile.text)

        //eg TestDemo
        def appName = configJson.appName

        //eg 1.0.1
        def appVersion = configJson.versionName
        // app_version = '1.0.2'

        def appPackageName = configJson.applicationId


        def apiToken = configJson.upload.apiToken

        def apkFilePath = jenkinsWorkspace + configJson.upload.apkPath

        println('====================jenkinsBuildNumber:' + jenkinsBuildNumber + '====================')
//        # println('====================jenkinsJobName:' + jenkinsJobName + '====================')
        println('====================workspace:' + jenkinsWorkspace + '====================')
        println('====================appName:' + appName + '====================')
        println('====================appVersion:' + appVersion + '====================')
        println("====================apkFilePath:" + apkFilePath + '====================')
        println("====================gitLog:" + gitLog + '====================')
        println("====================apiToken:" + apiToken + '====================')
        println("====================appPackageName:" + appPackageName + '====================')

        // 获取app上传凭证
//        String appUploadTokenUrl = 'curl -X "POST" "http://api.fir.im/apps" -H "Content-Type: application/json" -d "{\\"type\\":\\"android\\", \\"bundle_id\\":\\"${appPackageName}\\", \\"api_token\\":\\"${apiToken}\\"}"'
        def appUploadTokenUrl = ['bash', '-c', "curl -X \"POST\" \"http://api.fir.im/apps\" -H \"Content-Type: application/json\" -d \"{\\\"type\\\":\\\"android\\\", \\\"bundle_id\\\":\\\"${appPackageName}\\\", \\\"api_token\\\":\\\"${apiToken}\\\"}\""]

        println('====================getAppUploadTokenUrl====================')
        println(appUploadTokenUrl)
        println('========================================')


        def app_token_response = appUploadTokenUrl.execute().text

        def appTokenResponseJson = new JsonSlurper().parseText(app_token_response)

        println('====================get upload toke response: start====================')
        println(JsonOutput.prettyPrint(app_token_response))
        println('===================get upload toke response: end=====================')
        // ##check result
//        # status: 201
//
//        # {
//            #     "id": "5592ceb6537069f2a8000000",
//            #     "type": "ios",
//            #     "short": "yk37",
//            #     "cert": {
//                #         "icon": {
//                    #             "key": "xxxxx",
//                    #             "token": "xxxxxx",
//                    #             "upload_url": "http://upload.qiniu.com"
//                    #         },
//                #         "binary": {
//                    #             "key": "xxxxx",
//                    #             "token": "xxxxxx",
//                    #             "upload_url": "http://upload.qiniu.com"
//                    #         }
//                #     }
//            # }


        def errors = appTokenResponseJson.errors
        if (errors) {
            println(" upload break. beacause get token fails; msg:", errors)
            return
        }

        def iconKey = appTokenResponseJson.cert.icon.key
        def iconToken = appTokenResponseJson.cert.icon.token
        def iconUploadUrl = appTokenResponseJson.cert.icon.upload_url

        def binaryKey = appTokenResponseJson.cert.binary.key
        def binaryToken = appTokenResponseJson.cert.binary.token
        def binaryUploadUrl = appTokenResponseJson.cert.binary.upload_url

        //upload apk
        def uploadApkUrl = ['bash', '-c', 'curl   -F "key=' + binaryKey + '"' +        \
            ' -F "token=' + binaryToken + '"' +        \
            ' -F "file=@' + apkFilePath + '"' +        \
            ' -F "x:name=' + appName + '"' +        \
            ' -F "x:version=' + appVersion + '"' +        \
            ' -F "x:build=' + jenkinsBuildNumber + '"' +        \
            ' -F "x:changelog=' + gitLog + '"' +        \
            ' https://up.qbox.me']

        println('====================upload apk url====================')
        println(uploadApkUrl)
        println('========================================')


        def appTokenResult = uploadApkUrl.execute().text

//        # Example Response
//        #  {#     "download_url": "xxx",
//            #     "is_completed": true,
//            #     "release_id": "5b34893a959d69701bd788eb"
//            # }# process upload apk result

        def uploadApkResponseJson = new JsonSlurper().parseText(appTokenResult)
        println("====================上传APK文件响应 start====================")
        println(JsonOutput.prettyPrint(appTokenResult))
        println('====================上传APK文件响应 end====================')

        if (uploadApkResponseJson && uploadApkResponseJson.is_completed) {

            println('====================上传APK文件到fir.im平台成功====================')
            println('====================下载地址:' + uploadApkResponseJson.download_url + '====================')
            println('====================releaseId:' + uploadApkResponseJson.release_id + '====================')

            // ##apk文件上传成功后发送邮件；
            // 传递参数到发送邮件脚步中
//            os.system ( "python send_email.py %s" % app_version )
            sendEmail(appVersion)

        } else if (uploadApkResponseJson && uploadApkResponseJson.error) {
            println('====================上传APK文件失败====================')
            println('====================失败堆栈信息====================')
            println(uploadApkResponseJson.error)
            println('========================================')
        } else {
            println('====================上传APK文件失败 原因:未知====================')
        }
    }

    private static void sendEmail(String appVersion) {

        // load config.json
        def configFile = new File(sRootDirPath + File.separator + 'config.json')
        def configJson = new JsonSlurper().parseText(configFile.text)

        //发件人
        def sender_email_address = configJson.upload.senderEmail
        println('==========sender_email_address:' + sender_email_address + '==========')

        //收件人，通常时对应测试，产品，主管
        def receivers_email_address = configJson.upload.receiverEmails
        println('==========receivers_email_address:' + receivers_email_address + '==========')

        //发件人邮箱密码
        def password = configJson.upload.senderEmailPwd

        def smtp_server = 'smtp.exmail.qq.com'
        def server_port = 25

        def git_log = System.getenv('SCM_CHANGELOG')

        def blank_char = '&emsp;&emsp;&emsp;&emsp;'


        println('==========git_log:' + git_log + "==========")
        //获取的单个git记录中没有换行会变成空格，所以需要把空格替换为换行(html)
        //获取多个git记录会存在换行，需要转为<br/>
        if (git_log) {

            git_log = git_log.replace(' ', '<br/>' + blank_char)
            git_log = git_log.replace('\n', '<br/>' + blank_char)
            git_log += blank_char
        } else {
            git_log = '<br>'
        }

        println('==========处理后的git_log:' + git_log + "==========")

        def app_download_url = configJson.upload.appFirImDownloadUrl
        def app_version = appVersion

        println('==========appVersion' + app_version + '==========')
        def apiUrl = configJson.upload.apiUrl

        def update_content = ''

        if (git_log) {
            update_content = '更新内容:<br>' + blank_char + git_log + '<br>'
        } else {
            update_content = '<br>'
        }

        def jenkins_build_number = System.getenv('BUILD_NUMBER')
        if (sIsTest) {
            jenkins_build_number = '12'
        }

        def jenkins_job_name = System.getenv('JOB_NAME')
        if (sIsTest) {
            jenkins_job_name = 'job_name'
        }

        //http://192.168.33.132:8080/download/
        def local_download_url = "http://" +     \
                         configJson.upload.jenkinsServerIP +     \
                         ":8080/download/" +     \
                         configJson.upload.jenkinsFileBackDir +     \
                         "/" + jenkins_job_name +     \
                         "/" + jenkins_build_number +     \
                         "/apks"

        //local_download_url = "http://192.168.33.132:8080/download/"

        def email_content = 'App下载地址:<a href=\"' + app_download_url + '\">' + app_download_url + '</a><br>' +     \
                    '本地下载地址:<a href=\"' + local_download_url + '\">' + local_download_url + '</a><br>' +     \
                    '版本号:' + app_version + '<br>' +     \
                    'API地址:<a href=\"' + apiUrl + '\">' + apiUrl + '</a><br>' +     \
                    update_content +     \
                    '<b>注意:</b><br>' +     \
                    blank_char + '此邮件为自动发送，请勿回复!'

        println('==========email_content:' + email_content + "==========")

        def subject = configJson.upload.subject
        println('==========subject:' + subject + '==========')

        def sender_name = configJson.upload.senderName
        println('==========sender_name:' + sender_name + '==========')

        //设置邮件服务器参数、服务器端口等参数
        Properties props = new Properties()
        props.put("mail.smtp.host", smtp_server)
        props.put("mail.smtp.auth", "true");
        props.put("mail.transport.protocol", "smtp")
        //props.put("mail.smtp.port","25");  使用第三方的smtp服务器可以不用设置端口

        //设置Session对象，同时配置验证方法
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender_email_address, password)
            }
        })

        try {
            //创建Message对象，并设置相关参数
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender_email_address));


            InternetAddress[] receiverAddressList = new InternetAddress[receivers_email_address.size]
            for (int i = 0; i < receivers_email_address.size; i++) {

                receiverAddressList[i] = new InternetAddress(receivers_email_address[i])
            }


            message.setRecipients(Message.RecipientType.TO, receiverAddressList)

            //设置发送信息主题.信息正文
            message.setSubject(subject)
            message.setContent(email_content, "text/html;charset=utf-8")

            //发送信息
            Transport.send(message)
            System.out.println("==========send Email success!==========")
        } catch (MessagingException e) {
            e.printStackTrace()
            println('==========send Email fails' + e.getMessage() + '==========')
        }

    }
}