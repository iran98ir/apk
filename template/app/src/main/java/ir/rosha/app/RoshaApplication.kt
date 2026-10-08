/* =========================================================
   RoshaApplication.kt — کلاس Application
   مسیر: template/app/src/main/java/ir/rosha/app/RoshaApplication.kt
   =========================================================
   📌 فقط از config.json می‌خونه
   📌 اگه کلید اجباری نبود → اپ کرش می‌کنه
   ========================================================= */

package ir.rosha.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class RoshaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        // ===== لود تنظیمات =====
        AppConfig.init(this)

        // ===== اعتبارسنجی config =====
        val missing = AppConfig.validateAll()
        if (missing.isNotEmpty()) {
            val errorMsg = "❌ خطا در config.json:\n" + missing.joinToString("\n")
            throw IllegalStateException(errorMsg)
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
