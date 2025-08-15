package com.ezehmark.bytpay;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WebViewConsole";
    private WebView webView;
    private View splashScreen;
    private ImageView noWifiImage;
    private static final int SPLASH_DURATION = 5000; // 5 seconds

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        splashScreen = findViewById(R.id.splash_screen);
        webView = findViewById(R.id.webview);
        noWifiImage = findViewById(R.id.no_wifi_image);

        applySystemThemeUI();
        setupGoogleSignIn();

        // WebView setup
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        String modernUA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36";
        webSettings.setUserAgentString(modernUA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webSettings.setForceDark(WebSettings.FORCE_DARK_AUTO);
        }

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

        // Add JS interface for triggering native sign-in
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void triggerGoogleSignIn() {
                runOnUiThread(() -> startGoogleSignIn());
            }
        }, "AndroidApp");

        // Splash delay & internet check
        new Handler().postDelayed(() -> {
            splashScreen.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> splashScreen.setVisibility(View.GONE));

            if (isConnected()) {
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl("https://bytpay.live");
            } else {
                noWifiImage.setVisibility(View.VISIBLE);
            }
        }, SPLASH_DURATION);
    }

    /** INTERNET CHECK **/
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    /** THEME SETUP **/
    private void applySystemThemeUI() {
        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        Window window = getWindow();
        int barColor = isDark ? Color.parseColor("#6B7280") : Color.parseColor("#E5E7EB");
        window.setStatusBarColor(barColor);
        window.setNavigationBarColor(barColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            int appearance = isDark
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

    /** GOOGLE SIGN-IN **/
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from Firebase console
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void startGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                sendTokenToWebView(idToken);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendTokenToWebView(String token) {
        String js = "window.postMessage({ type: 'GOOGLE_SIGN_IN', token: '" + token + "' }, '*');";
        webView.evaluateJavascript(js, null);
    }

    /** THEME CHANGE HANDLER **/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applySystemThemeUI();
        setThemeForWebView();
    }

    /** BACK BUTTON BEHAVIOR **/
    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

