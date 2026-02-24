package com.example.smartremainder

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartremainder.databinding.ActivityDataAnaysisBinding
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class data_anaysis : AppCompatActivity() {

    private lateinit var binding: ActivityDataAnaysisBinding
    private lateinit var dbHelper: DatabaseHelper
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataAnaysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // ✅ Get logged-in user email from SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userEmail = prefs.getString("email", null)

        if (userEmail != null) {
            loadAnalyticsData(userEmail!!)
        } else {
            Log.e("DataAnalysis", "No logged-in user found!")
            binding.mlInsightText.text = "Please log in to view your personalized analysis."
        }
    }

    private fun loadAnalyticsData(userEmail: String) {
        try {
            // ✅ Get data only for the current logged-in user
            val weeklyAdherence = dbHelper.getWeeklyAdherenceStats(userEmail)
            val overallAdherence = dbHelper.getOverallAdherenceStats(userEmail)
            val responseTimeData = dbHelper.getResponseTimeStats(userEmail)

            Log.d("DataAnalysis", "Weekly Adherence size: ${weeklyAdherence.size}")
            Log.d("DataAnalysis", "Overall Adherence: $overallAdherence")
            Log.d("DataAnalysis", "Response Time size: ${responseTimeData.size}")

            if (weeklyAdherence.isNotEmpty()) {
                setupCombinedChart(weeklyAdherence)
            } else {
                Log.w("DataAnalysis", "No weekly adherence data")
            }

            setupPieChart(overallAdherence)
            setupAccuracyCard(overallAdherence)

            if (responseTimeData.isNotEmpty()) {
                setupScatterChart(responseTimeData)
            } else {
                Log.w("DataAnalysis", "No response time data")
            }

            generateSmartInsight(userEmail)
        } catch (e: Exception) {
            Log.e("DataAnalysis", "Error loading analytics: ${e.message}", e)
        }
    }

    private fun generateSmartInsight(userEmail: String) {
        try {
            val worstDay = dbHelper.getLowestAdherenceDay(userEmail)
            val insightText = if (worstDay.isNotBlank()) {
                "Our analysis suggests you struggle most on ${worstDay}s. Try setting an extra reminder on those days!"
            } else {
                "You are on a great track! No specific problem days found. Keep up the consistent work."
            }
            binding.mlInsightText.text = insightText
        } catch (e: Exception) {
            Log.e("DataAnalysis", "Error generating insight: ${e.message}", e)
            binding.mlInsightText.text =
                "Keep tracking your medications to get personalized insights!"
        }
    }

    private fun setupCombinedChart(weeklyAdherence: List<BarEntry>) {
        try {
            val combinedChart = binding.medicationCombinedChart
            combinedChart.clear()

            combinedChart.description.isEnabled = false
            combinedChart.drawOrder = arrayOf(CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE)
            combinedChart.setBackgroundColor(Color.WHITE)
            combinedChart.setDrawGridBackground(false)

            // Bar DataSet
            val barDataSet = BarDataSet(weeklyAdherence, "Weekly Adherence").apply {
                color = Color.parseColor("#E3F2FD")
                valueTextColor = Color.BLACK
                valueTextSize = 10f
                setDrawValues(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
                }
            }
            val barData = BarData(barDataSet)
            barData.barWidth = 0.8f

            // Line DataSet
            val lineEntries = weeklyAdherence.map { Entry(it.x, it.y) }
            val lineDataSet = LineDataSet(lineEntries, "Adherence Trend").apply {
                color = Color.parseColor("#5C6BC0")
                lineWidth = 3f
                setCircleColor(Color.parseColor("#5C6BC0"))
                circleRadius = 5f
                circleHoleRadius = 2.5f
                setDrawCircleHole(true)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
            }

            val combinedData = CombinedData().apply {
                setData(BarData(barDataSet))
                setData(LineData(lineDataSet))
            }

            combinedChart.data = combinedData
            combinedChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = Color.BLACK
                textSize = 11f
                setDrawGridLines(false)
            }

            combinedChart.axisLeft.apply {
                textColor = Color.BLACK
                textSize = 11f
                axisMinimum = 0f
                axisMaximum = 100f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
                }
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
            }

            combinedChart.axisRight.isEnabled = false
            combinedChart.legend.textColor = Color.BLACK
            combinedChart.animateY(1000)
            combinedChart.invalidate()

            Log.d("DataAnalysis", "Combined chart setup complete")
        } catch (e: Exception) {
            Log.e("DataAnalysis", "Error setting up combined chart: ${e.message}", e)
        }
    }

    private fun setupPieChart(overallAdherence: Pair<Float, Float>) {
        try {
            val pieChart = binding.medicationPieChart
            pieChart.clear()

            val takenPercent = overallAdherence.first
            val missedPercent = 100f - takenPercent

            val entries = ArrayList<PieEntry>().apply {
                if (takenPercent > 0) add(PieEntry(takenPercent, "Taken"))
                if (missedPercent > 0) add(PieEntry(missedPercent, "Missed"))
                if (isEmpty()) add(PieEntry(100f, "No Data"))
            }

            val colors = when {
                entries.size == 1 && entries[0].label == "No Data" -> listOf(Color.LTGRAY)
                else -> listOf(Color.parseColor("#66BB6A"), Color.parseColor("#EF5350"))
            }

            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors
                setDrawValues(false)
                sliceSpace = 2f
            }

            pieChart.apply {
                data = PieData(dataSet)
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 70f
                transparentCircleRadius = 75f
                setHoleColor(Color.WHITE)
                legend.isEnabled = false
                centerText = "Overall"
                setCenterTextSize(22f)
                setCenterTextColor(Color.GRAY)
                setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                animateY(1000)
                invalidate()
            }

            createPieChartLegend()
        } catch (e: Exception) {
            Log.e("DataAnalysis", "Error setting up pie chart: ${e.message}", e)
        }
    }

    private fun createPieChartLegend() {
        binding.pieChartLegend.removeAllViews()
        val legendItems = listOf(
            "Taken" to Color.parseColor("#66BB6A"),
            "Missed" to Color.parseColor("#EF5350")
        )

        for ((labelText, colorValue) in legendItems) {
            val legendItem = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 32 }
            }

            val colorBox = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(30, 30)
                setBackgroundColor(colorValue)
            }

            val label = TextView(this).apply {
                text = labelText
                textSize = 16f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = 12 }
            }

            legendItem.addView(colorBox)
            legendItem.addView(label)
            binding.pieChartLegend.addView(legendItem)
        }
    }

    private fun setupScatterChart(responseTimeData: List<Entry>) {
        try {
            val scatterChart = binding.medicationScatterChart
            scatterChart.clear()

            val dataSet = ScatterDataSet(responseTimeData, "Response Time").apply {
                color = Color.parseColor("#FFA726")
                setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                scatterShapeSize = 20f
                setDrawValues(false)
            }

            scatterChart.apply {
                data = ScatterData(dataSet)
                description.isEnabled = false
                legend.textColor = Color.BLACK
                animateXY(1000, 1000)
                invalidate()
            }
        } catch (e: Exception) {
            Log.e("DataAnalysis", "Error setting up scatter chart: ${e.message}", e)
        }
    }

    private fun setupAccuracyCard(overallAdherence: Pair<Float, Float>) {
        try {
            val accuracyPercent = overallAdherence.first
            binding.tvAccuracyPercentage.text = "${DecimalFormat("#.0").format(accuracyPercent)}%"
        } catch (e: Exception) {
            Log.e("DataAnalysis", "Error setting up accuracy card: ${e.message}", e)
            binding.tvAccuracyPercentage.text = "0.0%"
        }
    }
}
