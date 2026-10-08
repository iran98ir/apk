/* =========================================================
   RoshaApplication.kt — کلاس Application
   مسیر: template/app/src/main/java/ir/rosha/app/RoshaApplication.kt
   ========================================================= */

package ir.rosha.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate

class RoshaApplication : Application() {

    private val TAG = "RoshaApplication"

    override fun onCreate() {
        super.onCreate()

        instance = this

        // ===== لود تنظیمات =====
        AppConfig.init(this)

        // ===== اعتبارسنجی config (فقط لاگ، بدون کرش) =====
        val missing = AppConfig.validateAll()
        if (missing.isNotEmpty()) {
            Log.w(TAG, "⚠️ کلیدهای غایب: ${missing.size} مورد")
        } else {
            Log.d(TAG, "✅ config.json کامله")
        }

        // ===== غیرفعال کردن دارک مود =====
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    companion object {
        lateinit var instance: RoshaApplication
            private set

        fun getContext(): Context = instance.applicationContext
    }
}
