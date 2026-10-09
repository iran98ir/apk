/* =========================================================
   WebViewActivity.kt — وب‌ویوئر اصلی
   مسیر: template/app/src/main/java/ir/rosha/app/WebViewActivity.kt
   =========================================================
   📌 فقط از config.json می‌خونه
   📌 هیچ خطای خام مرورگری نشون داده نمیشه
   📌 همه‌ی خطاها (شبکه، HTTP، SSL، کرش) مدیریت شده
   ========================================================= */

package ir.rosha.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
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
    private var lastBackTime = 0L
    private var errorShown = false

    // ===== از config =====
    private val startUrl: String by lazy { AppConfig.str("webview", "url") }
    private val home: String by lazy { AppConfig.str("webview", "url_home").ifEmpty { startUrl } }
    private val jsEnabled: Boolean by lazy { AppConfig.bool("webview", "js_enabled") }
    private val zoomEnabled: Boolean by lazy { AppConfig.bool("webview", "zoom_enabled") }
    private val domStorage: Boolean by lazy { AppConfig.bool("webview", "dom_storage") }
    private val database: Boolean by lazy { AppConfig.bool("webview", "database") }
    private val backButton: Boolean by lazy { AppConfig.bool("webview", "back_button") }
    private val backDouble: Boolean by lazy { AppConfig.bool("webview", "back_double") }
    private val backExitMsg: String by lazy { AppConfig.str("webview", "back_exit_msg") }
    private val cacheEnabled: Boolean by lazy { AppConfig.bool("webview", "cache_enabled") }
    private val userAgentMode: String by lazy { AppConfig.str("webview", "user_agent") }
    private val userAgentCustom: String by lazy { AppConfig.str("webview", "user_agent_custom") }
    private val externalLinks: String by lazy { AppConfig.str("webview", "external_links") }
    private val mailLinks: String by lazy { AppConfig.str("webview", "mail_links") }
    private val telLinks: String by lazy { AppConfig.str("webview", "tel_links") }
    private val whatsappLinks: String by lazy { AppConfig.str("webview", "whatsapp_links") }
    private val telegramLinks: String by lazy { AppConfig.str("webview", "telegram_links") }
    private val instagramLinks: String by lazy { AppConfig.str("webview", "instagram_links") }
    private val errorEnabled: Boolean by lazy { AppConfig.bool("errors", "enabled") }

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

        if (backButton) {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val wv = webView
                    if (wv != null && wv.canGoBack() && !errorShown) {
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
            if (System.currentTimeMillis() - lastBackTime < 2000) {
                finish()
            } else {
                lastBackTime = System.currentTimeMillis()
                if (backExitMsg.isNotEmpty()) {
                    Toast.makeText(this, backExitMsg, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            finish()
        }
    }

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

        settings.javaScriptEnabled = jsEnabled
        settings.setSupportZoom(zoomEnabled)
        settings.builtInZoomControls = zoomEnabled
        settings.displayZoomControls = false
        settings.domStorageEnabled = domStorage
        settings.databaseEnabled = database

        settings.cacheMode = if (cacheEnabled) {
            WebSettings.LOAD_DEFAULT
        } else {
            WebSettings.LOAD_NO_CACHE
        }

        when (userAgentMode) {
            "mobile"  -> settings.userAgentString = settings.userAgentString + " RoshaApp"
            "desktop" -> settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            "custom"  -> if (userAgentCustom.isNotEmpty()) settings.userAgentString = userAgentCustom
        }

        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.mediaPlaybackRequiresUserGesture = false

        // ===== WebViewClient =====
        wv.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()

                if (url.startsWith("tel:") && telLinks == "external") {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                    return true
                }

                if (url.startsWith("mailto:") && mailLinks == "external") {
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                    return true
                }

                if ((url.contains("wa.me") || url.contains("api.whatsapp.com") || url.startsWith("whatsapp:")) && whatsappLinks == "external") {
                    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true } catch (e: Exception) { return false }
                }

                if ((url.contains("t.me") || url.startsWith("tg:")) && telegramLinks == "external") {
                    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true } catch (e: Exception) { return false }
                }

                if (url.contains("instagram.com") && instagramLinks == "external") {
                    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true } catch (e: Exception) { return false }
                }

                if (externalLinks == "external") {
                    val host = Uri.parse(startUrl).host ?: ""
                    if (!url.contains(host)) {
                        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true } catch (e: Exception) { return false }
                    }
                }

                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                currentUrl = url ?: ""
                errorShown = false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)

                if (request?.isForMainFrame != true) return
                if (errorShown) return
                errorShown = true

                if (!errorEnabled) return

                view?.stopLoading()

                val errorType = when (error?.errorCode) {
                    WebViewClient.ERROR_HOST_LOOKUP -> "dns"
                    WebViewClient.ERROR_CONNECT -> "conn"
                    WebViewClient.ERROR_TIMEOUT -> "to"
                    WebViewClient.ERROR_FAILED_SSL_HANDSHAKE -> "ssl"
                    WebViewClient.ERROR_BAD_URL -> "unk"
                    else -> "unk"
                }

                showError(errorType)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)

                if (request?.isForMainFrame != true) return
                if (errorShown) return
                errorShown = true

                if (!errorEnabled) return

                view?.stopLoading()

                val statusCode = errorResponse?.statusCode ?: 0
                val errorType = when (statusCode) {
                    404 -> "nf"
                    403 -> "fb"
                    500, 502, 503, 504 -> "server"
                    408 -> "to"
                    else -> "unk"
                }

                showError(errorType)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)

                if (errorShown) {
                    handler?.cancel()
                    return
                }
                errorShown = true

                // ===== لغو اتصال ناامن =====
                handler?.cancel()

                if (!errorEnabled) return

                view?.stopLoading()
                showError("ssl")
            }

            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                // ===== WebView کرش کرده =====
                if (!errorEnabled) return false

                webView?.destroy()
                webView = null

                showError("unk")
                return true
            }
        }

        // ===== WebChromeClient =====
        wv.webChromeClient = object : WebChromeClient() {}

        // ===== لود =====
        wv.loadUrl(startUrl)
    }

    /* =====================================================
       نمایش صفحه‌ی خطای زیبا
       ===================================================== */
    private fun showError(errorType: String) {
        val intent = Intent(this, ErrorActivity::class.java)
        intent.putExtra("error_type", errorType)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
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
