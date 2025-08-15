import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.ConsoleMessage;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        WebView webView = new WebView(this);
        
        // Enable JS
        webView.getSettings().setJavaScriptEnabled(true);
        
        // Enable DOM storage (localStorage, sessionStorage)
        webView.getSettings().setDomStorageEnabled(true);
        
        // Allow debugging for WebView JS
        WebView.setWebContentsDebuggingEnabled(true);
        
        // Optional: set a modern User-Agent for compatibility
        webView.getSettings().setUserAgentString(
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        );

        // Capture JS console logs
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d("WebViewConsole", consoleMessage.message() 
                        + " -- From line " + consoleMessage.lineNumber() 
                        + " of " + consoleMessage.sourceId());
                return true;
            }
        });

        // Keep links inside WebView
        webView.setWebViewClient(new WebViewClient());
        
        webView.loadUrl("https://bytpay.live");
        setContentView(webView);
    }
}
