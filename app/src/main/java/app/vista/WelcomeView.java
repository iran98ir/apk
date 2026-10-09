package app.vista;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WelcomeView extends FrameLayout {

    public interface OnEnterListener {
        void onEnter();
    }

    public WelcomeView(Context context, OnEnterListener listener) {
        super(context);

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setBackgroundColor(Color.parseColor(Config.WELCOME_BG_COLOR));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(32), dp(32), dp(32), dp(32));
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(root);

        if (Config.WELCOME_ICON != null && !Config.WELCOME_ICON.isEmpty()) {
            TextView icon = new TextView(context);
            icon.setText(Config.WELCOME_ICON);
            icon.setTextSize(64f);
            icon.setGravity(Gravity.CENTER);
            root.addView(icon);
        }

        if (Config.WELCOME_TITLE != null && !Config.WELCOME_TITLE.isEmpty()) {
            TextView title = new TextView(context);
            title.setText(Config.WELCOME_TITLE);
            title.setTextSize(22f);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.parseColor(Config.WELCOME_TITLE_COLOR));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            p.topMargin = dp(24);
            title.setLayoutParams(p);
            root.addView(title);
        }

        if (Config.WELCOME_SUBTITLE != null && !Config.WELCOME_SUBTITLE.isEmpty()) {
            TextView sub = new TextView(context);
            sub.setText(Config.WELCOME_SUBTITLE);
            sub.setTextSize(15f);
            sub.setGravity(Gravity.CENTER);
            sub.setTextColor(Color.parseColor(Config.WELCOME_SUBTITLE_COLOR));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            p.topMargin = dp(12);
            sub.setLayoutParams(p);
            root.addView(sub);
        }

        Button btn = new Button(context);
        btn.setText(Config.WELCOME_BUTTON_TEXT);
        btn.setTextSize(16f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextColor(Color.parseColor(Config.WELCOME_BUTTON_TEXT_COLOR));

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(Color.parseColor(Config.WELCOME_BUTTON_BG));
        bg.setCornerRadius(dp(12));
        btn.setBackground(bg);

        btn.setPadding(dp(48), dp(14), dp(48), dp(14));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.topMargin = dp(32);
        btn.setLayoutParams(bp);
        btn.setOnClickListener(v -> listener.onEnter());
        root.addView(btn);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
      }
