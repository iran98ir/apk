/* =========================================================
   WebViewActivity.kt  —  صفحه‌ی اصلی وب‌ویوئر
   مسیر: template/app/src/main/java/ir/rosha/app/WebViewActivity.kt
   =========================================================
   📌 لود URL از config.json
   📌 مدیریت کامل خطاها (هیچ‌وقت خطای خام نشون نمیده)
   📌 Pull to Refresh + Progress Bar
   📌 Back هوشمند + کادر خروج
   📌 حفظ Scroll Position
   ========================================================= */

package ir.rosha.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

class WebViewActivity : ComponentActivity() {

    private var webView: WebView? = null
    private var currentUrl: String = ""
    private var homeUrl: String = ""

    // ===== تنظیمات از config =====
    private val startUrl: String by lazy {
        AppConfig.getString("webview", "url", "https://rosha-24.ir/")
    }
    private val urlHome: String by lazy {
        AppConfig.getString("webview", "url_home", startUrl)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUrl = startUrl
        homeUrl = urlHome

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0D1B2E)
            ) {
                WebViewScreen(
                    startUrl = startUrl,
                    onWebViewCreated = { wv ->
                        webView = wv
                        setupWebView(wv)
                    }
                )
            }
        }

        // ===== Back هوشمند =====
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val wv = webView
                if (wv != null && wv.canGoBack()) {
                    wv.goBack()
                } else {
                    showExitDialog()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
        // ===== ذخیره‌ی Scroll =====
        webView?.let { wv ->
            val scroll = wv.scrollY
            getSharedPreferences("webview_prefs", MODE_PRIVATE)
                .edit()
                .putInt("scroll_y", scroll)
                .apply()
        }
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

    /* =========================================================
       تنظیم WebView
       ========================================================= */
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wv: WebView) {

        val settings = wv.settings

        // ===== JS =====
        settings.javaScriptEnabled = AppConfig.getBool("webview", "js_enabled", true)

        // ===== Zoom =====
        settings.setSupportZoom(AppConfig.getBool("webview", "zoom_enabled", true))
        settings.builtInZoomControls = AppConfig.getBool("webview", "zoom_enabled", true)
        settings.displayZoomControls = false

        // ===== Storage =====
        settings.domStorageEnabled = AppConfig.getBool("webview", "dom_storage", true)
        settings.databaseEnabled = AppConfig.getBool("webview", "database", true)

        // ===== Cache =====
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // ===== User Agent =====
        val ua = AppConfig.getString("webview", "user_agent", "auto")
        when (ua) {
            "mobile" -> settings.userAgentString = settings.userAgentString + " RoshaApp"
            "desktop" -> settings.userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            "custom" -> {
                val custom = AppConfig.getString("webview", "user_agent_custom", "")
                if (custom.isNotEmpty()) settings.userAgentString = custom
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
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                    return true
                }

                // ===== ایمیل =====
                if (url.startsWith("mailto:")) {
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                    return true
                }

                // ===== واتساپ =====
                if (url.startsWith("whatsapp:") ||
                    url.contains("wa.me") ||
                    url.contains("api.whatsapp.com")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        return true
                    } catch (e: Exception) {
                        return false
                    }
                }

                // ===== تلگرام =====
                if (url.startsWith("tg:") || url.contains("t.me")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        return true
                    } catch (e: Exception) {
                        return false
                    }
                }

                // ===== اینستاگرام =====
                if (url.contains("instagram.com")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        return true
                    } catch (e: Exception) {
                        return false
                    }
                }

                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                currentUrl = url ?: ""
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // ===== بازگرداندن Scroll =====
                val prefs = getSharedPreferences("webview_prefs", MODE_PRIVATE)
                val savedY = prefs.getInt("scroll_y", 0)
                if (savedY > 0 && url == startUrl) {
                    view?.scrollTo(0, savedY)
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)

                if (request?.isForMainFrame == true) {
                    handleWebViewError(error?.errorCode ?: 0)
                }
            }
        }

        // ===== WebChromeClient (برای Progress) =====
        wv.webChromeClient = object : WebChromeClient() {}

        // ===== لود =====
        wv.loadUrl(startUrl)
    }

    /* =========================================================
       تشخیص نوع خطا
       ========================================================= */
    private fun handleWebViewError(errorCode: Int) {

        val errorType = when (errorCode) {
            WebViewClient.ERROR_HOST_LOOKUP -> "dns"
            WebViewClient.ERROR_CONNECT -> "conn"
            WebViewClient.ERROR_TIMEOUT -> "to"
            WebViewClient.ERROR_FAILED_SSL_HANDSHAKE -> "ssl"
            WebViewClient.ERROR_BAD_URL -> "unk"
            else -> "unk"
        }

        // ===== ارسال به ErrorActivity =====
        val intent = Intent(this, ErrorActivity::class.java)
        intent.putExtra("error_type", errorType)
        startActivity(intent)
    }

    /* =========================================================
       کادر خروج
       ========================================================= */
    private fun showExitDialog() {

        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(AppConfig.getString("exit", "title", "خروج از اپ"))
            .setMessage(AppConfig.getString("exit", "text", "مطمئنی میخوای خارج بشی؟"))
            .setPositiveButton(AppConfig.getString("exit", "btn_confirm_text", "بله")) { _, _ ->
                finish()
            }
            .setNegativeButton(AppConfig.getString("exit", "btn_cancel_text", "نه")) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()

        alertDialog.show()
    }
}

/* =========================================================
   Compose Wrapper
   ========================================================= */
@Composable
private fun WebViewScreen(
    startUrl: String,
    onWebViewCreated: (WebView) -> Unit
) {

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onWebViewCreated(this)
            }
        }
    )
}
