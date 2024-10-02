package com.example.nlcs


import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityTrackingProgressBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener


class TrackingProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrackingProgressBinding

    val usageTracker = UsageTracker(this)
    lateinit var barChart: BarChart
    private lateinit var comparisonBarChart: BarChart

    // Khai báo ProgressBar và RecyclerView
    private lateinit var progressBar: ProgressBar


    // Biến để theo dõi trạng thái tải dữ liệu
    private var isWeeklyDataLoaded = false
    private var isComparisonDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)


        barChart = findViewById(R.id.weeklyBarChart)
        comparisonBarChart = findViewById(R.id.comparisonBarChart)

        // Set up toolbar
        setSupportActionBar(binding.toolbarStatistics)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbarStatistics.setNavigationOnClickListener {
            onBackPressed()
        }

        // Hiển thị ProgressBar và loading overlay khi bắt đầu tải dữ liệu
        showLoading(true)


        // Lấy dữ liệu biểu đồ và thiết lập listener cho BarChart
        getWeeklyUsageData()

        setupBarChartListener()

        // Thiết lập listener cho biểu đồ thứ 2 sau khi dữ liệu đã được cập nhật
        setupComparisonBarChartListener()

        // Lấy dữ liệu biểu đồ so sánh và thiết lập listener cho BarChart
        setupComparisonBarChart()
    }

    // Hàm tính toán ngày thứ 2 đến Chủ nhật của tuần này
    private fun getWeekDays(): List<String> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        // Đặt về đầu tuần (thứ 2)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val dateFormat =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) // Sử dụng định dạng dd-MM-yyyy

        val days = mutableListOf<String>()
        for (i in 0..6) { // Duyệt qua các ngày từ thứ 2 đến chủ nhật
            days.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1) // Chuyển sang ngày tiếp theo
        }

        return days
    }
    // Chỉnh định dạng ngày hiển thị
    private fun getWeekDaysForDisplay(): List<String> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY

        // Đặt về đầu tuần (thứ 2)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault()) // Hiển thị dd/MM

        val days = mutableListOf<String>()
        for (i in 0..6) { // Duyệt qua các ngày từ thứ 2 đến chủ nhật
            days.add(dateFormat.format(calendar.time)) // Thêm ngày vào danh sách
            calendar.add(Calendar.DAY_OF_MONTH, 1) // Chuyển sang ngày tiếp theo
        }

        return days
    }

    private fun getWeeklyUsageData() {
        val weekDays = getWeekDays()
        val weekDaysForDisplay = weekDays.map { day ->
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val parsedDate = dateFormat.parse(day)
            SimpleDateFormat("dd/MM", Locale.getDefault()).format(parsedDate!!)
        }
        val entries = mutableListOf<BarEntry>()

        usageTracker.getWeeklyUsage { weeklyData ->
            var totalWeekSeconds = 0

            weekDays.forEachIndexed { index, day ->
                val totalSeconds = weeklyData[day] ?: 0
                totalWeekSeconds += totalSeconds
                val totalHours = totalSeconds / 3600f
                entries.add(BarEntry(index.toFloat(), totalHours))
            }

            val averageTime = totalWeekSeconds / 7
            displayWeeklySummary(totalWeekSeconds, averageTime)
            updateBarChartWithData(entries, weekDaysForDisplay, averageTime / 3600f)

            // Đánh dấu rằng biểu đồ tuần đã tải xong
            isWeeklyDataLoaded = true // Đánh dấu là dữ liệu của biểu đồ tuần đã được tải xong
            checkIfDataLoaded() // Kiểm tra xem cả hai dữ liệu đã tải xong chưa
        }
    }

    // Hàm hiển thị tổng thời gian sử dụng của tuần và trung bình cộng
    // Hàm hiển thị tổng thời gian sử dụng của tuần và định dạng ngày tháng
    private fun displayWeeklySummary(totalSeconds: Int, averageSeconds: Int) {
        val totalTimeFormatted = formatTime(totalSeconds)
        val averageTimeFormatted = formatTime(averageSeconds)

        // Lấy các TextView
        val totalTextView = findViewById<TextView>(R.id.totalWeekTimeTextView)
        val averageTextView = findViewById<TextView>(R.id.averageWeekTimeTextView)

        // Định dạng chuỗi cho tổng thời gian sử dụng
        val totalPrefix = "Tổng thời gian sử dụng: "
        val totalSpannable = SpannableString(totalPrefix + totalTimeFormatted)
        totalSpannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.total_usage_time)), 0, totalPrefix.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        totalSpannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.orange_500)), totalPrefix.length, totalSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Định dạng chuỗi cho trung bình thời gian sử dụng
        val averagePrefix = "Trung bình thời gian sử dụng: "
        val averageSpannable = SpannableString(averagePrefix + averageTimeFormatted)
        averageSpannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.total_usage_time)), 0, averagePrefix.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        averageSpannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.orange_500)), averagePrefix.length, averageSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Cập nhật TextView với chuỗi đã định dạng
        totalTextView.text = totalSpannable
        averageTextView.text = averageSpannable

        // Lấy danh sách ngày đã định dạng để hiển thị
        val weekDaysDisplayWithYear = getWeekDays() // Lấy tuần với định dạng "dd-MM-yyyy"

        // Lấy ngày bắt đầu và ngày kết thúc của tuần
        val startDate = weekDaysDisplayWithYear.firstOrNull() ?: ""
        val endDate = weekDaysDisplayWithYear.lastOrNull() ?: ""

        // Cập nhật TextView để hiển thị tuần hiện tại dưới dạng "Từ: ngày bắt đầu - Đến: ngày kết thúc"
        findViewById<TextView>(R.id.weekSummaryTextView).text =
            "Tuần hiện tại: Từ $startDate - Đến $endDate"
    }

    // Hàm cập nhật dữ liệu lên biểu đồ
    private fun updateBarChartWithData(entries: List<BarEntry>, days: List<String>, averageHours: Float) {
        barChart.description.isEnabled = false
        barChart.setDrawValueAboveBar(false) // Không hiển thị dữ liệu trên cột
        barChart.setDrawGridBackground(false)
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.legend.isEnabled = false
        barChart.setDrawValueAboveBar(false) // ẩn giá trị trên thanh

        val barDataSet = BarDataSet(entries, "Thời gian học tập trong tuần")
        barDataSet.color = resources.getColor(R.color.blue_bei)

        // Điều chỉnh kích thước của nhãn để hiển thị tốt hơn khi nằm ngoài cột
        barDataSet.valueTextSize = 12f

        barDataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry?): String {
                val value = barEntry?.y ?: 0f
                return if (value == 0f) "" else formatTime((value * 3600).toInt())
            }
        }
        barDataSet.setDrawValues(false)

        val data = BarData(barDataSet)
        data.barWidth = 0.6f
        barChart.data = data

        // Thêm đường trung bình (average line)
        addAverageLine(averageHours)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(days)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 0f
        xAxis.setDrawGridLines(false)
        xAxis.axisLineWidth = 1.1f // Đặt độ dày cho đường kẻ của trục X
        xAxis.axisLineColor = resources.getColor(R.color.black) // Đặt màu cho đường kẻ

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setDrawLabels(true)
        yAxisLeft.axisLineWidth = 1f // Đặt độ dày cho đường kẻ của trục Y
        yAxisLeft.axisLineColor = resources.getColor(R.color.black) // Đặt màu cho đường kẻ

        val yAxisFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val totalSeconds = (value * 3600).toInt()
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                return when {
                    hours > 0 -> String.format("%d giờ", hours)
                    minutes > 0 -> String.format("%02d phút", minutes)
                    else -> String.format("%02d giây", seconds)
                }
            }
        }
        yAxisLeft.valueFormatter = yAxisFormatter
        barChart.axisRight.isEnabled = false

        barChart.invalidate()
    }

    // Hàm thêm đường trung bình vào biểu đồ
    private fun addAverageLine(averageHours: Float) {
        val limitLine = LimitLine(averageHours, "")
        limitLine.lineWidth = 2f
        limitLine.lineColor = resources.getColor(R.color.brightRed)  // Đặt màu đỏ cho đường trung bình
        limitLine.enableDashedLine(10f, 10f, 0f)  // Đặt kiểu nét đứt
        limitLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        limitLine.textSize = 12f

        // Thêm đường giới hạn vào trục Y
        val yAxisLeft = barChart.axisLeft
        yAxisLeft.addLimitLine(limitLine)
    }


    // Hàm tính toán giá trị tối đa cho trục Y dựa trên các entries
    private fun calculateYAxisMaximum(entries: List<BarEntry>): Float {
        val maxValue = entries.maxOfOrNull { it.y } ?: 0f
        // Đảm bảo tối thiểu là 1 và thêm một khoảng nhỏ để không gian trục Y không bị chật
        return if (maxValue < 1f) 1f else maxValue + 1f
    }

    // Hàm tính toán bước nhảy linh hoạt cho trục Y dựa trên giá trị tối đa
    private fun calculateGranularity(maxValue: Float): Float {
        return when {
            maxValue <= 1f -> 0.2f  // Nếu giá trị tối đa nhỏ hơn hoặc bằng 1 giờ, bước nhảy là 0.2 giờ (12 phút)
            maxValue <= 3f -> 0.5f  // Nếu giá trị tối đa nhỏ hơn hoặc bằng 3 giờ, bước nhảy là 0.5 giờ (30 phút)
            maxValue <= 6f -> 1f    // Nếu giá trị tối đa nhỏ hơn hoặc bằng 6 giờ, bước nhảy là 1 giờ
            else -> 2f              // Nếu giá trị tối đa lớn hơn 6 giờ, bước nhảy là 2 giờ
        }
    }

    // Hàm định dạng thời gian
    fun formatTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format("%d giờ %02d phút %02d giây", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d phút %02d giây", minutes, seconds)
            else -> String.format("%02d giây", seconds)
        }
    }

    // Hàm thêm listener cho BarChart
    private fun setupBarChartListener() {
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val xIndex = e.x.toInt()
                val daySelected = getWeekDays()[xIndex] // Lấy ngày được chọn từ danh sách tuần
                Log.d("Chart", "Ngày được chọn: $daySelected")

                // Lấy dữ liệu chi tiết từ Firestore cho ngày được chọn
                usageTracker.getDetailedUsageForDay(daySelected) { featureData ->
                    // Hiển thị dữ liệu trong một dialog
                    showFeatureDetailsDialog(daySelected, featureData)
                }
            }

            override fun onNothingSelected() {
                // Không làm gì khi không có cột nào được chọn
            }
        })
    }

    private fun showFeatureDetailsDialog(date: String, featureData: Map<String, Int>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chi tiết sử dụng cho ngày $date")

        // Tạo một LinearLayout để chứa các TextView cho từng feature
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER // Căn giữa toàn bộ bảng
        layout.setPadding(60, 16, 60, 16) // Tăng khoảng cách lề trái và phải so với bảng

        var totalTime = 0 // Biến để lưu tổng thời gian sử dụng

        // Sắp xếp featureData theo thời gian sử dụng giảm dần
        val sortedFeatureData = featureData.entries.sortedByDescending { it.value }

        sortedFeatureData.forEach { (feature, time) ->
            // Cộng dồn thời gian cho tổng
            totalTime += time

            // Tạo một dòng ngang cho từng feature
            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL

            // Tạo TextView cho tên feature
            val featureNameTextView = TextView(this)
            featureNameTextView.text = feature
            featureNameTextView.setPadding(16, 8, 16, 8)
            featureNameTextView.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ) // Để tên feature chiếm nhiều không gian hơn

            // Tạo TextView cho thời gian
            val featureTimeTextView = TextView(this)
            featureTimeTextView.text = formatTime(time)
            featureTimeTextView.gravity = Gravity.END // Căn phải thời gian
            featureTimeTextView.setPadding(16, 8, 16, 8)

            // Thêm TextView vào dòng
            rowLayout.addView(featureNameTextView)
            rowLayout.addView(featureTimeTextView)

            // Thêm dòng vào layout chính
            layout.addView(rowLayout)

            // Thêm đường kẻ dưới mỗi dòng (kể cả dòng cuối cùng)
            val divider = View(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2 // Độ dày của đường kẻ
            )
            params.setMargins(16, 0, 16, 0) // Tạo khoảng cách cho đường kẻ
            divider.layoutParams = params
            divider.setBackgroundColor(resources.getColor(R.color.black)) // Đặt màu cho đường kẻ

            // Thêm đường kẻ vào layout chính
            layout.addView(divider)
        }

        // Thêm TextView cho tổng thời gian sử dụng
        val totalTimeTextView = TextView(this)
        totalTimeTextView.text = "Tổng thời gian sử dụng: ${formatTime(totalTime)}"
        totalTimeTextView.setTypeface(null, android.graphics.Typeface.BOLD) // Tô đậm
        totalTimeTextView.setTextColor(resources.getColor(R.color.brightRed)) // Đổi màu chữ thành đỏ
        totalTimeTextView.gravity = Gravity.END // Căn phải
        totalTimeTextView.setPadding(0, 32, 64, 16) // Tăng khoảng cách lề phải và trái so với bảng

        // Thêm tổng thời gian vào layout chính
        layout.addView(totalTimeTextView)

        builder.setView(layout)
        builder.setPositiveButton("OK", null)
        builder.show()
    }



    // Hàm để hiển thị hoặc ẩn ProgressBar và loading overlay
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.visibility = View.VISIBLE
            binding.progressContainer.visibility = View.VISIBLE
        } else {
            binding.loadingOverlay.visibility = View.GONE
            binding.progressContainer.visibility = View.GONE
        }
    }

    // Hàm để thiết lập biểu đồ so sánh
    // Hàm để thiết lập biểu đồ so sánh tuần
    private fun setupComparisonBarChart() {
        // Lấy dữ liệu sử dụng của các tuần trước và tuần hiện tại
        usageTracker.getPreviousWeeksUsage(4) { weeksUsageData ->
            if (weeksUsageData.isNotEmpty()) {
                // Khởi tạo danh sách để lưu trữ các BarEntry
                val entries = mutableListOf<BarEntry>()

                // Nhãn cho trục X tương ứng với 4 tuần (3 tuần trước và tuần hiện tại)
                val weekLabels = listOf("3 tuần trước", "2 tuần trước", "1 tuần trước", "Tuần hiện tại")

                // Biến lưu tổng thời gian sử dụng và số tuần có dữ liệu
                var totalUsageAllWeeks = 0f
                var numberOfWeeksWithData = 0

                // Duyệt qua dữ liệu và tạo các mục BarEntry cho biểu đồ
                weeksUsageData.forEachIndexed { index, totalWeekUsage ->
                    val weekUsageInHours = totalWeekUsage / 3600f  // Chuyển đổi từ giây sang giờ

                    // Tính tổng thời gian sử dụng nếu có dữ liệu
                    if (weekUsageInHours > 0) {
                        totalUsageAllWeeks += weekUsageInHours
                        numberOfWeeksWithData++
                    }

                    // Thêm mục BarEntry vào danh sách (X là index của tuần, Y là giờ sử dụng)
                    entries.add(BarEntry(index.toFloat(), weekUsageInHours))
                }

                // Tính giá trị trung bình thời gian sử dụng giữa các tuần
                val averageUsage = if (numberOfWeeksWithData > 0) {
                    totalUsageAllWeeks / numberOfWeeksWithData
                } else {
                    0f  // Nếu không có dữ liệu, trung bình là 0
                }

                // Gọi hàm để cập nhật dữ liệu và hiển thị biểu đồ
                updateComparisonBarChartWithData(entries, weekLabels, averageUsage)

                // Hiển thị giá trị trung bình vào TextView
                val averageUsageTextView = findViewById<TextView>(R.id.averageUsageTextView)

                // Tạo SpannableString để thay đổi màu của từng phần
                val averageText = "Thời gian trung bình mỗi tuần: "
                val timeText = formatTime((averageUsage * 3600).toInt())

                val spannable = SpannableString(averageText + timeText)

                // Đặt màu xanh cho "Thời gian trung bình"
                spannable.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.total_usage_time)), 0, averageText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Đặt màu đỏ cho số thời gian trung bình
                spannable.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.brightRed)), averageText.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

