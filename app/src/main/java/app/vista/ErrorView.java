package app.vista;

import android.content.Context;
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
        setBackgroundColor(getColor(R.color.color_background));

        ErrorData data = getErrorData(type);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(32), dp(32), dp(32), dp(32));
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(layout);

        TextView icon = new TextView(context);
        icon.setText(data.icon);
        icon.setTextSize(64f);
        icon.setGravity(Gravity.CENTER);
        layout.addView(icon);

        TextView title = new TextView(context);
        title.setText(data.title);
        title.setTextSize(20f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(getColor(R.color.color_text));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin = dp(24);
        title.setLayoutParams(tp);
        layout.addView(title);

        TextView text = new TextView(context);
        text.setText(data.text);
        text.setTextSize(15f);
        text.setGravity(Gravity.CENTER);
        text.setTextColor(getColor(R.color.color_text_secondary));
        LinearLayout.LayoutParams txtP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        txtP.topMargin = dp(12);
        text.setLayoutParams(txtP);
        layout.addView(text);

        Button retry = new Button(context);
        retry.setText(getString(R.string.error_retry));
        retry.setTextSize(15f);
        retry.setTypeface(Typeface.DEFAULT_BOLD);
        retry.setTextColor(getColor(R.color.color_button_text));
        retry.setBackground(roundedBg(getColor(R.color.color_button), dp(12)));
        retry.setPadding(dp(48), dp(14), dp(48), dp(14));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.topMargin = dp(32);
        retry.setLayoutParams(bp);
        retry.setOnClickListener(v -> listener.onRetry());
        layout.addView(retry);
    }

    private ErrorData getErrorData(String type) {
        switch (type) {
            case "offline":
                return new ErrorData("📡",
                        getString(R.string.error_offline_title),
                        getString(R.string.error_offline_text));
            case "server":
                return new ErrorData("🛠",
                        getString(R.string.error_server_title),
                        getString(R.string.error_server_text));
            case "nf":
                return new ErrorData("🔍",
                        getString(R.string.error_nf_title),
                        getString(R.string.error_nf_text));
            case "fb":
                return new ErrorData("🚫",
                        getString(R.string.error_fb_title),
                        getString(R.string.error_fb_text));
            case "to":
                return new ErrorData("⏱",
                        getString(R.string.error_to_title),
                        getString(R.string.error_to_text));
            case "dns":
                return new ErrorData("🌍",
                        getString(R.string.error_dns_title),
                        getString(R.string.error_dns_text));
            case "ssl":
                return new ErrorData("🔒",
                        getString(R.string.error_ssl_title),
                        getString(R.string.error_ssl_text));
            case "conn":
                return new ErrorData("🔌",
                        getString(R.string.error_conn_title),
                        getString(R.string.error_conn_text));
            default:
                return new ErrorData("⚠️",
                        getString(R.string.error_unk_title),
                        getString(R.string.error_unk_text));
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

    private static class ErrorData {
        String icon;
        String title;
        String text;

        ErrorData(String icon, String title, String text) {
            this.icon = icon;
            this.title = title;
            this.text = text;
        }
    }
                  }
