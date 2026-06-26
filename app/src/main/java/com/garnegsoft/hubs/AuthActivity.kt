package com.garnegsoft.hubs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowInsets
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

class AuthActivity : AppCompatActivity() {


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)

        val webView = findViewById<WebView>(R.id.authWebView)
        ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                this.setMargins(
                    statusBarInsets.left,
                    statusBarInsets.top,
                    statusBarInsets.right,
                    0
                )
            }

            WindowInsetsCompat.CONSUMED
        }

        webView.apply {

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true

            webViewClient = AuthWebViewClient {
                this@AuthActivity.setResult(Activity.RESULT_OK, Intent().apply
                {
                    putExtra(
                        "cookies",
                        CookieManager.getInstance().getCookie("https://habr.com")
                    )
                })
                finish()
            }
            webChromeClient = WebChromeClient()

            loadUrl("https://habr.com/kek/v1/auth/habrahabr/?back=/ru/all/&hl=ru")

        }
    }

    class AuthWebViewClient(
        val loginSucceedUrl: String = "https://habr.com/ru/all/",
        val onLogin: () -> Unit,
    ) : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            url?.let {
                if (it.contains("/articles/"))
                    onLogin()
            }
                super.onPageStarted(view, url, favicon)
        }

    }


}