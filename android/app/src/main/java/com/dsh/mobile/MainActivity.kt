package com.dsh.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        urlEditText = findViewById(R.id.urlEditText)
        val openButton: Button = findViewById(R.id.openButton)
        val termuxButton: Button = findViewById(R.id.termuxButton)

        setupWebView()

        val prefs = getSharedPreferences("dsh_mobile", MODE_PRIVATE)
        urlEditText.setText(prefs.getString("last_url", "http://127.0.0.1:8080"))

        openButton.setOnClickListener {
            val url = urlEditText.text.toString().trim()
            if (url.isBlank()) {
                Toast.makeText(this, "请输入地址", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "http://$url"
            prefs.edit().putString("last_url", normalized).apply()
            webView.loadUrl(normalized)
        }

        termuxButton.setOnClickListener {
            try {
                TermuxLauncher.startTunnel(this)
                Toast.makeText(this, R.string.tunnel_started, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, R.string.tunnel_manual_hint, Toast.LENGTH_LONG).show()
            }
        }

        // 自动加载上次地址
        val lastUrl = prefs.getString("last_url", null)
        if (lastUrl != null) {
            webView.loadUrl(lastUrl)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
