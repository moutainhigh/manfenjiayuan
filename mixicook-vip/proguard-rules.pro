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

#默认开启，用以减小应用体积，移除未被使用的类和成员，并且会在优化动作执行之后再次执行（因为优化后可能会再次暴露一些未被使用的类和成员）。
#-dontshrink
#包明不混合大小写
-dontusemixedcaseclassnames
# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
#不优化输入的类文件
-dontoptimize
#指定代码的压缩级别,n 表示proguard对代码进行迭代优化的次数，Android一般为5
-optimizationpasses 5

#预校验
-dontpreverify

#混淆: 将类名、属性名、方法名混淆为难以读懂的字母，比如a,b,c
-flattenpackagehierarchy
-allowaccessmodification
-printmapping map.txt

#混淆时是否记录日志
-verbose

# Preserve some attributes that may be required for reflection.
#-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes Exceptions,InnerClasses
#不跳过library中的非public的类
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

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
# 保护指定的类和类的成员的名称，如果所有指定的类成员出席
-keepclasseswithmembernames class * {
    native <methods>;
}
# 保护指定的类和类的成员，但条件是所有指定的类和类成员是要存在
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
# We want to keep methods in Activity that could be used in the XML attribute onClick
# 保护指定类的成员，如果此类受到保护他们会保护的更好
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# 保护指定的类文件和类成员
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# adding this in to preserve line numbers so that the stack traces
# can be remapped
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable


#Note: there were 36 classes trying to access annotations using reflection.
#You should consider keeping the annotation attributes
#(using '-keepattributes *Annotation*').
#(http://proguard.sourceforge.net/manual/troubleshooting.html#attributes)
-keepattributes *Annotation*
#Note: there were 46 classes trying to access generic signatures using reflection.
#You should consider keeping the signature attributes
#(using '-keepattributes Signature').
#(http://proguard.sourceforge.net/manual/troubleshooting.html#attributes)
-keepattributes Signature


# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
# Keep the support library
-dontwarn android.support.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }


#解决反射需要引用资源文件
-keep public class com.manfenjiayuan.mixicook_vip.R$*{
public static final int *;
}

# fix ClassNotFoundException errors when ProGuard strips away your code by adding a -keep line in the proguard.cfg file. For example:
-keep class com.mfh.comn.** { * ; }
-keep class com.mfh.framework.** { * ; }
-keep class com.manfenjiayuan.im.** { * ; }
-keep class com.manfenjiayuan.business.** { * ; }
-keep class com.alibaba.fastjson.** { * ; }
-keep class com.manfenjiayuan.mixicook_vip.** { * ; }
-keep class com.bingshanguxue.vector_uikit.** { * ; }
-keep class android.serialport.api.** { * ; }
-keep class com.zebra.adc.decoder.** { * ; }
-keep class com.android.** { * ; }

# Butter Knife start
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}
# Butter Knife end


#alipay start
#specified twice.
#-libraryjars libs/alipaySdk-20161009.jar
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
#alipay end


#eventbus start (https://github.com/greenrobot/EventBus)
-keepattributes *Annotation*
#@org.greenrobot.eventbus.Subscribe <methods>;
-keepclassmembers class ** {
    public void onEvent*(**);
    public void onEventMainThread*(**);
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
#eventbus end


#umeng start
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
-keep class com.umeng.**
-keep public class com.umeng.fb.ui.ThreadView {
}
-dontwarn com.umeng.**
-dontwarn org.apache.commons.**
-keep public class * extends com.umeng.**
-keep class com.umeng.** {*; }
#umeng end

#glide start
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#glide end


#bugly start
-keep public class com.tencent.bugly.**{*;}
#bugly end


#amap start
#3D 地图
-keep   class com.amap.api.mapcore.**{*;}
-keep   class com.amap.api.maps.**{*;}
-keep   class com.autonavi.amap.mapcore.*{*;}
#定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}
#搜索
-keep   class com.amap.api.services.**{*;}
#2D地图
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}
#导航
-keep class com.amap.api.navi.**{*;}
-keep class com.autonavi.**{*;}
#amap end


#gexin start
-dontwarn com.igexin.**
-keep class com.igexin.**{*;}
#gexin end


#java.lang.NoSuchMethodError: android.util.Xml.asAttributeSet
-keep class org.xmlpull.v1.** { *; }

-keepattributes *JavascriptInterface*
-keep class android.webkit.JavascriptInterface {*;}