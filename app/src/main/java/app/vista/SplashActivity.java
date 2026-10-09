/* =========================================================
   SplashActivity.java — صفحه‌ی splash
   مسیر: app/src/main/java/app/vista/SplashActivity.java
   ========================================================= */

package app.vista;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long MIN_SPLASH_TIME_MS = 1500L;
    private static final long MAX_SPLASH_TIME_MS = 8000L;

    private boolean navigated = false;
    private long startTime;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();

        buildUI();
        startPreload();
    }

    private void buildUI() {
        ConfigLoader cfg = ConfigLoader.get(this);

        // ===== پس‌زمینه (گرادیانت) =====
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // گرادیانت از رنگ‌های splash
        int colorTop = getResources().getColor(R.color.splash_bg_1, getTheme());
        int colorBottom = getResources().getColor(R.color.splash_bg_2, getTheme());
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{colorTop, colorBottom});
        root.setBackground(bg);

        // ===== لوگو =====
        ImageView logo = new ImageView(this);
        int logoSize = dp(140);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
        logo.setLayoutParams(logoParams);
        logo.setImageResource(R.drawable.splash_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(logo);

        // انیمیشن ورود لوگو (scale + alpha)
        logo.setAlpha(0f);
        logo.setScaleX(0.85f);
        logo.setScaleY(0.85f);
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .start();

        // ===== عنوان =====
        TextView title = new TextView(this);
        title.setText(cfg.getSplashTitle());
        title.setTextSize(30f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(getResources().getColor(R.color.splash_title_color, getTheme()));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(24);
        title.setLayoutParams(titleParams);
        title.setAlpha(0f);
        root.addView(title);

        title.animate().alpha(1f).setStartDelay(200).setDuration(600).start();

        // ===== زیرعنوان =====
        TextView subtitle = new TextView(this);
        subtitle.setText(cfg.getSplashSubtitle());
        subtitle.setTextSize(16f);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(getResources().getColor(R.color.splash_subtitle_color, getTheme()));
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(12);
        subtitle.setLayoutParams(subParams);
        subtitle.setAlpha(0f);
        root.addView(subtitle);

        subtitle.animate().alpha(1f).setStartDelay(400).setDuration(600).start();

        // ===== ProgressBar =====
        ProgressBar progress = new ProgressBar(this, null,
                android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        int progressWidth = dp(160);
        int progressHeight = dp(4);
        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(
                progressWidth, progressHeight);
        progParams.topMargin = dp(48);
        progress.setLayoutParams(progParams);
        progress.setAlpha(0f);
        root.addView(progress);

        progress.animate().alpha(1f).setStartDelay(600).setDuration(600).start();

        setContentView(root);
    }

    private void startPreload() {
        PreloadManager.preload(getApplicationContext(), new PreloadManager.PreloadCallback() {
            @Override
            public void onPageLoaded() {
                scheduleNavigate(true);
            }

            @Override
            public void onPageFailed() {
                scheduleNavigate(false);
            }
        });

        handler.postDelayed(() -> {
            if (!navigated) navigateToMain(PreloadManager.isLoaded());
        }, MAX_SPLASH_TIME_MS);
    }

    private void scheduleNavigate(final boolean preloaded) {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = MIN_SPLASH_TIME_MS - elapsed;
        if (remaining < 0) remaining = 0;
        handler.postDelayed(() -> navigateToMain(preloaded), remaining);
    }

    private void navigateToMain(boolean preloaded) {
        if (navigated) return;
        navigated = true;

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra("page_preloaded", preloaded);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
