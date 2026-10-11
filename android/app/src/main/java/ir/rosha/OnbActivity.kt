package ir.rosha

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class OnbActivity : AppCompatActivity() {

    private var pager: ViewPager2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            buildNormal()
        } catch (e: Throwable) {
            showDebugError(e)
        }
    }

    private fun buildNormal() {
        val total = if (Config.onbCount == "2") 2 else 3

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.parseColor(Config.onbBg))

        pager = ViewPager2(this)
        pager?.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        pager?.adapter = OnbAdapter(total)
        root.addView(pager)

        val dots = LinearLayout(this)
        dots.orientation = LinearLayout.HORIZONTAL
        dots.gravity = Gravity.CENTER
        val dotsLp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dotsLp.gravity = Gravity.BOTTOM
        dotsLp.bottomMargin = dp(40)
        dots.layoutParams = dotsLp
        for (i in 0 until total) {
            val dot = android.view.View(this)
            val d = dp(if (i == 0) 26 else 9)
            val lp = LinearLayout.LayoutParams(d, dp(9))
            lp.marginEnd = dp(8)
            dot.layoutParams = lp
            val bg = android.graphics.drawable.GradientDrawable()
            bg.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            bg.cornerRadius = dp(9).toFloat()
            bg.setColor(Color.parseColor(if (i == 0) Config.onbBg else "#33000000"))
            dot.background = bg
            dots.addView(dot)
        }
        root.addView(dots)

        setContentView(root)

        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("onb_seen", true)
            .apply()
    }

    inner class OnbAdapter(private val total: Int) :
        androidx.recyclerview.widget.RecyclerView.Adapter<OnbAdapter.VH>() {

        inner class VH(val view: LinearLayout) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val container = LinearLayout(parent.context)
            container.orientation = LinearLayout.VERTICAL
            container.gravity = Gravity.CENTER
            container.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            container.setPadding(dp(28), dp(60), dp(28), dp(120))
            return VH(container)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val v = holder.view
            v.removeAllViews()

            val page = Config.onbPage(position)
            val iconSvg = page.optString("iconSvg", "")
            val iconColor = page.optString("iconColor", "#4F46E5")
            val iconColorOn = page.optBoolean("iconColorOn", true)
            val iconBgOn = page.optBoolean("iconBgOn", true)
            val iconBg = page.optString("iconBg", "#EEF2FF")
            val iconShape = page.optString("iconShape", "circle")
            val boxSize = page.optInt("iconBoxSize", 140)
            val iconSize = page.optInt("iconSize", 70)
            val title = page.optString("t", "")
            val desc = page.optString("d", "")
            val tSize = page.optInt("tSize", 24)
            val dSize = page.optInt("dSize", 16)

            val iconBox = FrameLayout(this@OnbActivity)
            val boxPx = dp(boxSize)
            val lp = LinearLayout.LayoutParams(boxPx, boxPx)
            lp.bottomMargin = dp(28)
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

            val iv = ImageView(this@OnbActivity)
            val ivPx = dp(iconSize)
            val ivLp = FrameLayout.LayoutParams(ivPx, ivPx)
            ivLp.gravity = Gravity.CENTER
            iv.layoutParams = ivLp
            SvgLoader.loadInto(iv, iconSvg, iconColor, iconColorOn)
            iconBox.addView(iv)

            v.addView(iconBox)

            val tvTitle = TextView(this@OnbActivity)
            tvTitle.text = title
            tvTitle.setTextColor(Color.parseColor(Config.onbTxt))
            tvTitle.textSize = tSize.toFloat()
            tvTitle.setTypeface(tvTitle.typeface, android.graphics.Typeface.BOLD)
            tvTitle.gravity = Gravity.CENTER
            v.addView(tvTitle)

            val tvDesc = TextView(this@OnbActivity)
            tvDesc.text = desc
            tvDesc.setTextColor(Color.parseColor(Config.onbTxt))
            tvDesc.alpha = 0.72f
            tvDesc.textSize = dSize.toFloat()
            tvDesc.gravity = Gravity.CENTER
            tvDesc.setPadding(0, dp(12), 0, 0)
            v.addView(tvDesc)
        }

        override fun getItemCount(): Int = total
    }

    private fun showDebugError(e: Throwable) {
        val scroll = ScrollView(this)
        val tv = TextView(this)
        tv.text = "⚠️ ONBOARDING ERROR\n\n" +
                "Class: ${e.javaClass.name}\n\n" +
                "Message: ${e.message}\n\n" +
                "Stack:\n${e.stackTraceToString()}"
        tv.setTextColor(Color.RED)
        tv.setBackgroundColor(Color.WHITE)
        tv.textSize = 12f
        tv.setPadding(dp(16), dp(16), dp(16), dp(16))
        scroll.addView(tv)
        setContentView(scroll)
    }

    override fun onBackPressed() {
        if (pager != null && pager!!.currentItem > 0) {
            pager!!.currentItem = pager!!.currentItem - 1
        } else {
            super.onBackPressed()
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
