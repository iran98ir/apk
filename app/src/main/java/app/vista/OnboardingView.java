package app.vista;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class OnboardingView extends FrameLayout {

    public interface OnFinishListener {
        void onFinish();
    }

    private final List<PageData> pages = new ArrayList<>();
    private final ViewPager2 pager;
    private final LinearLayout dotsLayout;
    private final Button nextButton;
    private final TextView skipButton;
    private int currentPage = 0;

    public OnboardingView(Context context, OnFinishListener listener) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setBackgroundColor(getColor(R.color.color_background));

        loadPages();

        pager = new ViewPager2(context);
        FrameLayout.LayoutParams pagerParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pager.setLayoutParams(pagerParams);
        pager.setAdapter(new PageAdapter());
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updateDots();
                updateButtons();
            }
        });
        addView(pager);

        dotsLayout = new LinearLayout(context);
        dotsLayout.setOrientation(LinearLayout.HORIZONTAL);
        dotsLayout.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams dotsParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dotsParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        dotsParams.bottomMargin = dp(140);
        dotsLayout.setLayoutParams(dotsParams);
        addView(dotsLayout);
        buildDots();

        nextButton = new Button(context);
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        btnParams.gravity = Gravity.BOTTOM;
        btnParams.leftMargin = dp(24);
        btnParams.rightMargin = dp(24);
        btnParams.bottomMargin = dp(48);
        nextButton.setLayoutParams(btnParams);
        nextButton.setText(getString(R.string.onb_next));
        nextButton.setTextSize(16f);
        nextButton.setTypeface(Typeface.DEFAULT_BOLD);
        nextButton.setTextColor(getColor(R.color.color_button_text));
        nextButton.setBackground(roundedBg(getColor(R.color.color_button), dp(12)));
        nextButton.setOnClickListener(v -> {
            if (currentPage < pages.size() - 1) {
                pager.setCurrentItem(currentPage + 1, true);
            } else {
                listener.onFinish();
            }
        });
        addView(nextButton);

        skipButton = new TextView(context);
        FrameLayout.LayoutParams skipParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        skipParams.gravity = Gravity.TOP | Gravity.END;
        skipParams.topMargin = dp(24);
        skipParams.rightMargin = dp(24);
        skipButton.setLayoutParams(skipParams);
        skipButton.setText(getString(R.string.onb_skip));
        skipButton.setTextSize(14f);
        skipButton.setTextColor(getColor(R.color.color_text));
        skipButton.setPadding(dp(12), dp(8), dp(12), dp(8));
        skipButton.setOnClickListener(v -> listener.onFinish());
        addView(skipButton);
    }

    private void loadPages() {
        pages.add(new PageData(
                getString(R.string.onb_s1_title),
                getString(R.string.onb_s1_text),
                getColor(R.color.color_background),
                getColor(R.color.color_text),
                getColor(R.color.color_text_secondary)));
        pages.add(new PageData(
                getString(R.string.onb_s2_title),
                getString(R.string.onb_s2_text),
                getColor(R.color.color_background),
                getColor(R.color.color_text),
                getColor(R.color.color_text_secondary)));
        pages.add(new PageData(
                getString(R.string.onb_s3_title),
                getString(R.string.onb_s3_text),
                getColor(R.color.color_background),
                getColor(R.color.color_text),
                getColor(R.color.color_text_secondary)));
    }

    private void buildDots() {
        dotsLayout.removeAllViews();
        for (int i = 0; i < pages.size(); i++) {
            View dot = new View(getContext());
            int size = dp(8);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(size, size);
            p.setMargins(dp(4), 0, dp(4), 0);
            dot.setLayoutParams(p);
            dot.setBackground(roundedBg(
                    i == currentPage ? getColor(R.color.color_accent) : getColor(R.color.color_text_secondary),
                    dp(4)));
            dotsLayout.addView(dot);
        }
    }

    private void updateDots() {
        buildDots();
    }

    private void updateButtons() {
        if (currentPage == pages.size() - 1) {
            nextButton.setText(getString(R.string.onb_start));
            skipButton.setVisibility(View.GONE);
        } else {
            nextButton.setText(getString(R.string.onb_next));
            skipButton.setVisibility(View.VISIBLE);
        }
    }

    private GradientDrawable roundedBg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private int getColor(int res) {
        return getContext().getResources().getColor(res, getContext().getTheme());
    }

    private String getString(int res) {
        return getContext().getString(res);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private static class PageData {
        String title;
        String text;
        int bg;
        int titleColor;
        int textColor;

        PageData(String title, String text, int bg, int titleColor, int textColor) {
            this.title = title;
            this.text = text;
            this.bg = bg;
            this.titleColor = titleColor;
            this.textColor = textColor;
        }
    }

    private class PageAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<PageAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
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
        public void onBindViewHolder(VH holder, int position) {
            PageData p = pages.get(position);
            holder.layout.removeAllViews();
            holder.layout.setBackgroundColor(p.bg);

            TextView title = new TextView(getContext());
            title.setText(p.title);
            title.setTextSize(26f);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(p.titleColor);
            holder.layout.addView(title);

            TextView text = new TextView(getContext());
            text.setText(p.text);
            text.setTextSize(16f);
            text.setGravity(Gravity.CENTER);
            text.setTextColor(p.textColor);
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

        class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            LinearLayout layout;
            VH(LinearLayout l) {
                super(l);
                layout = l;
            }
        }
    }
                                  }
