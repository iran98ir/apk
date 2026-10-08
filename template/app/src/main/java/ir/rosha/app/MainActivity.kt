package ir.rosha.app

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import ir.rosha.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ConfigLoader.load(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.webView

        setupWebView()
        applyConfig()
        loadUrl()
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = ConfigLoader.bool("webview.js_enabled", true)
            domStorageEnabled = ConfigLoader.bool("webview.dom_storage", true)
            databaseEnabled = ConfigLoader.bool("webview.database", true)
            builtInZoomControls = ConfigLoader.bool("webview.zoom_enabled", false)
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = if (ConfigLoader.bool("webview.cache_enabled", true))
                WebSettings.LOAD_DEFAULT
            else
                WebSettings.LOAD_NO_CACHE
            userAgentString = when (ConfigLoader.string("webview.user_agent", "auto")) {
                "mobile" -> "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36"
                "desktop" -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                "custom" -> ConfigLoader.string("webview.user_agent_custom", userAgentString)
                else -> userAgentString
            }
        }

        webView.webViewClient = WebViewClient()

        if (ConfigLoader.bool("webview.progress_bar", true)) {
            webView.webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    binding.progressBar.visibility =
                        if (newProgress < 100) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun applyConfig() {
        val bgColor = ConfigLoader.string("colors.color_background", "#0D1B2E")
        try {
            binding.root.setBackgroundColor(Color.parseColor(bgColor))
            webView.setBackgroundColor(Color.parseColor(bgColor))
        } catch (e: Exception) { }

        val progressColor = ConfigLoader.string("webview.progress_color", "#E8A33D")
        try {
            binding.progressBar.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor(progressColor))
        } catch (e: Exception) { }
    }

    private fun loadUrl() {
        val url = ConfigLoader.string("webview.url", "https://example.com")
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
