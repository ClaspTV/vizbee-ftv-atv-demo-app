# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/jesse/Library/Android/sdk/tools/proguard/proguard-android.txt
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

# ---------------------------
# Begin SDK Integration
# ---------------------------

# For keeping Vizbee classes with ProGuard
-keep class tv.vizbee.** { *; }

-keep class org.json.** { *; }

-dontwarn tv.vizbee.config.api.ui.cards.DeviceStatusCardConfig
-dontwarn tv.vizbee.environment.Environment
-dontwarn tv.vizbee.environment.net.handler.factory.NetworkHandlerFactory
-dontwarn tv.vizbee.environment.net.handler.implementations.reachability.LocalReachabilityIpProvider
-dontwarn tv.vizbee.environment.net.info.NetworkInfo
-dontwarn tv.vizbee.environment.net.manager.INetworkManager$NetworkChangeCallback
-dontwarn tv.vizbee.environment.net.manager.INetworkManager

# ---------------------------
# End SDK Integration
# ---------------------------