package com.google.papaia.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.papaia.R
import com.google.papaia.request.DailyAnalyticsStatRequest
import com.google.papaia.request.MonthlyAnalyticsRequest
import com.google.papaia.request.WeeklyAnalyticsRequest
import com.google.papaia.request.YearlyAnalyticsRequest
import com.google.papaia.response.DailyAnalyticsResponse
import com.google.papaia.response.MonthlyAnalyticsResponse
import com.google.papaia.response.WeeklyAnalyticsResponse
import com.google.papaia.response.YearlyAnalyticsResponse
import com.google.papaia.utils.CustomMarkerView
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var txtTitle: TextView
    private lateinit var btnDaily: MaterialButton
    private lateinit var btnWeekly: MaterialButton
    private lateinit var btnMonthly: MaterialButton
    private lateinit var btnYearly: MaterialButton
    private lateinit var btnBack: ImageView
    private lateinit var lineChart: LineChart

    enum class AnalyticsMode {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    private var currentMode: AnalyticsMode = AnalyticsMode.DAILY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_analytics)

        txtTitle = findViewById(R.id.txtviewTitle)
        btnDaily = findViewById(R.id.btnDaily)
        btnWeekly = findViewById(R.id.btnWeekly)
        btnMonthly = findViewById(R.id.btnMonthly)
        btnYearly = findViewById(R.id.btnYearly)
        btnBack = findViewById(R.id.btn_back)
        lineChart = findViewById(R.id.lineChart)

        setupButtonClickListeners()
        selectButton(btnDaily)
    }

    private fun setupButtonClickListeners() {
        btnDaily.setOnClickListener {
            selectButton(btnDaily)
            txtTitle.setText("Daily Analytics")
        }
        btnWeekly.setOnClickListener {
            selectButton(btnWeekly)
            txtTitle.setText("Weekly Analytics")
        }
        btnMonthly.setOnClickListener {
            selectButton(btnMonthly)
            txtTitle.setText("Monthly Analytics")
        }
        btnYearly.setOnClickListener {
            selectButton(btnYearly)
            txtTitle.setText("Yearly Analytics")
        }
        btnBack.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("navigateTo", "home")
            startActivity(intent)
            finish()
        }
    }

    private fun selectButton(selectedButton: MaterialButton) {
        // Reset all buttons to unselected state
        listOf(btnDaily, btnWeekly, btnMonthly, btnYearly).forEach { button ->
            button.setBackgroundResource(R.drawable.button_outline_primary)
            button.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        // Set selected button
        selectedButton.setBackgroundResource(R.drawable.button_primary_selector)
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white))

        when (selectedButton.id) {
            R.id.btnDaily -> {
                currentMode = AnalyticsMode.DAILY
                updateChartForDaily()
            }
            R.id.btnWeekly -> {
                currentMode = AnalyticsMode.WEEKLY
                updateChartForWeekly()
            }
            R.id.btnMonthly -> {
                currentMode = AnalyticsMode.MONTHLY
                updateChartForMonthly()
            }
            R.id.btnYearly -> {
                currentMode = AnalyticsMode.YEARLY
                updateChartForYearly()
            }
        }
    }

    private fun getAuthToken(): String? {
        val sharedPrefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("token", null)
    }

    private fun updateChartForDaily(){
        getYourDailyData() // this now handles everything inside
    }

    private fun updateChartForWeekly(){
        getYourWeeklyData()
    }

    private fun updateChartForMonthly(){
        getYourMonthlyData()
    }

    private fun updateChartForYearly(){
        getYourYearlyData()
    }

    /**API GET DATA**/
    private fun getYourDailyData() {
        val token = getAuthToken()

        if (token == null) {
            Log.e("Chart", "Token not found")
            return
        }
        RetrofitClient.instance.getDailyAnalytics("Bearer $token")
            .enqueue(object : Callback<DailyAnalyticsResponse> {
                override fun onResponse(
                    call: Call<DailyAnalyticsResponse>,
                    response: Response<DailyAnalyticsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val dailyStats = response.body()!!.dailyStats

                        if (dailyStats.isNullOrEmpty()) {
                            Log.w("Chart", "No daily data received")
                            return
                        }

                        val statsList = dailyStats.map {
                            DailyAnalyticsStatRequest(
                                day = it.day,
                                predictions = it.predictions
                            )
                        }

                        // Fill missing days from first to last data point
                        val filled = fillMissingDays(statsList)
//                        setupLineChart(filled)
                        setupLineChartDaily(filled)
                    } else {
                        Log.e("Chart", "Failed: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<DailyAnalyticsResponse>, t: Throwable) {
                    Log.e("Chart", "API error: ${t.message}")
                }
            })
    }

    private fun getYourWeeklyData() {
        val token = getAuthToken()

        if (token == null) {
            Log.e("Chart", "Token not found")
            return
        }
        RetrofitClient.instance.getWeeklyAnalytics("Bearer $token")
            .enqueue(object : Callback<WeeklyAnalyticsResponse> {
                override fun onResponse(
                    call: Call<WeeklyAnalyticsResponse>,
                    response: Response<WeeklyAnalyticsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val dailyStats = response.body()!!.weeklyStats

                        if (dailyStats.isNullOrEmpty()) {
                            Log.w("Chart", "No daily data received")
                            return
                        }

                        val statsList = dailyStats.map {
                            WeeklyAnalyticsRequest(
                                week = it.week,
                                predictions = it.predictions
                            )
                        }

                        // Fill missing days from first to last data point
                        val filled = fillMissingWeeks(statsList)
//                        setupLineChart(filled)
                        setupLineChartWeekly(filled)
                    } else {
                        Log.e("Chart", "Failed: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<WeeklyAnalyticsResponse>, t: Throwable) {
                    Log.e("Chart", "API error: ${t.message}")
                }
            })
    }

    private fun getYourMonthlyData() {
        val token = getAuthToken()

        if (token == null) {
            Log.e("Chart", "Token not found")
            return
        }
        RetrofitClient.instance.getMonthlyAnalytics("Bearer $token")
            .enqueue(object : Callback<MonthlyAnalyticsResponse> {
                override fun onResponse(
                    call: Call<MonthlyAnalyticsResponse>,
                    response: Response<MonthlyAnalyticsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val dailyStats = response.body()!!.monthlyStats

                        if (dailyStats.isNullOrEmpty()) {
                            Log.w("Chart", "No daily data received")
                            return
                        }

                        val statsList = dailyStats.map {
                            MonthlyAnalyticsRequest(
                                month = it.month,
                                predictions = it.predictions
                            )
                        }

                        // Fill missing days from first to last data point
                        val filled = fillMissingMonths(statsList)
//                        setupLineChart(filled)
                        setupLineChartMonthly(filled)
                    } else {
                        Log.e("Chart", "Failed: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MonthlyAnalyticsResponse>, t: Throwable) {
                    Log.e("Chart", "API error: ${t.message}")
                }
            })
    }

    private fun getYourYearlyData() {
        val token = getAuthToken()

        if (token == null) {
            Log.e("Chart", "Token not found")
            return
        }
        RetrofitClient.instance.getYearlyAnalytics("Bearer $token")
            .enqueue(object : Callback<YearlyAnalyticsResponse> {
                override fun onResponse(
                    call: Call<YearlyAnalyticsResponse>,
                    response: Response<YearlyAnalyticsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val dailyStats = response.body()!!.yearlyStats

                        if (dailyStats.isNullOrEmpty()) {
                            Log.w("Chart", "No daily data received")
                            return
                        }

                        val statsList = dailyStats.map {
                            YearlyAnalyticsRequest(
                                year = it.year,
                                predictions = it.predictions
                            )
                        }

                        // Fill missing days from first to last data point
                        val filled = fillMissingYears(statsList)
//                        setupLineChart(filled)
                        setupLineChartYearly(filled)
                    } else {
                        Log.e("Chart", "Failed: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<YearlyAnalyticsResponse>, t: Throwable) {
                    Log.e("Chart", "API error: ${t.message}")
                }
            })
    }

    /** ---------------- CHART SETUP ---------------- **/
    private fun setupLineChartDaily(dailyStats: List<DailyAnalyticsStatRequest>) {
        val diseaseMap = mutableMapOf<String, MutableList<Entry>>()
        val labels = mutableListOf<String>()

        dailyStats.forEachIndexed { index, stat ->
            val convertedDate = convertDateFormat(stat.day)
            labels.add(convertedDate)
            stat.predictions.forEach { (disease, count) ->
                diseaseMap.getOrPut(disease) { mutableListOf() }
                    .add(Entry(index.toFloat(), count.toFloat()))
            }
        }

        setupChart(diseaseMap, labels, dailyStats.size, "daily")
    }

    private fun setupLineChartWeekly(weeklyStats: List<WeeklyAnalyticsRequest>) {
        val diseaseMap = mutableMapOf<String, MutableList<Entry>>()
        val labels = mutableListOf<String>()

        weeklyStats.forEachIndexed { index, stat ->
            val convertedDate = convertDateFormat(stat.week)
            labels.add(convertedDate)
            stat.predictions.forEach { (disease, count) ->
                diseaseMap.getOrPut(disease) { mutableListOf() }
                    .add(Entry(index.toFloat(), count.toFloat()))
            }
        }

        setupChart(diseaseMap, labels, weeklyStats.size, "weekly")
    }

    private fun setupLineChartMonthly(monthlyStats: List<MonthlyAnalyticsRequest>) {
        val diseaseMap = mutableMapOf<String, MutableList<Entry>>()
        val labels = mutableListOf<String>()

        monthlyStats.forEachIndexed { index, stat ->
            val convertedDate = convertDateFormat(stat.month)
            labels.add(convertedDate)
            stat.predictions.forEach { (disease, count) ->
                diseaseMap.getOrPut(disease) { mutableListOf() }
                    .add(Entry(index.toFloat(), count.toFloat()))
            }
        }

        setupChart(diseaseMap, labels, monthlyStats.size, "monthly")
    }

    private fun setupLineChartYearly(yearlyStats: List<YearlyAnalyticsRequest>) {
        val diseaseMap = mutableMapOf<String, MutableList<Entry>>()
        val labels = mutableListOf<String>()

        yearlyStats.forEachIndexed { index, stat ->
            val convertedDate = convertDateFormat(stat.year)
            labels.add(convertedDate)
            stat.predictions.forEach { (disease, count) ->
                diseaseMap.getOrPut(disease) { mutableListOf() }
                    .add(Entry(index.toFloat(), count.toFloat()))
            }
        }

        setupChart(diseaseMap, labels, yearlyStats.size, "yearly")
    }

    private fun setupChart(diseaseMap: MutableMap<String, MutableList<Entry>>, labels: MutableList<String>, dataSize: Int, category: String) {

        val allDiseases = listOf("Healthy", "Anthracnose", "Powdery Mildew", "Ring Spot Virus")

        val colorMap = mapOf(
            "Healthy" to Color.parseColor("#4CAF50"),
            "Anthracnose" to Color.parseColor("#F44336"),
            "Powdery Mildew" to Color.parseColor("#2196F3"),
            "Ring Spot Virus" to Color.parseColor("#FF9800")
        )

        allDiseases.forEach { disease ->
            if (!diseaseMap.containsKey(disease)) {
                diseaseMap[disease] = mutableListOf()
                for (i in 0 until dataSize) {
                    diseaseMap[disease]?.add(Entry(i.toFloat(), 0f))
                }
            }
        }

//        val diseaseNames = diseaseMap.keys.toList()

        val lineDataSets = diseaseMap.entries.map { (key, value) ->
            LineDataSet(value, key).apply {
                color = colorMap[key] ?: Color.BLACK
                setCircleColor(color)
                circleRadius = 4f
                lineWidth = 2f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
        }

        lineChart.data = LineData(lineDataSets)

        val markerView =
            CustomMarkerView(this, R.layout.custom_marker_view, labels, allDiseases)
        markerView.chartView = lineChart
        lineChart.marker = markerView

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            labelRotationAngle = -45f
            setDrawGridLines(false)
            setLabelCount(5, false)
            setAvoidFirstLastClipping(true)
            yOffset = 5f
            textSize = 9f
            spaceMin = 0.5f
            spaceMax = 0.5f
        }

        lineChart.axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        lineChart.axisRight.isEnabled = false

        lineChart.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            textSize = 9f
            isWordWrapEnabled = true
            yOffset = 10f
            xOffset = 0f
            maxSizePercent = 1.0f
            formSize = 8f
            xEntrySpace = 4f
            yEntrySpace = 2f
        }

        lineChart.description.isEnabled = false
        lineChart.setExtraOffsets(15f, 20f, 15f, 50f)

        // Enable scrolling and dragging
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)

        when (category.lowercase()) {
            "daily" -> {
                lineChart.setVisibleXRangeMinimum(5f)
                lineChart.setVisibleXRangeMaximum(5f)
                lineChart.moveViewToX((dataSize - 5).coerceAtLeast(0).toFloat())
            }
            "weekly" -> {
                lineChart.setVisibleXRangeMinimum(7f)
                lineChart.setVisibleXRangeMaximum(7f)
                lineChart.moveViewToX((dataSize - 7).coerceAtLeast(0).toFloat())
            }
            "monthly" -> {
                lineChart.setVisibleXRangeMinimum(6f)
                lineChart.setVisibleXRangeMaximum(6f)
                lineChart.moveViewToX((dataSize - 6).coerceAtLeast(0).toFloat())
            }
            "yearly" -> {
                lineChart.setVisibleXRangeMaximum(dataSize.toFloat())
            }
        }


        // Move to the most recent data (rightmost)
//        lineChart.moveViewToX((dataSize - 1).toFloat())

        lineChart.isHighlightPerTapEnabled = true
        lineChart.isHighlightPerDragEnabled = false

        lineChart.animateX(1000)
        lineChart.invalidate()
    }

    private fun convertDateFormat(dateString: String): String {
        return try {
            when (currentMode) {
                AnalyticsMode.DAILY -> {
                    // Input: "Oct 26, 2025" -> Output: "Oct 26"
                    val inputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ENGLISH)
                    val date = inputFormat.parse(dateString)
                    if (date != null) {
                        java.text.SimpleDateFormat("MMM dd", java.util.Locale.ENGLISH).format(date)
                    } else dateString
                }
                AnalyticsMode.WEEKLY -> {
                    // Input: "Oct 20 - Oct 26, 2025" -> Output: "Oct 20-26"
                    val parts = dateString.split(" - ")
                    if (parts.size == 2) {
                        val start = parts[0] // "Oct 20"
                        val endPart = parts[1].split(", ") // ["Oct 26", "2025"]
                        if (endPart.size == 2) {
                            val endDay = endPart[0].split(" ").lastOrNull() // "26"
                            "$start-$endDay"
                        } else dateString
                    } else dateString
                }
                AnalyticsMode.MONTHLY -> {
                    // Input: "October 2025" -> Output: "Oct 2025"
                    val inputFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.ENGLISH)
                    val date = inputFormat.parse(dateString)
                    if (date != null) {
                        java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.ENGLISH).format(date)
                    } else dateString
                }
                AnalyticsMode.YEARLY -> {
                    // Input: "2025" -> Output: "2025"
                    dateString
                }
            }
        } catch (e: Exception) {
            Log.e("DateFormat", "Error converting date: $dateString", e)
            dateString
        }
    }

    /** -------------------- FILL MISSING DATA -------------------- **/
    private fun fillMissingDays(dailyStats: List<DailyAnalyticsStatRequest>): List<DailyAnalyticsStatRequest> {
        if (dailyStats.isEmpty()) return emptyList()

        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ENGLISH)
        val calendar = java.util.Calendar.getInstance()
        val filledStats = mutableListOf<DailyAnalyticsStatRequest>()

        // Create a map of existing data for quick lookup (standardized)
        val existingDataMap = mutableMapOf<String, DailyAnalyticsStatRequest>()
        dailyStats.forEach { stat ->
            val standardizedDate = parseAndStandardizeDate(stat.day)
            if (standardizedDate != null) {
                existingDataMap[standardizedDate] = stat
            }
        }

        // Sort the dates to find first date
        val sortedDates = existingDataMap.keys.mapNotNull { dateString ->
            try {
                dateFormat.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }.sorted()

        if (sortedDates.isEmpty()) return dailyStats

        val startDate = sortedDates.first()
        // Change: Use today's date as the end date instead of the last data point
        val endDate = java.util.Date() // Today

        // Generate every date from start to TODAY
        calendar.time = startDate
        while (!calendar.time.after(endDate)) {
            val currentDateString = dateFormat.format(calendar.time)
            val existingData = existingDataMap[currentDateString]

            if (existingData != null) {
                // Existing data found
                filledStats.add(
                    DailyAnalyticsStatRequest(currentDateString, existingData.predictions)
                )
            } else {
                // Missing date → fill with zeros
                val zeroPredictions = mapOf(
                    "Healthy" to 0,
                    "Anthracnose" to 0,
                    "Powdery Mildew" to 0,
                    "Ring Spot Virus" to 0
                )
                filledStats.add(DailyAnalyticsStatRequest(currentDateString, zeroPredictions))
            }

            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return filledStats
    }

    private fun fillMissingWeeks(weeklyStats: List<WeeklyAnalyticsRequest>): List<WeeklyAnalyticsRequest> {
        if (weeklyStats.isEmpty()) return emptyList()

        val calendar = java.util.Calendar.getInstance()
        val filledStats = mutableListOf<WeeklyAnalyticsRequest>()

        // Create a map of existing data
        val existingDataMap = mutableMapOf<String, WeeklyAnalyticsRequest>()
        weeklyStats.forEach { stat ->
            existingDataMap[stat.week] = stat
        }

        // Generate last 12 weeks
        val weeksToShow = 12
        val weekLabels = mutableListOf<Pair<String, java.util.Date>>()

        for (i in (weeksToShow - 1) downTo 0) {
            calendar.time = java.util.Date()
            calendar.add(java.util.Calendar.WEEK_OF_YEAR, -i)
            calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)

            val weekStart = calendar.clone() as java.util.Calendar
            val weekEnd = calendar.clone() as java.util.Calendar
            weekEnd.add(java.util.Calendar.DAY_OF_MONTH, 6) // Sunday

            val weekLabel = if (weekStart.get(java.util.Calendar.YEAR) == weekEnd.get(java.util.Calendar.YEAR)) {
                "${java.text.SimpleDateFormat("MMM d", java.util.Locale.ENGLISH).format(weekStart.time)} - ${java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.ENGLISH).format(weekEnd.time)}"
            } else {
                "${java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.ENGLISH).format(weekStart.time)} - ${java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.ENGLISH).format(weekEnd.time)}"
            }

            weekLabels.add(Pair(weekLabel, weekStart.time))
        }

        // Match existing data with generated weeks
        weekLabels.forEach { (weekLabel, _) ->
            val existingData = existingDataMap[weekLabel]
            if (existingData != null) {
                filledStats.add(existingData)
            } else {
                val zeroPredictions = mapOf(
                    "Healthy" to 0,
                    "Anthracnose" to 0,
                    "Powdery Mildew" to 0,
                    "Ring Spot Virus" to 0
                )
                filledStats.add(WeeklyAnalyticsRequest(weekLabel, zeroPredictions))
            }
        }

        return filledStats
    }

    private fun fillMissingMonths(monthlyStats: List<MonthlyAnalyticsRequest>): List<MonthlyAnalyticsRequest> {
        if (monthlyStats.isEmpty()) return emptyList()

        val monthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.ENGLISH)
        val calendar = java.util.Calendar.getInstance()
        val filledStats = mutableListOf<MonthlyAnalyticsRequest>()

        // Create a map of existing data
        val existingDataMap = mutableMapOf<String, MonthlyAnalyticsRequest>()
        monthlyStats.forEach { stat ->
            existingDataMap[stat.month] = stat
        }

        // Generate last 12 months
        val monthsToShow = 12

        for (i in (monthsToShow - 1) downTo 0) {
            calendar.time = java.util.Date()
            calendar.add(java.util.Calendar.MONTH, -i)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)

            val monthLabel = monthFormat.format(calendar.time)
            val existingData = existingDataMap[monthLabel]

            if (existingData != null) {
                filledStats.add(existingData)
            } else {
                val zeroPredictions = mapOf(
                    "Healthy" to 0,
                    "Anthracnose" to 0,
                    "Powdery Mildew" to 0,
                    "Ring Spot Virus" to 0
                )
                filledStats.add(MonthlyAnalyticsRequest(monthLabel, zeroPredictions))
            }
        }

        return filledStats
    }

    private fun fillMissingYears(yearlyStats: List<YearlyAnalyticsRequest>): List<YearlyAnalyticsRequest> {
        if (yearlyStats.isEmpty()) return emptyList()

        val yearFormat = java.text.SimpleDateFormat("yyyy", java.util.Locale.ENGLISH)
        val calendar = java.util.Calendar.getInstance()
        val filledStats = mutableListOf<YearlyAnalyticsRequest>()

        // Create a map of existing data
        val existingDataMap = mutableMapOf<String, YearlyAnalyticsRequest>()
        yearlyStats.forEach { stat ->
            existingDataMap[stat.year] = stat
        }

        // Generate last 5 years
        val yearsToShow = 5

        for (i in (yearsToShow - 1) downTo 0) {
            calendar.time = java.util.Date()
            calendar.add(java.util.Calendar.YEAR, -i)
            calendar.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)

            val yearLabel = yearFormat.format(calendar.time)
            val existingData = existingDataMap[yearLabel]

            if (existingData != null) {
                filledStats.add(existingData)
            } else {
                val zeroPredictions = mapOf(
                    "Healthy" to 0,
                    "Anthracnose" to 0,
                    "Powdery Mildew" to 0,
                    "Ring Spot Virus" to 0
                )
                filledStats.add(YearlyAnalyticsRequest(yearLabel, zeroPredictions))
            }
        }

        return filledStats
    }

    private fun parseAndStandardizeDate(dateString: String): String? {
        val targetFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ENGLISH)

        // List of possible date formats your backend might return
        val possibleFormats = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.ENGLISH),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.ENGLISH),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", java.util.Locale.ENGLISH),
            java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.ENGLISH),
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ENGLISH),
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ENGLISH),
            java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.ENGLISH)
        )

        for (format in possibleFormats) {
            try {
                val parsedDate = format.parse(dateString)
                if (parsedDate != null) {
                    return targetFormat.format(parsedDate)
                }
            } catch (e: Exception) {
                // Continue trying other formats
            }
        }

        // If it's a timestamp number (milliseconds), try parsing as long
        try {
            val timestamp = dateString.toLongOrNull()
            if (timestamp != null) {
                val date = java.util.Date(timestamp)
                return targetFormat.format(date)
            }
        } catch (e: Exception) {
            // Continue
        }

        return null
    }


    /** ---------END CHART SETUP **/
}