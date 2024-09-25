package com.example.nlcs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class UsageTracker(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UsageData", Context.MODE_PRIVATE)
    private val db = FirebaseFirestore.getInstance() // Firestore instance

    // Lưu thời gian sử dụng một chức năng vào SharedPreferences và Firestore

    fun addUsageTime(feature: String, seconds: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid  // Lấy userId từ Firebase Authentication

        if (userId != null) {
            val currentUsage = sharedPreferences.getInt(feature, 0)
            val newUsage = currentUsage + seconds
            sharedPreferences.edit().putInt(feature, newUsage).apply()

            Log.d("UsageTracker", "Feature: $feature, Added Time: $seconds giây, New Total: $newUsage giây")

            // Tính toán tổng số phút và tổng số giờ
            val totalMinutes = newUsage / 60
            val totalHours = newUsage / 3600

            // Lưu thời gian sử dụng vào Firestore
            val usageData = hashMapOf(
                "userId" to userId,  // Lưu userId
                "feature" to feature,
                "total_usage_seconds" to newUsage,  // Lưu tổng thời gian sử dụng bằng giây
                "total_usage_minutes" to totalMinutes,  // Lưu tổng thời gian sử dụng bằng phút
                "total_usage_hours" to totalHours,  // Lưu tổng thời gian sử dụng bằng giờ
                "date" to SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())  // Ngày hiện tại
            )

            // Lưu dữ liệu vào Firestore ở collection usage_times
            db.collection("usage_times")
                .add(usageData)
                .addOnSuccessListener { documentReference ->
                    Log.d("UsageTracker", "Usage data saved with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("UsageTracker", "Error saving usage data", e)
                }

            // Lưu dữ liệu đã sắp xếp vào Firestore
            saveSortedUsageDataToFirestore()  // Gọi hàm này sau khi thêm thời gian sử dụng
        } else {
            Log.w("UsageTracker", "User not logged in, cannot save usage data.")
        }
    }


    // Lưu dữ liệu đã sắp xếp vào Firestore
    fun saveSortedUsageDataToFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid  // Lấy userId từ Firebase Authentication

        if (userId != null) {
            // Lấy ngày hiện tại
            val currentDate = System.currentTimeMillis()
            val sharedPrefs = sharedPreferences.getLong("last_usage_date", 0)

            // Kiểm tra xem có phải ngày mới không
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = sharedPrefs
            val lastDate = calendar.get(Calendar.DAY_OF_YEAR)

            calendar.timeInMillis = currentDate
            val today = calendar.get(Calendar.DAY_OF_YEAR)

            // Nếu ngày đã thay đổi, reset dữ liệu
            if (lastDate != today) {
                sharedPreferences.edit().clear().apply() // Reset dữ liệu
                sharedPreferences.edit().putLong("last_usage_date", currentDate).apply() // Cập nhật ngày mới
            }

            val sortedUsageData = getSortedUsageData()

            val dataToSave = sortedUsageData.map { (feature, time) ->
                hashMapOf(
                    "userId" to userId,  // Lưu userId
                    "feature" to feature,
                    "total_usage_seconds" to time,
                    "date" to SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),  // Ngày hiện tại
//                    "timestamp" to System.currentTimeMillis()  // Thời gian hiện tại từ thiết bị
                )
            }

            dataToSave.forEach { usageData ->
                db.collection("sorted_usage_times")
                    .add(usageData)
                    .addOnSuccessListener { documentReference ->
                        Log.d("UsageTracker", "Sorted usage data saved with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("UsageTracker", "Lỗi khi lưu dữ liệu sử dụng đã sắp xếp", e)
                    }
            }

            // Cập nhật ngày cuối cùng đã sử dụng
            sharedPreferences.edit().putLong("last_usage_date", currentDate).apply()
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lưu dữ liệu sử dụng đã sắp xếp.")
        }
    }


    // Lấy dữ liệu sử dụng từ SharedPreferences và trả về một Map (vẫn theo giây)
    fun getUsageData(): Map<String, Int> {
        val data = sharedPreferences.all.filterKeys { it in listOf("Hẹn giờ tập trung", "Ghi chú", "Sơ đồ tư duy", "Thẻ ghi nhớ", "Nhắc nhở") }
            .mapValues { (_, value) -> value as Int } // Giá trị lưu là giây
        Log.d("UsageTracker", "Usage Data: $data")
        return data
    }


    // Trả về dữ liệu sử dụng theo thứ tự giảm dần (dữ liệu là giây)
    fun getSortedUsageData(): List<Pair<String, Int>> {
        return sharedPreferences.all.filterKeys { it in listOf("Hẹn giờ tập trung", "Ghi chú", "Sơ đồ tư duy", "Thẻ ghi nhớ", "Nhắc nhở") }
            .mapValues { (_, value) -> value as Int } // Giá trị lưu là giây
            .toList()
            .sortedByDescending { (_, value) -> value }  // Sắp xếp theo giá trị giảm dần
    }

    // Các hàm hỗ trợ lấy dữ liệu riêng lẻ
    fun getFocusModeUsageData(): Int {
        return sharedPreferences.getInt("Hẹn giờ tập trung", 0)
    }

    fun getNoteUsageData(): Int {
        return sharedPreferences.getInt("Ghi chú", 0)
    }

    fun getMindMapUsageData(): Int {
        return sharedPreferences.getInt("Sơ đồ tư duy", 0)
    }

    fun getFlashCardUsageData(): Int {
        return sharedPreferences.getInt("Thẻ ghi nhớ", 0)
    }

    fun getReminderUsageData(): Int {
        return sharedPreferences.getInt("Nhắc nhở", 0)
    }

    // Trả về dữ liệu sử dụng theo ngày, tuần, tháng, năm (có thể tùy chỉnh thêm)
    fun getDailyUsageData(): Map<String, Int> {
        return mapOf(
            "Hẹn giờ tập trung" to getFocusModeUsageData(),
            "Ghi chú" to getNoteUsageData(),
            "Sơ đồ tư duy" to getMindMapUsageData(),
            "Thẻ ghi nhớ" to getFlashCardUsageData(),
            "Nhắc nhở" to getReminderUsageData()
        )
    }

    fun getWeeklyUsageData(): Map<String, Int> {
        return mapOf(
            "Hẹn giờ tập trung" to getFocusModeUsageData(),
            "Ghi chú" to getNoteUsageData(),
            "Sơ đồ tư duy" to getMindMapUsageData(),
            "Thẻ ghi nhớ" to getFlashCardUsageData(),
            "Nhắc nhở" to getReminderUsageData()
        )
    }

    fun getMonthlyUsageData(): Map<String, Int> {
        return mapOf(
            "Hẹn giờ tập trung" to getFocusModeUsageData(),
            "Ghi chú" to getNoteUsageData(),
            "Sơ đồ tư duy" to getMindMapUsageData(),
            "Thẻ ghi nhớ" to getFlashCardUsageData(),
            "Nhắc nhở" to getReminderUsageData()
        )
    }

    fun getYearlyUsageData(): Map<String, Int> {
        return mapOf(
            "Hẹn giờ tập trung" to getFocusModeUsageData(),
            "Ghi chú" to getNoteUsageData(),
            "Sơ đồ tư duy" to getMindMapUsageData(),
            "Thẻ ghi nhớ" to getFlashCardUsageData(),
            "Nhắc nhở" to getReminderUsageData()
        )
    }

    //  tìm kiếm dữ liệu dựa trên khoảng ngày
    fun getUsageDataBetweenDates(startDate: Date, endDate: Date, callback: (Map<String, Int>) -> Unit) {
        val usageData = mutableMapOf<String, Int>()

        // Format the dates to match the format stored in Firestore
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val start = dateFormat.format(startDate)
        val end = dateFormat.format(endDate)

        // Query Firestore for data between start and end dates
        db.collection("usage_times")
            .whereGreaterThanOrEqualTo("date", start)
            .whereLessThanOrEqualTo("date", end)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val featureName = document.getString("feature") ?: continue
                    val usageTime = document.getLong("total_usage_seconds")?.toInt() ?: 0

                    // Add or accumulate the usage time for each feature
                    usageData[featureName] = usageData.getOrDefault(featureName, 0) + usageTime
                }

                // Log the retrieved data
                Log.d("FirestoreData", "Retrieved data: $usageData")
                callback(usageData)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error retrieving data: ${e.message}")
                callback(usageData) // Return empty data if error occurs
            }
    }

    fun getDailyUsageTotal(callback: (Map<String, Int>) -> Unit) {
        val usageData = mutableMapOf<String, Int>()

        // Lấy ngày hiện tại
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        // Query Firestore để lấy dữ liệu cho ngày hiện tại
        db.collection("usage_times")
            .whereEqualTo("date", currentDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val featureName = document.getString("feature") ?: continue
                    val usageTime = document.getLong("total_usage_seconds")?.toInt() ?: 0

                    // Cộng dồn thời gian sử dụng cho mỗi chức năng
                    usageData[featureName] = usageData.getOrDefault(featureName, 0) + usageTime
                }

                // Log dữ liệu đã lấy
                Log.d("DailyUsageData", "Retrieved daily usage data: $usageData")
                callback(usageData)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error retrieving daily usage data: ${e.message}")
                callback(usageData) // Trả về dữ liệu rỗng nếu có lỗi
            }
    }

}
