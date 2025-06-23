package com.example.budgetbuddy

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbuddy.databinding.ActivityGraphBinding
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding
    private val calendarStart = Calendar.getInstance()
    private val calendarEnd = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.tvStartDate.text = sdf.format(calendarStart.time)
        binding.tvEndDate.text = sdf.format(calendarEnd.time)

        binding.tvStartDate.setOnClickListener {
            pickDate(calendarStart) { binding.tvStartDate.text = sdf.format(it.time) }
        }
        binding.tvEndDate.setOnClickListener {
            pickDate(calendarEnd) { binding.tvEndDate.text = sdf.format(it.time) }
        }

        binding.btnGenerateChart.setOnClickListener {
            generateChart()
        }
    }

    private fun pickDate(calendar: Calendar, onDatePicked: (Calendar) -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, y, m, d ->
            calendar.set(y, m, d)
            onDatePicked(calendar)
        }, year, month, day).show()
    }

    private fun generateChart() {
        val startDate = binding.tvStartDate.text.toString()
        val endDate = binding.tvEndDate.text.toString()

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            try {
                val goal = db.goalDao().getGoal()
                val results = db.expenseDao().getTotalSpentPerCategory(startDate, endDate)

                val entries = ArrayList<BarEntry>()
                val labels = ArrayList<String>()

                results.forEachIndexed { index, (categoryName, total) ->
                    entries.add(BarEntry(index.toFloat(), total.toFloat()))
                    labels.add(categoryName)
                }

                val dataSet = BarDataSet(entries, "Spending")
                dataSet.color = Color.BLUE

                val barData = BarData(dataSet)
                barData.barWidth = 0.9f

                val chart = binding.barChart
                chart.data = barData
                chart.setFitBars(true)
                chart.description.isEnabled = false
                chart.axisRight.isEnabled = false

                val xAxis = chart.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                xAxis.labelRotationAngle = -45f

                chart.axisLeft.removeAllLimitLines()
                if (goal != null) {
                    chart.axisLeft.addLimitLine(
                        LimitLine(goal.minAmount.toFloat(), "Min Goal").apply {
                            lineColor = Color.GREEN
                            lineWidth = 2f
                            textColor = Color.GREEN
                        }
                    )
                    chart.axisLeft.addLimitLine(
                        LimitLine(goal.maxAmount.toFloat(), "Max Goal").apply {
                            lineColor = Color.RED
                            lineWidth = 2f
                            textColor = Color.RED
                        }
                    )
                }

                chart.invalidate()

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@GraphActivity, "Error loading chart: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
