/* =========================================================
   PreloadManager.java — لود پس‌زمینه‌ی WebView
   مسیر: app/src/main/java/app/vista/PreloadManager.java
   ========================================================= */

package app.vista;

import android.annotation.SuppressLint;
import android.content.Context;
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

        String url = ConfigLoader.get().getUrl();

        mainHandler.post(() -> {
            try {
                WebView webView = new WebView(appContext);
                setupWebView(webView, appContext);

                final Runnable timeoutRunnable = () -> {
                    if (completed.compareAndSet(false, true)) {
                        callback.onPageFailed();
                    }
                };
                mainHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        sCachedWebView = view;
                        sIsLoaded = true;
                        mainHandler.removeCallbacks(timeoutRunnable);
                        if (completed.compareAndSet(false, true)) {
                            callback.onPageLoaded();
                        }
                    }

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                        if (request.isForMainFrame()) {
                            mainHandler.removeCallbacks(timeoutRunnable);
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
                                mainHandler.removeCallbacks(timeoutRunnable);
                                if (completed.compareAndSet(false, true)) {
                                    callback.onPageFailed();
                                }
                            }
                        }
                    }
                });

                webView.loadUrl(url);

            } catch (Exception e) {
                Log.e(TAG, "Preload error: " + e.getMessage());
                if (completed.compareAndSet(false, true)) {
                    callback.onPageFailed();
                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void setupWebView(WebView webView, Context context) {
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
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setTextZoom(100);

        int bgColor = context.getResources().getColor(R.color.color_background, null);
        webView.setBackgroundColor(bgColor);
    }

    public static WebView takeWebView() {
        WebView wv = sCachedWebView;
        sCachedWebView = null;
        return wv;
    }

    public static boolean isLoaded() {
        return sIsLoaded && sCachedWebView != null;
    }

    public static void clear() {
        if (sCachedWebView != null) {
            try {
                sCachedWebView.stopLoading();
                sCachedWebView.loadUrl("about:blank");
                sCachedWebView.removeAllViews();
                sCachedWebView.destroy();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing WebView: " + e.getMessage());
            }
            sCachedWebView = null;
        }
        sIsLoaded = false;
    }

    public static void reset() {
        clear();
    }
          }
