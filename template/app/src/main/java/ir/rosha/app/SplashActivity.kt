/* =========================================================
   SplashActivity.kt  —  صفحه‌ی اسپلش
   مسیر: template/app/src/main/java/ir/rosha/app/SplashActivity.kt
   =========================================================
   📌 اسپلش با انیمیشن و لودر
   📌 بعد از مدت مشخص، میره به صفحه‌ی بعدی
   📌 تصمیم میگیره: Onboarding یا VPN Warning
   ========================================================= */

package ir.rosha.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
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

        // ===== چک آیا کاربر قبلاً آنبوردینگ دیده؟ =====
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean("onboarding_done", false)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0D1B2E)
            ) {
                SplashScreen(
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
private fun SplashScreen(onFinished: () -> Unit) {

    // ===== انیمیشن ورود =====
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // ===== ورود با انیمیشن =====
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f))
        )
        alpha.animateTo(1f, animationSpec = tween(400))
    }

    // ===== بعد از مدت مشخص، برو به بعدی =====
    LaunchedEffect(Unit) {
        delay(2200)
        onFinished()
    }

    // ===== گرادیانت پس‌زمینه =====
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D1B2E),
            Color(0xFF1B2A4A)
        )
    )

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

            // ===== لوگو (دایره‌ی طلایی) =====
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .background(
                        color = Color(0xFFE8A33D),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ر",
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== عنوان =====
            Text(
                text = "روشا ۲۴",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.alpha(alpha.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== زیرعنوان =====
            Text(
                text = "همراه تو در هر مرحله",
                color = Color(0xFFB8B0A0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(alpha.value),
                textAlign = TextAlign.Center
            )
        }

        // ===== لودر پایین صفحه =====
        LoaderDots(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
            color = Color(0xFFE8A33D)
        )
    }
}

/* =========================================================
   لودر سه نقطه
   ========================================================= */
@Composable
private fun LoaderDots(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    val dots = remember { listOf(Animatable(0.4f), Animatable(0.4f), Animatable(0.4f)) }

    LaunchedEffect(Unit) {
        while (true) {
            dots.forEachIndexed { index, anim ->
                delay(150L)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(300)
                )
                anim.animateTo(
                    targetValue = 0.4f,
                    animationSpec = tween(300)
                )
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
