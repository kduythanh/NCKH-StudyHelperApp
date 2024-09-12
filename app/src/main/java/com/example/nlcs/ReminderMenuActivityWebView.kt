package com.example.nlcs

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityReminderMenuApiBinding
import com.example.nlcs.databinding.ActivityReminderMenuWebviewBinding

class ReminderMenuActivityWebView : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var binding: ActivityReminderMenuWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderMenuWebviewBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_reminder_menu_webview)
        // Xử lý hành động của nút mũi tên
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        webView = findViewById(R.id.web_view)
//        // Cấu hình WebView
        webView.settings.apply {
            javaScriptEnabled = true // Cho phép chạy JavaScript
//            domStorageEnabled = true // Cho phép sử dụng DOM Storage
            loadWithOverviewMode = true // Tự động điều chỉnh kích thước nội dung
            useWideViewPort = true // Sử dụng toàn bộ chiều rộng của màn hình
            setSupportZoom(true)
            builtInZoomControls = true // Cho phép zoom
            displayZoomControls = true // Hiện các control zoom
        }
        // Tải trang web
        webView.loadUrl("https://accounts.google.com/v3/signin/identifier?dsh=S1611624178:1665765818620318&continue=https://calendar.google.com/calendar/r&followup=https://calendar.google.com/calendar/r&osid=1&passive=1209600&service=cl&flowName=GlifWebSignIn&flowEntry=ServiceLogin&ifkv=AQDHYWrL2lk0_Bcr1n1Y-f-i1sNZRKJK8CNisliX9rpozkqKhY2Jby8gsVZ_wDz_oHqiWmN6uZ6s6g")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Quay lại màn hình trước đó khi nhấn nút mũi tên
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}



