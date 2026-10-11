package ir.rosha

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.parseColor(Config.welcomeBg))

        // سه ردیف: بالا / وسط / پایین
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val midRow = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val botRow = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        // ── آیکون ──
        if (Config.welcomeHasIcon && Config.welcomeIconPos != "hidden") {
            val iconBox = buildIconBox()
            when (Config.welcomeIconPos) {
                "top"    -> topRow.addView(iconBox)
                "center" -> midRow.addView(iconBox)
                "bottom" -> botRow.addView(iconBox)
            }
        }

        // ── متن + زیرمتن ──
        if (Config.welcomeTextPos != "hidden") {
            val textBlock = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
            val tvTitle = TextView(this).apply {
                text = Config.welcomeTitle
                setTextColor(Color.parseColor(Config.welcomeTxt))
                textSize = Config.welcomeTSize.toFloat()
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
            }
            val tvDesc = TextView(this).apply {
                text = Config.welcomeDesc
                setTextColor(Color.parseColor(Config.welcomeTxt))
                alpha = 0.72f
                textSize = Config.welcomeDSize.toFloat()
                gravity = Gravity.CENTER
                setPadding(0, dp(12), 0, 0)
            }
            textBlock.addView(tvTitle)
            if (Config.welcomeDesc.isNotEmpty()) textBlock.addView(tvDesc)

            when (Config.welcomeTextPos) {
                "top"    -> topRow.addView(textBlock)
                "center" -> midRow.addView(textBlock)
                "bottom" -> botRow.addView(textBlock)
            }
        }

        // ── دکمه ورود ──
        if (Config.welcomeBtnPos != "hidden") {
            val btn = buildLoginButton()
            val wrapper = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
            wrapper.addView(btn)
            when (Config.welcomeBtnPos) {
                "top"    -> topRow.addView(wrapper)
                "center" -> midRow.addView(wrapper)
                "bottom" -> botRow.addView(wrapper)
            }
        }

        // چیدمان سه ردیفه
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(dp(28), dp(60), dp(28), dp(60))
        }

        topRow.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply { gravity = Gravity.CENTER }

        midRow.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply { gravity = Gravity.CENTER }

        botRow.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply { gravity = Gravity.CENTER }

        container.addView(topRow)
        container.addView(midRow)
        container.addView(botRow)
        root.addView(container)

        setContentView(root)
    }

    private fun buildIconBox(): FrameLayout {
        val boxPx = dp(Config.welcomeIconBoxSize)
        val iconBox = FrameLayout(this)
        val lp = LinearLayout.LayoutParams(boxPx, boxPx)
        iconBox.layoutParams = lp

        if (Config.welcomeIconBgOn) {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(Color.parseColor(Config.welcomeIconBg))
            shape.cornerRadius = when (Config.welcomeIconShape) {
                "circle"   -> boxPx / 2f
                "rounded"  -> boxPx * 0.22f
                "square"   -> 0f
                "squircle" -> boxPx * 0.30f
                else       -> boxPx / 2f
            }
            iconBox.background = shape
        }

        val iv = ImageView(this)
        val ivPx = dp(Config.welcomeIconSize)
        val ivLp = FrameLayout.LayoutParams(ivPx, ivPx)
        ivLp.gravity = Gravity.CENTER
        iv.layoutParams = ivLp
        SvgLoader.loadInto(iv, Config.welcomeIconSvg, Config.welcomeIconColor, Config.welcomeIconColorOn)
        iconBox.addView(iv)

        return iconBox
    }

    private fun buildLoginButton(): TextView {
        val btn = TextView(this)
        btn.text = Config.welcomeBtn
        btn.setTextColor(Color.parseColor(Config.welcomeBtnTxt))
        btn.textSize = Config.welcomeBtnSize.toFloat()
        btn.setTypeface(btn.typeface, android.graphics.Typeface.BOLD)
        btn.gravity = Gravity.CENTER

        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        val bg = Config.welcomeBtnBg
        val style = Config.welcomeBtnStyle

        if (style == "outline") {
            shape.setColor(Color.TRANSPARENT)
            shape.setStroke(dp(2), Color.parseColor(bg))
            btn.setTextColor(Color.parseColor(bg))
        } else {
            shape.setColor(Color.parseColor(bg))
        }

        shape.cornerRadius = when (Config.welcomeBtnShape) {
            "pill"   -> 999f
            "square" -> 0f
            else     -> dp(Config.welcomeBtnRadius).toFloat()
        }

        btn.background = shape
        btn.setPadding(dp(38), dp(16), dp(38), dp(16))

        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        btn.layoutParams = lp

        btn.setOnClickListener {
            startActivity(Intent(this, WebActivity::class.java))
            finish()
        }

        return btn
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
