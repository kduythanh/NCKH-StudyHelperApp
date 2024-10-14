package com.example.nlcs

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.databinding.ActivityStatisticsBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var usageTracker: UsageTracker

    // List of features and usage times
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeatureListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UsageTracker
        usageTracker = UsageTracker(this)

        // Setup RecyclerView for displaying list of feature usage
        recyclerView = findViewById(R.id.featureListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load sorted usage data and set it to the adapter
        usageTracker.getSortedUsageData { sortedFeatureList ->
            adapter = FeatureListAdapter(sortedFeatureList)  // Time stored in seconds
            recyclerView.adapter = adapter
        }

        // Set up toolbar
        setSupportActionBar(binding.toolbarStatistics)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        binding.toolbarStatistics.setNavigationOnClickListener {
            onBackPressed()
        }

        // Setup Bar Chart
        setupBarChart()

        // Display current date and time
        displayCurrentDateTime()

        // Setup Search Button
        val searchButton: Button = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            // Navigate to Time Selection Activity
            val intent = Intent(this, TimeSelectionActivity::class.java)
            startActivity(intent)
        }
        // Setup Track Progress Button
        val trackProgressButton = findViewById<Button>(R.id.trackProgressButton)
        trackProgressButton.setOnClickListener {
            val intent = Intent(this, TrackingProgressActivity::class.java)
            startActivity(intent)
        }

        // Setup Total Usage Current Time
        val totalTimeTextView = findViewById<TextView>(R.id.totalTimeTextView)
        usageTracker.getTotalUsageForToday { totalTime ->
            val prefixText = "Tổng thời gian sử dụng: "
            val totalText = "$totalTime"  // Ví dụ: "11 giờ 20 phút 8 giây"

            // Kết hợp chuỗi tổng
            val fullText = "$prefixText$totalText"

            // Tạo SpannableString từ fullText
            val spannableString = SpannableString(fullText)

            // Đổi màu cho phần "11 giờ 20 phút 8 giây" (totalText)
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.another_color)) // Màu khác
            spannableString.setSpan(
                colorSpan,
                prefixText.length,  // Bắt đầu từ sau phần "Tổng thời gian sử dụng: "
                fullText.length,    // Đến hết chuỗi
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Hiển thị SpannableString trong TextView
            totalTimeTextView.text = spannableString
        }

    }

    // Function to setup the Bar Chart
    private fun setupBarChart() {
        val barChart: BarChart = binding.barChart
        barChart.description.isEnabled = false // chặn hiển thị mô tả
        barChart.setDrawValueAboveBar(false) // ẩn giá trị trên thanh
        barChart.setDrawGridBackground(false)  // ẩn nền lưới
        barChart.setPinchZoom(false) // ẩn zoom bằng cử chỉ
        barChart.setScaleEnabled(false) // ẩn zoom bằng thu phóng
        barChart.legend.isEnabled = false // ẩn chú thích

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // ẩn lưới theo trục x
        xAxis.granularity = 1f
        xAxis.axisLineWidth = 1f // Đặt độ dày cho đường kẻ của trục X
        xAxis.axisLineColor = resources.getColor(R.color.black) // Đặt màu cho đường kẻ

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        yAxisLeft.spaceTop = 15f
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisLineWidth = 1f // Đặt độ dày cho đường kẻ của trục Y
        yAxisLeft.axisLineColor = resources.getColor(R.color.black) // Đặt màu cho đường kẻ
        yAxisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return formatTime(value.toInt())
            }
        }

        barChart.axisRight.isEnabled = false

        // Load initial data
        updateChartData()

    }

    // Helper function to format time into hours, minutes, and seconds
    private fun formatTime(seconds: Int): String {
        return when {
            seconds >= 3600 -> {
                val hours = seconds / 3600
                val remainingMinutes = (seconds % 3600) / 60
                val remainingSeconds = seconds % 60
                if (remainingMinutes == 0 && remainingSeconds == 0) {
                    "$hours giờ"
                } else if (remainingMinutes > 0 && remainingSeconds == 0) {
                    "$hours giờ $remainingMinutes phút"
                } else {
                    "${hours}g ${remainingMinutes}p $remainingSeconds giây"
                }
            }

            seconds >= 60 -> {
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                if (remainingSeconds == 0) {
                    "$minutes phút"
                } else {
                    "${minutes}p $remainingSeconds giây"
                }
            }

            else -> {
                "$seconds giây"
            }
        }
    }

    // Function to update chart data based on selected time frame
    private fun updateChartData() {
        // Hiển thị lớp mờ và tiến trình
        val loadingOverlay = findViewById<View>(R.id.loadingOverlay)
        val progressContainer = findViewById<ConstraintLayout>(R.id.progressContainer)

        // Hiển thị khi tải dữ liệu
        loadingOverlay.visibility = View.VISIBLE
        progressContainer.visibility = View.VISIBLE



        // Tạo bo góc cho biểu đồ
        val barChart: BarChart = binding.barChart
        barChart.renderer =
            CylinderBarChartRenderer(barChart, barChart.animator, barChart.viewPortHandler)

        // Lấy dữ liệu sử dụng từ UsageTracker
        usageTracker.getUsageData { usageData ->
            // Sau khi tải dữ liệu hoàn tất, ẩn chúng
            loadingOverlay.visibility = View.GONE
            progressContainer.visibility = View.GONE

            // Danh sách chức năng theo thứ tự ưu tiên
            val orderedKeys = listOf(
                "Hẹn giờ tập trung",
                "Ghi chú",
                "Sơ đồ tư duy",
                "Thẻ ghi nhớ",
                "Nhắc nhở"
            )

            // Đảm bảo rằng mỗi mục trong orderedKeys đều có trong filteredUsageData, kể cả khi thời gian sử dụng là 0
            val filteredUsageData = orderedKeys.associateWith { key ->
                usageData[key] ?: 0
            }

            // Kiểm tra nếu không có dữ liệu
            if (filteredUsageData.values.all { it == 0 }) {
                binding.barChart.clear()
            } else {
                // Chuyển đổi dữ liệu thành danh sách các BarEntry
                val entries = filteredUsageData.entries.mapIndexed { index, (key, value) ->
                    BarEntry(index.toFloat(), value.toFloat())
                }

                // Tạo BarDataSet với danh sách entries và đặt nhãn
                val dataSet = BarDataSet(entries, "Feature Usage (giây)")
                dataSet.color = Color.parseColor("#F7BEBE")
                dataSet.setDrawValues(false)

                // Tạo BarData và thiết lập dữ liệu cho biểu đồ
                val barData = BarData(dataSet)
                binding.barChart.data = barData
                barData.barWidth = 0.6f

                // Cài đặt trình định dạng trục X để hiển thị tên các tính năng
                binding.barChart.xAxis.valueFormatter =
                    IndexAxisValueFormatter(filteredUsageData.keys.toList())

                // Làm mới biểu đồ
                binding.barChart.invalidate()
            }
        }
    }


    // Function to display current date and time
    private fun displayCurrentDateTime() {
        val currentDate = Calendar.getInstance().time

        // Định dạng ngày tháng theo định dạng "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        // Lấy giờ, phút và giây
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        // Định dạng thời gian
        val formattedTime = "$hours giờ $minutes phút $seconds giây"

        val currentDateTextView: TextView = findViewById(R.id.currentDateTextView)
        currentDateTextView.text = "Thống kê ngày : " + formattedDate

        val currentTimeTextView: TextView = findViewById(R.id.currentTimeTextView)
        currentTimeTextView.text = "Cập nhật lúc: " + formattedTime
    }
}
