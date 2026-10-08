/* =========================================================
   OnboardingActivity.kt — ۳ صفحه اول
   مسیر: template/app/src/main/java/ir/rosha/app/OnboardingActivity.kt
   =========================================================
   📌 همه چیز از config.json خونده می‌شه
   ========================================================= */

package ir.rosha.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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

        // ===== از config =====
        val enabled = AppConfig.bool("onboarding", "enabled", true)

        // اگه خاموش بود → مستقیم برو WebView
        if (!enabled) {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_done", true)
                .apply()
            startActivity(Intent(this, WebViewActivity::class.java))
            finish()
            return
        }

        val skipText = AppConfig.str("onboarding", "skip_text", "رد کردن")
        val nextText = AppConfig.str("onboarding", "next_text", "بعدی")
        val prevText = AppConfig.str("onboarding", "prev_text", "قبلی")
        val startText = AppConfig.str("onboarding", "start_text", "شروع کن")
        val btnBg = AppConfig.color("onboarding", "btn_bg", "#E8A33D")
        val btnTextColor = AppConfig.color("onboarding", "btn_text_color", "#FFFFFF")
        val dotActive = AppConfig.color("onboarding", "dot_active", "#E8A33D")
        val dotInactive = AppConfig.color("onboarding", "dot_inactive", "#4A3A30")

        // ===== اسلایدها =====
        val slides = listOf(
            buildSlide(1),
            buildSlide(2),
            buildSlide(3)
        )

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0D1B2E)
            ) {
                OnboardingScreen(
                    slides = slides,
                    skipText = skipText,
                    nextText = nextText,
                    prevText = prevText,
                    startText = startText,
                    btnBg = btnBg,
                    btnTextColor = btnTextColor,
                    dotActive = dotActive,
                    dotInactive = dotInactive,
                    onFinish = {
                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("onboarding_done", true)
                            .apply()

                        startActivity(Intent(this, VpnWarningActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    private fun buildSlide(num: Int): SlideData {
        val prefix = "s${num}_"
        return SlideData(
            enabled = AppConfig.bool("onboarding", "${prefix}enabled", true),
            icon = if (AppConfig.has("onboarding", "${prefix}title")) "✨" else "✨",
            title = AppConfig.str("onboarding", "${prefix}title", ""),
            text = AppConfig.str("onboarding", "${prefix}text", ""),
            titleColor = AppConfig.color("onboarding", "${prefix}title_color", "#FFFFFF"),
            textColor = AppConfig.color("onboarding", "${prefix}text_color", "#B8B0A0"),
            bgType = AppConfig.str("onboarding", "${prefix}bg_type", "solid"),
            bg1 = AppConfig.color("onboarding", "${prefix}bg_1", "#0D1B2E"),
            bg2 = AppConfig.color("onboarding", "${prefix}bg_2", "#1B2A4A")
        )
    }
}

/* =========================================================
   داده‌ی اسلاید
   ========================================================= */
data class SlideData(
    val enabled: Boolean,
    val icon: String,
    val title: String,
    val text: String,
    val titleColor: Int,
    val textColor: Int,
    val bgType: String,
    val bg1: Int,
    val bg2: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OnboardingScreen(
    slides: List<SlideData>,
    skipText: String,
    nextText: String,
    prevText: String,
    startText: String,
    btnBg: Int,
    btnTextColor: Int,
    dotActive: Int,
    dotInactive: Int,
    onFinish: () -> Unit
) {

    val activeSlides = slides.filter { it.enabled }.ifEmpty { slides }

    val pagerState = rememberPagerState(pageCount = { activeSlides.size })
    val scope = rememberCoroutineScope()

    val currentSlide = activeSlides.getOrNull(pagerState.currentPage)
    val bg1 = currentSlide?.bg1 ?: 0xFF0D1B2E.toInt()
    val bg2 = currentSlide?.bg2 ?: 0xFF1B2A4A.toInt()

    val bgGradient = if (currentSlide?.bgType == "gradient") {
        Brush.verticalGradient(listOf(Color(bg1), Color(bg2)))
    } else {
        Brush.verticalGradient(listOf(Color(bg1), Color(bg1)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ===== دکمه رد کردن =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (pagerState.currentPage < activeSlides.size - 1) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = skipText,
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
                SlidePage(activeSlides[page])
            }

            // ===== نقطه‌ها =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeSlides.indices.forEach { index ->
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
                                if (isActive) Color(dotActive) else Color(dotInactive)
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
                            text = prevText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < activeSlides.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(btnBg),
                        contentColor = Color(btnTextColor)
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage < activeSlides.size - 1) nextText else startText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

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
        if (slide.title.isNotEmpty()) {
            Text(
                text = slide.title,
                color = Color(slide.titleColor),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ===== متن =====
        if (slide.text.isNotEmpty()) {
            Text(
                text = slide.text,
                color = Color(slide.textColor),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
        }
    }
}
