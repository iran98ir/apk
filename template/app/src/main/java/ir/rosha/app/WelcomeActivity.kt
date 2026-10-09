/* =========================================================
   WelcomeActivity.kt — صفحه ورود به اپ
   مسیر: template/app/src/main/java/ir/rosha/app/WelcomeActivity.kt
   =========================================================
   📌 فقط از config.json می‌خونه
   📌 آیکون اختیاری — اگه خالی بود نشون داده نمیشه
   📌 رنگ نوار بالا/پایین از colors.color_background
   ========================================================= */

package ir.rosha.app

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class WelcomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== رنگ نوار بالا و پایین =====
        val bgBarColor = AppConfig.color("colors", "color_background")
        window.statusBarColor = bgBarColor
        window.navigationBarColor = bgBarColor

        // ===== از config.json =====
        val enabled          = AppConfig.bool("welcome", "enabled")

        // ===== اگه خاموش بود → مستقیم WebView =====
        if (!enabled) {
            goWebView()
            return
        }

        val title            = AppConfig.str("welcome", "title")
        val subtitle         = AppConfig.str("welcome", "subtitle")
        val icon             = AppConfig.str("welcome", "icon")
        val buttonText       = AppConfig.str("welcome", "button_text")
        val buttonBg         = AppConfig.color("welcome", "button_bg")
        val buttonTextColor  = AppConfig.color("welcome", "button_text_color")
        val bgColor          = AppConfig.color("welcome", "bg_color")
        val titleColor       = AppConfig.color("welcome", "title_color")
        val subtitleColor    = AppConfig.color("welcome", "subtitle_color")

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(bgColor)
            ) {
                WelcomeScreen(
                    title           = title,
                    subtitle        = subtitle,
                    icon            = icon,
                    buttonText      = buttonText,
                    buttonBg        = buttonBg,
                    buttonTextColor = buttonTextColor,
                    bgColor         = bgColor,
                    titleColor      = titleColor,
                    subtitleColor   = subtitleColor,
                    onEnter         = { goWebView() }
                )
            }
        }
    }

    private fun goWebView() {
        startActivity(Intent(this, WebViewActivity::class.java))
        finish()
    }
}

@Composable
private fun WelcomeScreen(
    title: String,
    subtitle: String,
    icon: String,
    buttonText: String,
    buttonBg: Int,
    buttonTextColor: Int,
    bgColor: Int,
    titleColor: Int,
    subtitleColor: Int,
    onEnter: () -> Unit
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = CubicBezierEasing(0.34f, 1.0f, 0.64f, 1.0f))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(bgColor)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ===== آیکون (اختیاری) =====
            if (icon.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color(titleColor).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 64.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // ===== تیتر (اختیاری) =====
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    color = Color(titleColor),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.alpha(alpha.value),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ===== زیرتیتر (اختیاری) =====
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = Color(subtitleColor),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.alpha(alpha.value),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(48.dp))
            }

            // ===== دکمه ورود =====
            Button(
                onClick = onEnter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(buttonBg),
                    contentColor = Color(buttonTextColor)
                )
            ) {
                Text(
                    text = buttonText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
