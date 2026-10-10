package app.vista;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private boolean navigated = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();

        if (!Config.SPLASH_ENABLED) {
            navigateToMain();
            return;
        }

        buildUI();
        startPreload();
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        applyBackground(root);

        ImageView logo = new ImageView(this);
        int logoSize = dp(Config.SPLASH_LOGO_SIZE);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
        logo.setLayoutParams(logoParams);
        logo.setImageResource(R.drawable.splash_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setAlpha(0f);
        root.addView(logo);

        applyAnim(logo, Config.SPLASH_LOGO_ANIM, 0);

        if (Config.SPLASH_TITLE != null && !Config.SPLASH_TITLE.isEmpty()) {
            TextView title = new TextView(this);
            title.setText(Config.SPLASH_TITLE);
            title.setTextSize(Config.SPLASH_TITLE_SIZE);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.parseColor(Config.SPLASH_TITLE_COLOR));
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tp.topMargin = dp(24);
            title.setLayoutParams(tp);
            title.setAlpha(0f);
            root.addView(title);
            applyAnim(title, Config.SPLASH_TITLE_ANIM, 200);
        }

        if (Config.SPLASH_SUBTITLE != null && !Config.SPLASH_SUBTITLE.isEmpty()) {
            TextView subtitle = new TextView(this);
            subtitle.setText(Config.SPLASH_SUBTITLE);
            subtitle.setTextSize(Config.SPLASH_SUBTITLE_SIZE);
            subtitle.setGravity(Gravity.CENTER);
            subtitle.setTextColor(Color.parseColor(Config.SPLASH_SUBTITLE_COLOR));
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            sp.topMargin = dp(12);
            subtitle.setLayoutParams(sp);
            subtitle.setAlpha(0f);
            root.addView(subtitle);
            applyAnim(subtitle, Config.SPLASH_SUBTITLE_ANIM, 400);
        }

        if (Config.SPLASH_SHOW_LOADER) {
            View loader = buildLoader();
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(48);
            loader.setLayoutParams(lp);
            loader.setAlpha(0f);
            root.addView(loader);
            applyAnim(loader, "fadeIn", 600);
        }

        setContentView(root);
    }

    private void applyBackground(LinearLayout root) {
        String type = Config.SPLASH_BG_TYPE;
        if ("gradient".equals(type)) {
            GradientDrawable g = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{
                            Color.parseColor(Config.SPLASH_BG_COLOR_1),
                            Color.parseColor(Config.SPLASH_BG_COLOR_2)
                    });
            root.setBackground(g);
        } else {
            root.setBackgroundColor(Color.parseColor(Config.SPLASH_BG_COLOR_SOLID));
        }
    }

    private View buildLoader() {
        String type = Config.SPLASH_LOADER_TYPE;
        int color = Color.parseColor(Config.SPLASH_LOADER_COLOR);

        if ("bar".equals(type)) {
            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setIndeterminate(true);
            pb.setLayoutParams(new LinearLayout.LayoutParams(dp(160), dp(4)));
            try {
                pb.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(color));
            } catch (Exception ignored) {}
            return pb;
        } else if ("spinner".equals(type)) {
            ProgressBar pb = new ProgressBar(this);
            pb.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
            try {
                pb.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(color));
            } catch (Exception ignored) {}
            return pb;
        } else {
            LinearLayout dots = new LinearLayout(this);
            dots.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < 3; i++) {
                View d = new View(this);
                int size = dp(10);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(size, size);
                p.setMargins(dp(5), 0, dp(5), 0);
                d.setLayoutParams(p);
                GradientDrawable g = new GradientDrawable();
                g.setShape(GradientDrawable.OVAL);
                g.setColor(color);
                d.setBackground(g);
                dots.addView(d);

                ObjectAnimator oa = ObjectAnimator.ofFloat(d, "alpha", 0.3f, 1f, 0.3f);
                oa.setDuration(1200);
                oa.setStartDelay(i * 150);
                oa.setRepeatCount(ValueAnimator.INFINITE);
                oa.start();

                ObjectAnimator sc = ObjectAnimator.ofFloat(d, "scaleX", 0.6f, 1f, 0.6f);
                sc.setDuration(1200);
                sc.setStartDelay(i * 150);
                sc.setRepeatCount(ValueAnimator.INFINITE);
                sc.start();
            }
            return dots;
        }
    }

    private void applyAnim(View v, String anim, long delay) {
        long dur = 700;
        if ("none".equals(anim) || anim == null) {
            v.setAlpha(1f);
            return;
        }
        if ("fadeIn".equals(anim)) {
            AlphaAnimation a = new AlphaAnimation(0f, 1f);
            a.setDuration(dur);
            a.setStartOffset(delay);
            a.setFillAfter(true);
            v.startAnimation(a);
        } else if ("scale".equals(anim)) {
            AnimationSet s = new AnimationSet(true);
            ScaleAnimation sc = new ScaleAnimation(0.7f, 1f, 0.7f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            sc.setDuration(dur);
            AlphaAnimation a = new AlphaAnimation(0f, 1f);
            a.setDuration(dur);
            s.addAnimation(sc);
            s.addAnimation(a);
            s.setStartOffset(delay);
            s.setFillAfter(true);
            v.startAnimation(s);
        } else if ("pulse".equals(anim)) {
            AlphaAnimation a = new AlphaAnimation(0f, 1f);
            a.setDuration(dur);
            a.setStartOffset(delay);
            a.setFillAfter(true);
            v.startAnimation(a);

            ObjectAnimator scale = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.08f, 1f);
            scale.setDuration(1800);
            scale.setStartDelay(delay + dur);
            scale.setRepeatCount(ValueAnimator.INFINITE);
            scale.start();

            ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.08f, 1f);
            scaleY.setDuration(1800);
            scaleY.setStartDelay(delay + dur);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.start();
        } else if ("bounce".equals(anim)) {
            AlphaAnimation a = new AlphaAnimation(0f, 1f);
            a.setDuration(dur);
            a.setStartOffset(delay);
            a.setFillAfter(true);
            v.startAnimation(a);

            ObjectAnimator t = ObjectAnimator.ofFloat(v, "translationY", 0f, -dp(10), 0f);
            t.setDuration(800);
            t.setStartDelay(delay + dur);
            t.setRepeatCount(ValueAnimator.INFINITE);
            t.start();
        } else if ("rotate".equals(anim)) {
            AlphaAnimation a = new AlphaAnimation(0f, 1f);
            a.setDuration(dur);
            a.setStartOffset(delay);
            a.setFillAfter(true);
            v.startAnimation(a);

            ObjectAnimator r = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
            r.setDuration(2000);
            r.setStartDelay(delay + dur);
            r.setRepeatCount(ValueAnimator.INFINITE);
            r.start();
        } else if ("float".equals(anim)) {
            AlphaAnimation a = new AlphaAnimation(0f, 1f);
            a.setDuration(dur);
            a.setStartOffset(delay);
            a.setFillAfter(true);
            v.startAnimation(a);

            ObjectAnimator t = ObjectAnimator.ofFloat(v, "translationY", 0f, -dp(8), 0f);
            t.setDuration(1500);
            t.setStartDelay(delay + dur);
            t.setRepeatCount(ValueAnimator.INFINITE);
            t.start();
        } else if ("fadeUp".equals(anim)) {
            AnimationSet s = new AnimationSet(true);
            TranslateAnimation t = new TranslateAnimation(0, 0, dp(20), 0);
            t.setDuration(dur);
            AlphaAnimation a = new AlphaAnimation(0f, 1f);
            a.setDuration(dur);
            s.addAnimation(t);
            s.addAnimation(a);
            s.setStartOffset(delay);
            s.setFillAfter(true);
            v.startAnimation(s);
        } else if ("slideRight".equals(anim)) {
            AnimationSet s = new AnimationSet(true);
            TranslateAnimation t = new TranslateAnimation(-dp(40), 0, 0, 0);
            t.setDuration(dur);
            AlphaAnimation a = new AlphaAnimation(0f, 1f);
            a.setDuration(dur);
            s.addAnimation(t);
            s.addAnimation(a);
            s.setStartOffset(delay);
            s.setFillAfter(true);
            v.startAnimation(s);
        } else {
            v.setAlpha(1f);
        }
    }

    private void startPreload() {
        PreloadManager.preload(getApplicationContext(), new PreloadManager.PreloadCallback() {
            @Override
            public void onPageLoaded() {
                scheduleNavigate();
            }

            @Override
            public void onPageFailed() {
                scheduleNavigate();
            }
        });

        handler.postDelayed(this::navigateToMain, Math.max(Config.SPLASH_DURATION, 8000));
    }

    private void scheduleNavigate() {
        long elapsed = System.currentTimeMillis() - startTime;
        long wait = Math.max(Config.SPLASH_DURATION - elapsed, 0);
        handler.postDelayed(this::navigateToMain, wait);
    }

    private void navigateToMain() {
        if (navigated) return;
        navigated = true;

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra("page_preloaded", PreloadManager.isLoaded());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
                }
