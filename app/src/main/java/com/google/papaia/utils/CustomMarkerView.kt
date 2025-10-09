package com.google.papaia.utils

import android.content.Context
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

        val builder = StringBuilder()
        var hasData = false

        chart?.data?.dataSets?.forEachIndexed { index, dataSet ->
            val entry = dataSet.getEntryForXValue(xIndex.toFloat(), Float.NaN)
            if (entry != null && entry.y > 0) {
                // Mark the clicked dataset with an indicator (e.g., ► or •)
                val indicator = if (index == clickedDataSetIndex) "► " else "   "
                builder.append("$indicator${dataSet.label}: ${entry.y.toInt()}\n")
                hasData = true
            }
        }

        if (hasData) {
            tvDate.text = date
            tvDisease.text = "Diseases (tap to cycle)"
            tvCount.text = builder.toString().trim()
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}