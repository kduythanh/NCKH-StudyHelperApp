package com.example.nlcs

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.databinding.ActivityTimeSelectionBinding
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

class TimeSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeSelectionBinding
    private lateinit var usageTracker: UsageTracker

    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()

    // Khai báo ProgressBar và RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: FeatureListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UsageTracker
        usageTracker = UsageTracker(this)

        // Set up toolbar
        setSupportActionBar(binding.toolbarTimeSelection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Quay lại"
        binding.toolbarTimeSelection.setNavigationOnClickListener {
            onBackPressed()
        }

        // Set up Date Picker
        setupDatePickers()

        // Set up Bar Chart
        setupBarChart()

        // Khởi tạo ProgressBar
        progressBar = binding.progressBar

        // Setup RecyclerView for displaying list of feature usage
        recyclerView = binding.featureListRecyclerView // Sử dụng binding để truy cập RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up "View Statistics" button
        binding.viewStatisticsButton.setOnClickListener {
            // Kiểm tra ngày bắt đầu và ngày kết thúc
            val currentDate = Calendar.getInstance()

            // Kiểm tra nếu ngày bắt đầu và/hoặc ngày kết thúc rỗng
            val startDateText = binding.startDateTextView.text.toString().trim()
            val endDateText = binding.endDateTextView.text.toString().trim()

            when {
                startDateText.isEmpty() && endDateText.isEmpty() -> {
                    showError("Vui lòng chọn cả ngày bắt đầu và ngày kết thúc.")
                    return@setOnClickListener
                }
                startDateText.isEmpty() -> {
                    showError("Vui lòng chọn ngày bắt đầu.")
                    return@setOnClickListener
                }
                endDateText.isEmpty() -> {
                    showError("Vui lòng chọn ngày kết thúc.")
                    return@setOnClickListener
                }
                startDate.after(endDate) -> {
                    showError("Ngày bắt đầu không được lớn hơn ngày kết thúc.")
                    return@setOnClickListener
                }
                endDate.after(currentDate) -> {
                    showError("Ngày kết thúc không được lớn hơn ngày hiện tại.")
                    return@setOnClickListener
                }
            }

            // Hiển thị ProgressBar
            progressBar.visibility = View.VISIBLE
            // Cập nhật biểu đồ và RecyclerView với khoảng thời gian đã chọn
            updateChartData()
        }


    }

    // Setup date pickers for selecting start and end dates
    private fun setupDatePickers() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Start Date Picker
        binding.startDateTextView.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    startDate.set(year, month, dayOfMonth)
                    binding.startDateTextView.text = dateFormat.format(startDate.time)
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // End Date Picker
        binding.endDateTextView.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    endDate.set(year, month, dayOfMonth)
                    binding.endDateTextView.text = dateFormat.format(endDate.time)
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    // Setup the Bar Chart
    private fun setupBarChart() {
        val barChart: BarChart = binding.barChartTimeSelection
        barChart.description.isEnabled = false // Disable description
        barChart.setDrawValueAboveBar(false) // Hide values on top of bars
        barChart.setDrawGridBackground(false) // Remove background grid
        barChart.setPinchZoom(false) // Disable pinch zoom
        barChart.setScaleEnabled(false) // Disable scaling
        barChart.legend.isEnabled = false // Hide legend

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // Hide grid lines on X axis
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


    private fun updateChartData() {
        // Chuyển đổi Calendar thành Date
        val startDateInDate = startDate.time
        val endDateInDate = endDate.time

        // Gọi hàm getUsageDataBetweenDates với callback
        usageTracker.getUsageDataBetweenDates(startDateInDate, endDateInDate) { usageData ->
            Log.d("UsageData", "Usage Data: $usageData") // Kiểm tra dữ liệu trả về

            // Ẩn ProgressBar khi dữ liệu đã được tải về
            progressBar.visibility = View.GONE

            if (usageData.isEmpty()) {
                // Handle no data case
                binding.barChartTimeSelection.clear()
                recyclerView.visibility = View.GONE // Ẩn RecyclerView nếu không có dữ liệu
                return@getUsageDataBetweenDates
            } else {
                recyclerView.visibility = View.VISIBLE // Hiện RecyclerView nếu có dữ liệu
            }

            // Xác định thứ tự chức năng
            val orderedKeys =
                listOf("Hẹn giờ tập trung", "Ghi chú", "Sơ đồ tư duy", "Thẻ ghi nhớ", "Nhắc nhở")

            // Tạo danh sách các entries theo thứ tự đã xác định
            val entries = orderedKeys.mapIndexed { index, feature ->
                BarEntry(
                    index.toFloat(),
                    usageData[feature]?.toFloat() ?: 0f
                ) // Sử dụng giá trị 0 nếu không có dữ liệu
            }

            val dataSet = BarDataSet(entries, "Feature Usage (giây)")
            dataSet.color = Color.parseColor("#FF5733")
            dataSet.setDrawValues(false)

            val barData = BarData(dataSet)
            binding.barChartTimeSelection.data = barData
            barData.barWidth = 0.6f

            binding.barChartTimeSelection.xAxis.valueFormatter =
                IndexAxisValueFormatter(orderedKeys)
            binding.barChartTimeSelection.invalidate()

            // Cập nhật RecyclerView với dữ liệu usageData
            // Tạo danh sách sortedFeatureList và sắp xếp theo thời gian sử dụng
            val sortedFeatureList = usageData.toList()
                .sortedByDescending { it.second } // Sắp xếp theo giá trị sử dụng, từ cao đến thấp

            adapter = FeatureListAdapter(sortedFeatureList)
            recyclerView.adapter = adapter
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
