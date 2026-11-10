# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Strip verbose and debug logging calls to reduce dex size
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# Keep the #708 verifier workaround scoped to the affected types. Global -dontoptimize and
# -dontobfuscate disable those R8 passes for the entire app and produce 0% Android vitals scores.
# These rules intentionally prohibit optimization and renaming of the jsoup/AndroidX types involved
# in the strict-ART hierarchy failure, while the rest of the app can be optimized and obfuscated.
-keep class org.jsoup.** { *; }
-keep class androidx.compose.runtime.Latch { *; }
-keep class androidx.core.view.WindowInsetsControllerCompat* { *; }
# Native code resolves bridge classes, fields and callbacks by their original JNI names/signatures.
# Keep private members too; they are entry points that are invisible to R8's Java call graph.
-keep class is.xyz.mpv.** { *; }
-keep class net.mediaarea.mediainfo.lib.** { *; }
-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.apache.logging.log4j.**
-dontwarn org.slf4j.**

# Keep all classes that use reflection or are loaded dynamically
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
