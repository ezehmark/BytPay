package com.ezehmark.bytpay

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WebViewConsole"
        private const val SPLASH_DURATION = 5000
        private const val RC_SIGN_IN = 100
    }

    private var webView: WebView? = null
    private var splashScreen: View? = null
    private var noWifiImage: ImageView? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            splashScreen = findViewById(R.id.splash_screen)
            webView = findViewById(R.id.webview)
            noWifiImage = findViewById(R.id.no_wifi_image)

            applySystemThemeUI()
            setupGoogleSignIn()
            setupWebView()

            // JS interface to trigger Google Sign-In
            webView?.addJavascriptInterface(object {
                @JavascriptInterface
                fun triggerGoogleSignIn() {
                    runOnUiThread { startGoogleSignIn() }
                }
            }, "AndroidApp")

            // Splash delay
            Handler(Looper.getMainLooper()).postDelayed({
                splashScreen?.animate()?.alpha(0f)?.setDuration(500)
                    ?.withEndAction { splashScreen?.visibility = View.GONE }

                if (isConnected()) {
                    webView?.visibility = View.VISIBLE
                    webView?.loadUrl("https://bytpay.live")
                } else {
                    webView?.visibility = View.GONE
                    noWifiImage?.visibility = View.VISIBLE
                }
            }, SPLASH_DURATION.toLong())

        } catch (e: Exception) {
            Log.e(TAG, "Crash in onCreate", e)
        }
    }

    private fun setupWebView() {
        try {
            webView?.settings?.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/120.0.0.0 Mobile Safari/537.36"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    forceDark = WebSettings.FORCE_DARK_AUTO
                }
            }

            WebView.setWebContentsDebuggingEnabled(true)

            webView?.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    setThemeForWebView()
                }
            }

            webView?.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d(TAG, "${it.message()} -- line ${it.lineNumber()} of ${it.sourceId() ?: "unknown"}")
                    }
                    return true
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "WebView setup failed", e)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread {
                        noWifiImage?.visibility = View.GONE
                        webView?.visibility = View.VISIBLE
                        if (webView?.url == null) webView?.loadUrl("https://bytpay.live")
                    }
                }

                override fun onLost(network: Network) {
                    runOnUiThread {
                        webView?.visibility = View.GONE
                        noWifiImage?.visibility = View.VISIBLE
                    }
                }
            }
            cm.registerDefaultNetworkCallback(networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Network callback failed", e)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            networkCallback?.let {
                val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                cm.unregisterNetworkCallback(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unregister network callback failed", e)
        }
    }

    private fun applySystemThemeUI() {
        try {
            val isDark =
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val window: Window = window
            val barColor = if (isDark) Color.parseColor("#6B7280") else Color.parseColor("#E5E7EB")
            window.statusBarColor = barColor
            window.navigationBarColor = barColor

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val appearance = if (isDark) 0 else
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                window.insetsController?.setSystemBarsAppearance(
                    appearance,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            } else {
                val decor = window.decorView
                var flags = decor.systemUiVisibility
                flags = if (!isDark) {
                    flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0
                } else {
                    flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv() else flags
                }
                decor.systemUiVisibility = flags
            }
        } catch (e: Exception) {
            Log.e(TAG, "Theme setup failed", e)
        }
    }

    private fun setThemeForWebView() {
        try {
            val isDark =
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val theme = if (isDark) "dark" else "light"

            val js = """
                document.documentElement.setAttribute('data-theme', '$theme');
                if (window.themeChange) { window.themeChange('$theme'); }
                try {
                  const mql = window.matchMedia('(prefers-color-scheme: dark)');
                  Object.defineProperty(mql, 'matches', { value: ${isDark}, configurable: true });
                  window.dispatchEvent(new Event('change'));
                } catch(e) { console.log('Theme event injection failed', e); }
            """.trimIndent()

            webView?.evaluateJavascript(js, null)
        } catch (e: Exception) {
            Log.e(TAG, "WebView theme injection failed", e)
        }
    }

    /** OLD GOOGLE SIGN-IN **/
    private fun setupGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Log.e(TAG, "GoogleSignInClient init failed", e)
        }
    }

    private fun startGoogleSignIn() {
        try {
            mGoogleSignInClient?.signInIntent?.let {
                startActivityForResult(it, RC_SIGN_IN)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == RC_SIGN_IN) {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = try {
                    task.getResult(ApiException::class.java)
                } catch (e: ApiException) {
                    Log.e(TAG, "GoogleSignIn failed", e)
                    null
                }
                account?.idToken?.let { sendTokenToWebView(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onActivityResult failed", e)
        }
    }

    private fun sendTokenToWebView(token: String) {
        try {
            val js = "if (window.onGoogleSignIn) { window.onGoogleSignIn('$token'); }"
            webView?.evaluateJavascript(js, null)
        } catch (e: Exception) {
            Log.e(TAG, "Sending token to WebView failed", e)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySystemThemeUI()
        setThemeForWebView()
    }

    override fun onBackPressed() {
        webView?.let {
            if (it.canGoBack()) {
                it.goBack()
                return
            }
        }
        super.onBackPressed()
    }

    private fun isConnected(): Boolean {
        return try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.activeNetwork != null
        } catch (e: Exception) {
            false
        }
    }
}
