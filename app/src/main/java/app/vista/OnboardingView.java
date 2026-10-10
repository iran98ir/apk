package app.vista;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class OnboardingView extends FrameLayout {

    public interface OnFinishListener {
        void onFinish();
    }

    private final List<Page> pages = new ArrayList<>();
    private ViewPager2 pager;
    private LinearLayout dotsLayout;
    private Button nextButton;
    private TextView skipButton;
    private int current = 0;

    public OnboardingView(Context context, OnFinishListener listener) {
        super(context);

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setBackgroundColor(Color.parseColor(Config.COLOR_BACKGROUND));

        if (Config.ONB_S1_ENABLED) pages.add(new Page(1));
        if (Config.ONB_S2_ENABLED) pages.add(new Page(2));
        if (Config.ONB_S3_ENABLED) pages.add(new Page(3));

        if (pages.isEmpty()) {
            post(listener::onFinish);
            return;
        }

        pager = new ViewPager2(context);
        pager.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        pager.setAdapter(new Adapter());
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                current = position;
                buildDots();
                updateButtons();
            }
        });
        addView(pager);

        dotsLayout = new LinearLayout(context);
        dotsLayout.setOrientation(LinearLayout.HORIZONTAL);
        dotsLayout.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams dp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        dp.bottomMargin = dp(140);
        dotsLayout.setLayoutParams(dp);
        addView(dotsLayout);
        buildDots();

        nextButton = new Button(context);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        bp.gravity = Gravity.BOTTOM;
        bp.leftMargin = dp(24);
        bp.rightMargin = dp(24);
        bp.bottomMargin = dp(48);
        nextButton.setLayoutParams(bp);
        nextButton.setText(Config.ONB_NEXT_TEXT);
        nextButton.setTextSize(16f);
        nextButton.setTypeface(Typeface.DEFAULT_BOLD);
        nextButton.setTextColor(Color.parseColor(Config.ONB_BTN_TEXT_COLOR));
        nextButton.setBackground(roundBg(Color.parseColor(Config.ONB_BTN_BG), dp(12)));
        nextButton.setOnClickListener(v -> {
            if (current < pages.size() - 1) {
                pager.setCurrentItem(current + 1, true);
            } else {
                listener.onFinish();
            }
        });
        addView(nextButton);

        skipButton = new TextView(context);
        FrameLayout.LayoutParams skp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        skp.gravity = Gravity.TOP | Gravity.END;
        skp.topMargin = dp(24);
        skp.rightMargin = dp(24);
        skipButton.setLayoutParams(skp);
        skipButton.setText(Config.ONB_SKIP_TEXT);
        skipButton.setTextSize(14f);
        skipButton.setTextColor(Color.parseColor(Config.COLOR_TEXT));
        skipButton.setPadding(dp(12), dp(8), dp(12), dp(8));
        skipButton.setOnClickListener(v -> listener.onFinish());
        addView(skipButton);

        updateButtons();
    }

    private void buildDots() {
        dotsLayout.removeAllViews();
        for (int i = 0; i < pages.size(); i++) {
            View dot = new View(getContext());
            int width = dp(i == current ? 18 : 8);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, dp(8));
            p.setMargins(dp(4), 0, dp(4), 0);
            dot.setLayoutParams(p);
            int color = i == current
                    ? Color.parseColor(Config.ONB_DOT_ACTIVE)
                    : Color.parseColor(Config.ONB_DOT_INACTIVE);
            dot.setBackground(roundBg(color, dp(4)));
            dotsLayout.addView(dot);
        }
    }

    private void updateButtons() {
        if (current == pages.size() - 1) {
            nextButton.setText(Config.ONB_START_TEXT);
            skipButton.setVisibility(GONE);
        } else {
            nextButton.setText(Config.ONB_NEXT_TEXT);
            skipButton.setVisibility(VISIBLE);
        }
    }

    private GradientDrawable roundBg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private static class Page {
        String title, text, titleColor, textColor, bgType, bgSolid, bg1, bg2;
        Page(int n) {
            if (n == 1) {
                title = Config.ONB_S1_TITLE;
                text = Config.ONB_S1_TEXT;
                titleColor = Config.ONB_S1_TITLE_COLOR;
                textColor = Config.ONB_S1_TEXT_COLOR;
                bgType = Config.ONB_S1_BG_TYPE;
                bgSolid = Config.ONB_S1_BG_SOLID;
                bg1 = Config.ONB_S1_BG_1;
                bg2 = Config.ONB_S1_BG_2;
            } else if (n == 2) {
                title = Config.ONB_S2_TITLE;
                text = Config.ONB_S2_TEXT;
                titleColor = Config.ONB_S2_TITLE_COLOR;
                textColor = Config.ONB_S2_TEXT_COLOR;
                bgType = Config.ONB_S2_BG_TYPE;
                bgSolid = Config.ONB_S2_BG_SOLID;
                bg1 = Config.ONB_S2_BG_1;
                bg2 = Config.ONB_S2_BG_2;
            } else {
                title = Config.ONB_S3_TITLE;
                text = Config.ONB_S3_TEXT;
                titleColor = Config.ONB_S3_TITLE_COLOR;
                textColor = Config.ONB_S3_TEXT_COLOR;
                bgType = Config.ONB_S3_BG_TYPE;
                bgSolid = Config.ONB_S3_BG_SOLID;
                bg1 = Config.ONB_S3_BG_1;
                bg2 = Config.ONB_S3_BG_2;
            }
        }
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(dp(32), dp(32), dp(32), dp(32));
            layout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new VH(layout);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Page p = pages.get(position);
            holder.layout.removeAllViews();

            if ("gradient".equals(p.bgType)) {
                GradientDrawable g = new GradientDrawable(
                        GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{Color.parseColor(p.bg1), Color.parseColor(p.bg2)});
                holder.layout.setBackground(g);
            } else {
                holder.layout.setBackgroundColor(Color.parseColor(p.bgSolid));
            }

            TextView title = new TextView(getContext());
            title.setText(p.title);
            title.setTextSize(26f);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.parseColor(p.titleColor));
            holder.layout.addView(title);

            TextView text = new TextView(getContext());
            text.setText(p.text);
            text.setTextSize(16f);
            text.setGravity(Gravity.CENTER);
            text.setTextColor(Color.parseColor(p.textColor));
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tp.topMargin = dp(16);
            text.setLayoutParams(tp);
            holder.layout.addView(text);
        }

        @Override
        public int getItemCount() {
            return pages.size();
        }

        class VH extends RecyclerView.ViewHolder {
            LinearLayout layout;
            VH(LinearLayout l) {
                super(l);
                layout = l;
            }
        }
    }
                         }
