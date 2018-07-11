# -*- coding:utf8 -*-
import codecs
import sys
import os
import json
from property_util import Properties

reload(sys)
sys.setdefaultencoding('utf8')

# 运行时环境变量
# jenkins_build_number = os.getenv('BUILD_NUMBER')
jenkins_build_number = '31'

# jenkins_job_name = os.getenv('JOB_NAME')
jenkins_job_name = 'TestDemo'

# jenkins_workspace = os.getenv('WORKSPACE')
jenkins_workspace = '/Users/dingbaosheng/.jenkins/workspace/TestDemo'
#
git_log = os.getenv('SCM_CHANGELOG')
if git_log is None:
    git_log = ''
print ('====================原始gitLog:' + git_log + '====================')
git_log = git_log.replace(' ', '\n')


config_file_path = os.getcwd()+'/config.properties'
print ('config_file_path:'+config_file_path)
properties = Properties(config_file_path)

#eg TestDemo
# app_name = properties.get("APP_NAME")
app_name = 'TestDemo'

#eg 1.0.1
app_version = sys.argv[1]
# app_version = '1.0.2'

app_package_name = properties.get("APP_PACKAGE_NAME")
api_token = properties.get("API_TOKEN")

build_apk_name = 'app-release-' + jenkins_build_number + '.apk'
apk_file_path = jenkins_workspace + '/app/build/outputs/apk/release/' + build_apk_name

print('====================jenkinsBuildNumber:' + jenkins_build_number + '====================')
print('====================jenkinsJobName:' + jenkins_job_name + '====================')
print('====================workspace:' + jenkins_workspace + '====================')
print('====================appName:' + app_name + '====================')
print('====================appVersion:' + app_version + '====================')
print("====================buildApkName:" + build_apk_name + '====================')
print("====================apkFilePath:" + apk_file_path + '====================')
print("====================gitLog:" + git_log + '====================')


# 获取app上传凭证
app_upload_token_url = 'curl -X "POST" "http://api.fir.im/apps" -H "Content-Type: application/json" -d "{\\"type\\":\\"android\\", \\"bundle_id\\":\\"%s\\", \\"api_token\\":\\"%s\\"}"' % (app_package_name, api_token)

print('====================getAppUploadTokenUrl:' + app_upload_token_url + " ====================")
app_token_response = os.popen(app_upload_token_url)

msg = app_token_response.readlines()

app_token_result = json.loads((msg[0]))
print('====================get upload toke response:', str(app_token_result), '=====================')
##check result
# status: 201

# {
#     "id": "5592ceb6537069f2a8000000",
#     "type": "ios",
#     "short": "yk37",
#     "cert": {
#         "icon": {
#             "key": "xxxxx",
#             "token": "xxxxxx",
#             "upload_url": "http://upload.qiniu.com"
#         },
#         "binary": {
#             "key": "xxxxx",
#             "token": "xxxxxx",
#             "upload_url": "http://upload.qiniu.com"
#         }
#     }
# }
print('')
print('====================get upload toke response:', str(app_token_result), '=====================')

errors = app_token_result.get('errors')
if errors is not None:

    print("get token fails; msg:", errors)

else:

    icon_key = app_token_result['cert']['icon']['key']
    icon_token = app_token_result['cert']['icon']['token']
    icon_upload_url = app_token_result['cert']['icon']['upload_url']

    binary_key = app_token_result['cert']['binary']['key']
    binary_token = app_token_result['cert']['binary']['token']
    binary_upload_url = app_token_result['cert']['binary']['upload_url']

    build_apk_name = 'app-release-' + jenkins_build_number + '.apk'


    # upload apk

    upload_apk_url = 'curl   -F "key=' + binary_key + '"' + \
                   ' -F "token=' + binary_token + '"' + \
                   ' -F "file=@' + apk_file_path + '"' + \
                   ' -F "x:name=' + app_name + '"' + \
                   ' -F "x:version=' + app_version + '"' + \
                   ' -F "x:build=' + jenkins_build_number + '"' + \
                   ' -F "x:changelog=' + git_log + '"' + \
                   ' https://up.qbox.me'

    print('====================upload apk url:' + upload_apk_url + '====================')
    uploadResult = os.popen(upload_apk_url)
    uploadApkMsg = uploadResult.readlines()
    app_token_result = json.loads((uploadApkMsg[0]))
    print('====================upload apk response:' + str(app_token_result) + '====================')

    # Example Response
    #  {
    #     "download_url": "xxx",
    #     "is_completed": true,
    #     "release_id": "5b34893a959d69701bd788eb"
    # }
    # process upload apk result
    if 'is_completed' in app_token_result and app_token_result['is_completed'] is True:
        print('====================上传APK文件到fir.im平台成功====================')
        print('====================下载地址:', app_token_result['download_url'], '====================')
        print('====================releaseId:', app_token_result['release_id'], '====================')

        ##apk文件上传成功后发送邮件；
        # 传递参数到发送邮件脚步中
        os.system("python send_email.py %s" % app_version)

    elif 'error' in app_token_result:
        print('====================上传APK文件失败====================')
        print('====================失败堆栈信息:', app_token_result.get('error'), '====================')
