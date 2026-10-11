package ir.rosha

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // فقط یه TextView ساده — بدون Config، بدون SVG، بدون WebView
        val tv = TextView(this)
        tv.text = "STEP 1 OK"
        tv.textSize = 32f
        tv.setTextColor(Color.WHITE)
        tv.setBackgroundColor(Color.parseColor("#4F46E5"))
        tv.gravity = Gravity.CENTER

        val root = FrameLayout(this)
        root.addView(tv)

        setContentView(root)

        // بعد از ۳ ثانیه بسته شه
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 3000)
    }
}
