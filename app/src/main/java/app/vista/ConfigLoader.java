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

    public String getUrl() {
        return Config.WV_URL;
    }

    public String getHomeUrl() {
        return Config.WV_URL_HOME != null && !Config.WV_URL_HOME.isEmpty()
                ? Config.WV_URL_HOME : Config.WV_URL;
    }
}
