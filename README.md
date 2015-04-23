
签名文件的密码和别名

密码：12345678

别名：hycs-tencent

应用宝测试环境到正式环境的处理
1.应用宝后台支付配置中设置正式资料
2，修改sdkobject。java中支付设置unipayAPI.setEnv("test");test为release
3.修改assets的/msdkconfig.xml中
正式环境与测试环境选择，msdktest为测试环境域名, msdk为正式环境域名
MSDK_URL=http://msdktest.qq.com
;MSDK_URL=http://msdk.qq.com
4，删除测试手机的腾讯支付服务，
