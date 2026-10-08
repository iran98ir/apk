/* =========================================================
   AppConfig.kt  —  لود تنظیمات از config.json
   مسیر: template/app/src/main/java/ir/rosha/app/AppConfig.kt
   =========================================================
   📌 این کلاس، فایل config.json رو از assets میخونه
   📌 همه‌ی تنظیمات پنل از اینجا در دسترس اپ قرار میگیره
   ========================================================= */

package ir.rosha.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject

object AppConfig {

    private var jsonData: JsonObject? = null

    /* ===== لود تنظیمات ===== */
    fun init(context: Context) {
        try {
            val jsonString = context.assets
                .open("config.json")
                .bufferedReader()
                .use { it.readText() }

            jsonData = Gson().fromJson(jsonString, JsonObject::class.java)

        } catch (e: Exception) {
            e.printStackTrace()
            jsonData = JsonObject()
        }
    }

    /* ===== دسترسی به یک بخش از تنظیمات ===== */
    fun getSection(section: String): JsonObject? {
        return try {
            jsonData?.getAsJsonObject(section)
        } catch (e: Exception) {
            null
        }
    }

    /* ===== گرفتن رشته ===== */
    fun getString(section: String, key: String, default: String = ""): String {
        return try {
            val s = getSection(section) ?: return default
            if (s.has(key) && !s.get(key).isJsonNull) {
                s.get(key).asString
            } else {
                default
            }
        } catch (e: Exception) {
            default
        }
    }

    /* ===== گرفتن عدد ===== */
    fun getInt(section: String, key: String, default: Int = 0): Int {
        return try {
            val s = getSection(section) ?: return default
            if (s.has(key) && !s.get(key).isJsonNull) {
                s.get(key).asInt
            } else {
                default
            }
        } catch (e: Exception) {
            default
        }
    }

    /* ===== گرفتن اعشار ===== */
    fun getFloat(section: String, key: String, default: Float = 0f): Float {
        return try {
            val s = getSection(section) ?: return default
            if (s.has(key) && !s.get(key).isJsonNull) {
                s.get(key).asFloat
            } else {
                default
            }
        } catch (e: Exception) {
            default
        }
    }

    /* ===== گرفتن بولین ===== */
    fun getBool(section: String, key: String, default: Boolean = false): Boolean {
        return try {
            val s = getSection(section) ?: return default
            if (s.has(key) && !s.get(key).isJsonNull) {
                s.get(key).asBoolean
            } else {
                default
            }
        } catch (e: Exception) {
            default
        }
    }

    /* ===== گرفتن رنگ (مثلاً #E8A33D به Int) ===== */
    fun getColor(section: String, key: String, default: String = "#E8A33D"): Int {
        val hex = getString(section, key, default)
        return try {
            android.graphics.Color.parseColor(hex)
        } catch (e: Exception) {
            android.graphics.Color.parseColor(default)
        }
    }

    /* ===== دسترسی مستقیم به کل داده ===== */
    fun getAll(): JsonObject? = jsonData
}
