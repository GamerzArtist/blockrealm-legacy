package com.gamerzartist.blockrealm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

public final class MainActivity extends Activity {
    private static final String TAG = "BlockRealm";
    private static final String APP_URL = "https://appassets.androidplatform.net/assets/index.html?android=1&build=40000";

    private FrameLayout root;
    private WebView webView;
    private TextView nativeError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );
        enterImmersiveMode();
        buildNativeBootSurface();
        createGameWebView();
    }

    private void buildNativeBootSurface() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(11, 14, 12));

        nativeError = new TextView(this);
        nativeError.setTextColor(Color.WHITE);
        nativeError.setTextSize(15f);
        nativeError.setGravity(Gravity.CENTER);
        nativeError.setPadding(36, 36, 36, 36);
        nativeError.setText("BLOCKREALM: LEGACY 4.0.0\n\nStarting Android renderer…");
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        root.addView(nativeError, lp);
        setContentView(root);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void createGameWebView() {
        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(11, 14, 12));
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setKeepScreenOn(true);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                Log.println(toPriority(message.messageLevel()), TAG,
                        message.message() + " @" + message.sourceId() + ":" + message.lineNumber());
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClientCompat() {
            @Override
            public boolean shouldOverrideUrlLoading(
                    @NonNull WebView view,
                    @NonNull WebResourceRequest request
            ) {
                String url = request.getUrl().toString();
                if (url.startsWith("https://appassets.androidplatform.net/")) {
                    return false;
                }
                Log.w(TAG, "Blocked unsafe WebView navigation to: " + url);
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(
                    @NonNull WebView view,
                    @NonNull WebResourceRequest request
            ) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageFinished(@NonNull WebView view, @NonNull String url) {
                nativeError.setVisibility(View.GONE);
                view.setVisibility(View.VISIBLE);
                enterImmersiveMode();
            }

            @Override
            public void onReceivedError(
                    @NonNull WebView view,
                    @NonNull WebResourceRequest request,
                    @NonNull androidx.webkit.WebResourceErrorCompat error
            ) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    showNativeError("The game shell could not load.\n\n" + error.getDescription());
                }
            }

            @Override
            public void onReceivedHttpError(
                    @NonNull WebView view,
                    @NonNull WebResourceRequest request,
                    @NonNull WebResourceResponse errorResponse
            ) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (request.isForMainFrame()) {
                    showNativeError("Android asset error: HTTP " + errorResponse.getStatusCode());
                }
            }
        });

        webView.setVisibility(View.INVISIBLE);
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        webView.loadUrl(APP_URL);
    }

    @Override
    public void onBackPressed() {
        if (webView == null) {
            super.onBackPressed();
            return;
        }
        webView.evaluateJavascript(
                "window.BlockRealmAndroidBack ? window.BlockRealmAndroidBack() : false",
                result -> {
                    if (!"true".equals(result)) finishFromBack();
                }
        );
    }

    private void finishFromBack() {
        super.onBackPressed();
    }

    private void showNativeError(String message) {
        nativeError.setText("BLOCKREALM COULD NOT START\n\n" + message +
                "\n\nUpdate Android System WebView/Chrome, then reopen the game.");
        nativeError.setVisibility(View.VISIBLE);
        if (webView != null) webView.setVisibility(View.GONE);
    }

    private static int toPriority(ConsoleMessage.MessageLevel level) {
        switch (level) {
            case ERROR: return Log.ERROR;
            case WARNING: return Log.WARN;
            case LOG: return Log.INFO;
            case TIP: return Log.DEBUG;
            default: return Log.VERBOSE;
        }
    }

    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterImmersiveMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
        enterImmersiveMode();
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.evaluateJavascript(
                    "window.BlockRealmAndroidPause && window.BlockRealmAndroidPause()",
                    null
            );
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            root.removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
