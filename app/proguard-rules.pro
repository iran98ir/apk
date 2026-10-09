-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface
-keepattributes *Annotation*

-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

-keep class app.vista.** { *; }
-keep class **.Config { *; }

-keep class android.webkit.** { *; }
-dontwarn android.webkit.**

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
