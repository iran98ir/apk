/* =========================================================
   AppConfig.kt — لود تنظیمات از config.json
   مسیر: template/app/src/main/java/ir/rosha/app/AppConfig.kt
   =========================================================
   📌 فقط از config.json می‌خونه
   📌 اگه کلید نبود → به پشتیبان پیش‌فرض برمی‌گرده (بدون کرش)
   ========================================================= */

package ir.rosha.app

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

object AppConfig {

    private const val TAG = "AppConfig"

    private var json: JsonObject? = null

    /* =====================================================
       لود
       ===================================================== */
    fun init(context: Context) {
        json = try {
            val text = context.assets.open("config.json")
                .bufferedReader()
                .use { it.readText() }

            Log.d(TAG, "📖 config.json خوانده شد — حجم: ${text.length} کاراکتر")

            val parsed = Gson().fromJson(text, JsonObject::class.java)
            Log.d(TAG, "✅ config.json پارس شد")
            parsed
        } catch (e: Exception) {
            Log.e(TAG, "❌ خطا در خواندن config.json: ${e.message}", e)
            JsonObject()
        }
    }

    /* =====================================================
       دسترسی به بخش
       ===================================================== */
    private fun section(name: String): JsonObject? {
        return try {
            // ===== پشتیبانی از name.subname =====
            if (name.contains(".")) {
                val parts = name.split(".")
                var current: JsonObject = json ?: return null

                for (part in parts) {
                    if (!current.has(part) || !current.get(part).isJsonObject) {
                        return null
                    }
                    current = current.getAsJsonObject(part)
                }
                return current
            }

            json?.getAsJsonObject(name)
        } catch (e: Exception) {
            Log.w(TAG, "خطا در خواندن بخش $name: ${e.message}")
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
       گرفتن مقادیر — با fallback امن
       ===================================================== */
    fun str(sectionName: String, key: String, default: String = ""): String {
        return try {
            val s = section(sectionName) ?: return default
            if (!s.has(key) || s.get(key).isJsonNull) return default
            s.get(key).asString
        } catch (e: Exception) {
            Log.w(TAG, "خطا در خواندن $sectionName.$key → استفاده از '$default'")
            default
        }
    }

    fun int(sectionName: String, key: String, default: Int = 0): Int {
        return try {
            val s = section(sectionName) ?: return default
            if (!s.has(key) || s.get(key).isJsonNull) return default
            s.get(key).asInt
        } catch (e: Exception) {
            Log.w(TAG, "خطا در خواندن $sectionName.$key → استفاده از $default")
            default
        }
    }

    fun float(sectionName: String, key: String, default: Float = 0f): Float {
        return try {
            val s = section(sectionName) ?: return default
            if (!s.has(key) || s.get(key).isJsonNull) return default
            s.get(key).asFloat
        } catch (e: Exception) {
            Log.w(TAG, "خطا در خواندن $sectionName.$key → استفاده از $default")
            default
        }
    }

    fun bool(sectionName: String, key: String, default: Boolean = false): Boolean {
        return try {
            val s = section(sectionName) ?: return default
            if (!s.has(key) || s.get(key).isJsonNull) return default

            val elem = s.get(key)
            when {
                elem.isJsonPrimitive && elem.asJsonPrimitive.isBoolean -> elem.asBoolean
                elem.isJsonPrimitive && elem.asJsonPrimitive.isNumber  -> elem.asInt != 0
                elem.isJsonPrimitive && elem.asJsonPrimitive.isString  -> {
                    val v = elem.asString.lowercase()
                    v == "true" || v == "1" || v == "yes" || v == "on"
                }
                else -> default
            }
        } catch (e: Exception) {
            Log.w(TAG, "خطا در خواندن $sectionName.$key → استفاده از $default")
            default
        }
    }

    fun color(sectionName: String, key: String, default: String = "#000000"): Int {
        return try {
            val s = section(sectionName) ?: return Color.parseColor(default)
            if (!s.has(key) || s.get(key).isJsonNull) return Color.parseColor(default)

            val colorStr = s.get(key).asString
            if (colorStr.isBlank()) return Color.parseColor(default)
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            Log.w(TAG, "خطا در خواندن رنگ $sectionName.$key → استفاده از $default")
            try {
                Color.parseColor(default)
            } catch (e2: Exception) {
                Color.BLACK
            }
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
       اعتبارسنجی — فقط لاگ می‌کنه، کرش نمی‌کنه
       ===================================================== */
    fun validateAll(): List<String> {
        val required = mapOf(
            "branding" to listOf(
                "app_name", "app_name_en", "package_name",
                "version_code", "version_name"
            ),
            "splash" to listOf(
                "enabled", "duration", "title", "subtitle",
                "title_color", "subtitle_color", "title_size", "subtitle_size",
                "loader_color", "show_loader", "logo_size",
                "bg_type", "bg_color_1", "bg_color_2"
            ),
            "onboarding" to listOf(
                "enabled", "skip_text", "next_text", "prev_text", "start_text",
                "btn_bg", "btn_text_color", "dot_active", "dot_inactive"
            ),
            "vpn" to listOf(
                "enabled", "top_title", "top_subtitle", "footnote", "show_recheck"
            ),
            "webview" to listOf(
                "url", "url_home", "js_enabled", "zoom_enabled",
                "dom_storage", "database", "cache_enabled",
                "user_agent", "user_agent_custom",
                "progress_bar", "progress_color",
                "back_button", "back_double", "back_exit_msg",
                "external_links", "mail_links", "tel_links",
                "whatsapp_links", "telegram_links", "instagram_links"
            ),
            "errors" to listOf(
                "enabled", "show_retry", "show_home",
                "retry_text", "retry_bg", "retry_color",
                "home_text", "home_color", "home_border"
            ),
            "exit" to listOf(
                "enabled", "double_back", "double_back_msg", "show_icon",
                "dialog_type", "radius", "border_width",
                "icon", "title", "title_color", "text", "text_color",
                "bg_color", "border_color", "overlay_color",
                "btn_confirm_text", "btn_confirm_bg", "btn_confirm_color",
                "btn_cancel_text", "btn_cancel_bg", "btn_cancel_color",
                "btn_layout"
            )
        )

        val missing = mutableListOf<String>()
        required.forEach { (sectionName, keys) ->
            keys.forEach { key ->
                if (!has(sectionName, key)) {
                    missing.add("$sectionName.$key")
                }
            }
        }

        // ===== چک VPN state‌ها =====
        listOf("state_on", "state_off", "state_unknown").forEach { state ->
            listOf("icon", "title", "text", "color", "bg", "btn").forEach { key ->
                if (!has("vpn.$state", key)) {
                    missing.add("vpn.$state.$key")
                }
            }
        }

        // ===== چک vpn.recheck =====
        listOf("text", "bg", "border", "color").forEach { key ->
            if (!has("vpn.recheck", key)) {
                missing.add("vpn.recheck.$key")
            }
        }

        if (missing.isNotEmpty()) {
            Log.w(TAG, "⚠️ کلیدهای غایب در config.json:\n" + missing.joinToString("\n"))
        } else {
            Log.d(TAG, "✅ همه‌ی کلیدها موجودند")
        }

        return missing
    }

    /* =====================================================
       دسترسی مستقیم
       ===================================================== */
    fun all(): JsonObject? = json
}
