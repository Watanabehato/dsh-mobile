package com.dsh.mobile

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dsh.mobile.SshTunnelService.SshConfig

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var hostEditText: EditText
    private lateinit var sshPortEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var dshPortEditText: EditText
    private lateinit var localPortEditText: EditText
    private lateinit var connectButton: Button
    private lateinit var statusTextView: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var loadedUrl: String? = null

    private val statusRunnable = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        hostEditText = findViewById(R.id.hostEditText)
        sshPortEditText = findViewById(R.id.sshPortEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        dshPortEditText = findViewById(R.id.dshPortEditText)
        localPortEditText = findViewById(R.id.localPortEditText)
        connectButton = findViewById(R.id.connectButton)
        statusTextView = findViewById(R.id.statusTextView)

        setupWebView()
        requestNotificationPermissionIfNeeded()

        val prefs = getSharedPreferences("dsh_mobile", MODE_PRIVATE)
        hostEditText.setText(prefs.getString("host", ""))
        sshPortEditText.setText(prefs.getString("ssh_port", "22"))
        usernameEditText.setText(prefs.getString("username", ""))
        dshPortEditText.setText(prefs.getString("dsh_port", "3080"))
        localPortEditText.setText(prefs.getString("local_port", "3080"))
        passwordEditText.setText(prefs.getString("password", ""))

        connectButton.setOnClickListener {
            if (SshTunnelState.connected) {
                SshTunnelService.stop(this)
                connectButton.text = getString(R.string.connect)
            } else {
                val config = readConfig()
                if (config == null) {
                    Toast.makeText(this, R.string.config_invalid, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                saveConfig(config)
                SshTunnelService.start(this, config)
                connectButton.text = getString(R.string.disconnect)
                statusTextView.text = getString(R.string.connecting)
            }
        }

        handler.post(statusRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(statusRunnable)
        super.onDestroy()
    }

    private fun readConfig(): SshConfig? {
        val host = hostEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val sshPort = sshPortEditText.text.toString().toIntOrNull() ?: 22
        val dshPort = dshPortEditText.text.toString().toIntOrNull() ?: 3080
        val localPort = localPortEditText.text.toString().toIntOrNull() ?: 3080

        if (host.isEmpty() || username.isEmpty() || password.isEmpty()) {
            return null
        }
        return SshConfig(host, sshPort, username, password, dshPort, localPort)
    }

    private fun saveConfig(config: SshConfig) {
        getSharedPreferences("dsh_mobile", MODE_PRIVATE).edit()
            .putString("host", config.host)
            .putString("ssh_port", config.sshPort.toString())
            .putString("username", config.username)
            .putString("password", config.password)
            .putString("dsh_port", config.dshPort.toString())
            .putString("local_port", config.localPort.toString())
            .apply()
    }

    private fun updateStatus() {
        statusTextView.text = SshTunnelState.message

        if (SshTunnelState.connected) {
            connectButton.text = getString(R.string.disconnect)
            val url = "http://127.0.0.1:${SshTunnelState.localPort}"
            if (loadedUrl != url) {
                loadedUrl = url
                webView.loadUrl(url)
            }
        } else {
            connectButton.text = getString(R.string.connect)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
            }
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
