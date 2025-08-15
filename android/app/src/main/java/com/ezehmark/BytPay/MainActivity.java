package com.ezehmark.bytpay;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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

        // WebView settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        String modernUA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36";
        webSettings.setUserAgentString(modernUA);

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

        // Load the website
        webView.loadUrl("https://bytpay.live");

        // Hide splash after 3 seconds, show WebView
        new Handler().postDelayed(() -> {
            splashScreen.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }, SPLASH_DURATION);
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
