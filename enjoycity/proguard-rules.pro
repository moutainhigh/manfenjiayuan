# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/bingshanguxue/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#压缩: 移除无效的类、属性、方法等
#优化: 优化字节码，并删除未使用的结构

#包明不混合大小写
-dontusemixedcaseclassnames
# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
#不优化输入的类文件
-dontoptimize
#预校验
-dontpreverify

#混淆: 将类名、属性名、方法名混淆为难以读懂的字母，比如a,b,c
-flattenpackagehierarchy
-allowaccessmodification
-printmapping map.txt

#指定代码的压缩级别
-optimizationpasses 7
#混淆时是否记录日志
-verbose

-keepattributes Exceptions,InnerClasses
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-ignorewarnings
# 不进行混淆保持原样
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends java.lang.Throwable {*;}
-keep public class * extends java.lang.Exception {*;}


# 支付宝 开始
#-libraryjars libs/alipaySDK-20160623.jar

#-keep class com.alipay.android.app.IAlixPay{*;}
#-keep class com.alipay.android.app.IAlixPay$Stub{*;}
#-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
#-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
#-keep class com.alipay.sdk.app.PayTask{ public *;}
#-keep class com.alipay.sdk.app.AuthTask{ public *;}
# 支付宝 结束
