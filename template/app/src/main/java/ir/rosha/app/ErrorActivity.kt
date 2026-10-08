/* =========================================================
   ErrorActivity.kt  —  صفحات خطا
   مسیر: template/app/src/main/java/ir/rosha/app/ErrorActivity.kt
   =========================================================
   📌 ۹ نوع خطا با ظاهر زیبا
   📌 کاربر هرگز خطای خام مرورگر رو نمیبینه
   📌 دکمه‌ی تلاش مجدد + بازگشت به خانه
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

        // ===== نوع خطا از intent =====
        val errorType = intent.getStringExtra("error_type") ?: "unk"

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0D1B2E)
            ) {
                ErrorScreen(
                    errorType = errorType,
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
}

@Composable
private fun ErrorScreen(
    errorType: String,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {

    // ===== اطلاعات هر خطا =====
    val data = getErrorData(errorType)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B2E), Color(0xFF1B2A4A))
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
                    .background(data.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.icon,
                    fontSize = 64.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== تیتر =====
            Text(
                text = data.title,
                color = data.color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== متن =====
            Text(
                text = data.text,
                color = Color(0xFFB8B0A0),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ===== دکمه‌ی تلاش مجدد =====
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8A33D),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "🔄 تلاش مجدد",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== دکمه‌ی بازگشت به خانه =====
            OutlinedButton(
                onClick = onHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFB8B0A0)
                )
            ) {
                Text(
                    text = "🏠 بازگشت به خانه",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* =========================================================
   داده‌ی هر خطا
   ========================================================= */
private data class ErrorData(
    val icon: String,
    val title: String,
    val text: String,
    val color: Color
)

private fun getErrorData(type: String): ErrorData {
    return when (type) {
        "offline" -> ErrorData(
            icon = "📡",
            title = "اتصال اینترنت قطع است",
            text = "لطفاً اینترنت خود را بررسی کنید و دوباره تلاش کنید.",
            color = Color(0xFFC73E3E)
        )
        "server" -> ErrorData(
            icon = "🛠",
            title = "سرور در دسترس نیست",
            text = "مشکلی از سمت سرور پیش آمده. لطفاً چند لحظه بعد دوباره امتحان کنید.",
            color = Color(0xFFE8A33D)
        )
        "nf" -> ErrorData(
            icon = "🔍",
            title = "صفحه پیدا نشد",
            text = "متأسفانه صفحه‌ای که دنبالش بودید وجود ندارد.",
            color = Color(0xFF5B7FFF)
        )
        "fb" -> ErrorData(
            icon = "🚫",
            title = "دسترسی مسدود است",
            text = "شما به این بخش دسترسی ندارید.",
            color = Color(0xFFC73E3E)
        )
        "to" -> ErrorData(
            icon = "⏱",
            title = "زمان پاسخ سرور تمام شد",
            text = "اتصال شما کند است. لطفاً دوباره تلاش کنید.",
            color = Color(0xFFE8A33D)
        )
        "dns" -> ErrorData(
            icon = "🌍",
            title = "سرور در دسترس نیست",
            text = "لطفاً اتصال اینترنت خود را بررسی کنید.",
            color = Color(0xFFE8A33D)
        )
        "ssl" -> ErrorData(
            icon = "🔒",
            title = "اتصال امن برقرار نشد",
            text = "مشکلی در امنیت اتصال وجود دارد. لطفاً دوباره تلاش کنید.",
            color = Color(0xFFC73E3E)
        )
        "conn" -> ErrorData(
            icon = "🔌",
            title = "اتصال برقرار نشد",
            text = "لطفاً اینترنت خود را بررسی کنید و دوباره تلاش کنید.",
            color = Color(0xFFC73E3E)
        )
        else -> ErrorData(
            icon = "⚠️",
            title = "خطای نامشخص",
            text = "مشکلی پیش آمده. لطفاً دوباره تلاش کنید.",
            color = Color(0xFF7A6A60)
        )
    }
}
