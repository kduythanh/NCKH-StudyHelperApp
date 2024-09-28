package com.example.nlcs

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // Thêm import cho SetOptions
import java.text.SimpleDateFormat
import java.util.*

// UsageTracker class: lớp theo dõi thời gian sử dụng
class UsageTracker(context: Context) {
    private val db = FirebaseFirestore.getInstance() // Firestore instance

    // Lưu thời gian sử dụng một chức năng vào Firestore
    fun addUsageTime(feature: String, seconds: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid  // Lấy userId từ Firebase Authentication

        if (userId != null) {
            val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val usageDocRef = db.collection("usage_times").document("$userId-$feature-$date")

            val updates = hashMapOf<String, Any>(
                "userId" to userId,
                "feature" to feature,
                "date" to date,
                "total_usage_seconds" to FieldValue.increment(seconds.toLong()),
                "total_usage_minutes" to FieldValue.increment(seconds.toLong() / 60),
                "total_usage_hours" to FieldValue.increment(seconds.toLong() / 3600)
            )

            // Sử dụng SetOptions.merge() để hợp nhất các trường thay vì ghi đè
            usageDocRef.set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("UsageTracker", "Cập nhật dữ liệu sử dụng thành công cho $feature")
                }
                .addOnFailureListener { e ->
                    Log.w("UsageTracker", "Lỗi khi cập nhật dữ liệu sử dụng", e)
                }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lưu dữ liệu sử dụng.")
        }
    }

    // Lưu dữ liệu đã sắp xếp lên Firestore
    fun saveSortedUsageDataToFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            getSortedUsageData { sortedUsageData ->
                sortedUsageData.forEach { (feature, time) ->
                    val usageData = hashMapOf(
                        "userId" to userId,
                        "feature" to feature,
                        "total_usage_seconds" to time,
                        "date" to date
                    )
                    db.collection("sorted_usage_times").add(usageData)
                        .addOnSuccessListener { documentReference ->
                            Log.d(
                                "UsageTracker",
                                "Lưu dữ liệu sử dụng đã sắp xếp với ID: ${documentReference.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w("UsageTracker", "Lỗi khi lưu dữ liệu sử dụng đã sắp xếp", e)
                        }
                }
            }
        } else {
            Log.w(
                "UsageTracker",
                "Người dùng chưa đăng nhập, không thể lưu dữ liệu sử dụng đã sắp xếp."
            )
        }
    }

    // Lấy dữ liệu sử dụng từ Firestore
    fun getUsageData(callback: (Map<String, Int>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

            db.collection("usage_times")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val usageData = mutableMapOf<String, Int>()
                    for (document in querySnapshot.documents) {
                        val feature = document.getString("feature") ?: continue
                        val usageSeconds = document.getLong("total_usage_seconds")?.toInt() ?: 0
                        usageData[feature] = usageSeconds
                    }
                    callback(usageData)
                }
                .addOnFailureListener { e ->
                    Log.e("UsageTracker", "Lỗi khi lấy dữ liệu sử dụng: ${e.message}")
                    callback(emptyMap())
                }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lấy dữ liệu sử dụng.")
            callback(emptyMap())
        }
    }

    // Lấy dữ liệu sử dụng đã sắp xếp từ Firestore
    fun getSortedUsageData(callback: (List<Pair<String, Int>>) -> Unit) {
        getUsageData { usageData ->
            val sortedUsageData = usageData.toList().sortedByDescending { (_, value) -> value }
            callback(sortedUsageData)
        }
    }

    // Lấy dữ liệu sử dụng từ Firestore cho một khoảng thời gian
    fun getUsageDataBetweenDates(
        startDate: Date,
        endDate: Date,
        callback: (Map<String, Int>) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val calendar = Calendar.getInstance()
            calendar.time = startDate

            val endCalendar = Calendar.getInstance()
            endCalendar.time = endDate

            val dates = mutableListOf<String>()
            while (!calendar.after(endCalendar)) {
                dates.add(dateFormat.format(calendar.time))
                calendar.add(Calendar.DATE, 1)
            }

            db.collection("usage_times")
                .whereEqualTo("userId", userId)
                .whereIn("date", dates)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val usageData = mutableMapOf<String, Int>()
                    for (document in querySnapshot.documents) {
                        val feature = document.getString("feature") ?: continue
                        val usageSeconds = document.getLong("total_usage_seconds")?.toInt() ?: 0
                        usageData[feature] = usageData.getOrDefault(feature, 0) + usageSeconds
                    }
                    callback(usageData)
                }
                .addOnFailureListener { e ->
                    Log.e("UsageTracker", "Lỗi khi lấy dữ liệu sử dụng: ${e.message}")
                    callback(emptyMap())
                }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lấy dữ liệu sử dụng.")
            callback(emptyMap())
        }
    }

    // Lấy dữ liệu sử dụng từ Firestore cho một ngày cụ thể
    fun getFirestoreUsageDataForDate(date: String, callback: (Map<String, Int>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            db.collection("usage_times")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val usageData = mutableMapOf<String, Int>()
                    for (document in querySnapshot.documents) {
                        val feature = document.getString("feature") ?: continue
                        val usageSeconds = document.getLong("total_usage_seconds")?.toInt() ?: 0
                        usageData[feature] = usageSeconds
                    }
                    callback(usageData)
                }
                .addOnFailureListener { e ->
                    Log.e("UsageTracker", "Lỗi khi lấy dữ liệu cho ngày $date: ${e.message}")
                    callback(emptyMap())
                }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lấy dữ liệu sử dụng.")
            callback(emptyMap())
        }
    }

    // Hàm tính tổng thời gian sử dụng của ngày hiện tại và hiển thị dưới dạng giờ, phút, giây
    fun getTotalUsageForToday(callback: (String) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

            db.collection("usage_times")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    var totalSeconds = 0
                    for (document in querySnapshot.documents) {
                        val usageSeconds = document.getLong("total_usage_seconds")?.toInt() ?: 0
                        totalSeconds += usageSeconds
                    }

                    // Chuyển đổi tổng giây thành giờ, phút, giây
                    val hours = totalSeconds / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60

                    // Tạo chuỗi hiển thị dựa trên giá trị giờ, phút, giây
                    val totalTimeFormatted = when {
                        hours > 0 -> "$hours giờ $minutes phút $seconds giây"
                        minutes > 0 -> "$minutes phút $seconds giây"
                        else -> "$seconds giây"
                    }

                    callback(totalTimeFormatted)
                }
                .addOnFailureListener { e ->
                    Log.e("UsageTracker", "Lỗi khi lấy dữ liệu sử dụng: ${e.message}")
                    callback("0 giây")
                }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lấy dữ liệu sử dụng.")
            callback("0 giây")
        }
    }

    // Hàm tính tổng thời gian sử dụng của một ngày cụ thể
    fun getTotalUsageForSpecificDay(date: String, callback: (Int) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            db.collection("usage_times")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    var totalSeconds = 0
                    for (document in querySnapshot.documents) {
                        val usageSeconds = document.getLong("total_usage_seconds")?.toInt() ?: 0
                        totalSeconds += usageSeconds
                    }
                    callback(totalSeconds)
                }
                .addOnFailureListener { e ->
                    Log.e("UsageTracker", "Lỗi khi lấy dữ liệu sử dụng cho ngày $date: ${e.message}")
                    callback(0)
                }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lấy dữ liệu sử dụng.")
            callback(0)
        }
    }


    // Hàm lấy tổng thời gian sử dụng trong tuần hiện tại
    fun getWeeklyUsage(callback: (Map<String, Int>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val weeklyData = mutableMapOf<String, Int>()  // Dùng để lưu tổng thời gian của mỗi ngày

        if (userId != null) {
            // Lấy dữ liệu cho từng ngày trong tuần hiện tại
            val calendar = Calendar.getInstance()
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            for (i in 0..6) {  // Duyệt qua 7 ngày trong tuần
                val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
                Log.d("getWeeklyUsage", "Lấy dữ liệu cho ngày: $date") // Log thông tin ngày đang xử lý

                // Truy vấn Firestore để lấy dữ liệu cho từng ngày
                db.collection("usage_times")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", date)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        var totalSeconds = 0
                        for (document in querySnapshot.documents) {
                            totalSeconds += document.getLong("total_usage_seconds")?.toInt() ?: 0
                        }

                        // Ghi lại tổng số giây sử dụng của ngày đó
                        weeklyData[date] = totalSeconds
                        Log.d("getWeeklyUsage", "Ngày: $date, Tổng giây: $totalSeconds")

                        // Khi hoàn tất 7 ngày, gọi callback với dữ liệu đã tính toán
                        if (weeklyData.size == 7) {  // Đảm bảo đã tính toán đủ 7 ngày
                            Log.d("getWeeklyUsage", "Dữ liệu hoàn thành: $weeklyData")
                            callback(weeklyData)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("getWeeklyUsage", "Lỗi khi lấy dữ liệu cho ngày $date: ${e.message}")
                        callback(emptyMap())  // Trả về Map rỗng nếu có lỗi
                    }

                // Di chuyển đến ngày tiếp theo
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        } else {
            Log.w("getWeeklyUsage", "Người dùng chưa đăng nhập.")
            callback(emptyMap())  // Trả về Map rỗng nếu người dùng chưa đăng nhập
        }
    }


    // Hàm thống kê thời gian sử dụng theo tháng (12 tháng trong năm)
    fun getMonthlyUsage(callback: (Map<String, Int>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val dateFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()

            // Lấy danh sách 12 tháng trong năm
            val months = mutableListOf<String>()
            for (i in 0..11) {
                months.add(dateFormat.format(calendar.time))
                calendar.add(Calendar.MONTH, -1)  // Lùi về 1 tháng
            }

            db.collection("usage_times")
                .whereEqualTo("userId", userId)
                .whereIn("date", months)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val usageData = mutableMapOf<String, Int>()
                    for (document in querySnapshot.documents) {
                        val month = document.getString("date")?.substring(3) ?: continue
                        val usageSeconds = document.getLong("total_usage_seconds")?.toInt() ?: 0
                        usageData[month] = usageData.getOrDefault(month, 0) + usageSeconds
                    }
                    callback(usageData)
                }
                .addOnFailureListener { e ->
                    Log.e("UsageTracker", "Lỗi khi lấy dữ liệu sử dụng cho tháng: ${e.message}")
                    callback(emptyMap())
                }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lấy dữ liệu sử dụng.")
            callback(emptyMap())
        }
    }

    // Hàm trong UsageTracker để lấy dữ liệu chi tiết theo ngày
    fun getDetailedUsageForDay(date: String, callback: (Map<String, Int>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            db.collection("usage_times")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val featureData = mutableMapOf<String, Int>()
                    for (document in querySnapshot.documents) {
                        val feature = document.getString("feature") ?: continue
                        val usageSeconds = document.getLong("total_usage_seconds")?.toInt() ?: 0

                        // Kiểm tra nếu tính năng đã có trong map, cộng dồn thời gian
                        featureData[feature] = featureData.getOrDefault(feature, 0) + usageSeconds
                    }
                    callback(featureData)
                }
                .addOnFailureListener { e ->
                    Log.e("UsageTracker", "Lỗi khi lấy dữ liệu chi tiết cho ngày $date: ${e.message}")
                    callback(emptyMap())
                }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lấy dữ liệu chi tiết.")
            callback(emptyMap())
        }
    }


    fun getTotalUsageBetweenDates(
        startDate: Date,
        endDate: Date,
        callback: (Int) -> Unit
    ) {
        getUsageDataBetweenDates(startDate, endDate) { usageData ->
            val totalSeconds = usageData.values.sum()  // Tính tổng thời gian sử dụng (giây)
            callback(totalSeconds)
        }
    }


    fun getPreviousWeeksUsage(weeks: Int, callback: (List<Int>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val weeksData = MutableList(weeks) { 0 }
            var callbacksReceived = 0

            val calendar = Calendar.getInstance()

            for (week in 0 until weeks) {
                val weekStart = calendar.clone() as Calendar
                weekStart.firstDayOfWeek = Calendar.MONDAY
                weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                weekStart.add(Calendar.WEEK_OF_YEAR, -week)

                val weekEnd = weekStart.clone() as Calendar
                weekEnd.add(Calendar.DAY_OF_WEEK, 6)

                val weekIndex = weeks - week - 1  // Correct index mapping

                getTotalUsageBetweenDates(weekStart.time, weekEnd.time) { totalUsage ->
                    weeksData[weekIndex] = totalUsage
                    callbacksReceived += 1

                    if (callbacksReceived == weeks) {
                        callback(weeksData)
                    }
                }
            }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập.")
            callback(emptyList())
        }
    }

    // Hàm lấy dữ liệu chi tiết cho từng tính năng trong một tuần
    fun getDetailedUsageForWeek(startDate: String, endDate: String, callback: (Map<String, Int>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Lấy danh sách các ngày từ startDate đến endDate
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()

            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            if (start != null && end != null) {
                calendar.time = start
                val datesInWeek = mutableListOf<String>()
                while (!calendar.after(end)) {
                    datesInWeek.add(dateFormat.format(calendar.time)) // Thêm ngày vào danh sách
                    calendar.add(Calendar.DATE, 1) // Chuyển sang ngày tiếp theo
                }

                db.collection("usage_times")
                    .whereEqualTo("userId", userId)
                    .whereIn("date", datesInWeek) // Lọc theo các ngày trong tuần
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val featureData = mutableMapOf<String, Int>()
                        for (document in querySnapshot.documents) {
                            val feature = document.getString("feature") ?: continue
                            val usageSeconds = document.getLong("total_usage_seconds")?.toInt() ?: 0

                            // Cộng dồn thời gian sử dụng cho từng tính năng
                            featureData[feature] = featureData.getOrDefault(feature, 0) + usageSeconds
                        }
                        callback(featureData)
                    }
                    .addOnFailureListener { e ->
                        Log.e("UsageTracker", "Lỗi khi lấy dữ liệu chi tiết cho tuần từ $startDate đến $endDate: ${e.message}")
                        callback(emptyMap())
                    }
            } else {
                Log.e("UsageTracker", "Lỗi định dạng ngày startDate hoặc endDate.")
                callback(emptyMap())
            }
        } else {
            Log.w("UsageTracker", "Người dùng chưa đăng nhập, không thể lấy dữ liệu chi tiết.")
            callback(emptyMap())
        }
    }



}
