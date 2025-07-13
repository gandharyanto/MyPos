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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Room Database
-keep class id.tugas.pos.data.model.** { *; }
-keep class id.tugas.pos.data.database.** { *; }

# ViewModel and LiveData
-keep class id.tugas.pos.viewmodel.** { *; }
-keep class androidx.lifecycle.** { *; }

# Repository
-keep class id.tugas.pos.data.repository.** { *; }

# Thermal Printer
-keep class com.dantsu.escposprinter.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# iText PDF
-keep class com.itextpdf.** { *; }

# ThreeTenABP
-keep class org.threeten.bp.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep generic signatures
-keepattributes Signature

# Keep exceptions
-keepattributes Exceptions

# Keep inner classes
-keep class id.tugas.pos.**$* { *; }