package ir.rosha

import android.app.Application
import android.util.Log
import java.io.File

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            Config.init(this)
        } catch (e: Throwable) {
            // ذخیره خطا در فایل
            try {
                val f = File(getExternalFilesDir(null), "crash_log.txt")
                f.writeText("${e.javaClass.name}: ${e.message}\n\n${e.stackTraceToString()}")
            } catch (_: Exception) {}
            Log.e("MUMU_CRASH", "Config init failed", e)
        }
    }
}
