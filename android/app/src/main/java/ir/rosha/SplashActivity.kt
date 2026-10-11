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

        val root = FrameLayout(this)
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        root.setBackgroundColor(Color.parseColor(Config.splashBg))

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.gravity = Gravity.CENTER
        container.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // ── آیکون ──
        if (Config.splashHasIcon) {
            val iconBox = FrameLayout(this)
            val boxSize = dp(Config.splashIconBoxSize)
            val lp = LinearLayout.LayoutParams(boxSize, boxSize)
            lp.bottomMargin = dp(20)
            iconBox.layoutParams = lp

            if (Config.splashIconBgOn) {
                val shape = GradientDrawable()
                shape.shape = GradientDrawable.RECTANGLE
                shape.setColor(Color.parseColor(Config.splashIconBg))
                shape.cornerRadius = when (Config.splashIconShape) {
                    "circle"   -> boxSize / 2f
                    "rounded"  -> boxSize * 0.22f
                    "square"   -> 0f
                    "squircle" -> boxSize * 0.30f
                    else       -> boxSize / 2f
                }
                iconBox.background = shape
            }

            val iv = ImageView(this)
            val ivSize = dp(Config.splashIconSize)
            val ivLp = FrameLayout.LayoutParams(ivSize, ivSize)
            ivLp.gravity = Gravity.CENTER
            iv.layoutParams = ivLp
            SvgLoader.loadInto(iv, Config.splashIconSvg, Config.splashIconColor, Config.splashIconColorOn)
            iconBox.addView(iv)

            container.addView(iconBox)
        }

        // ── متن ──
        if (Config.splashHasText && Config.splashTitle.isNotEmpty()) {
            val tv = TextView(this)
            tv.text = Config.splashTitle
            tv.setTextColor(Color.parseColor(Config.splashTxt))
            tv.textSize = Config.splashTSize.toFloat()
            tv.setTypeface(tv.typeface, android.graphics.Typeface.BOLD)
            tv.gravity = Gravity.CENTER
            container.addView(tv)
        }

        root.addView(container)
        setContentView(root)

        // ── انتقال ──
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = when {
                Config.onbActive && shouldShowOnb() -> Intent(this, OnbActivity::class.java)
                Config.welcomeActive -> Intent(this, WelcomeActivity::class.java)
                else -> Intent(this, WebActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2000)
    }

    private fun shouldShowOnb(): Boolean {
        if (!Config.onbFirstRunOnly) return true
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return !prefs.getBoolean("onb_seen", false)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
