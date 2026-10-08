/* =========================================================
   VpnWarningActivity.kt  —  صفحه‌ی هشدار فیلترشکن
   مسیر: template/app/src/main/java/ir/rosha/app/VpnWarningActivity.kt
   =========================================================
   📌 این صفحه فقط هشداره
   📌 کاربر چه VPN روشن باشه چه نباشه میتونه رد بشه
   📌 با زدن دکمه‌ی ورود → میره به WebView
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
import ir.rosha.app.utils.VpnDetector

class VpnWarningActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== تشخیص وضعیت VPN =====
        val vpnStatus = VpnDetector.detect(this)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0D1B2E)
            ) {
                VpnWarningScreen(
                    vpnStatus = vpnStatus,
                    onEnter = {
                        // ===== کاربر وارد شد → برو به WebView =====
                        startActivity(Intent(this, WebViewActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

/* =========================================================
   وضعیت VPN
   ========================================================= */
enum class VpnState {
    ON,        // مطمئنیم روشنه
    OFF,       // مطمئنیم خاموشه
    UNKNOWN    // نمیدونیم
}

@Composable
private fun VpnWarningScreen(
    vpnStatus: VpnState,
    onEnter: () -> Unit
) {

    // ===== متن‌ها بر اساس وضعیت =====
    val (icon, title, text, color) = when (vpnStatus) {
        VpnState.ON -> VpnScreenData(
            icon = "⚠️",
            title = "فیلترشکن شما روشن است",
            text = "برای استفاده‌ی کامل از اپ، لطفاً فیلترشکن خود را خاموش کنید.",
            color = Color(0xFFC73E3E)
        )
        VpnState.OFF -> VpnScreenData(
            icon = "✅",
            title = "آماده‌ی شروع هستی",
            text = "لطفاً از خاموش بودن فیلترشکن مطمئن شو و وارد شو.",
            color = Color(0xFF2D7A5F)
        )
        VpnState.UNKNOWN -> VpnScreenData(
            icon = "🔒",
            title = "برای شروع آماده‌ای",
            text = "توصیه می‌کنیم فیلترشکن شما خاموش باشد تا اپ به‌درستی کار کند.",
            color = Color(0xFFE8A33D)
        )
    }

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
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 64.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== تیتر =====
            Text(
                text = title,
                color = color,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== متن =====
            Text(
                text = text,
                color = Color(0xFFB8B0A0),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ===== دکمه‌ی ورود =====
            Button(
                onClick = onEnter,
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
                    text = "ورود",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== متن پایین =====
            Text(
                text = "در صورت روشن بودن فیلترشکن، ممکن است اپ به‌درستی کار نکند.",
                color = Color(0xFF7A6A60),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

/* =========================================================
   داده‌ی صفحه
   ========================================================= */
private data class VpnScreenData(
    val icon: String,
    val title: String,
    val text: String,
    val color: Color
)
