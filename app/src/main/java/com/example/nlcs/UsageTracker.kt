package com.example.nlcs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.*
import kotlin.random.Random

class UsageTracker(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UsageData", Context.MODE_PRIVATE)

//    fun addUsageTime(feature: String, minutes: Int) {
//        val currentUsage = sharedPreferences.getInt(feature, 0)
//        sharedPreferences.edit().putInt(feature, currentUsage + minutes).apply()
//        // sharedPreferences.edit().clear().apply()

//    }

    fun addUsageTime(feature: String, minutes: Int) {
        val currentUsage = sharedPreferences.getInt(feature, 0)
        val newUsage = currentUsage + minutes
        sharedPreferences.edit().putInt(feature, newUsage).apply()
        Log.d("UsageTracker", "Feature: $feature, Added Time: $minutes, New Total: $newUsage")
    }


//    fun getUsageData(): Map<String, Int> {
//        return sharedPreferences.all.filterKeys { it == "FocusMode" || it == "Note" || it == "MindMap" || it == "FlashCard"}
//            .mapValues { (_, value) -> value as Int }
//    }

    fun getUsageData(): Map<String, Int> {
        val data = sharedPreferences.all.filterKeys { it in listOf("FocusMode", "Note", "MindMap", "FlashCard") }
            .mapValues { (_, value) -> value as Int }
        Log.d("UsageTracker", "Usage Data: $data")
        return data
    }

    fun getFocusModeUsageData(): Int {
        return sharedPreferences.getInt("FocusMode", 0)
    }

    // Lấy dữ liệu sử dụng cho Note
    fun getNoteUsageData(): Int {
        return sharedPreferences.getInt("Note", 0)
    }

    fun getMindMapUsageData(): Int {
        return sharedPreferences.getInt("MindMap", 0)
    }

    fun getFlashCardUsageData(): Int {
        return sharedPreferences.getInt("FlashCard", 0)
    }

    fun getDailyUsageData(): Map<String, Int> {
        val hours = listOf("12AM", "3AM", "6AM", "9AM", "12PM", "3PM", "6PM", "9PM")
        return mapOf(
            "FocusMode" to getFocusModeUsageData(),
            "Note" to getNoteUsageData(),
            "MindMap" to getMindMapUsageData(),
            "FlashCard" to getFlashCardUsageData()
        )
    }

    fun getWeeklyUsageData(): Map<String, Int> {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        // Implement weekly data retrieval
        return mapOf(
            "FocusMode" to getFocusModeUsageData(),
            "Note" to getNoteUsageData(),
            "MindMap" to getMindMapUsageData(),
            "FlashCard" to getFlashCardUsageData()
        )
    }

    fun getMonthlyUsageData(): Map<String, Int> {
        val weeks = listOf("Week 1", "Week 2", "Week 3", "Week 4")
        // Implement monthly data retrieval
        return mapOf(
            "FocusMode" to getFocusModeUsageData(),
            "Note" to getNoteUsageData(),
            "MindMap" to getMindMapUsageData(),
            "FlashCard" to getFlashCardUsageData()
        )
    }

    fun getYearlyUsageData(): Map<String, Int> {
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        // Implement yearly data retrieval
        return mapOf(
            "FocusMode" to getFocusModeUsageData(),
            "Note" to getNoteUsageData(),
            "MindMap" to getMindMapUsageData(),
            "FlashCard" to getFlashCardUsageData()
        )
    }

    // Helper method to get the current week of the month
    private fun getCurrentWeekOfMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.WEEK_OF_MONTH)
    }

    // Helper method to get the current month
    private fun getCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH)
    }

    // trả về dữ liệu đã được sắp xếp theo thời gian sử dụng.
    fun getSortedUsageData(): List<Pair<String, Int>> {
        return sharedPreferences.all.filterKeys { it in listOf("FocusMode", "Note", "MindMap", "FlashCard") }
            .mapValues { (_, value) -> value as Int }
            .toList()
            .sortedByDescending { (_, value) -> value }  // Sắp xếp theo giá trị giảm dần
    }

}