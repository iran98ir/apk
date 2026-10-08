/* =========================================================
   SplashActivity.kt — صفحه اسپلش
   مسیر: template/app/src/main/java/ir/rosha/app/SplashActivity.kt
   ========================================================= */

package ir.rosha.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🚀 SplashActivity شروع شد")

        // ===== از config.json =====
        val enabled       = AppConfig.bool("splash", "enabled", true)
        val duration      = AppConfig.int("splash", "duration", 2000)
        val title         = AppConfig.str("splash", "title", "")
        val subtitle      = AppConfig.str("splash", "subtitle", "")
        val titleColor    = AppConfig.color("splash", "title_color", "#E8A33D")
        val subtitleColor = AppConfig.color("splash", "subtitle_color", "#B8B0A0")
        val titleSize     = AppConfig.int("splash", "title_size", 28)
        val subtitleSize  = AppConfig.int("splash", "subtitle_size", 16)
        val loaderColor   = AppConfig.color("splash", "loader_color", "#E8A33D")
        val showLoader    = AppConfig.bool("splash", "show_loader", true)
        val logoSize      = AppConfig.int("splash", "logo_size", 180)
        val bgType        = AppConfig.str("splash", "bg_type", "solid")
        val bgColor1      = AppConfig.color("splash", "bg_color_1", "#0
