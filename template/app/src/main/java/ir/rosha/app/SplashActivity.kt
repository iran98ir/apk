/* =========================================================
   SplashActivity.kt — صفحه اسپلش (نسخه تست)
   ========================================================= */

package ir.rosha.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

        // ===== تست: لاگ به Toast =====
        android.widget.Toast.makeText(this, "🟢 SPLASH اجرا شد", android.widget.Toast.LENGTH_LONG).show()

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean("onboarding_done", false)
        val enabled = AppConfig.bool("splash", "enabled", true)

        // ===== اگه splash خاموش بود =====
        if (!enabled) {
            android.widget.Toast.makeText(this, "⚠️ splash خاموشه", android.widget.Toast.LENGTH_LONG).show()
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
                color = Color(0xFF0D1B2E)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0D1B2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🟢 SPLASH",
                            color = Color.Green,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "این صفحه ۴ ثانیه باز می‌مونه\nبعدش می‌ره Onboarding",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ===== بعد از ۴ ثانیه برو بعدی =====
                LaunchedEffect(Unit) {
                    delay(4000)
                    val nextIntent = if (onboardingDone) {
                        Intent(this@SplashActivity, WebViewActivity::class.java)
                    } else {
                        Intent(this@SplashActivity, OnboardingActivity::class.java)
                    }
                    startActivity(nextIntent)
                    finish()
                }
            }
        }
    }
}
