/* =========================================================
   OnboardingActivity.kt  —  ۳ صفحه‌ی اول اپ
   مسیر: template/app/src/main/java/ir/rosha/app/OnboardingActivity.kt
   =========================================================
   📌 ۳ صفحه با اسلایدر
   📌 دکمه‌ی بعدی/قبلی/رد کردن
   📌 فقط بار اول نشون داده میشه
   ========================================================= */

package ir.rosha.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0D1B2E)
            ) {
                OnboardingScreen(
                    onFinish = {
                        // ===== ذخیره‌ی اینکه کاربر دیده =====
                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("onboarding_done", true)
                            .apply()

                        // ===== برو به VPN Warning =====
                        startActivity(Intent(this, VpnWarningActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingScreen(onFinish: () -> Unit) {

    // ===== ۳ اسلاید =====
    val slides = listOf(
        SlideData(
            icon = "🎯",
            title = "به اپ ما خوش آمدی",
            text = "همراه تو در هر مرحله"
        ),
        SlideData(
            icon = "⚡",
            title = "سریع و امن",
            text = "با چند کلیک هرچی میخوای"
        ),
        SlideData(
            icon = "🚀",
            title = "آماده‌ای؟",
            text = "بزن بریم که شروع کنیم"
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

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
            modifier = Modifier.fillMaxSize()
        ) {

            // ===== دکمه‌ی رد کردن =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (pagerState.currentPage < slides.size - 1) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = "رد کردن",
                            color = Color(0xFFB8B0A0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ===== اسلایدر =====
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                SlidePage(slides[page])
            }

            // ===== نقطه‌ها =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.indices.forEach { index ->
                    val isActive = pagerState.currentPage == index

                    val width by animateFloatAsState(
                        targetValue = if (isActive) 24f else 8f,
                        label = "dot_width"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = width.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color(0xFFE8A33D) else Color(0xFF4A3A30)
                            )
                    )
                }
            }

            // ===== دکمه‌ها =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ===== دکمه‌ی قبلی =====
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFB8B0A0)
                        )
                    ) {
                        Text(
                            text = "قبلی",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ===== دکمه‌ی بعدی/شروع =====
                Button(
                    onClick = {
                        if (pagerState.currentPage < slides.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier
                        .weight(if (pagerState.currentPage > 0) 1f else 1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8A33D),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage < slides.size - 1) "بعدی" else "شروع کن",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

/* =========================================================
   یه اسلاید
   ========================================================= */
@Composable
private fun SlidePage(slide: SlideData) {

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
                .background(Color(0x1AE8A33D)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = slide.icon,
                fontSize = 64.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ===== عنوان =====
        Text(
            text = slide.title,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ===== متن =====
        Text(
            text = slide.text,
            color = Color(0xFFB8B0A0),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}

/* =========================================================
   داده‌ی اسلاید
   ========================================================= */
data class SlideData(
    val icon: String,
    val title: String,
    val text: String
)
