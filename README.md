# PostBuild

一个自动打包并上传到公测平台并发送邮件通知对应的插件

## 集成

### 1. 根build.gradle文件修改

在你工程的根build.gradle头部添加如下代码

```groovy
import groovy.json.JsonSlurper

// Top-level build file where you can add configuration options common to all sub-projects/modules.

//load config.json file
def jsonSlurper = new JsonSlurper()
def config = jsonSlurper.parse(file(rootProject.rootDir.getAbsolutePath() + File.separator + 'config.json'))

//注入到ext的config变量中
rootProject.ext.config = config
```


以便在初始化时加载全局配置信息

### 2. 主工程build.gradle配置

```groovy
//add
plugins {
    id 'com.fzm.post.build.plugin' version '1.1.3'
}
```
应用插件

### 3. 添加config.json配置文件
在project目录添加config.json文件

```json
{
  "minSdkVersion": 19,
  "targetSdkVersion": 23,
  "compileSdkVersion": 23,
  "buildToolsVersion": "23.0.3",

  "versionCode": 100004,
  "versionName": "1.0.0.06",
  "applicationId": "com.fzm.mobile.phone.rental",
  "appName": "MobilePhoneRentalAndroid",

  "upload": {

    "apiToken":"fir api Token",
    "apkPath":"/mobile-phone-rental/build/outputs/apk/release/mobile-phone-rental-release.apk",

    "senderName":"sender's name",
    "senderEmail":"sender's email",
    "senderEmailPwd":"pwd",
    "subject":"Android XX App 测试包有更新",
    "appFirImDownloadUrl":"app download url",
    "apiUrl":"http://122.224.124.250:10011/rent/api/测试服务地址",
    "receiverEmails":[
      "dbs@33.cn",
      "dbs@163.com"
    ],
    "jenkinsServerIP":"192.168.33.132",
    "jenkinsFileBackDir":"android-build"
  }
}
```



