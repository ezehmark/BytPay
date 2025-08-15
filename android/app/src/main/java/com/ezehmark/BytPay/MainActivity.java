package com.ezehmark.bytpay;

import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WebViewConsole";
    private WebView webView; // class-level variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create WebView programmatically
        webView = new WebView(this);

        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Enable DOM storage
        webSettings.setDomStorageEnabled(true);

        // Optional: Set modern User-Agent
        String modernUA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36";
        webSettings.setUserAgentString(modernUA);

        // Keep navigation inside WebView
        webView.setWebViewClient(new WebViewClient());

        // Enable JS console logging safely
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

        // Load your website
        webView.loadUrl("https://bytpay.live");

        // Set WebView as content view
        setContentView(webView);
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
