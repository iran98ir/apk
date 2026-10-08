/* =========================================================
   AppConfig.kt — لود تنظیمات از config.json
   مسیر: template/app/src/main/java/ir/rosha/app/AppConfig.kt
   =========================================================
   📌 فقط از config.json دستور می‌گیره
   📌 بدون پیش‌فرض — اگه کلید نبود، خطا می‌ده
   ========================================================= */

package ir.rosha.app

import android.content.Context
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

object AppConfig {

    private var json: JsonObject? = null

    /* =====================================================
       لود
       ===================================================== */
    fun init(context: Context) {
        json = try {
            val text = context.assets.open("config.json")
                .bufferedReader()
                .use { it.readText() }
            Gson().fromJson(text, JsonObject::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            JsonObject()
        }
    }

    /* =====================================================
       دسترسی به بخش
       ===================================================== */
    private fun section(name: String): JsonObject? {
        return try {
            json?.getAsJsonObject(name)
        } catch (e: Exception) {
            null
        }
    }

    /* =====================================================
       بررسی وجود کلید
       ===================================================== */
    fun has(sectionName: String, key: String): Boolean {
        return try {
            val s = section(sectionName) ?: return false
            s.has(key) && !s.get(key).isJsonNull
        } catch (e: Exception) {
            false
        }
    }

    /* =====================================================
       گرفتن مقادیر (بدون پیش‌فرض)
       📌 اگه کلید نبود، خطای واضح پرتاب می‌کنه
       ===================================================== */
    fun str(sectionName: String, key: String): String {
        requireKey(sectionName, key)
        return try {
            section(sectionName)!!.get(key).asString
        } catch (e: Exception) {
            throw IllegalStateException("خطا در خواندن $sectionName.$key از config.json")
        }
    }

    fun int(sectionName: String, key: String): Int {
        requireKey(sectionName, key)
        return try {
            section(sectionName)!!.get(key).asInt
        } catch (e: Exception) {
            throw IllegalStateException("خطا در خواندن $sectionName.$key از config.json")
        }
    }

    fun float(sectionName: String, key: String): Float {
        requireKey(sectionName, key)
        return try {
            section(sectionName)!!.get(key).asFloat
        } catch (e: Exception) {
            throw IllegalStateException("خطا در خواندن $sectionName.$key از config.json")
        }
    }

    fun bool(sectionName: String, key: String): Boolean {
        requireKey(sectionName, key)
        return try {
            section(sectionName)!!.get(key).asBoolean
        } catch (e: Exception) {
            throw IllegalStateException("خطا در خواندن $sectionName.$key از config.json")
        }
    }

    fun color(sectionName: String, key: String): Int {
        requireKey(sectionName, key)
        return try {
            Color.parseColor(section(sectionName)!!.get(key).asString)
        } catch (e: Exception) {
            throw IllegalStateException("خطا در خواندن رنگ $sectionName.$key از config.json")
        }
    }

    fun obj(sectionName: String, key: String): JsonObject? {
        return try {
            val s = section(sectionName) ?: return null
            if (s.has(key) && s.get(key).isJsonObject) s.getAsJsonObject(key) else null
        } catch (e: Exception) {
            null
        }
    }

    fun arr(sectionName: String, key: String): JsonArray? {
        return try {
            val s = section(sectionName) ?: return null
            if (s.has(key) && s.get(key).isJsonArray) s.getAsJsonArray(key) else null
        } catch (e: Exception) {
            null
        }
    }

    /* =====================================================
       پرتاب خطا اگه کلید نبود
       ===================================================== */
    private fun requireKey(sectionName: String, key: String) {
        if (section(sectionName) == null) {
            throw IllegalStateException("بخش $sectionName توی config.json نیست")
        }
        if (!has(sectionName, key)) {
            throw IllegalStateException("کلید $sectionName.$key توی config.json نیست")
        }
    }

    /* =====================================================
       اعتبارسنجی اسپلش
       📌 لیست کلیدهای غایب رو برمی‌گردونه
       ===================================================== */
    fun validateSplash(): List<String> {
        val required = listOf(
            "enabled",
            "duration",
            "title",
            "subtitle",
            "title_color",
            "subtitle_color",
            "title_size",
            "subtitle_size",
            "loader_color",
            "show_loader",
            "logo_size",
            "bg_type",
            "bg_color_1",
            "bg_color_2"
        )
        val missing = mutableListOf<String>()
        required.forEach { key ->
            if (!has("splash", key)) {
                missing.add("splash.$key")
            }
        }
        if (!has("branding", "app_name")) {
            missing.add("branding.app_name")
        }
        if (!has("onboarding", "enabled")) {
            missing.add("onboarding.enabled")
        }
        if (!has("vpn", "enabled")) {
            missing.add("vpn.enabled")
        }
        return missing
    }

    /* =====================================================
       دسترسی مستقیم
       ===================================================== */
    fun all(): JsonObject? = json
}
