package com.example.nlcs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.example.nlcs.databinding.ActivityStatisticsBinding
import java.util.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private val features = listOf("Mindmap", "Focus Mode", "Flashcard", "Note", "Reminder")
    private val timeRanges = listOf("Day", "Week", "Month", "Year")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbarStatistics)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Quay lại"
        binding.toolbarStatistics.setNavigationOnClickListener {
            onBackPressed()
        }

        // Set up the spinner and initial chart
        setupSpinner()
        updateChart("Day") // Initial chart setup with "Day" as default
    }

    // Set up the spinner for selecting time ranges (Day, Week, Month, Year)
    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeRanges)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeRange.adapter = adapter
        binding.spinnerTimeRange.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateChart(timeRanges[position])
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    // Method to update the bar chart based on the selected time range
    private fun updateChart(timeRange: String) {
        val entries = mutableListOf<List<BarEntry>>()
        val labels = when (timeRange) {
            "Day" -> (0..23).map { it.toString() }
            "Week" -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            "Month" -> (1..30).map { it.toString() }
            "Year" -> listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            else -> listOf()
        }

        // Gather data for each feature
        for (feature in features) {
            val featureEntries = getUsageDataForBar(feature, timeRange, labels.size)
            entries.add(featureEntries)
        }

        // Create BarDataSets for each feature and style them
        val dataSets = entries.mapIndexed { index, featureEntries ->
            BarDataSet(featureEntries, features[index]).apply {
                color = Color.rgb(Random().nextInt(256), Random().nextInt(256), Random().nextInt(256))
                setDrawValues(true)
            }
        }

        // Configure the bar chart
        binding.chart.apply {
            data = BarData(dataSets)
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
            }

            axisRight.isEnabled = false

            // Ensure Y-axis shows time in minutes
            axisLeft.apply {
                axisMinimum = 0f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} min" // Display minutes
                    }
                }
            }

            invalidate() // Refresh the chart
        }
    }

    // Retrieve the usage data for the bar chart based on the selected time range
    private fun getUsageDataForBar(feature: String, timeRange: String, dataPoints: Int): List<BarEntry> {
        val sharedPref = getSharedPreferences("UsageStats", Context.MODE_PRIVATE)
        return (0 until dataPoints).map { i ->
            val key = "${feature}_${timeRange}_$i"
            BarEntry(i.toFloat(), sharedPref.getFloat(key, 0f)) // Get saved minutes
        }
    }

    // Method to log the feature usage in minutes for Day, Week, Month, Year
    fun logFeatureUsage(feature: String, duration: Long) {
        val sharedPref = getSharedPreferences("UsageStats", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            val minutes = duration / 60000f // Convert duration to minutes

            // Log for day (based on hours)
            val dayKey = "${feature}_Day_${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}"
            putFloat(dayKey, sharedPref.getFloat(dayKey, 0f) + minutes)

            // Log for week (based on days of the week)
            val weekKey = "${feature}_Week_${Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1}"
            putFloat(weekKey, sharedPref.getFloat(weekKey, 0f) + minutes)

            // Log for month (based on day of the month)
            val monthKey = "${feature}_Month_${Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1}"
            putFloat(monthKey, sharedPref.getFloat(monthKey, 0f) + minutes)

            // Log for year (based on month of the year)
            val yearKey = "${feature}_Year_${Calendar.getInstance().get(Calendar.MONTH)}"
            putFloat(yearKey, sharedPref.getFloat(yearKey, 0f) + minutes)

            apply()
        }
    }
}
