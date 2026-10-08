/* =========================================================
   ErrorActivity.kt — صفحات خطا
   مسیر: template/app/src/main/java/ir/rosha/app/ErrorActivity.kt
   =========================================================
   📌 همه چیز از config.json خونده می‌شه
   ========================================================= */

package ir.rosha.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ErrorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorType = intent.getStringExtra("error_type") ?: "unk"

        // ===== از config =====
        val enabled = AppConfig.bool("errors", "enabled", true)

        if (!enabled) {
            startActivity(Intent(this, WebViewActivity::class.java))
            finish()
            return
        }

        val showRetry = AppConfig.bool("errors", "show_retry", true)
        val showHome = AppConfig.bool("errors", "show_home", true)
        val retryText = AppConfig.str("errors", "retry_text", "🔄 تلاش مجدد")
        val retryBg = AppConfig.color("errors", "retry_bg", "#E8A33D")
        val retryColor = AppConfig.color("errors", "retry_color", "#FFFFFF")
        val homeText = AppConfig.str("errors", "home_text", "🏠 بازگشت به خانه")
        val homeColor = AppConfig.color("errors", "home_color", "#B8B0A0")
        val homeBorder = AppConfig.color("errors", "home_border", "#B8B0A0")

        val data = buildErrorData(errorType)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(data.bg)
            ) {
                ErrorScreen(
                    data = data,
                    showRetry = showRetry,
                    showHome = showHome,
                    retryText = retryText,
                    retryBg = retryBg,
                    retryColor = retryColor,
                    homeText = homeText,
                    homeColor = homeColor,
                    homeBorder = homeBorder,
                    onRetry = {
                        startActivity(Intent(this, WebViewActivity::class.java))
                        finish()
                    },
                    onHome = {
                        startActivity(Intent(this, WebViewActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    private fun buildErrorData(type: String): ErrorData {
        val prefix = when (type) {
            "offline" -> "offline"
            "server" -> "server"
            "nf" -> "nf"
            "fb" -> "fb"
            "to" -> "to"
            "dns" -> "dns"
            "ssl" -> "ssl"
            "conn" -> "conn"
            else -> "unk"
        }
        return ErrorData(
            icon = AppConfig.str("errors", "${prefix}_icon", "⚠️"),
            title = AppConfig.str("errors", "${prefix}_title", "خطای نامشخص"),
            text = AppConfig.str("errors", "${prefix}_text", ""),
            color = AppConfig.color("errors", "${prefix}_color", "#E8A33D"),
            bg = AppConfig.color("errors", "${prefix}_bg", "#0D1B2E")
        )
    }
}

private data class ErrorData(
    val icon: String,
    val title: String,
    val text: String,
    val color: Int,
    val bg: Int
)

@Composable
private fun ErrorScreen(
    data: ErrorData,
    showRetry: Boolean,
    showHome: Boolean,
    retryText: String,
    retryBg: Int,
    retryColor: Int,
    homeText: String,
    homeColor: Int,
    homeBorder: Int,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(data.bg), Color(0xFF1B2A4A))
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ===== آیکون =====
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(data.color).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.icon,
                    fontSize = 64.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== عنوان =====
            Text(
                text = data.title,
                color = Color(data.color),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== متن =====
            if (data.text.isNotEmpty()) {
                Text(
                    text = data.text,
                    color = Color(0xFFB8B0A0),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ===== دکمه تلاش مجدد =====
            if (showRetry) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(retryBg),
                        contentColor = Color(retryColor)
                    )
                ) {
                    Text(
                        text = retryText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ===== دکمه خانه =====
            if (showHome) {
                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(homeColor)
                    )
                ) {
                    Text(
                        text = homeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
