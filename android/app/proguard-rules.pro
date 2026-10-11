# حفظ کلاس‌های پروژه
-keep class ir.rosha.** { *; }

# حفظ کلاس‌های WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# حفظ AndroidSVG
-keep class com.caverock.androidsvg.** { *; }
-dontwarn com.caverock.androidsvg.**
