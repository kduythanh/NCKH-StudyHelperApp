package com.example.nlcs

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding

    //Import usageTracker class to get using time
    private lateinit var usageTracker: UsageTracker

    // Add list of function
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeatureListAdapter
    //End

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usageTracker = UsageTracker(this)  // Ensure UsageTracker is properly implemented

        // Add list of function
        recyclerView = findViewById(R.id.featureListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        //End


        // Load data - sort - Count using time except statistic
        val sortedFeatureList = usageTracker.getSortedUsageData()
        adapter = FeatureListAdapter(sortedFeatureList)
        recyclerView.adapter = adapter
        //end

        //Set up toolbar
        setSupportActionBar(binding.toolbarStatistics)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Về trang chủ"

        binding.toolbarStatistics.setNavigationOnClickListener {
            onBackPressed()
        }

        // end

        setupSpinner()
        setupBarChart()
        setupSeeAllActivityButton()
    }

    private fun setupSpinner() {
        val timeFrames = arrayOf("Day", "Week", "Month", "Year")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeFrames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.timeFrameSpinner.adapter = adapter

        // Handling spinner selection
        binding.timeFrameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateChartData(timeFrames[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing if no selection
            }
        }
    }

    private fun setupBarChart() {
        val barChart: BarChart = binding.barChart
        barChart.description.isEnabled = false
        barChart.setDrawValueAboveBar(false)
        barChart.setDrawGridBackground(false)
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.legend.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        val leftAxis = barChart.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()} min"
            }
        }

        barChart.axisRight.isEnabled = false

        // Load initial data for "Week"
        updateChartData("Week")
    }

    private fun updateChartData(timeFrame: String) {
        val usageData = when (timeFrame) {
            "Day" -> usageTracker.getDailyUsageData()
            "Week" -> usageTracker.getWeeklyUsageData()
            "Month" -> usageTracker.getMonthlyUsageData()
            "Year" -> usageTracker.getYearlyUsageData()
            else -> usageTracker.getWeeklyUsageData()
        }

        if (usageData.isEmpty()) {
            // Xử lý khi không có dữ liệu
            binding.barChart.clear()
            return
        }

        // Fix: Sử dụng `map` để tạo các mục BarEntry
        val entries = usageData.entries.map { (key, value) ->
            BarEntry(usageData.keys.indexOf(key).toFloat(), value.toFloat())
        }

        val dataSet = BarDataSet(entries, "Feature Usage (minutes)")
        dataSet.color = Color.GREEN
        dataSet.setDrawValues(false)

        val barData = BarData(dataSet)
        binding.barChart.data = barData

        // Đảm bảo nhãn xAxis khớp với thứ tự dữ liệu
        binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(usageData.keys.toList())
        binding.barChart.invalidate()
    }

    private fun setupSeeAllActivityButton() {
        binding.seeAllActivityButton.setOnClickListener {
            // Implement the action to see all activity
        }
    }
}
