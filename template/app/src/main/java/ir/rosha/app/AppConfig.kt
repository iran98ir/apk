/* =========================================================
   AppConfig.kt — لود تنظیمات از config.json
   مسیر: template/app/src/main/java/ir/rosha/app/AppConfig.kt
   =========================================================
   📌 همه چیز از config.json خونده می‌شه
   📌 مطابق ساختار پنل روشا
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
       گرفتن مقادیر
       ===================================================== */
    fun str(sectionName: String, key: String, def: String = ""): String {
        return try {
            val s = section(sectionName) ?: return def
            if (s.has(key) && !s.get(key).isJsonNull) s.get(key).asString else def
        } catch (e: Exception) { def }
    }

    fun int(sectionName: String, key: String, def: Int = 0): Int {
        return try {
            val s = section(sectionName) ?: return def
            if (s.has(key) && !s.get(key).isJsonNull) s.get(key).asInt else def
        } catch (e: Exception) { def }
    }

    fun float(sectionName: String, key: String, def: Float = 0f): Float {
        return try {
            val s = section(sectionName) ?: return def
            if (s.has(key) && !s.get(key).isJsonNull) s.get(key).asFloat else def
        } catch (e: Exception) { def }
    }

    fun bool(sectionName: String, key: String, def: Boolean = false): Boolean {
        return try {
            val s = section(sectionName) ?: return def
            if (s.has(key) && !s.get(key).isJsonNull) s.get(key).asBoolean else def
        } catch (e: Exception) { def }
    }

    fun color(sectionName: String, key: String, def: String = "#FFFFFF"): Int {
        val hex = str(sectionName, key, def)
        return parseColor(hex, def)
    }

    fun obj(sectionName: String, key: String): JsonObject? {
        return try {
            val s = section(sectionName) ?: return null
            if (s.has(key) && s.get(key).isJsonObject) s.getAsJsonObject(key) else null
        } catch (e: Exception) { null }
    }

    fun arr(sectionName: String, key: String): JsonArray? {
        return try {
            val s = section(sectionName) ?: return null
            if (s.has(key) && s.get(key).isJsonArray) s.getAsJsonArray(key) else null
        } catch (e: Exception) { null }
    }

    /* =====================================================
       گرفتن رنگ از Hex
       ===================================================== */
    fun parseColor(hex: String, def: String = "#FFFFFF"): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            try {
                Color.parseColor(def)
            } catch (e2: Exception) {
                Color.WHITE
            }
        }
    }

    /* =====================================================
       بررسی وجود کلید
       ===================================================== */
    fun has(sectionName: String, key: String): Boolean {
        return try {
            val s = section(sectionName) ?: return false
            s.has(key) && !s.get(key).isJsonNull
        } catch (e: Exception) { false }
    }

    /* =====================================================
       دسترسی مستقیم به کل داده
       ===================================================== */
    fun all(): JsonObject? = json
}
