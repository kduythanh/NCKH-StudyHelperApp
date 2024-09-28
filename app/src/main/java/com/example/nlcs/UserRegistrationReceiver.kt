package com.example.nlcs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UserRegistrationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val userId = intent.getStringExtra("USER_ID")
        // Lưu userID vào SharedPreferences
        if (userId != null) {
            val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("userID", userId).apply()
            Log.d("UserRegistrationReceiver", "Lưu userID: $userId")
        }
    }
}