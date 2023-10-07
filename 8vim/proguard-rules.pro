-keepattributes Synthetic,Signature,*Annotation*,EnclosingMethod,InnerClasses

-keep class com.fasterxml.jackson.** {*; }
-keep class com.networknt.schema.** { *; }

-dontwarn org.jcodings.**
-dontwarn org.joni.**
-dontwarn com.fasterxml.jackson.databind.**

-keep,allowobfuscation,allowshrinking class arrow.core.Either
-keep,allowobfuscation,allowshrinking class arrow.core.Option

-keep class inc.flide.vim8.ime.layout.** { *; }

-keep class ch.qos.logback.** { *; }
-keepclassmembers class ch.qos.logback.classic.pattern.* { <init>(); }
-keepclassmembers class ch.qos.logback.** { *; }
-keepclassmembers class org.slf4j.impl.** { *; }
-dontwarn ch.qos.logback.core.net.*
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

-keep class inc.flide.vim8.R
-keep class inc.flide.vim8.R$* {
    <fields>;
}