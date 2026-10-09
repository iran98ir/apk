package app.vista;

import android.app.Application;

public class VistaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ConfigLoader.get(this);
    }
}
