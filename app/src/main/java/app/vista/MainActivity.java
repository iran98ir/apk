/* =========================================================
   MainActivity.java — WebView اصلی
   مسیر: app/src/main/java/app/vista/MainActivity.java
   ========================================================= */

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
import android.widget.ProgressBar;
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

    private boolean pagePreloaded = false;
    private long lastBackPressTime = 0L;
    private boolean errorShown = false;

    private int colorBg;
    private int colorProgress;

    private String baseUrl;
    private String baseDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ConfigLoader cfg = ConfigLoader.get(this);
        baseUrl = cfg.getUrl();
        baseDomain = extractDomain(baseUrl);
        colorBg = cfg.getColorBackground();

        // ساخت root با کد
        rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.setBackgroundColor(colorBg);

        // WebView
        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.addView(webView);

        // ProgressBar بالای صفحه
        progressBar = new ProgressBar(this, null,
                android.R.attr.progressBarStyleHorizontal);
        FrameLayout.LayoutParams progParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3));
        progParams.gravity = Gravity.TOP;
        progressBar.setLayoutParams(progParams);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        rootLayout.addView(progressBar);

        setContentView(rootLayout);

        setupWebView();
        setupBackPressHandler();

        pagePreloaded = getIntent().getBooleanExtra("page_preloaded", false);

        if (pagePreloaded) {
            WebView cached = PreloadManager.takeWebView();
            if (cached != null) {
                attachCachedWebView(cached);
            } else {
                loadUrl(baseUrl);
            }
        } else {
            if (isNetworkAvailable()) {
                loadUrl(baseUrl);
            } else {
                showError("offline");
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setTextZoom(100);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setSupportMultipleWindows(false);

        webView.setBackgroundColor(colorBg);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                WebView newWebView = new WebView(MainActivity.this);
                WebSettings s = newWebView.getSettings();
                s.setJavaScriptEnabled(true);
                s.setDomStorageEnabled(true);
                s.setSupportMultipleWindows(true);

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

                WebView.WebViewTransport transport =
                        (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100 && progressBar.getVisibility() != View.VISIBLE) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(newProgress);
                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                        GeolocationPermissions.Callback callback) {
                callback.invoke(origin, false, false);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.deny();
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
                progressBar.setVisibility(View.GONE);
                if (errorShown) {
                    hideError();
                }
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
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    if (!isNetworkAvailable()) {
                        showError("offline");
                    } else {
                        showError("server");
                    }
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                            WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (request.isForMainFrame()) {
                    int status = errorResponse != null ? errorResponse.getStatusCode() : -1;
                    if (status == 404) {
                        showError("nf");
                    } else if (status == 403) {
                        showError("fb");
                    } else if (status == 504) {
                        showError("to");
                    } else if (status >= 400 && status < 600) {
                        showError("server");
                    }
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                request.addRequestHeader("User-Agent", userAgent);
                request.setTitle(fileName);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm =
                        (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    Toast.makeText(MainActivity.this,
                            "در حال دانلود…", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this,
                        "دانلود ناموفق بود", Toast.LENGTH_SHORT).show();
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
            rootLayout.addView(cached, 0);
            if (webView != null) webView.destroy();
            webView = cached;
            setupWebView();
        } catch (Exception e) {
            loadUrl(baseUrl);
        }
    }

    private boolean handleUrl(String url) {
        if (url == null || url.isEmpty()) return false;

        String lowerUrl = url.toLowerCase(Locale.ROOT);

        if (lowerUrl.startsWith("tel:") ||
            lowerUrl.startsWith("mailto:") ||
            lowerUrl.startsWith("sms:") ||
            lowerUrl.startsWith("whatsapp:") ||
            lowerUrl.startsWith("tg:") ||
            lowerUrl.startsWith("instagram:") ||
            lowerUrl.startsWith("market:") ||
            lowerUrl.startsWith("intent:") ||
            lowerUrl.startsWith("geo:")) {

            if (lowerUrl.startsWith("intent:")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "intent parse error");
                }
                return true;
            }
            openExternal(url);
            return true;
        }

        if (lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://")) {
            return false;
        }

        return true;
    }

    private void openExternal(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "اپی برای باز کردن این لینک پیدا نشد",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "openExternal error: " + e.getMessage());
        }
    }

    private void loadUrl(String url) {
        if (webView == null) return;
        webView.setVisibility(View.VISIBLE);
        hideError();
        webView.loadUrl(url);
    }

    private void showError(String type) {
        webView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        if (errorView != null) {
            rootLayout.removeView(errorView);
        }
        errorView = new ErrorView(this, type, () -> {
            hideError();
            loadUrl(baseUrl);
        });
        rootLayout.addView(errorView);

        errorShown = true;
    }

    private void hideError() {
        if (errorView != null) {
            rootLayout.removeView(errorView);
            errorView = null;
        }
        webView.setVisibility(View.VISIBLE);
        errorShown = false;
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities =
                        cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities == null) return false;
                return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            } else {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
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
                showExitDialog();
            }
        });
    }

    private void showExitDialog() {
        long now = System.currentTimeMillis();
        if (now - lastBackPressTime < 2000) {
            finishAffinity();
            return;
        }
        lastBackPressTime = now;

        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_title)
                .setMessage(R.string.exit_text)
                .setPositiveButton(R.string.exit_confirm, (dialog, which) -> finishAffinity())
                .setNegativeButton(R.string.exit_cancel, (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private String extractDomain(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host == null) return "";
            return host.toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            return "";
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
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
                ViewGroup parent = (ViewGroup) webView.getParent();
                if (parent != null) parent.removeView(webView);
                webView.stopLoading();
                webView.loadUrl("about:blank");
                webView.removeAllViews();
                webView.destroy();
                webView = null;
            } catch (Exception e) {
                Log.e(TAG, "onDestroy WebView error: " + e.getMessage());
            }
        }
        super.onDestroy();
    }
                  }
