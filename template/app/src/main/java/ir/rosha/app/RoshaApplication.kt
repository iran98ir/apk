/* =========================================================
   RoshaApplication.kt — کلاس Application
   مسیر: template/app/src/main/java/ir/rosha/app/RoshaApplication.kt
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

        // ===== غیرفعال کردن دارک مود =====
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    companion object {
        lateinit var instance: RoshaApplication
            private set

        fun getContext(): Context = instance.applicationContext
    }
}
