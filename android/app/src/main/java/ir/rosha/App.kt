package ir.rosha

import android.app.Application
import android.util.Log
import java.io.File

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            Config.init(this)
            Log.d("ROSHA", "✓ Config initialized successfully")
        } catch (e: Throwable) {
            // ذخیره خطا در فایل برای دیباگ
            try {
                val f = File(getExternalFilesDir(null), "crash_log.txt")
                f.writeText("${e.javaClass.name}: ${e.message}\n\n${e.stackTraceToString()}")
                Log.e("ROSHA", "Crash log saved to: ${f.absolutePath}")
            } catch (_: Exception) {}
            Log.e("ROSHA", "Config init failed", e)
        }
    }
}
