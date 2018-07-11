# -*- coding:utf8 -*-

import smtplib
import sys
import os
from email import encoders
from email.header import Header
from email.mime.text import MIMEText
from email.utils import parseaddr, formataddr
from property_util import Properties


def _format_addr(s):
    name, addr = parseaddr(s)
    return formataddr(( \
        Header(name, 'utf-8').encode(), \
        addr.encode('utf-8') if isinstance(addr, unicode) else addr))


properties = Properties('../config.properties')

# 发件人
sender_email_address = properties.get("SENDER_EMAIL_ADDRESS")

# 收件人，通常时对应测试，产品，主管
receivers_email_address = properties.get("RECEIVERS_EMAIL_ADDRESS").split(',')

# 发件人邮箱密码
password = properties.get("SENDER_EMAIL_PWD")

smtp_server = 'smtp.exmail.qq.com'
server_port = 25

git_log = os.getenv('SCM_CHANGELOG')

blank_char = '&emsp;&emsp;'

if git_log:

    print('==========git_log:' + git_log + "==========")
    # 获取的单个git记录中没有换行会变成空格，所以需要把空格替换为换行(html)
    # 获取多个git记录会存在换行，需要转为<br/>
    git_log = git_log.replace(' ', '<br/>' + blank_char)
    git_log = git_log.replace('\n', '<br/>' + blank_char)
    git_log += blank_char
    pass
else:
    git_log = ''

# add
# gitLog = '1.first 2.second 3.third2'

print('==========处理后的git_log:' + git_log + "==========")


app_desc = properties.get("APP_DESC")
app_download_url = properties.get("APP_FIR_IM_DOWNLOAD_URL")
app_version = sys.argv[1]
# appVersion = '1.0.2'

print ('==========appDesc:' + app_desc + '==========')
print ('==========appVersion' + app_version + '==========')
apiUrl = properties.get("API_URL")

update_content = ''

if git_log:
    update_content = '更新内容:<br>' + blank_char + git_log + '<br>'
else:
    update_content = ''

email_content = 'App下载地址:<a href=\"' + app_download_url + '\">' + app_download_url + '</a><br>' + \
                '版本号:' + app_version + '<br>' + \
                'API地址:<a href=\"' + apiUrl + '\">' + apiUrl + '</a><br>' + \
                update_content

print('==========email_content:' + email_content + "==========")

subject = app_desc + ' Android测试包有更新'

senderName = properties.get("SENDER_NAME")

msg = MIMEText(email_content, 'html', 'utf-8')
msg['From'] = _format_addr('%s <%s>' % (senderName, sender_email_address))
msg['To'] = _format_addr('<%s>' % receivers_email_address)
msg['Subject'] = Header(subject, 'utf-8').encode()

try:
    server = smtplib.SMTP(smtp_server, server_port)  # SMTP协议默认端口是25
    # server.set_debuglevel(1)
    server.login(sender_email_address, password)
    server.sendmail(sender_email_address, receivers_email_address, msg.as_string())
    print ('====================发送邮件成功====================')
except smtplib.SMTPException as e:
    print ('====================发送邮件失败====================')
    print ('====================失败异常:' + e + '====================')
finally:
    server.quit()
