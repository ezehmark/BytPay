# Keep React Native classes
-keep class com.facebook.react.** { *; }
-keepclassmembers class * {
    @com.facebook.react.uimanager.annotations.ReactProp <methods>;
}
-keepclassmembers class * {
    @com.facebook.react.bridge.ReactMethod <methods>;
}

# WebView
-keep class com.reactnativecommunity.webview.** { *; }

# Avoid stripping Hermes
-keep class com.facebook.hermes.** { *; }
