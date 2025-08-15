package com.ezehmark.bytpay;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WebViewConsole";
    private WebView webView;
    private View splashScreen;
    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        splashScreen = findViewById(R.id.splash_screen);
        webView = findViewById(R.id.webview);

        // --- Apply system theme styles to status/nav bars ---
        applySystemThemeUI();

        // --- WebView Settings ---
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        String modernUA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36";
        webSettings.setUserAgentString(modernUA);

        // Force dark mode for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webSettings.setForceDark(WebSettings.FORCE_DARK_AUTO);
        }

        // --- WebView Clients ---
        WebView.setWebContentsDebuggingEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Set theme when page is loaded
                setThemeForWebView();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage != null) {
                    String msg = consoleMessage.message();
                    int line = consoleMessage.lineNumber();
                    String source = consoleMessage.sourceId() != null ? consoleMessage.sourceId() : "unknown";
                    Log.d(TAG, msg + " -- From line " + line + " of " + source);
                }
                return true;
            }
        });

        // --- Load website ---
        webView.loadUrl("https://bytpay.live");

        // --- Show splash for 3 seconds ---
        new Handler().postDelayed(() -> {
            splashScreen.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }, SPLASH_DURATION);
    }

    /**
     * Applies status bar and navigation bar colors/icons according to system theme.
     */
    private void applySystemThemeUI() {
        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        Window window = getWindow();
        int statusBarColor = isDark ? Color.parseColor("#E5E7EB") : Color.parseColor("#FFFFFF");
        int navBarColor = isDark ? Color.parseColor("#E5E7EB") : Color.parseColor("#FFFFFF");
        window.setStatusBarColor(statusBarColor);
        window.setNavigationBarColor(navBarColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final int appearance = isDark
                    ? 0
                    : WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
            window.getInsetsController().setSystemBarsAppearance(
                    appearance,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            );
        } else {
            View decor = window.getDecorView();
            int flags = decor.getSystemUiVisibility();
            if (!isDark) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            decor.setSystemUiVisibility(flags);
        }
    }

    /**
     * Sends the current system theme to the WebView's document.
     */
    private void setThemeForWebView() {
        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        String theme = isDark ? "dark" : "light";

        String js = "document.documentElement.setAttribute('data-theme', '" + theme + "');" +
                "if (window.themeChange) { window.themeChange('" + theme + "'); }";
        webView.evaluateJavascript(js, null);
    }

    /**
     * Detects device theme changes in real time.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applySystemThemeUI(); // Update system UI colors/icons
        setThemeForWebView(); // Notify WebView
    }

    /**
     * Back button navigates WebView history before closing app.
     */
    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
