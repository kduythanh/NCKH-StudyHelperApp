package com.example.nlcs

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class ReminderMenuActivityAPI : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_menu_api)
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
}



