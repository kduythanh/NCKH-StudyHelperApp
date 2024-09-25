package com.example.nlcs

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

        // Set up toolbar
        setSupportActionBar(binding.toolbarStatistics)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Về trang chủ"

        binding.toolbarStatistics.setNavigationOnClickListener {
            onBackPressed()
        }

        // Setup ProgressBar
        val progressBar: ProgressBar = findViewById(R.id.progressBar)

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

        // Get daily usage data and update UI
        getDailyUsageData(progressBar)
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

        val leftAxis = barChart.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return formatTime(value.toInt())
            }
        }

        barChart.axisRight.isEnabled = false
    }

    // Function to get daily usage data and update UI
    // Function to get daily usage data and update UI
    private fun getDailyUsageData(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE // Hiển thị ProgressBar
        usageTracker.getDailyUsageTotal { usageData ->
            // Cập nhật RecyclerView
            val sortedFeatureList = usageData.toList().sortedByDescending { it.second }
            adapter = FeatureListAdapter(sortedFeatureList)
            recyclerView.adapter = adapter

            // Cập nhật biểu đồ với dữ liệu hôm nay
            updateChartData(usageData)

            // Ẩn ProgressBar sau khi dữ liệu đã tải xong
            progressBar.visibility = View.GONE
        }
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

    // Function to update chart data based on daily usage data
    private fun updateChartData(usageData: Map<String, Int>) {
        if (usageData.isEmpty()) {
            // Handle when there is no data
            binding.barChart.clear()
            return
        }

        val entries = usageData.entries.mapIndexed { index, (key, value) ->
            BarEntry(index.toFloat(), value.toFloat())
        }

        val dataSet = BarDataSet(entries, "Feature Usage (giây)")  // Updated label to show time in seconds
        dataSet.color = Color.parseColor("#FF5733") // Màu cam
        dataSet.setDrawValues(false)

        val barData = BarData(dataSet)
        binding.barChart.data = barData

        // Thiết lập độ rộng cột bằng phương thức setBarWidth()
        barData.barWidth = 0.6f // Đặt độ rộng cột (giá trị từ 0.0 đến 1.0)

        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(usageData.keys.toList())
        binding.barChart.invalidate()
    }

    // Function to display current date and time
//    private fun displayCurrentDateTime() {
//        val currentDate = Calendar.getInstance().time
//        // Định dạng ngày tháng theo định dạng "dd/MM/yyyy"
//        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//        val formattedDate = dateFormat.format(currentDate)
//
//        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
//        val formattedTime = timeFormat.format(currentDate)
//
//        val currentDateTextView: TextView = findViewById(R.id.currentDateTextView)
//        currentDateTextView.text = "Ngày thống kê: " + formattedDate
//        val currentTimeTextView: TextView = findViewById(R.id.currentTimeTextView)
//        currentTimeTextView.text = "Thời điểm thống kê: " + formattedTime
//    }

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
        currentDateTextView.text = "Ngày thống kê: " + formattedDate

        val currentTimeTextView: TextView = findViewById(R.id.currentTimeTextView)
        currentTimeTextView.text = "Thời điểm thống kê: " + formattedTime
    }

}
