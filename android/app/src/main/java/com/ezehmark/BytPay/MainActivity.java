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
        setContentView(R.layout.activity_main); // XML layout with splash ImageView & WebView

        splashScreen = findViewById(R.id.splash_screen);
        webView = findViewById(R.id.webview);

        // --- Detect system theme ---
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDark = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        // --- Set status bar & navigation bar colors ---
        Window window = getWindow();
        int statusBarColor = isDark ? Color.parseColor("#E5E7EB") : Color.parseColor("#FFFFFF"); // gray-200 vs white
        int navBarColor = isDark ? Color.parseColor("#E5E7EB") : Color.parseColor("#FFFFFF");
        window.setStatusBarColor(statusBarColor);
        window.setNavigationBarColor(navBarColor);

        // --- Set light/dark icons ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
            final int appearance = isDark
                    ? 0 // light icons on dark background
                    : WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS; // dark icons on light background
            window.getInsetsController().setSystemBarsAppearance(
                    appearance,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            );
        } else { // older versions
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
        webView.setWebViewClient(new WebViewClient());
        WebView.setWebContentsDebuggingEnabled(true);
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

        // --- Pass theme to Next.js ---
        String theme = isDark ? "dark" : "light";
        new Handler().postDelayed(() -> {
            webView.evaluateJavascript(
                    "document.documentElement.setAttribute('data-theme', '" + theme + "');",
                    null
            );
        }, 100);

        // --- Show splash for 3 seconds ---
        new Handler().postDelayed(() -> {
            splashScreen.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }, SPLASH_DURATION);
    }

    // --- Back button navigates WebView history ---
    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
