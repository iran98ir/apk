/* =========================================================
   ConfigLoader.java — لودر تنظیمات از BuildConfig
   مسیر: app/src/main/java/app/vista/ConfigLoader.java
   ========================================================= */

package app.vista;

import android.content.Context;

public class ConfigLoader {

    private static ConfigLoader instance;
    private final Context context;

    private ConfigLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized ConfigLoader get(Context context) {
        if (instance == null) {
            instance = new ConfigLoader(context);
        }
        return instance;
    }

    public static ConfigLoader get() {
        if (instance == null) {
            throw new IllegalStateException("ConfigLoader not initialized");
        }
        return instance;
    }

    // ===== رشته‌ها =====
    public String getAppName() {
        return context.getString(R.string.app_name);
    }

    public String getUrl() {
        return context.getString(R.string.base_url);
    }

    public String getSplashTitle() {
        return context.getString(R.string.splash_title);
    }

    public String getSplashSubtitle() {
        return context.getString(R.string.splash_subtitle);
    }

    // ===== رنگ‌ها =====
    public int getColorPrimary() {
        return context.getColor(R.color.color_primary);
    }

    public int getColorBackground() {
        return context.getColor(R.color.color_background);
    }

    public int getColorText() {
        return context.getColor(R.color.color_text);
    }

    public int getColorButton() {
        return context.getColor(R.color.color_button);
    }

    public int getColorButtonText() {
        return context.getColor(R.color.color_button_text);
    }
}
