package app.vista;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class PreloadManager {

    private static final String TAG = "PreloadManager";
    private static final long TIMEOUT_MS = 8000L;

    @SuppressLint("StaticFieldLeak")
    private static volatile WebView sCachedWebView = null;
    private static volatile boolean sIsLoaded = false;

    public interface PreloadCallback {
        void onPageLoaded();
        void onPageFailed();
    }

    public static void preload(@NonNull Context context, @NonNull PreloadCallback callback) {
        if (sIsLoaded && sCachedWebView != null) {
            callback.onPageLoaded();
            return;
        }

        Context appContext = context.getApplicationContext();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        AtomicBoolean completed = new AtomicBoolean(false);

        String url = Config.WV_URL;

        mainHandler.post(() -> {
            try {
                WebView webView = new WebView(appContext);
                setupWebView(webView, appContext);

                final Runnable timeout = () -> {
                    if (completed.compareAndSet(false, true)) {
                        callback.onPageFailed();
                    }
                };
                mainHandler.postDelayed(timeout, TIMEOUT_MS);

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        sCachedWebView = view;
                        sIsLoaded = true;
                        mainHandler.removeCallbacks(timeout);
                        if (completed.compareAndSet(false, true)) {
                            callback.onPageLoaded();
                        }
                    }

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                        if (request.isForMainFrame()) {
                            mainHandler.removeCallbacks(timeout);
                            if (completed.compareAndSet(false, true)) {
                                callback.onPageFailed();
                            }
                        }
                    }

                    @Override
                    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                        super.onReceivedHttpError(view, request, errorResponse);
                        if (request.isForMainFrame()) {
                            int status = errorResponse != null ? errorResponse.getStatusCode() : -1;
                            if (status >= 400) {
                                mainHandler.removeCallbacks(timeout);
                                if (completed.compareAndSet(false, true)) {
                                    callback.onPageFailed();
                                }
                            }
                        }
                    }
                });

                webView.loadUrl(url);

            } catch (Exception e) {
                Log.e(TAG, "preload: " + e.getMessage());
                if (completed.compareAndSet(false, true)) {
                    callback.onPageFailed();
                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void setupWebView(WebView webView, Context context) {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(Config.WV_JS);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setDomStorageEnabled(Config.WV_DOM);
        s.setDatabaseEnabled(Config.WV_DATABASE);
        s.setCacheMode(Config.WV_CACHE ? WebSettings.LOAD_DEFAULT : WebSettings.LOAD_NO_CACHE);
        s.setSupportZoom(Config.WV_ZOOM);
        s.setBuiltInZoomControls(Config.WV_ZOOM);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setAllowFileAccess(Config.WV_FILE_UPLOAD);
        s.setAllowContentAccess(Config.WV_FILE_UPLOAD);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);

        if ("custom".equals(Config.WV_USER_AGENT)) {
            s.setUserAgentString(Config.WV_USER_AGENT_CUSTOM);
        }

        try {
            webView.setBackgroundColor(Color.parseColor(Config.COLOR_BACKGROUND));
        } catch (Exception ignored) {
            webView.setBackgroundColor(Color.WHITE);
        }
    }

    public static WebView takeWebView() {
        WebView wv = sCachedWebView;
        sCachedWebView = null;
        return wv;
    }

    public static boolean isLoaded() {
        return sIsLoaded && sCachedWebView != null;
    }

    public static void clear()
