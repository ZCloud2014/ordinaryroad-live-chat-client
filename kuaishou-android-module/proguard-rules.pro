# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Protobuf classes
-keep class com.kuaishou.live.protobuf.** { *; }
-keep class com.google.protobuf.** { *; }

# Keep model classes
-keep class com.kuaishou.live.model.** { *; }

# Keep API classes
-keep class com.kuaishou.live.api.** { *; }

# Keep listener interfaces
-keep interface com.kuaishou.live.listener.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# JSON
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
