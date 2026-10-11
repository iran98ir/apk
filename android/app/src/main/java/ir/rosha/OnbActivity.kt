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

            val page = Config.onb
