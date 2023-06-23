# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/flide/Programming/Android/adt_linux/sdk/tools/proguard/proguard-android.txt
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
# Proguard configuration for Jackson 2.x
-dontwarn org.slf4j.impl.StaticLoggerBinder
-keepattributes *Annotation*,EnclosingMethod,Signature
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keepnames class com.fasterxml.jackson.** { *; }
-keep class com.networknt.schema.** { *; }
-dontwarn org.jcodings.Encoding
-dontwarn org.jcodings.specific.UTF8Encoding
-dontwarn org.joni.Matcher
-dontwarn org.joni.Regex
-dontwarn org.joni.Syntax
-dontwarn org.joni.exception.SyntaxException
-dontwarn com.fasterxml.jackson.databind.**
-keep public class inc.flide.vim8.structures.** {
    *;
}