// Cập nhật TextView với chuỗi đã định dạng
                averageUsageTextView.text = spannable
                // Đánh dấu rằng biểu đồ so sánh đã tải xong
                isComparisonDataLoaded = true
                checkIfDataLoaded()  // Kiểm tra xem cả hai biểu đồ đã tải xong chưa
            } else {
                Log.e("ComparisonBarChart", "Không có đủ dữ liệu để hiển thị biểu đồ.")
            }
        }
    }

    // Hàm cập nhật dữ liệu lên biểu đồ so sánh tuần
    private fun updateComparisonBarChartWithData(entries: List<BarEntry>, weekLabels: List<String>, averageHours: Float) {
        comparisonBarChart.description.isEnabled = false
        comparisonBarChart.setDrawValueAboveBar(false) // Không hiển thị dữ liệu trên cột
        comparisonBarChart.setDrawGridBackground(false)
        comparisonBarChart.setPinchZoom(false)
        comparisonBarChart.setScaleEnabled(false)
        comparisonBarChart.legend.isEnabled = false
        comparisonBarChart.setDrawValueAboveBar(false) // ẩn giá trị trên thanh

        // Tạo BarDataSet cho biểu đồ với dữ liệu tuần
        val barDataSet = BarDataSet(entries, "So sánh thời gian sử dụng theo tuần")
        barDataSet.color = resources.getColor(R.color.light_yellow)

        // Điều chỉnh kích thước của nhãn để hiển thị tốt hơn khi nằm ngoài cột
        barDataSet.valueTextSize = 12f

        barDataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry?): String {
                val value = barEntry?.y ?: 0f
                return if (value == 0f) "" else formatTime((value * 3600).toInt())
            }
        }
        barDataSet.setDrawValues(false)

        val data = BarData(barDataSet)
        data.barWidth = 0.6f
        comparisonBarChart.data = data

        // Thêm đường trung bình (average line) vào biểu đồ tuần
        addAverageLineToComparisonChart(averageHours)

        val xAxis = comparisonBarChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(weekLabels)  // Sử dụng nhãn tuần
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 0f
        xAxis.setDrawGridLines(false)
        xAxis.axisLineWidth = 1.1f // Đặt độ dày cho đường kẻ của trục X
        xAxis.axisLineColor = resources.getColor(R.color.black) // Đặt màu cho đường kẻ

        val yAxisLeft = comparisonBarChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setDrawLabels(true)
        yAxisLeft.axisLineWidth = 1f // Đặt độ dày cho đường kẻ của trục Y
        yAxisLeft.axisLineColor = resources.getColor(R.color.black) // Đặt màu cho đường kẻ

        // Định dạng trục Y để hiển thị thời gian dạng giờ/phút
        val yAxisFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val totalSeconds = (value * 3600).toInt()
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                return when {
                    hours > 0 -> String.format("%d giờ", hours)
                    minutes > 0 -> String.format("%02d phút", minutes)
                    else -> String.format("%02d giây", seconds)
                }
            }
        }
        yAxisLeft.valueFormatter = yAxisFormatter
        comparisonBarChart.axisRight.isEnabled = false

        comparisonBarChart.invalidate() // Vẽ lại biểu đồ
    }




    // Hàm để kiểm tra trạng thái tải dữ liệu
    private fun checkIfDataLoaded() {
        if (isWeeklyDataLoaded && isComparisonDataLoaded) {
            showLoading(false) // Ẩn ProgressBar khi cả hai dữ liệu đã tải xong
        }
    }

    // Hàm thêm listener cho biểu đồ so sánh
    private fun setupComparisonBarChartListener() {
        comparisonBarChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val weekIndex = e.x.toInt() // Lấy chỉ số của cột được chọn

                // Lấy dữ liệu của tuần từ biểu đồ thay vì gọi lại usageTracker
                val totalWeekUsage = comparisonBarChart.data.getDataSetByIndex(0).getEntryForIndex(weekIndex).y
                val totalWeekUsageInSeconds = (totalWeekUsage * 3600).toInt() // Chuyển đổi từ giờ sang giây

                // Lấy dữ liệu của tuần trước đó (nếu có)
                val previousWeekUsageInSeconds = if (weekIndex > 0) {
                    val previousWeekUsage = comparisonBarChart.data.getDataSetByIndex(0).getEntryForIndex(weekIndex - 1).y
                    (previousWeekUsage * 3600).toInt()
                } else {
                    null  // Nếu là tuần đầu tiên (3 tuần trước), không có tuần trước để so sánh
                }

                // Hiển thị thông tin tổng thời gian sử dụng của tuần và phần trăm thay đổi
                showWeekUsageDetailsDialog(weekIndex, totalWeekUsageInSeconds, previousWeekUsageInSeconds)
            }

            override fun onNothingSelected() {
                // Không làm gì khi không có cột nào được chọn
            }
        })
    }



    // Hàm hiển thị thông tin tổng thời gian sử dụng của tuần
    // Hàm hiển thị thông tin tổng thời gian sử dụng của tuần và so sánh với tuần trước
    // Hàm hiển thị thông tin tổng thời gian sử dụng của tuần và so sánh với tuần trước
    // Hàm hiển thị thông tin tổng thời gian sử dụng của tuần và so sánh với tuần trước
    private fun showWeekUsageDetailsDialog(weekIndex: Int, totalSeconds: Int, previousWeekSeconds: Int?) {
        val builder = AlertDialog.Builder(this)
        val weekLabel = when (weekIndex) {
            0 -> "3 tuần trước"
            1 -> "2 tuần trước"
            2 -> "1 tuần trước"
            3 -> "Tuần hiện tại"
            else -> "Không xác định"
        }

        builder.setTitle("Tổng thời gian sử dụng của $weekLabel")

        // Tạo LinearLayout để chứa TextView
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER // Căn giữa toàn bộ bảng
        layout.setPadding(60, 16, 60, 16) // Tăng khoảng cách lề trái và phải so với bảng

        // Tạo TextView hiển thị tổng thời gian sử dụng
        val totalTimeTextView = TextView(this)
        totalTimeTextView.text = "Tổng thời gian sử dụng: ${formatTime(totalSeconds)}"
        totalTimeTextView.setTypeface(null, android.graphics.Typeface.BOLD) // Tô đậm
        totalTimeTextView.setTextColor(resources.getColor(R.color.brightRed)) // Đổi màu chữ thành đỏ
        totalTimeTextView.gravity = Gravity.CENTER // Căn giữa văn bản

        // Thêm TextView vào layout
        layout.addView(totalTimeTextView)

        // Nếu có dữ liệu tuần trước đó, tính toán phần trăm thay đổi và chênh lệch thời gian
        previousWeekSeconds?.let {
            if (it > 0) {
                val changePercentage = ((totalSeconds - it) / it.toFloat()) * 100
                val timeDifference = totalSeconds - it // Chênh lệch thời gian (có thể âm hoặc dương)

                // Tạo TextView hiển thị phần trăm thay đổi
                val changeTextView = TextView(this)
                changeTextView.text = if (changePercentage >= 0) {
                    "Tăng ${String.format("%.2f", changePercentage)}% so với tuần trước"
                } else {
                    "Giảm ${String.format("%.2f", -changePercentage)}% so với tuần trước"
                }
                changeTextView.setTextColor(resources.getColor(if (changePercentage >= 0) R.color.total_usage_time else R.color.brightRed))
                changeTextView.gravity = Gravity.CENTER // Căn giữa văn bản

                // Thêm TextView vào layout
                layout.addView(changeTextView)

                // Tạo TextView hiển thị số giờ, phút, giây tăng hoặc giảm
                val timeDifferenceTextView = TextView(this)
                timeDifferenceTextView.text = if (timeDifference >= 0) {
                    "Nhiều hơn ${formatTime(timeDifference)} so với tuần trước"
                } else {
                    "Ít hơn ${formatTime(-timeDifference)} so với tuần trước"
                }
                timeDifferenceTextView.setTextColor(resources.getColor(if (timeDifference >= 0) R.color.total_usage_time else R.color.brightRed))
                timeDifferenceTextView.gravity = Gravity.CENTER

                // Thêm TextView hiển thị chênh lệch thời gian vào layout
                layout.addView(timeDifferenceTextView)
            } else {
                // Trường hợp tuần trước đó có dữ liệu nhưng là 0 giây
                val noPreviousDataTextView = TextView(this)
                noPreviousDataTextView.text = "Không có dữ liệu sử dụng tuần trước để so sánh."
                noPreviousDataTextView.gravity = Gravity.CENTER
                layout.addView(noPreviousDataTextView)
            }
        } ?: run {
            // Nếu không có dữ liệu của tuần trước (null)
            val noDataTextView = TextView(this)
            noDataTextView.text = "Không có dữ liệu của tuần trước để so sánh"
            noDataTextView.gravity = Gravity.CENTER
            layout.addView(noDataTextView)
        }

        builder.setView(layout)
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    // Hàm thêm đường trung bình vào biểu đồ so sánh
    private fun addAverageLineToComparisonChart(averageHours: Float) {
        val limitLine = LimitLine(averageHours, "")
        limitLine.lineWidth = 2f
        limitLine.lineColor = resources.getColor(R.color.brightRed)  // Đặt màu đỏ cho đường trung bình
        limitLine.enableDashedLine(10f, 10f, 0f)  // Đặt kiểu nét đứt
        limitLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        limitLine.textSize = 12f

        // Thêm đường giới hạn vào trục Y của biểu đồ so sánh
        val yAxisLeft = comparisonBarChart.axisLeft
        yAxisLeft.addLimitLine(limitLine)
    }




}
