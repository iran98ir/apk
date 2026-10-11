package ir.rosha

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── مقداردهی Config ──
        try {
            Config.init(this)
        } catch (e: Throwable) {
            showCrashScreen("Config init failed", e)
            return
        }

        // ── مقدارگیری با محافظ ──
        val bg: String
        val hasIcon: Boolean
        val hasText: Boolean
        val iconSvg: String
        val iconColor: String
        val iconColorOn: Boolean
        val iconBgOn: Boolean
        val iconBg: String
        val iconShape: String
        val iconBoxSize: Int
        val iconSize: Int
        val title: String
        val tSize: Int
        val txtColor: String

        try {
            bg          = Config.splashBg
            hasIcon     = Config.splashHasIcon
            hasText     = Config.splashHasText
            iconSvg     = Config.splashIconSvg
            iconColor   = Config.splashIconColor
            iconColorOn = Config.splashIconColorOn
            iconBgOn    = Config.splashIconBgOn
            iconBg      = Config.splashIconBg
            iconShape   = Config.splashIconShape
            iconBoxSize = Config.splashIconBoxSize
            iconSize    = Config.splashIconSize
            title       = Config.splashTitle
            tSize       = Config.splashTSize
            txtColor    = Config.splashTxt
        } catch (e: Throwable) {
            showCrashScreen("Read splash config failed", e)
            return
        }

        // ── ساخت صفحه ──
        try {
            val root = FrameLayout(this)
            root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            root.setBackgroundColor(parseColorSafe(bg, Color.BLACK))

            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            container.gravity = Gravity.CENTER
            container.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // ── آیکون ──
            if (hasIcon && iconSvg.isNotBlank()) {
                val iconBox = FrameLayout(this)
                val boxSize = dp(iconBoxSize)
                val lp = LinearLayout.LayoutParams(boxSize, boxSize)
                lp.bottomMargin = dp(20)
                iconBox.layoutParams = lp

                if (iconBgOn) {
                    val shape = GradientDrawable()
                    shape.shape = GradientDrawable.RECTANGLE
                    shape.setColor(parseColorSafe(iconBg, Color.TRANSPARENT))
                    shape.cornerRadius = when (iconShape) {
                        "circle"   -> boxSize / 2f
                        "rounded"  -> boxSize * 0.22f
                        "square"   -> 0f
                        "squircle" -> boxSize * 0.30f
                        else       -> boxSize / 2f
                    }
                    iconBox.background = shape
                }

                val iv = ImageView(this)
                val ivSize = dp(iconSize)
                val ivLp = FrameLayout.LayoutParams(ivSize, ivSize)
                ivLp.gravity = Gravity.CENTER
                iv.layoutParams = ivLp
                SvgLoader.loadInto(iv, iconSvg, iconColor, iconColorOn)
                iconBox.addView(iv)

                container.addView(iconBox)
            }

            // ── متن ──
            if (hasText && title.isNotEmpty()) {
                val tv = TextView(this)
                tv.text = title
                tv.setTextColor(parseColorSafe(txtColor, Color.WHITE))
                tv.textSize = tSize.toFloat()
                tv.setTypeface(tv.typeface, android.graphics.Typeface.BOLD)
                tv.gravity = Gravity.CENTER
                container.addView(tv)
            }

            root.addView(container)
            setContentView(root)

        } catch (e: Throwable) {
            showCrashScreen("Build splash UI failed", e)
            return
        }

        // ── انتقال بعد از پایان اسپلش ──
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val next = when {
                    Config.onbActive && shouldShowOnb() -> Intent(this, OnbActivity::class.java)
                    Config.welcomeActive -> Intent(this, WelcomeActivity::class.java)
                    else -> Intent(this, WebActivity::class.java)
                }
                startActivity(next)
                finish()
            } catch (e: Throwable) {
                showCrashScreen("Navigate failed", e)
            }
        }, 2000)
    }

    private fun shouldShowOnb(): Boolean {
        if (!Config.onbFirstRunOnly) return true
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return !prefs.getBoolean("onb_seen", false)
    }

    private fun parseColorSafe(hex: String, fallback: Int): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Throwable) {
            fallback
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    // ═══════════════ صفحه خطا ═══════════════
    private fun showCrashScreen(stage: String, e: Throwable) {
        try {
            val scroll = android.widget.ScrollView(this)
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(dp(20), dp(60), dp(20), dp(40))
            layout.setBackgroundColor(Color.parseColor("#0F172A"))

            val title = TextView(this)
            title.text = "⚠ اپ خطا داد"
            title.setTextColor(Color.parseColor("#F87171"))
            title.textSize = 22f
            title.setTypeface(title.typeface, android.graphics.Typeface.BOLD)
            layout.addView(title)

            val stageTv = TextView(this)
            stageTv.text = "مرحله: $stage"
            stageTv.setTextColor(Color.parseColor("#FCD34D"))
            stageTv.textSize = 14f
            stageTv.setPadding(0, dp(16), 0, dp(8))
            layout.addView(stageTv)

            val bodyTv = TextView(this)
            bodyTv.text = "${e.javaClass.name}: ${e.message}\n\n${e.stackTraceToString()}"
            bodyTv.setTextColor(Color.WHITE)
            bodyTv.textSize = 12f
            bodyTv.setPadding(0, dp(8), 0, dp(16))
            bodyTv.setTextIsSelectable(true)
            bodyTv.typeface = android.graphics.Typeface.MONOSPACE
            layout.addView(bodyTv)

            val copyBtn = TextView(this)
            copyBtn.text = "📋 کپی خطا"
            copyBtn.setTextColor(Color.WHITE)
            copyBtn.textSize = 16f
            copyBtn.setTypeface(copyBtn.typeface, android.graphics.Typeface.BOLD)
            copyBtn.gravity = Gravity.CENTER
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(Color.parseColor("#4F46E5"))
            shape.cornerRadius = dp(14).toFloat()
            copyBtn.background = shape
            copyBtn.setPadding(dp(30), dp(14), dp(30), dp(14))
            copyBtn.setOnClickListener {
                val cm = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                cm.setPrimaryClip(android.content.ClipData.newPlainText("crash", bodyTv.text))
                android.widget.Toast.makeText(this, "کپی شد ✓", android.widget.Toast.LENGTH_SHORT).show()
            }
            layout.addView(copyBtn)

            scroll.addView(layout)
            setContentView(scroll)
        } catch (t: Throwable) {
            // اگه خود این صفحه هم خطا داد، ساده‌ترین حالت
            val tv = TextView(this)
            tv.text = "${stage}: ${e.message}"
            tv.setTextColor(Color.WHITE)
            tv.setBackgroundColor(Color.BLACK)
            setContentView(tv)
        }
    }
}
