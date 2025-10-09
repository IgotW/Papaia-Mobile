package com.google.papaia.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.google.papaia.R

class CustomMarkerView(
    context: Context,
    layoutResource: Int,
    private val dates: List<String>,
    private val diseaseNames: List<String>
) : MarkerView(context, layoutResource) {

    private val tvDate: TextView = findViewById(R.id.tvDate)
    private val tvDisease: TextView = findViewById(R.id.tvDisease)
    private val tvCount: TextView = findViewById(R.id.tvCount)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null || highlight == null) return

        val xIndex = e.x.toInt()
        val date = if (xIndex in dates.indices) dates[xIndex] else "Unknown"

        // Get reference to the chart
        val chart = chartView as? LineChart

        // Get the clicked dataset index
        val clickedDataSetIndex = highlight.dataSetIndex

//        val builder = StringBuilder()
        val builder = SpannableStringBuilder()
        var hasData = false

        chart?.data?.dataSets?.forEachIndexed { index, dataSet ->
            val entry = dataSet.getEntryForXValue(xIndex.toFloat(), Float.NaN)
            val yValue = entry?.y ?: 0f

            if (yValue > 0) hasData = true

            // Mark only the clicked dataset
//            val indicator = if (index == clickedDataSetIndex) "► " else "   "
//            builder.append("$indicator${dataSet.label}: ${yValue.toInt()}\n")
            if (entry != null && entry.y > 0) {
//                // Mark the clicked dataset with an indicator (e.g., ► or •)
//                val indicator = if (index == clickedDataSetIndex) "► " else "   "
//                builder.append("${dataSet.label}: ${entry.y.toInt()}\n")

                val diseaseName = dataSet.label
                val start = builder.length
                builder.append("$diseaseName: ")

                builder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + diseaseName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                val countStart = builder.length
                builder.append("${yValue.toInt()}\n")

                // 🔹 Color for count (gray or green)
                builder.setSpan(
                    android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#FFD54F")), // light gray
                    countStart,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // 🔹 If no data for that X value, show all diseases with count 0
        if (!hasData) {
//            builder.clear()
//            diseaseNames.forEach { disease ->
//                builder.append("$disease: 0\n")
//            }
            builder.clear()
            diseaseNames.forEach { disease ->
                val start = builder.length
                builder.append("$disease: ")
                builder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    start + disease.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                val countStart = builder.length
                builder.append("0\n")

                builder.setSpan(
                    android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#FFD54F")),
                    countStart,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        tvDate.text = date
//        tvDisease.text = ""
        tvCount.text = builder.trim()

        super.refreshContent(e, highlight)

//        chart?.data?.dataSets?.forEachIndexed { index, dataSet ->
//            val entry = dataSet.getEntryForXValue(xIndex.toFloat(), Float.NaN)
//            if (entry != null && entry.y > 0) {
//                // Mark the clicked dataset with an indicator (e.g., ► or •)
//                val indicator = if (index == clickedDataSetIndex) "► " else "   "
//                builder.append("$indicator${dataSet.label}: ${entry.y.toInt()}\n")
//            }
//        }
//
//            tvDate.text = date
//            tvDisease.text = "Diseases (tap to cycle)"
//            tvCount.text = builder.toString().trim()
//
//        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}