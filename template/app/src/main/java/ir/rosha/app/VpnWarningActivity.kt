/* =========================================================
   VpnWarningActivity.kt — هشدار فیلترشکن
   مسیر: template/app/src/main/java/ir/rosha/app/VpnWarningActivity.kt
   =========================================================
   📌 فقط از config.json می‌خونه
   📌 رنگ نوار بالا/پایین از colors.color_background
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

        // ===== رنگ نوار بالا و پایین =====
        val bgBarColor = AppConfig.color("colors", "color_background")
        window.statusBarColor = bgBarColor
        window.navigationBarColor = bgBarColor

        // ===== از config.json =====
        val enabled = AppConfig.bool("vpn", "enabled")

        if (!enabled) {
            startActivity(Intent(this, WebViewActivity::class.java))
            finish()
            return
        }

        val vpnStatus = VpnDetector.detect(this)

        val topTitle        = AppConfig.str("vpn", "top_title")
        val topSubtitle     = AppConfig.str("vpn", "top_subtitle")
        val footnote        = AppConfig.str("vpn", "footnote")
        val showRecheck     = AppConfig.bool("vpn", "show_recheck")
        val recheckText     = AppConfig.str("vpn", "recheck_text")
        val recheckBg       = AppConfig.str("vpn", "recheck_bg")
        val recheckBorder   = AppConfig.color("vpn", "recheck_border")
        val recheckColor    = AppConfig.color("vpn", "recheck_color")

        // ===== state_on =====
        val onIcon  = AppConfig.str("vpn.state_on", "icon")
        val onTitle = AppConfig.str("vpn.state_on", "title")
        val onText  = AppConfig.str("vpn.state_on", "text")
        val onColor = AppConfig.color("vpn.state_on", "color")
        val onBg    = AppConfig.color("vpn.state_on", "bg")
        val onBtn   = AppConfig.str("vpn.state_on", "btn")

        // ===== state_off =====
        val offIcon  = AppConfig.str("vpn.state_off", "icon")
        val offTitle = AppConfig.str("vpn.state_off", "title")
        val offText  = AppConfig.str("vpn.state_off", "text")
        val offColor = AppConfig.color("vpn.state_off", "color")
        val offBg    = AppConfig.color("vpn.state_off", "bg")
        val offBtn   = AppConfig.str("vpn.state_off", "btn")

        // ===== state_unknown =====
        val unkIcon  = AppConfig.str("vpn.state_unknown", "icon")
        val unkTitle = AppConfig.str("vpn.state_unknown", "title")
        val unkText  = AppConfig.str("vpn.state_unknown", "text")
        val unkColor = AppConfig.color("vpn.state_unknown", "color")
        val unkBg    = AppConfig.color("vpn.state_unknown", "bg")
        val unkBtn   = AppConfig.str("vpn.state_unknown", "btn")

        val data = when (vpnStatus) {
            VpnState.ON      -> VpnScreenData(onIcon, onTitle, onText, onColor, onBg, onBtn)
            VpnState.OFF     -> VpnScreenData(offIcon, offTitle, offText, offColor, offBg, offBtn)
            VpnState.UNKNOWN -> VpnScreenData(unkIcon, unkTitle, unkText, unkColor, unkBg, unkBtn)
        }

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(data.bg)
            ) {
                VpnWarningScreen(
                    data          = data,
                    topTitle      = topTitle,
                    topSubtitle   = topSubtitle,
                    footnote      = footnote,
                    showRecheck   = showRecheck,
                    recheckText   = recheckText,
                    recheckBg     = recheckBg,
                    recheckBorder = recheckBorder,
                    recheckColor  = recheckColor,
                    onRecheck     = { recreate() },
                    onEnter       = {
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
    recheckBg: String,
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

            Text(
                text = data.title,
                color = Color(data.color),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

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
