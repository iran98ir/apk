package app.vista;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private WebView webView;
    private ProgressBar progressBar;
    private ErrorView errorView;
    private FrameLayout rootLayout;
    private LinearLayout welcomeView;
    private LinearLayout vpnView;
    private LinearLayout onboardingView;

    private boolean pagePreloaded = false;
    private long lastBackPressTime = 0L;
    private boolean errorShown = false;
    private boolean reachedWebView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.setBackgroundColor(Color.parseColor(Config.COLOR_BACKGROUND));
        setContentView(rootLayout);

        pagePreloaded = getIntent().getBooleanExtra("page_preloaded", false);

        if (Config.ONB_ENABLED) {
            showOnboarding();
        } else if (Config.WELCOME_ENABLED) {
            showWelcome();
        } else if (Config.VPN_ENABLED) {
            showVpn("unknown");
        } else {
            startWebView();
        }
    }

    private void showOnboarding() {
        OnboardingView v = new OnboardingView(this, () -> {
            rootLayout.removeAllViews();
            if (Config.WELCOME_ENABLED) {
                showWelcome();
            } else if (Config.VPN_ENABLED) {
                showVpn("unknown");
            } else {
                startWebView();
            }
        });
        rootLayout.addView(v);
    }

    private void showWelcome() {
        WelcomeView v = new WelcomeView(this, () -> {
            rootLayout.removeAllViews();
            if (Config.VPN_ENABLED) {
                showVpn("unknown");
            } else {
                startWebView();
            }
        });
        rootLayout.addView(v);
    }

    private void showVpn(String state) {
        VpnView v = new VpnView(this, state, () -> {
            rootLayout.removeAllViews();
            startWebView();
        });
        rootLayout.addView(v);
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    private void startWebView() {
        if (reachedWebView) return;
        reachedWebView = true;

        rootLayout.removeAllViews();

        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.addView(webView);

        if (Config.WV_PROGRESS_BAR) {
            progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(Config.WV_PROGRESS_HEIGHT));
            pp.gravity = Gravity.TOP;
            progressBar.setLayoutParams(pp);
            progressBar.setMax(100);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.GONE);
            try {
                progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                        Color.parseColor(Config.WV_PROGRESS_COLOR)));
            } catch (Exception ignored) {}
            rootLayout.addView(progressBar);
        }

        setupWebView();
        setupBackPressHandler();

        if (pagePreloaded) {
            WebView cached = PreloadManager.takeWebView();
            if (cached != null) {
                attachCachedWebView(cached);
            } else {
                loadUrl(Config.WV_URL);
            }
        } else {
            if (isNetworkAvailable()) {
                loadUrl(Config.WV_URL);
            } else {
                showError("offline");
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(Config.WV_JS);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setDomStorageEnabled(Config.WV_DOM);
        s.setDatabaseEnabled(Config.WV_DATABASE);
        s.setGeolocationEnabled(Config.WV_GEOLOCATION);
        s.setSupportZoom(Config.WV_ZOOM);
        s.setBuiltInZoomControls(Config.WV_ZOOM);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setTextZoom(100);
        s.setAllowFileAccess(Config.WV_FILE_UPLOAD);
        s.setAllowContentAccess(Config.WV_FILE_UPLOAD);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        s.setSupportMultipleWindows(false);

        if ("custom".equals(Config.WV_USER_AGENT)) {
            s.setUserAgentString(Config.WV_USER_AGENT_CUSTOM);
        } else if ("desktop".equals(Config.WV_USER_AGENT)) {
            s.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36");
        }

        if (Config.WV_CACHE) {
            s.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }

        webView.setBackgroundColor(Color.parseColor(Config.COLOR_BACKGROUND));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView newWebView = new WebView(MainActivity.this);
                WebSettings ns = newWebView.getSettings();
                ns.setJavaScriptEnabled(true);
                ns.setDomStorageEnabled(true);
                ns.setSupportMultipleWindows(true);
                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                        webView.loadUrl(req.getUrl().toString());
                        return true;
                    }
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v, String url) {
                        webView.loadUrl(url);
                        return true;
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (progressBar == null) return;
                if (newProgress < 100 && progressBar.getVisibility() != View.VISIBLE) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(newProgress);
                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, Config.WV_GEOLOCATION, false);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Config.WV_CAMERA || Config.WV_MIC) {
                    request.grant(request.getResources());
                } else {
                    request.deny();
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                errorShown = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (errorShown) hideError();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    showError(isNetworkAvailable() ? "server" : "offline");
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (request.isForMainFrame()) {
                    int status = errorResponse != null ? errorResponse.getStatusCode() : -1;
                    if (status == 404) showError("nf");
                    else if (status == 403) showError("fb");
                    else if (status == 504) showError("to");
                    else if (status >= 400 && status < 600) showError("server");
                }
            }
        });

        webView.setDownloadListener((url, ua, cd, mime, len) -> {
            try {
                String name = URLUtil.guessFileName(url, cd, mime);
                DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                req.setMimeType(mime);
                req.addRequestHeader("User-Agent", ua);
                req.setTitle(name);
                req.allowScanningByMediaScanner();
                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(req);
                    Toast.makeText(this, "در حال دانلود…", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "دانلود ناموفق", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attachCachedWebView(WebView cached) {
        try {
            if (cached.getParent() != null) {
                ((ViewGroup) cached.getParent()).removeView(cached);
            }
            cached.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            rootLayout.removeView(webView);
            if (webView != null) webView.destroy();
            webView = cached;
            rootLayout.addView(webView, 0);
            setupWebView();
        } catch (Exception e) {
            loadUrl(Config.WV_URL);
        }
    }

    private boolean handleUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        String low = url.toLowerCase(Locale.ROOT);

        if (low.startsWith("tel:") || low.startsWith("mailto:") || low.startsWith("sms:")
                || low.startsWith("whatsapp:") || low.startsWith("tg:")
                || low.startsWith("instagram:") || low.startsWith("market:")
                || low.startsWith("intent:") || low.startsWith("geo:")) {

            if (low.startsWith("intent:")) {
                try {
                    Intent i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    startActivity(i);
                } catch (Exception e) {
                    Log.e(TAG, "intent error");
                }
                return true;
            }
            openExternal(url);
            return true;
        }

        if (low.startsWith("http://") || low.startsWith("https://")) return false;
        return true;
    }

    private void openExternal(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "اپی برای باز کردن این لینک نیست", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "openExternal: " + e.getMessage());
        }
    }

    private void loadUrl(String url) {
        if (webView == null) return;
        webView.setVisibility(View.VISIBLE);
        hideError();
        webView.loadUrl(url);
    }

    private void showError(String type) {
        if (!Config.ERR_ENABLED) return;
        webView.setVisibility(View.GONE);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (errorView != null) rootLayout.removeView(errorView);
        errorView = new ErrorView(this, type, () -> {
            hideError();
            loadUrl(Config.WV_URL);
        });
        rootLayout.addView(errorView);
        errorShown = true;
    }

    private void hideError() {
        if (errorView != null) {
            rootLayout.removeView(errorView);
            errorView = null;
        }
        if (webView != null) webView.setVisibility(View.VISIBLE);
        errorShown = false;
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities cap = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return cap != null && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            } else {
                NetworkInfo ni = cm.getActiveNetworkInfo();
                return ni != null && ni.isConnected();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (errorShown) {
                    finishAffinity();
                    return;
                }
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                    return;
                }
                if (Config.WV_BACK_DOUBLE) {
                    long now = System.currentTimeMillis();
                    if (now - lastBackPressTime < 2000) {
                        finishAffinity();
                    } else {
                        lastBackPressTime = now;
                        Toast.makeText(MainActivity.this, Config.WV_BACK_EXIT_MSG, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showExitDialog();
                }
            }
        });
    }

    private void showExitDialog() {
        if (!Config.EXIT_ENABLED) {
            finishAffinity();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(Config.EXIT_TITLE)
                .setMessage(Config.EXIT_TEXT)
                .setPositiveButton(Config.EXIT_BTN_CONFIRM_TEXT, (d, w) -> finishAffinity())
                .setNegativeButton(Config.EXIT_BTN_CANCEL_TEXT, (d, w) -> d.dismiss())
                .setCancelable(true)
                .show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            try {
                ViewGroup p = (ViewGroup) webView.getParent();
                if (p != null) p.removeView(webView);
                webView.stopLoading();
                webView.loadUrl("about:blank");
                webView.removeAllViews();
                webView.destroy();
                webView = null;
            } catch (Exception e) {
                Log.e(TAG, "onDestroy: " + e.getMessage());
            }
        }
        super.onDestroy();
    }
                                        }
