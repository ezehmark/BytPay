package com.ezehmark.bytpay

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException

class MainActivity : AppCompatActivity() {

    private val TAG = "WebViewConsole"
    private lateinit var webView: WebView
    private lateinit var splashScreen: View
    private lateinit var noWifiImage: ImageView

    private val SPLASH_DURATION = 5000

    // Google Identity Services
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    // Network monitoring
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        splashScreen = findViewById(R.id.splash_screen)
        webView = findViewById(R.id.webview)
        noWifiImage = findViewById(R.id.no_wifi_image)

        applySystemThemeUI()
        setupGoogleIdentity()

        // WebView setup
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        val modernUA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36"
        webSettings.userAgentString = modernUA

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webSettings.forceDark = WebSettings.FORCE_DARK_AUTO
        }

        WebView.setWebContentsDebuggingEnabled(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                setThemeForWebView()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                consoleMessage?.let {
                    val msg = it.message()
                    val line = it.lineNumber()
                    val source = it.sourceId() ?: "unknown"
                    Log.d(TAG, "$msg -- From line $line of $source")
                }
                return true
            }
        }

        // JS interface for sign-in
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun triggerGoogleSignIn() {
                runOnUiThread { startGoogleSignIn() }
            }
        }, "AndroidApp")

        // Splash delay & internet check
        Handler().postDelayed({
            splashScreen.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction { splashScreen.visibility = View.GONE }

            if (isConnected()) {
                webView.visibility = View.VISIBLE
                webView.loadUrl("https://bytpay.live")
            } else {
                webView.visibility = View.GONE
                noWifiImage.visibility = View.VISIBLE
            }
        }, SPLASH_DURATION.toLong())
    }

    /** NETWORK LISTENER **/
    override fun onStart() {
        super.onStart()
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    noWifiImage.visibility = View.GONE
                    webView.visibility = View.VISIBLE
                    if (webView.url == null) {
                        webView.loadUrl("https://bytpay.live")
                    }
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    webView.visibility = View.GONE
                    noWifiImage.visibility = View.VISIBLE
                }
            }
        }

        cm.registerDefaultNetworkCallback(networkCallback!!)
    }

    override fun onStop() {
        super.onStop()
        networkCallback?.let {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(it)
        }
    }

    /** THEME SETUP **/
    private fun applySystemThemeUI() {
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

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
            if (!isDark) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            } else {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            }
            decor.systemUiVisibility = flags
        }
    }

    private fun setThemeForWebView() {
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val theme = if (isDark) "dark" else "light"

        val js = """
            document.documentElement.setAttribute('data-theme', '$theme');
            if (window.themeChange) { window.themeChange('$theme'); }
            try {
              const mql = window.matchMedia('(prefers-color-scheme: dark)');
              Object.defineProperty(mql, 'matches', { value: ${if (isDark) "true" else "false"}, configurable: true });
              window.dispatchEvent(new Event('change'));
            } catch(e) { console.log('Theme event injection failed', e); }
        """
        webView.evaluateJavascript(js, null)
    }

    /** GOOGLE IDENTITY SERVICES **/
    private fun setupGoogleIdentity() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    private fun startGoogleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        200, null, 0, 0, 0, null
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Sign-in failed: ${it.localizedMessage}")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    sendTokenToWebView(idToken)
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun sendTokenToWebView(token: String) {
        val js = "if (window.onGoogleSignIn) { window.onGoogleSignIn('$token'); }"
        webView.evaluateJavascript(js, null)
    }

    /** THEME CHANGE HANDLER **/
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySystemThemeUI()
        setThemeForWebView()
    }

    /** BACK BUTTON BEHAVIOR **/
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun isConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetworkInfo
        return network != null && network.isConnected
    }
}
