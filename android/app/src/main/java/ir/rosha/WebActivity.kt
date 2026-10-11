package ir.rosha

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class WebActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var rootLayout: FrameLayout
    private var pendingFileChooser: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_REQ = 1001

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootLayout = FrameLayout(this)

        // ── نوار پیشرفت ──
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progressBar.max = 100
        progressBar.visibility = View.GONE
        val progLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(3)
        )
        progressBar.layoutParams = progLp
        rootLayout.addView(progressBar)

        // ── WebView ──
        webView = WebView(this)
        val wvLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView.layoutParams = wvLp
        rootLayout.addView(webView)

        setContentView(rootLayout)

        setupWebView()
        setupClients()
        setupDownloadListener()

        // لود اولیه
        if (Config.webUrl.isNotEmpty()) {
            webView.loadUrl(Config.webUrl)
        } else {
            showErrorPage("offline")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            loadsImagesAutomatically = true
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        if (Config.webKeepSession) {
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        }
    }

    private fun setupClients() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()

                // لینک‌های همیشه مرورگر
                if (Config.webBrowserOnly.isNotEmpty()) {
                    val domains = Config.webBrowserOnly.split(",").map { it.trim() }
                    if (domains.any { url.contains(it, ignoreCase = true) }) {
                        openInBrowser(url)
                        return true
                    }
                }

                // لینک‌های خارجی
                if (Config.webExtLink) {
                    val mainHost = Uri.parse(Config.webUrl).host ?: ""
                    val thisHost = request.url.host ?: ""
                    if (mainHost.isNotEmpty() && thisHost.isNotEmpty() && !thisHost.endsWith(mainHost)) {
                        openInBrowser(url)
                        return true
                    }
                }

                return false
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 0
            }

            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    val errKey = when (error.errorCode) {
                        WebViewClient.ERROR_HOST_LOOKUP      -> "dns"
                        WebViewClient.ERROR_TIMEOUT          -> "timeout"
                        WebViewClient.ERROR_CONNECT          -> "down"
                        WebViewClient.ERROR_FAILED_SSL_HANDSHAKE -> "ssl"
                        else -> "offline"
                    }
                    showErrorPage(errKey)
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                pendingFileChooser?.onReceiveValue(null)
                pendingFileChooser = filePathCallback

                val intent = fileChooserParams.createIntent()
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQ)
                } catch (e: Exception) {
                    pendingFileChooser = null
                    return false
                }
                return true
            }
        }
    }

    private fun setupDownloadListener() {
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            try {
                val req = DownloadManager.Request(Uri.parse(url))
                req.setMimeType(mimeType)
                req.addRequestHeader("User-Agent", userAgent)
                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                req.setDestinationInExternalPublicDir(
                    android.os.Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimeType)
                )
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(req)
            } catch (e: Exception) {
                // دانلود ناموفق
            }
        }
    }

    private fun openInBrowser(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            webView.loadUrl(url)
        }
    }

    private fun showErrorPage(errKey: String) {
        val root = FrameLayout(this)
        root.setBackgroundColor(Color.parseColor(Config.errorBg))

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.gravity = Gravity.CENTER
        container.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        container.setPadding(dp(28), dp(28), dp(28), dp(28))

        val iconSvg = Config.errorItemField(errKey, "iconSvg", "")
        val iconColor = Config.errorItemField(errKey, "iconColor", "#DC2626")
        val iconColorOn = Config.errorItemBool(errKey, "iconColorOn", true)
        val iconBgOn = Config.errorItemBool(errKey, "iconBgOn", true)
        val iconBg = Config.errorItemField(errKey, "iconBg", "#FEE2E2")
        val iconShape = Config.errorItemField(errKey, "iconShape", "circle")
        val iconSize = Config.errorItemInt(errKey, "iconSize", 70)
        val iconBoxSize = Config.errorItemInt(errKey, "iconBoxSize", 140)
        val title = Config.errorItemField(errKey, "t", "خطا")
        val desc = Config.errorItemField(errKey, "d", "")

        if (iconSvg.isNotEmpty()) {
            val boxPx = dp(iconBoxSize)
            val iconBox = FrameLayout(this)
            val lp = LinearLayout.LayoutParams(boxPx, boxPx)
            lp.bottomMargin = dp(24)
            iconBox.layoutParams = lp

            if (iconBgOn) {
                val shape = android.graphics.drawable.GradientDrawable()
                shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                shape.setColor(Color.parseColor(iconBg))
                shape.cornerRadius = when (iconShape) {
                    "circle"   -> boxPx / 2f
                    "rounded"  -> boxPx * 0.22f
                    "square"   -> 0f
                    "squircle" -> boxPx * 0.30f
                    else       -> boxPx / 2f
                }
                iconBox.background = shape
            }

            val iv = ImageView(this)
            val ivPx = dp(iconSize)
            val ivLp = FrameLayout.LayoutParams(ivPx, ivPx)
            ivLp.gravity = Gravity.CENTER
            iv.layoutParams = ivLp
            SvgLoader.loadInto(iv, iconSvg, iconColor, iconColorOn)
            iconBox.addView(iv)

            container.addView(iconBox)
        }

        val tvTitle = TextView(this)
        tvTitle.text = title
        tvTitle.setTextColor(Color.parseColor(Config.errorTxt))
        tvTitle.textSize = Config.errorTSize.toFloat()
        tvTitle.setTypeface(tvTitle.typeface, android.graphics.Typeface.BOLD)
        tvTitle.gravity = Gravity.CENTER
        container.addView(tvTitle)

        if (desc.isNotEmpty()) {
            val tvDesc = TextView(this)
            tvDesc.text = desc
            tvDesc.setTextColor(Color.parseColor(Config.errorTxt))
            tvDesc.alpha = 0.72f
            tvDesc.textSize = Config.errorDSize.toFloat()
            tvDesc.gravity = Gravity.CENTER
            tvDesc.setPadding(0, dp(12), 0, 0)
            container.addView(tvDesc)
        }

        val btn = TextView(this)
        btn.text = Config.errorRetryText
        btn.setTextColor(Color.WHITE)
        btn.textSize = 16f
        btn.setTypeface(btn.typeface, android.graphics.Typeface.BOLD)
        btn.gravity = Gravity.CENTER

        val btnShape = android.graphics.drawable.GradientDrawable()
        btnShape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        btnShape.setColor(Color.parseColor(Config.errorBtnBg))
        btnShape.cornerRadius = when (Config.errorBtnShape) {
            "pill"   -> 999f
            "square" -> 0f
            else     -> dp(14).toFloat()
        }
        btn.background = btnShape
        btn.setPadding(dp(38), dp(16), dp(38), dp(16))

        val btnLp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        btnLp.topMargin = dp(24)
        btn.layoutParams = btnLp

        btn.setOnClickListener {
            setContentView(rootLayout)
            if (Config.webUrl.isNotEmpty()) {
                webView.loadUrl(Config.webUrl)
            }
        }
        container.addView(btn)

        root.addView(container)
        setContentView(root)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQ) {
            if (pendingFileChooser != null) {
                val results = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                pendingFileChooser?.onReceiveValue(results)
                pendingFileChooser = null
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
            return
        }

        if (Config.exitActive) {
            showExitDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle(Config.exitTitle)
            .setMessage(Config.exitMsg)
            .setPositiveButton(Config.exitYes) { _, _ -> finish() }
            .setNegativeButton(Config.exitNo, null)
            .show()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
