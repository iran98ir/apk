package ir.rosha

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Config — خواننده app.json
 * تمام مقادیر پنل از این کلاس خونده می‌شن
 */
object Config {

    private var json: JSONObject? = null

    fun init(context: Context) {
        if (json != null) return
        val txt = context.assets.open("app.json").bufferedReader().use { it.readText() }
        json = JSONObject(txt)
    }

    private fun root(): JSONObject = json ?: JSONObject()

    // ═══════════════ برندینگ ═══════════════
    val mode: String         get() = root().optJSONObject("brand")?.optString("mode", "new") ?: "new"
    val appName: String      get() = root().optJSONObject("brand")?.optString("name", "") ?: ""
    val packageName: String  get() = root().optJSONObject("brand")?.optString("pkg", "") ?: ""
    val versionName: String  get() = root().optJSONObject("brand")?.optString("ver", "1.0.0") ?: "1.0.0"
    val versionCode: Int     get() = root().optJSONObject("brand")?.optInt("vcode", 1) ?: 1

    // ═══════════════ اسپلش ═══════════════
    private fun splash(): JSONObject = root().optJSONObject("splash") ?: JSONObject()
    val splashBg: String       get() = splash().optString("bg", "#FFFFFF")
    val splashBg2: String      get() = splash().optString("bg2", splashBg)
    val splashBgMode: String   get() = splash().optString("bgMode", "single")
    val splashBgAngle: Int     get() = splash().optInt("bgAngle", 135)
    val splashTxt: String      get() = splash().optString("txt", "#000000")
    val splashTitle: String    get() = splash().optString("title", "")
    val splashTSize: Int       get() = splash().optInt("tSize", 26)
    val splashHasIcon: Boolean get() = splash().optBoolean("hasIcon", true)
    val splashHasText: Boolean get() = splash().optBoolean("hasText", true)
    val splashIconSvg: String  get() = splash().optString("iconSvg", "")
    val splashIconColor: String get() = splash().optString("iconColor", "#FFFFFF")
    val splashIconColorOn: Boolean get() = splash().optBoolean("iconColorOn", true)
    val splashIconBgOn: Boolean get() = splash().optBoolean("iconBgOn", true)
    val splashIconBg: String   get() = splash().optString("iconBg", "#FFFFFF")
    val splashIconShape: String get() = splash().optString("iconShape", "circle")
    val splashIconSize: Int    get() = splash().optInt("iconSize", 60)
    val splashIconBoxSize: Int get() = splash().optInt("iconBoxSize", 120)
    val splashIconPos: String  get() = splash().optString("iconPos", "top")
    val splashTextPos: String  get() = splash().optString("textPos", "bottom")

    // ═══════════════ انبوردینگ ═══════════════
    private fun onb(): JSONObject = root().optJSONObject("onb") ?: JSONObject()
    val onbActive: Boolean       get() = onb().optBoolean("active", true)
    val onbFirstRunOnly: Boolean get() = onb().optBoolean("firstRunOnly", true)
    val onbCount: String         get() = onb().optString("count", "3")
    val onbBg: String            get() = onb().optString("bg", "#FFFFFF")
    val onbBg2: String           get() = onb().optString("bg2", onbBg)
    val onbBgMode: String        get() = onb().optString("bgMode", "single")
    val onbBgAngle: Int          get() = onb().optInt("bgAngle", 135)
    val onbTxt: String           get() = onb().optString("txt", "#000000")

    fun onbPages(): JSONArray = onb().optJSONArray("pages") ?: JSONArray()

    fun onbPage(i: Int): JSONObject {
        val arr = onbPages()
        return if (i < arr.length()) arr.optJSONObject(i) ?: JSONObject() else JSONObject()
    }

    // ═══════════════ ولکام ═══════════════
    private fun welcome(): JSONObject = root().optJSONObject("welcome") ?: JSONObject()
    val welcomeActive: Boolean      get() = welcome().optBoolean("active", true)
    val welcomeBg: String           get() = welcome().optString("bg", "#FFFFFF")
    val welcomeBg2: String          get() = welcome().optString("bg2", welcomeBg)
    val welcomeBgMode: String       get() = welcome().optString("bgMode", "single")
    val welcomeBgAngle: Int         get() = welcome().optInt("bgAngle", 135)
    val welcomeTxt: String          get() = welcome().optString("txt", "#000000")
    val welcomeTitle: String        get() = welcome().optString("t", "")
    val welcomeDesc: String         get() = welcome().optString("d", "")
    val welcomeBtn: String          get() = welcome().optString("btn", "")
    val welcomeTSize: Int           get() = welcome().optInt("tSize", 28)
    val welcomeDSize: Int           get() = welcome().optInt("dSize", 17)
    val welcomeBtnSize: Int         get() = welcome().optInt("btnSize", 17)
    val welcomeBtnRadius: Int       get() = welcome().optInt("btnRadius", 14)
    val welcomeBtnShape: String     get() = welcome().optString("btnShape", "rounded")
    val welcomeBtnStyle: String     get() = welcome().optString("btnStyle", "elevated")
    val welcomeBtnBg: String        get() = welcome().optString("btnBg", "#4F46E5")
    val welcomeBtnTxt: String       get() = welcome().optString("btnTxt", "#FFFFFF")
    val welcomeHasIcon: Boolean     get() = welcome().optBoolean("hasIcon", true)
    val welcomeIconSvg: String      get() = welcome().optString("iconSvg", "")
    val welcomeIconColor: String    get() = welcome().optString("iconColor", "#4F46E5")
    val welcomeIconColorOn: Boolean get() = welcome().optBoolean("iconColorOn", true)
    val welcomeIconBgOn: Boolean    get() = welcome().optBoolean("iconBgOn", true)
    val welcomeIconBg: String       get() = welcome().optString("iconBg", "#EEF2FF")
    val welcomeIconShape: String    get() = welcome().optString("iconShape", "circle")
    val welcomeIconSize: Int        get() = welcome().optInt("iconSize", 60)
    val welcomeIconBoxSize: Int     get() = welcome().optInt("iconBoxSize", 120)
    val welcomeIconPos: String      get() = welcome().optString("iconPos", "top")
    val welcomeTextPos: String      get() = welcome().optString("textPos", "center")
    val welcomeBtnPos: String       get() = welcome().optString("btnPos", "bottom")

