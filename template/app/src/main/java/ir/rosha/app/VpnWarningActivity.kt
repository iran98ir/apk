/* =========================================================
   VpnWarningActivity.kt — هشدار فیلترشکن
   مسیر: template/app/src/main/java/ir/rosha/app/VpnWarningActivity.kt
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
import ir.rosha.app.utils.VpnDetector

class VpnWarningActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== از config =====
        val enabled = AppConfig.bool("vpn", "enabled", false)

        // اگه خاموش بود → مستقیم WebView
        if (!enabled) {
            startActivity(Intent(this, WebViewActivity::class.java))
            finish()
            return
        }

        val vpnStatus = VpnDetector.detect(this)

        val topTitle = AppConfig.str("vpn", "top_title", "توجه مهم")
        val topSubtitle = AppConfig.str("vpn", "top_subtitle", "")
        val footnote = AppConfig.str("vpn", "footnote", "")
        val showRecheck = AppConfig.bool("vpn", "show_recheck", true)

        // ===== recheck =====
        val recheckText = AppConfig.str("vpn", "recheck_text", "🔄 بررسی مجدد")
        val recheckBg = AppConfig.str("vpn", "recheck_bg", "transparent")
        val recheckBorder = AppConfig.color("vpn", "recheck_border", "#B8B0A0")
        val recheckColor = AppConfig.color("vpn", "recheck_color", "#B8B0A0")

        // ===== state_on =====
        val onIcon = AppConfig.str("vpn", "state_on_icon", "⚠️")
        val onTitle = AppConfig.str("vpn", "state_on_title", "فیلترشکن شما روشن است")
        val onText = AppConfig.str("vpn", "state_on_text", "")
        val onColor = AppConfig.color("vpn", "state_on_color", "#C73E3E")
        val onBg = AppConfig.color("vpn", "state_on_bg", "#5B1A1D")
        val onBtn = AppConfig.str("vpn", "state_on_btn", "ورود")

        // ===== state_off =====
        val offIcon = AppConfig.str("vpn", "state_off_icon", "✅")
        val offTitle = AppConfig.str("vpn", "state_off_title", "آماده‌ی شروع هستی")
        val offText = AppConfig.str("vpn", "state_off_text", "")
        val offColor = AppConfig.color("vpn", "state_off_color", "#2D7A5F")
        val offBg = AppConfig.color("vpn", "state_off_bg", "#0D1B2E")
        val offBtn = AppConfig.str("vpn", "state_off_btn", "ورود")

        // ===== state_unknown =====
        val unkIcon = AppConfig.str("vpn", "state_unknown_icon", "🔒")
        val unkTitle = AppConfig.str("vpn", "state_unknown_title", "برای شروع آماده‌ای")
        val unkText = AppConfig.str("vpn", "state_unknown_text", "")
        val unkColor = AppConfig.color("vpn", "state_unknown_color", "#E8A33D")
        val unkBg = AppConfig.color("vpn", "state_unknown_bg", "#0D1B2E")
        val unkBtn = AppConfig.str("vpn", "state_unknown_btn", "ورود")

        val data = when (vpnStatus) {
            VpnState.ON -> VpnScreenData(onIcon, onTitle, onText, onColor, onBg, onBtn)
            VpnState.OFF -> VpnScreenData(offIcon, offTitle, offText, offColor, offBg, offBtn)
            VpnState.UNKNOWN -> VpnScreenData(unkIcon, unkTitle, unkText, unkColor, unkBg, unkBtn)
        }

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(data.bg)
            ) {
                VpnWarningScreen(
                    data = data,
                    topTitle = topTitle,
                    topSubtitle = topSubtitle,
                    footnote = footnote,
                    showRecheck = showRecheck,
                    recheckText = recheckText,
                    recheckBorder = recheckBorder,
                    recheckColor = recheckColor,
                    onRecheck = { recreate() },
                    onEnter = {
                        startActivity(Intent(this, WebViewActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

enum class VpnState {
    ON, OFF, UNKNOWN
}

private data class VpnScreenData(
    val icon: String,
    val title: String,
    val text: String,
    val color: Int,
    val bg: Int,
    val btn: String
)

@Composable
private fun VpnWarningScreen(
    data: VpnScreenData,
    topTitle: String,
    topSubtitle: String,
    footnote: String,
    showRecheck: Boolean,
    recheckText: String,
    recheckBorder: Int,
    recheckColor: Int,
    onRecheck: () -> Unit,
    onEnter: () -> Unit
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

            // ===== عنوان بالا =====
            if (topTitle.isNotEmpty()) {
                Text(
                    text = topTitle,
                    color = Color(data.color),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (topSubtitle.isNotEmpty()) {
                Text(
                    text = topSubtitle,
                    color = Color(0xFFB8B0A0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

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

            // ===== دکمه ورود =====
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
                    text = data.btn,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // ===== دکمه بررسی مجدد =====
            if (showRecheck) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRecheck,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(recheckColor)
                    )
                ) {
                    Text(
                        text = recheckText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ===== متن پایین =====
            if (footnote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = footnote,
                    color = Color(0xFF7A6A60),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
