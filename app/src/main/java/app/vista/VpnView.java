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

public class VpnView extends FrameLayout {

    public interface OnProceedListener {
        void onProceed();
    }

    public VpnView(Context context, String state, OnProceedListener listener) {
        super(context);

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        String icon, title, text, color, bg, btnText;

        if ("on".equals(state)) {
            icon = Config.VPN_ON_ICON;
            title = Config.VPN_ON_TITLE;
            text = Config.VPN_ON_TEXT;
            color = Config.VPN_ON_COLOR;
            bg = Config.VPN_ON_BG;
            btnText = Config.VPN_ON_BTN;
        } else if ("off".equals(state)) {
            icon = Config.VPN_OFF_ICON;
            title = Config.VPN_OFF_TITLE;
            text = Config.VPN_OFF_TEXT;
            color = Config.VPN_OFF_COLOR;
            bg = Config.VPN_OFF_BG;
            btnText = Config.VPN_OFF_BTN;
        } else {
            icon = Config.VPN_UNK_ICON;
            title = Config.VPN_UNK_TITLE;
            text = Config.VPN_UNK_TEXT;
            color = Config.VPN_UNK_COLOR;
            bg = Config.VPN_UNK_BG;
            btnText = Config.VPN_UNK_BTN;
        }

        setBackgroundColor(Color.parseColor(bg));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(32), dp(32), dp(32), dp(32));
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(root);

        if (Config.VPN_TOP_TITLE != null && !Config.VPN_TOP_TITLE.isEmpty()) {
            TextView top = new TextView(context);
            top.setText(Config.VPN_TOP_TITLE);
            top.setTextSize(13f);
            top.setGravity(Gravity.CENTER);
            top.setTextColor(Color.parseColor(color));
            top.setAlpha(0.7f);
            root.addView(top);
        }

        TextView iconV = new TextView(context);
        iconV.setText(icon);
        iconV.setTextSize(64f);
        iconV.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        ip.topMargin = dp(24);
        iconV.setLayoutParams(ip);
        root.addView(iconV);

        TextView titleV = new TextView(context);
        titleV.setText(title);
        titleV.setTextSize(20f);
        titleV.setTypeface(Typeface.DEFAULT_BOLD);
        titleV.setGravity(Gravity.CENTER);
        titleV.setTextColor(Color.parseColor(color));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin = dp(20);
        titleV.setLayoutParams(tp);
        root.addView(titleV);

        TextView textV = new TextView(context);
        textV.setText(text);
        textV.setTextSize(15f);
        textV.setGravity(Gravity.CENTER);
        textV.setTextColor(Color.parseColor(color));
        textV.setAlpha(0.85f);
        LinearLayout.LayoutParams xp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        xp.topMargin = dp(12);
        textV.setLayoutParams(xp);
        root.addView(textV);

        Button btn = new Button(context);
        btn.setText(btnText);
        btn.setTextSize(15f);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextColor(Color.WHITE);

        GradientDrawable bgg = new GradientDrawable();
        bgg.setShape(GradientDrawable.RECTANGLE);
        bgg.setColor(Color.parseColor(color));
        bgg.setCornerRadius(dp(12));
        btn.setBackground(bgg);

        btn.setPadding(dp(48), dp(14), dp(48), dp(14));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.topMargin = dp(32);
        btn.setLayoutParams(bp);
        btn.setOnClickListener(v -> listener.onProceed());
        root.addView(btn);

        if (Config.VPN_FOOTNOTE != null && !Config.VPN_FOOTNOTE.isEmpty()) {
            TextView fn = new TextView(context);
            fn.setText(Config.VPN_FOOTNOTE);
            fn.setTextSize(11f);
            fn.setGravity(Gravity.CENTER);
            fn.setTextColor(Color.parseColor(color));
            fn.setAlpha(0.6f);
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            fp.topMargin = dp(20);
            fn.setLayoutParams(fp);
            root.addView(fn);
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
                }
