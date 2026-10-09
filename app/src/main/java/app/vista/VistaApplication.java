/* =========================================================
   VistaApplication.java — کلاس Application
   مسیر: app/src/main/java/app/vista/VistaApplication.java
   ========================================================= */

package app.vista;

import android.app.Application;
import android.util.Log;

public class VistaApplication extends Application {

    private static final String TAG = "VistaApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "App started");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "App terminated");
    }
}
