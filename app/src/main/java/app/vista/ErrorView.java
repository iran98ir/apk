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

public class ErrorView extends FrameLayout {

    public interface OnRetryListener {
        void onRetry();
    }

    public ErrorView(Context context, String type, OnRetryListener listener) {
        super(context);

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        Data d = getData(type);
        setBackgroundColor(Color.parseColor(d.bg));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(32), dp(32), dp(32), dp(32));
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(root);

        TextView icon = new TextView(context);
        icon.setText(d.icon);
        icon.setTextSize(64f);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(context);
        title.setText(d.title);
        title.setTextSize(20f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.parseColor(d.color));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin = dp(24);
        title.setLayoutParams(tp);
        root.addView(title);

        TextView text = new TextView(context);
        text.setText(d.text);
        text.setTextSize(15f);
        text.setGravity(Gravity.CENTER);
        text.setTextColor(Color.parseColor(d.color));
        text.setAlpha(0.85f);
        LinearLayout.LayoutParams xp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        xp.topMargin = dp(12);
        text.setLayoutParams(xp);
        root.addView(text);

        if (Config.ERR_SHOW_RETRY) {
            Button retry = new Button(context);
            retry.setText(Config.ERR_RETRY_TEXT);
            retry.setTextSize(15f);
            retry.setTypeface(Typeface.DEFAULT_BOLD);
            retry.setTextColor(Color.parseColor(Config.ERR_RETRY_COLOR));

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setColor(Color.parseColor(Config.ERR_RETRY_BG));
            bg.setCornerRadius(dp(12));
            retry.setBackground(bg);

            retry.setPadding(dp(48), dp(14), dp(48), dp(14));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            rp.topMargin = dp(32);
            retry.setLayoutParams(rp);
            retry.setOnClickListener(v -> listener.onRetry());
            root.addView(retry);
        }
    }

    private Data getData(String type) {
        switch (type) {
            case "offline":
                return new Data(Config.ERR_OFFLINE_ICON, Config.ERR_OFFLINE_TITLE, Config.ERR_OFFLINE_TEXT, Config.ERR_OFFLINE_COLOR, Config.ERR_OFFLINE_BG);
            case "server":
                return new Data(Config.ERR_SERVER_ICON, Config.ERR_SERVER_TITLE, Config.ERR_SERVER_TEXT, Config.ERR_SERVER_COLOR, Config.ERR_SERVER_BG);
            case "nf":
                return new Data(Config.ERR_NF_ICON, Config.ERR_NF_TITLE, Config.ERR_NF_TEXT, Config.ERR_NF_COLOR, Config.ERR_NF_BG);
            case "fb":
                return new Data(Config.ERR_FB_ICON, Config.ERR_FB_TITLE, Config.ERR_FB_TEXT, Config.ERR_FB_COLOR, Config.ERR_FB_BG);
            case "to":
                return new Data(Config.ERR_TO_ICON, Config.ERR_TO_TITLE, Config.ERR_TO_TEXT, Config.ERR_TO_COLOR, Config.ERR_TO_BG);
            case "dns":
                return new Data(Config.ERR_DNS_ICON, Config.ERR_DNS_TITLE, Config.ERR_DNS_TEXT, Config.ERR_DNS_COLOR, Config.ERR_DNS_BG);
            case "ssl":
                return new Data(Config.ERR_SSL_ICON, Config.ERR_SSL_TITLE, Config.ERR_SSL_TEXT, Config.ERR_SSL_COLOR, Config.ERR_SSL_BG);
            case "conn":
                return new Data(Config.ERR_CONN_ICON, Config.ERR_CONN_TITLE, Config.ERR_CONN_TEXT, Config.ERR_CONN_COLOR, Config.ERR_CONN_BG);
            default:
                return new Data(Config.ERR_UNK_ICON, Config.ERR_UNK_TITLE, Config.ERR_UNK_TEXT, Config.ERR_UNK_COLOR, Config.ERR_UNK_BG);
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private static class Data {
        String icon, title, text, color, bg;
        Data(String i, String t, String x, String c, String b) {
            icon = i;
            title = t;
            text = x;
            color = c;
            bg = b;
        }
    }
}