    // ═══════════════ وب‌ویوئر ═══════════════
    private fun web(): JSONObject = root().optJSONObject("web") ?: JSONObject()
    val webUrl: String              get() = web().optString("url", "")
    val webExtLink: Boolean         get() = web().optBoolean("extLink", true)
    val webDeepLink: Boolean        get() = web().optBoolean("deepLink", true)
    val webDeepLinkUrl: String      get() = web().optString("deepLinkUrl", "")
    val webKeepSession: Boolean     get() = web().optBoolean("keepSession", true)
    val webBrowserOnly: String      get() = web().optString("browserOnly", "")

    // ═══════════════ کادر خروج ═══════════════
    private fun exit(): JSONObject = root().optJSONObject("exit") ?: JSONObject()
    val exitActive: Boolean      get() = exit().optBoolean("active", true)
    val exitTitle: String        get() = exit().optString("t", "")
    val exitMsg: String          get() = exit().optString("m", "")
    val exitBg: String           get() = exit().optString("bg", "#FFFFFF")
    val exitTxt: String          get() = exit().optString("txt", "#0F172A")
    val exitTSize: Int           get() = exit().optInt("tSize", 20)
    val exitMSize: Int           get() = exit().optInt("mSize", 16)
    val exitBtnSize: Int         get() = exit().optInt("btnSize", 16)
    val exitAnim: String         get() = exit().optString("anim", "glass")
    val exitYes: String          get() = exit().optString("yes", "بله")
    val exitNo: String           get() = exit().optString("no", "خیر")
    val exitYesBg: String        get() = exit().optString("yesBg", "#DC2626")
    val exitYesTxt: String       get() = exit().optString("yesTxt", "#FFFFFF")
    val exitNoBg: String         get() = exit().optString("noBg", "#E5E7EB")
    val exitNoTxt: String        get() = exit().optString("noTxt", "#0F172A")
    val exitBtnShape: String     get() = exit().optString("btnShape", "rounded")
    val exitBtnStyle: String     get() = exit().optString("btnStyle", "elevated")
    val exitBtnLayout: String    get() = exit().optString("btnLayout", "row")

    // ═══════════════ خطاها ═══════════════
    private fun error(): JSONObject = root().optJSONObject("error") ?: JSONObject()
    val errorActive: Boolean   get() = error().optBoolean("active", true)
    val errorBg: String        get() = error().optString("bg", "#FFFFFF")
    val errorBg2: String       get() = error().optString("bg2", errorBg)
    val errorBgMode: String    get() = error().optString("bgMode", "single")
    val errorBgAngle: Int      get() = error().optInt("bgAngle", 135)
    val errorTxt: String       get() = error().optString("txt", "#0F172A")
    val errorTSize: Int        get() = error().optInt("tSize", 22)
    val errorDSize: Int        get() = error().optInt("dSize", 16)
    val errorRetryText: String get() = error().optString("retryText", "تلاش مجدد")
    val errorBtnBg: String     get() = error().optString("btnBg", "#4F46E5")
    val errorBtnShape: String  get() = error().optString("btnShape", "rounded")
    val errorBtnStyle: String  get() = error().optString("btnStyle", "elevated")

    fun errorItem(key: String): JSONObject {
        val items = error().optJSONObject("items") ?: return JSONObject()
        return items.optJSONObject(key) ?: JSONObject()
    }

    fun errorItemField(key: String, field: String, def: String = ""): String {
        return errorItem(key).optString(field, def)
    }

    fun errorItemBool(key: String, field: String, def: Boolean = true): Boolean {
        return errorItem(key).optBoolean(field, def)
    }

    fun errorItemInt(key: String, field: String, def: Int = 0): Int {
        return errorItem(key).optInt(field, def)
    }
}
