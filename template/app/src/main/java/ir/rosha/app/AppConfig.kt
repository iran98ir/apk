/* =========================================================
   AppConfig.kt — لود تنظیمات از config.json
   مسیر: template/app/src/main/java/ir/rosha/app/AppConfig.kt
   =========================================================
   📌 فقط از config.json می‌خونه
   📌 هیچ مقدار پیش‌فرضی نداره
   📌 اگه کلید نبود → خطای واضح پرتاب می‌کنه
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
       گرفتن مقادیر — بدون پیش‌فرض
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
       اعتبارسنجی کل config
       ===================================================== */
    fun validateAll(): List<String> {
        val required = mapOf(
            "branding" to listOf(
                "app_name",
                "app_name_en",
                "package_name",
                "version_code",
                "version_name"
            ),
            "splash" to listOf(
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
                "bg_type",
                "bg_color_1",
                "bg_color_2"
            ),
            "onboarding" to listOf(
                "enabled",
                "skip_text",
                "next_text",
                "prev_text",
                "start_text",
                "btn_bg",
                "btn_text_color",
                "dot_active",
                "dot_inactive"
            ),
            "vpn" to listOf(
                "enabled",
                "top_title",
                "top_subtitle",
                "footnote",
                "show_recheck"
            ),
            "welcome" to listOf(
                "enabled",
                "title",
                "subtitle",
                "icon",
                "button_text",
                "button_bg",
                "button_text_color",
                "bg_color",
                "title_color",
                "subtitle_color"
            ),
            "webview" to listOf(
                "url",
                "url_home",
                "js_enabled",
                "zoom_enabled",
                "dom_storage",
                "database",
                "cache_enabled",
                "user_agent",
                "user_agent_custom",
                "progress_bar",
                "progress_color",
                "back_button",
                "back_double",
                "back_exit_msg",
                "external_links",
                "mail_links",
                "tel_links",
                "whatsapp_links",
                "telegram_links",
                "instagram_links"
            ),
            "errors" to listOf(
                "enabled",
                "show_retry",
                "show_home",
                "retry_text",
                "retry_bg",
                "retry_color",
                "home_text",
                "home_color",
                "home_border"
            ),
            "exit" to listOf(
                "enabled",
                "double_back",
                "double_back_msg",
                "show_icon",
                "dialog_type",
                "radius",
                "border_width",
                "icon",
                "title",
                "title_color",
                "text",
                "text_color",
                "bg_color",
                "border_color",
                "overlay_color",
                "btn_confirm_text",
                "btn_confirm_bg",
                "btn_confirm_color",
                "btn_cancel_text",
                "btn_cancel_bg",
                "btn_cancel_color",
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
                val s = section("vpn")
                if (s != null && s.has(state) && s.get(state).isJsonObject) {
                    val sub = s.getAsJsonObject(state)
                    if (!sub.has(key) || sub.get(key).isJsonNull) {
                        missing.add("vpn.$state.$key")
                    }
                } else {
                    missing.add("vpn.$state")
                }
            }
        }

        return missing
    }

    /* =====================================================
       دسترسی مستقیم
       ===================================================== */
    fun all(): JsonObject? = json
}
