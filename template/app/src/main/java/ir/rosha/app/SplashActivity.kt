/* =========================================================
   SplashActivity.kt — صفحه اسپلش
   مسیر: template/app/src/main/java/ir/rosha/app/SplashActivity.kt
   =========================================================
   📌 فقط از config.json می‌خونه
   📌 رنگ نوار بالا/پایین از colors.color_background
   📌 بعد از اسپلش: Onboarding یا VPN یا Welcome یا WebView
   ========================================================= */

package ir.rosha.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== رنگ نوار بالا و پایین =====
        val bgBarColor = AppConfig.color("colors", "color_background")
        window.statusBarColor = bgBarColor
        window.navigationBarColor = bgBarColor

        // ===== از config.json =====
        val enabled       = AppConfig.bool("splash", "enabled")
        val duration      = AppConfig.int("splash", "duration")
        val title         = AppConfig.str("splash", "title")
        val subtitle      = AppConfig.str("splash", "subtitle")
        val titleColor    = AppConfig.color("splash", "title_color")
        val subtitleColor = AppConfig.color("splash", "subtitle_color")
        val titleSize     = AppConfig.int("splash", "title_size")
        val subtitleSize  = AppConfig.int("splash", "subtitle_size")
        val loaderColor   = AppConfig.color("splash", "loader_color")
        val showLoader    = AppConfig.bool("splash", "show_loader")
        val bgType        = AppConfig.str("splash", "bg_type")
        val bgColor1      = AppConfig.color("splash", "bg_color_1")
        val bgColor2      = AppConfig.color("splash", "bg_color_2")

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val onboardingDone    = prefs.getBoolean("onboarding_done", false)
        val onboardingEnabled = AppConfig.bool("onboarding", "enabled")
        val vpnEnabled        = AppConfig.bool("vpn", "enabled")
        val welcomeEnabled    = AppConfig.bool("welcome", "enabled")

        // ===== اگه اسپلش خاموش بود =====
        if (!enabled) {
            goNext(onboardingDone, onboardingEnabled, vpnEnabled, welcomeEnabled)
            return
        }

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(bgColor1)
            ) {
                SplashScreen(
                    duration      = duration.toLong(),
                    title         = title,
                    subtitle      = subtitle,
                    titleColor    = titleColor,
                    subtitleColor = subtitleColor,
                    titleSize     = titleSize,
                    subtitleSize  = subtitleSize,
                    loaderColor   = loaderColor,
                    showLoader    = showLoader,
                    bgType        = bgType,
                    bgColor1      = bgColor1,
                    bgColor2      = bgColor2,
                    onFinished    = { goNext(onboardingDone, onboardingEnabled, vpnEnabled, welcomeEnabled) }
                )
            }
        }
    }

    private fun goNext(
        onboardingDone: Boolean,
        onboardingEnabled: Boolean,
        vpnEnabled: Boolean,
        welcomeEnabled: Boolean
    ) {
        val next = when {
            !onboardingDone && onboardingEnabled -> OnboardingActivity::class.java
            !onboardingDone && vpnEnabled        -> VpnWarningActivity::class.java
            welcomeEnabled                       -> WelcomeActivity::class.java
            else                                 -> WebViewActivity::class.java
        }
        startActivity(Intent(this, next))
        finish()
    }
}

@Composable
private fun SplashScreen(
    duration: Long,
    title: String,
    subtitle: String,
    titleColor: Int,
    subtitleColor: Int,
    titleSize: Int,
    subtitleSize: Int,
    loaderColor: Int,
    showLoader: Boolean,
    bgType: String,
    bgColor1: Int,
    bgColor2: Int,
    onFinished: () -> Unit
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = CubicBezierEasing(0.34f, 1.0f, 0.64f, 1.0f))
        )
    }

    LaunchedEffect(Unit) {
        delay(duration)
        onFinished()
    }

    val bgBrush = if (bgType == "gradient") {
        Brush.verticalGradient(colors = listOf(Color(bgColor1), Color(bgColor2)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(bgColor1), Color(bgColor1)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    color = Color(titleColor),
                    fontSize = titleSize.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.alpha(alpha.value),
                    textAlign = TextAlign.Center
                )
            }

            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = subtitle,
                    color = Color(subtitleColor),
                    fontSize = subtitleSize.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.alpha(alpha.value),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showLoader) {
            LoaderDots(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                color = Color(loaderColor)
            )
        }
    }
}

@Composable
private fun LoaderDots(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    val dots = remember { listOf(Animatable(0.4f), Animatable(0.4f), Animatable(0.4f)) }

    LaunchedEffect(Unit) {
        while (true) {
            dots.forEach { anim ->
                delay(150L)
                anim.animateTo(1f, animationSpec = tween(300))
                anim.animateTo(0.4f, animationSpec = tween(300))
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dots.forEach { anim ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(anim.value)
                    .alpha(anim.value)
                    .background(color, CircleShape)
            )
        }
    }
}
