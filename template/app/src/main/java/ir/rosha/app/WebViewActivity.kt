/* =========================================================
   WebViewActivity.kt — وب‌ویوئر اصلی
   مسیر: template/app/src/main/java/ir/rosha/app/WebViewActivity.kt
   =========================================================
   📌 همه چیز از config.json خونده می‌شه
   ========================================================= */

package ir.rosha.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

class WebViewActivity : ComponentActivity() {

    private var webView: WebView? = null
    private var currentUrl: String = ""
    private var homeUrl: String = ""

    // ===== از config =====
    private val startUrl: String by lazy {
        AppConfig.str("webview", "url", "https://example.com")
    }
    private val home: String by lazy {
        AppConfig.str("webview", "url_home", startUrl).ifEmpty { startUrl }
    }
    private val jsEnabled: Boolean by lazy {
        AppConfig.bool("webview", "js_enabled", true)
    }
    private val zoomEnabled: Boolean by lazy {
        AppConfig.bool("webview", "zoom_enabled", false)
    }
    private val domStorage: Boolean by lazy {
        AppConfig.bool("webview", "dom_storage", true)
    }
    private val database: Boolean by lazy {
        AppConfig.bool("webview", "database", true)
    }
    private val progressBar: Boolean by lazy {
        AppConfig.bool("webview", "progress_bar", true)
    }
    private val progressColor: Int by lazy {
        AppConfig.color("webview", "progress_color", "#E8A33D")
    }
    private val backButton: Boolean by lazy {
        AppConfig.bool("webview", "back_button", true)
    }
    private val backDouble: Boolean by lazy {
        AppConfig.bool("webview", "back_double", true)
    }
    private val backExitMsg: String by lazy {
        AppConfig.str("webview", "back_exit_msg", "")
    }
    private val cacheEnabled: Boolean by lazy {
        AppConfig.bool("webview", "cache_enabled", true)
    }
    private val userAgentMode: String by lazy {
        AppConfig.str("webview", "user_agent", "auto")
    }
    private val userAgentCustom: String by lazy {
        AppConfig.str("webview", "user_agent_custom", "")
    }
    private val externalLinks: String by lazy {
        AppConfig.str("webview", "external_links", "inapp")
    }
    private val mailLinks: String by lazy {
        AppConfig.str("webview", "mail_links", "external")
    }
    private val telLinks: String by lazy {
        AppConfig.str("webview", "tel_links", "external")
    }
    private val whatsappLinks: String by lazy {
        AppConfig.str("webview", "whatsapp_links", "external")
    }
    private val telegramLinks: String by lazy {
        AppConfig.str("webview", "telegram_links", "external")
    }
    private val instagramLinks: String by lazy {
        AppConfig.str("webview", "instagram_links", "external")
    }
    private val errorEnabled: Boolean by lazy {
        AppConfig.bool("errors", "enabled", true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUrl = startUrl
        homeUrl = home

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0D1B2E)
            ) {
                WebViewScreen { wv ->
                    webView = wv
                    setupWebView(wv)
                }
            }
        }

        // ===== Back هوشمند =====
        if (backButton) {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val wv = webView
                    if (wv != null && wv.canGoBack()) {
                        wv.goBack()
                    } else {
                        handleExitBack()
                    }
                }
            })
        }
    }

    private fun handleExitBack() {
        if (backDouble) {
            if (backExitMsg.isNotEmpty()) {
                android.widget.Toast.makeText(this, backExitMsg, android.widget.Toast.LENGTH_SHORT).show()
            }
            // دوبار بزن → خروج
            if (System.currentTimeMillis() - lastBackTime < 2000) {
                finish()
            } else {
                lastBackTime = System.currentTimeMillis()
            }
        } else {
            finish()
        }
    }

    private var lastBackTime = 0L

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wv: WebView) {

        val settings = wv.settings

        // ===== JS =====
        settings.javaScriptEnabled = jsEnabled

        // ===== Zoom =====
        settings.setSupportZoom(zoomEnabled)
        settings.builtInZoomControls = zoomEnabled
        settings.displayZoomControls = false

        // ===== Storage =====
        settings.domStorageEnabled = domStorage
        settings.databaseEnabled = database

        // ===== Cache =====
        settings.cacheMode = if (cacheEnabled) {
            WebSettings.LOAD_DEFAULT
        } else {
            WebSettings.LOAD_NO_CACHE
        }

        // ===== User Agent =====
        when (userAgentMode) {
            "mobile" -> settings.userAgentString = settings.userAgentString + " RoshaApp"
            "desktop" -> settings.userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            "custom" -> if (userAgentCustom.isNotEmpty()) {
                settings.userAgentString = userAgentCustom
            }
        }

        // ===== Mixed Content =====
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        // ===== Media =====
        settings.mediaPlaybackRequiresUserGesture = false

        // ===== WebViewClient =====
        wv.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()

                // ===== تلفن =====
                if (url.startsWith("tel:")) {
                    if (telLinks == "external") {
                        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                        return true
                    }
                }

                // ===== ایمیل =====
                if (url.startsWith("mailto:")) {
                    if (mailLinks == "external") {
                        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                        return true
                    }
                }

                // ===== واتساپ =====
                if (url.contains("wa.me") || url.contains("api.whatsapp.com") || url.startsWith("whatsapp:")) {
                    if (whatsappLinks == "external") {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            return true
                        } catch (e: Exception) { return false }
                    }
                }

                // ===== تلگرام =====
                if (url.contains("t.me") || url.startsWith("tg:")) {
                    if (telegramLinks == "external") {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            return true
                        } catch (e: Exception) { return false }
                    }
                }

                // ===== اینستاگرام =====
                if (url.contains("instagram.com")) {
                    if (instagramLinks == "external") {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            return true
                        } catch (e: Exception) { return false }
                    }
                }

                // ===== لینک‌های خارجی =====
                if (externalLinks == "external") {
                    val isSameDomain = url.contains(Uri.parse(startUrl).host ?: "")
                    if (!isSameDomain) {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            return true
                        } catch (e: Exception) { return false }
                    }
                }

                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                currentUrl = url ?: ""
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)

                if (!errorEnabled) return

                if (request?.isForMainFrame == true) {
                    val errorType = when (error?.errorCode) {
                        WebViewClient.ERROR_HOST_LOOKUP -> "dns"
                        WebViewClient.ERROR_CONNECT -> "conn"
                        WebViewClient.ERROR_TIMEOUT -> "to"
                        WebViewClient.ERROR_FAILED_SSL_HANDSHAKE -> "ssl"
                        else -> "unk"
                    }

                    val intent = Intent(this@WebViewActivity, ErrorActivity::class.java)
                    intent.putExtra("error_type", errorType)
                    startActivity(intent)
                }
            }
        }

        // ===== WebChromeClient (Progress Bar) =====
        if (progressBar) {
            wv.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                }
            }
        } else {
            wv.webChromeClient = object : WebChromeClient() {}
        }

        // ===== لود =====
        wv.loadUrl(startUrl)
    }
}

@Composable
private fun WebViewScreen(onCreated: (WebView) -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onCreated(this)
            }
        }
    )
}
