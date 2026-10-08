/* =========================================================
   RoshaApplication.kt  —  کلاس اصلی Application
   مسیر: template/app/src/main/java/ir/rosha/app/RoshaApplication.kt
   ========================================================= */

package ir.rosha.app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class RoshaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ===== ذخیره Context برای استفاده در AppConfig =====
        instance = this

        // ===== لود تنظیمات از config.json =====
        AppConfig.init(this)

        // ===== غیرفعال کردن حالت شب (طبق درخواست) =====
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    companion object {
        lateinit var instance: RoshaApplication
            private set

        fun getContext(): Context = instance.applicationContext
    }
}
