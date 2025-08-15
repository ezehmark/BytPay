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
    private static final int SPLASH_DURATION = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        splashScreen = findViewById(R.id.splash_screen);
        webView = findViewById(R.id.webview);

        // Apply system theme styles
        applySystemThemeUI();

        // WebView Settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        String modernUA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36";
        webSettings.setUserAgentString(modernUA);

        // Allow CSS auto dark mode for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webSettings.setForceDark(WebSettings.FORCE_DARK_AUTO);
        }

        // WebView Clients
        WebView.setWebContentsDebuggingEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
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

        // Load website
        webView.loadUrl("https://bytpay.live");

        // Splash delay
        new Handler().postDelayed(() -> {
            splashScreen.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }, SPLASH_DURATION);
    }

    /**
     * Applies status/navigation bar colors and icon themes based on system dark mode.
     */
    private void applySystemThemeUI() {
        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        Window window = getWindow();

        // Set colors
        int barColor = isDark ? Color.parseColor("#E5E7EB") : Color.parseColor("#FFFFFF");
        window.setStatusBarColor(barColor);
        window.setNavigationBarColor(barColor);

        // Adjust icon colors (light icons for dark theme, dark icons for light theme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int appearance = isDark
                    ? 0 // Light icons
                    : WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS; // Dark icons
            window.getInsetsController().setSystemBarsAppearance(
                    appearance,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            );
        } else {
            View decor = window.getDecorView();
            int flags = decor.getSystemUiVisibility();
            if (!isDark) { // Light mode → dark icons
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            } else { // Dark mode → light icons
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            decor.setSystemUiVisibility(flags);
        }
    }

    /**
     * Sends the current theme to WebView + calls window.themeChange().
     */
    private void setThemeForWebView() {
        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        String theme = isDark ? "dark" : "light";

        String js =
                "document.documentElement.setAttribute('data-theme', '" + theme + "');" +
                "if (window.themeChange) { window.themeChange('" + theme + "'); }" +
                "try {" +
                "  const mql = window.matchMedia('(prefers-color-scheme: dark)');" +
                "  Object.defineProperty(mql, 'matches', { value: " + (isDark ? "true" : "false") + ", configurable: true });" +
                "  window.dispatchEvent(new Event('change'));" +
                "} catch(e) { console.log('Theme event injection failed', e); }";

        webView.evaluateJavascript(js, null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applySystemThemeUI();
        setThemeForWebView();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
