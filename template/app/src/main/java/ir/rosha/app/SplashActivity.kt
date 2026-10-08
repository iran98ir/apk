/* =========================================================
   SplashActivity.kt — صفحه اسپلش
   مسیر: template/app/src/main/java/ir/rosha/app/SplashActivity.kt
   =========================================================
   📌 همه چیز از config.json خونده می‌شه
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

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean("onboarding_done", false)

        // ===== از config =====
        val enabled = AppConfig.bool("splash", "enabled", true)
        val duration = AppConfig.int("splash", "duration", 2000)
        val title = AppConfig.str("splash", "title", "")
        val subtitle = AppConfig.str("splash", "subtitle", "")
        val titleColor = AppConfig.color("splash", "title_color", "#E8A33D")
        val subtitleColor = AppConfig.color("splash", "subtitle_color", "#B8B0A0")
        val titleSize = AppConfig.int("splash", "title_size", 28)
        val subtitleSize = AppConfig.int("splash", "subtitle_size", 16)
        val loaderColor = AppConfig.color("splash", "loader_color", "#E8A33D")
        val showLoader = AppConfig.bool("splash", "show_loader", true)
        val logoSize = AppConfig.int("splash", "logo_size", 180)

        // پس‌زمینه
        val bgType = AppConfig.str("splash", "bg_type", "gradient")
        val bgColor1 = AppConfig.color("splash", "bg_color_1", "#0D1B2E")
        val bgColor2 = AppConfig.color("splash", "bg_color_2", "#1B2A4A")

        // اسم اپ از branding
        val appName = AppConfig.str("branding", "app_name", "اپلیکیشن")

        // ===== اگه splash خاموش بود =====
        if (!enabled) {
            val nextIntent = if (onboardingDone) {
                Intent(this, WebViewActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            startActivity(nextIntent)
            finish()
            return
        }

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(bgColor1)
            ) {
                SplashScreen(
                    duration = duration.toLong(),
                    appName = appName,
                    title = title.ifEmpty { appName },
                    subtitle = subtitle,
                    titleColor = titleColor,
                    subtitleColor = subtitleColor,
                    titleSize = titleSize,
                    subtitleSize = subtitleSize,
                    loaderColor = loaderColor,
                    showLoader = showLoader,
                    logoSize = logoSize,
                    bgType = bgType,
                    bgColor1 = bgColor1,
                    bgColor2 = bgColor2,
                    onFinished = {
                        val nextIntent = if (onboardingDone) {
                            Intent(this, WebViewActivity::class.java)
                        } else {
                            Intent(this, OnboardingActivity::class.java)
                        }
                        startActivity(nextIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun SplashScreen(
    duration: Long,
    appName: String,
    title: String,
    subtitle: String,
    titleColor: Int,
    subtitleColor: Int,
    titleSize: Int,
    subtitleSize: Int,
    loaderColor: Int,
    showLoader: Boolean,
    logoSize: Int,
    bgType: String,
    bgColor1: Int,
    bgColor2: Int,
    onFinished: () -> Unit
) {

    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f))
        )
        alpha.animateTo(1f, animationSpec = tween(400))
    }

    LaunchedEffect(Unit) {
        delay(duration)
        onFinished()
    }

    val bgGradient = if (bgType == "gradient") {
        Brush.verticalGradient(colors = listOf(Color(bgColor1), Color(bgColor2)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(bgColor1), Color(bgColor1)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ===== لوگو (دایره) =====
            Box(
                modifier = Modifier
                    .size(logoSize.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .background(
                        color = Color(titleColor),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appName.take(1).ifEmpty { "ر" },
                    color = Color.White,
                    fontSize = (logoSize / 2).sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== عنوان =====
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    color = Color(titleColor),
                    fontSize = titleSize.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.alpha(alpha.value),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ===== زیرعنوان =====
            if (subtitle.isNotEmpty()) {
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

        // ===== لودر =====
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
