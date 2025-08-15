# ============================
# Keep only essential React Native classes
# ============================
-keep class com.facebook.react.** { *; }
-keepclassmembers class * {
    @com.facebook.react.uimanager.annotations.ReactProp <methods>;
}
-keepclassmembers class * {
    @com.facebook.react.bridge.ReactMethod <methods>;
}

# Avoid stripping Hermes if accidentally enabled
-keep class com.facebook.hermes.** { *; }
-keep class com.facebook.jni.** { *; }

# ============================
# WebView
# ============================
-keep class com.reactnativecommunity.webview.** { *; }

# ============================
# Your App Classes
# ============================
-keep class com.ezehmark.byta.MainApplication { *; }
-keep class com.ezehmark.byta.MainActivity { *; }

# Keep only classes in your app package
-keep class com.ezehmark.byta.** { *; }

# ============================
# Remove unused AndroidX / support classes
# ============================
-dontwarn androidx.**
-dontwarn android.support.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.squareup.**
-dontwarn com.google.**

# ============================
# Keep annotations
# ============================
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ============================
# Remove all unused methods and fields in other libraries
# ============================
-dontshrink
