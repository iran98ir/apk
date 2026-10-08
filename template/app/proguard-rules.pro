# =========================================================
# proguard-rules.pro  —  قوانین ProGuard
# مسیر: template/app/proguard-rules.pro
# =========================================================

# ===== حفظ کلاس‌های اصلی =====
-keep class ir.rosha.app.** { *; }

# ===== WebView =====
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
    public void *(android.webkit.WebView, int);
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ===== Gson =====
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ===== Kotlin =====
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# ===== Compose =====
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
