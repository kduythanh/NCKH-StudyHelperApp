package com.example.nlcs.preferen

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity

class UserSharePreferences(context: FragmentActivity) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user", Context.MODE_PRIVATE)

    //clear
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        //create variables
        const val KEY_CLASS_ID: String = "class_id"
    }
}
