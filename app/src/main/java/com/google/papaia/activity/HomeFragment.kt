package com.google.papaia.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.messaging.FirebaseMessaging
import com.google.papaia.R
import com.google.papaia.adapter.HistoryAdapter
import com.google.papaia.adapter.HomeHistoryAdapter
import com.google.papaia.request.AnalyticsStatRequest
import com.google.papaia.request.LatLonRequest
import com.google.papaia.request.UpdateFcmRequest
import com.google.papaia.response.DailyAnalyticsResponse
import com.google.papaia.response.DailyTipResponse
import com.google.papaia.response.FarmDetailsResponse
import com.google.papaia.response.FcmResponse
import com.google.papaia.response.IdentificationStatsResponse
import com.google.papaia.response.PredictionHistoryResponse
import com.google.papaia.response.TipResponse
import com.google.papaia.response.TodaysPredictionResponse
import com.google.papaia.utils.CustomMarkerView
import com.google.papaia.utils.DailyTipWorker
import com.google.papaia.utils.RetrofitClient
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var bearerToken: String
    private lateinit var userId: String
    private lateinit var lineChart: LineChart
    private lateinit var analytics_summary: TextView
    private lateinit var txtview_username: TextView
    private lateinit var txtDailyTips: TextView
    private lateinit var imageProfile: CircleImageView
    private lateinit var listViewScanHistory: ListView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var button_seemore: TextView
    private lateinit var todayScans: TextView
    private lateinit var healthPercent: TextView
    private lateinit var start_scanning: TextView
    private lateinit var farmName: TextView
    private lateinit var farmLocation: TextView
    private lateinit var countHealthy: TextView
    private lateinit var countDiseased: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var dailyTipReceiver: BroadcastReceiver? = null


//    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
//
//    // Auto-refresh variables
//    private val refreshHandler = Handler(Looper.getMainLooper())
//    private val refreshInterval = 30000L // 30 seconds
//    private var refreshRunnable: Runnable? = null

    private var activeFragment: Fragment? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Calculate delay until next 6AM
//        val now = Calendar.getInstance()
//        val target = Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, 6)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//            if (before(now)) {
//                add(Calendar.DAY_OF_YEAR, 1)
//            }
//        }
//        val initialDelay = target.timeInMillis - now.timeInMillis
//
//        // Schedule daily job
//        val workRequest = PeriodicWorkRequestBuilder<DailyTipWorker>(1, TimeUnit.DAYS)
//            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
//            .build()
//
//        WorkManager.getInstance(requireContext())
//            .enqueueUniquePeriodicWork(
//                "DailyTipWork",
//                ExistingPeriodicWorkPolicy.UPDATE,
//                workRequest
//            )


        initViews(view)

        // Get username from SharedPreferences
        val prefs = requireContext().getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        val username = prefs.getString("username", "User")
        val token = prefs.getString("token", "")
        val user = prefs.getString("userId", "User")
        bearerToken = "Bearer $token"
        userId = user ?: ""

        // Update the TextView
        txtview_username.text = "Hello, $username"

        button_seemore.setOnClickListener {
            startActivity(
                Intent(context, HistoryActivity::class.java)
            )
        }
        start_scanning.setOnClickListener {
            (requireActivity() as DashboardActivity).changeTab(1)
        }

        checkLocationPermission()

        // Register receiver for daily tip updates from FCM
        dailyTipReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val tipText = intent?.getStringExtra("tip_text")
                if (!tipText.isNullOrEmpty()) {
                    txtDailyTips.text = tipText
                    Log.d("HomeFragment", "Daily tip updated from FCM: $tipText")
                }
            }
        }

        val filter = IntentFilter("DAILY_TIP_RECEIVED")

        // Use ContextCompat for proper API compatibility
        ContextCompat.registerReceiver(
            requireContext(),
            dailyTipReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )



//        RetrofitClient.instance.getWeeklyAnalytics(bearerToken).enqueue(object :
//            Callback<DailyAnalyticsResponse> {
//            override fun onResponse(
//                call: Call<DailyAnalyticsResponse>,
//                response: Response<DailyAnalyticsResponse>
//            ) {
//                if (response.isSuccessful && response.body() != null) {
//                    val weeklyStats = response.body()!!.weeklyStats
//                    val entries = ArrayList<Entry>()
//                    val labels = ArrayList<String>()
//
//                    weeklyStats.forEachIndexed { index, stat ->
//                        val total = stat.predictions.values.sum()
//                        entries.add(Entry(index.toFloat(), total.toFloat()))
//                        labels.add(stat.week) // Already a readable format like "Jul 21 - Jul 27, 2025"
//                    }
//
//                    val dataSet = LineDataSet(entries, "Predictions per Week")
//                    dataSet.color = android.graphics.Color.BLUE
//                    dataSet.valueTextColor = android.graphics.Color.BLACK
//                    dataSet.circleRadius = 5f
//                    dataSet.setCircleColor(android.graphics.Color.RED)
//                    dataSet.lineWidth = 2f
//
//                    val lineData = LineData(dataSet)
//                    lineChart.data = lineData
//
//                    val xAxis = lineChart.xAxis
//                    xAxis.position = XAxis.XAxisPosition.BOTTOM
//                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)
//                    xAxis.granularity = 1f
//                    xAxis.setDrawGridLines(false)
//                    xAxis.labelRotationAngle = -45f
//                    xAxis.textSize = 10f
//
//                    lineChart.axisRight.isEnabled = false
//                    lineChart.description.isEnabled = false
//                    lineChart.animateX(1000)
//                    lineChart.invalidate()
//                } else {
//                    Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
//                }
//            }

//            override fun onFailure(call: Call<DailyAnalyticsResponse>, t: Throwable) {
//                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
    }

    private fun initViews(view: View) {
        txtview_username = view.findViewById<TextView>(R.id.txtview_home_username)
        txtDailyTips  = view.findViewById<TextView>(R.id.txtview_dailytips)
        button_seemore = view.findViewById<TextView>(R.id.home_button_seemore)
        listViewScanHistory = view.findViewById<ListView>(R.id.listViewScanHistory)
        emptyStateContainer = view.findViewById(R.id.empty_state_container)
        todayScans = view.findViewById(R.id.scans_count)
        healthPercent = view.findViewById(R.id.health_percent)
        start_scanning = view.findViewById(R.id.btn_start_scanning)
        farmName = view.findViewById(R.id.txtview_farm_name)
        farmLocation = view.findViewById(R.id.txtview_farm_location)
        countHealthy = view.findViewById(R.id.txtview_count_healthy)
        countDiseased = view.findViewById(R.id.txtview_count_diseased)
        imageProfile = view.findViewById(R.id.profile_image)

        lineChart = view.findViewById(R.id.lineChart)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

//        // Add SwipeRefreshLayout - make sure to add this to your XML layout
//        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
//
//        // Setup pull-to-refresh
//        swipeRefreshLayout.setOnRefreshListener {
//            refreshData()
//        }
//
//        // Customize refresh indicator colors
//        swipeRefreshLayout.setColorSchemeResources(
//            R.color.primary,
//            R.color.secondary,
//            R.color.tertiary
//        )
    }


    override fun onResume() {
        super.onResume()

        // Load cached tip first (in case FCM sent one while app was closed)
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val cachedTip = prefs.getString("last_tip", null)
        if (!cachedTip.isNullOrEmpty()) {
            txtDailyTips.text = cachedTip
        }

        getFarmDetails(bearerToken)

//        fetchLocationAndGenerateTip(bearerToken)
//        fetchDailyTip(bearerToken)
        getCountScans(bearerToken)
        getDailyAnalytics(bearerToken)
        getStats()
        getPredictionHistory()
        updateProfileImage()
        updateFcmToken()
    }

    private fun updateProfileImage() {
        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val profileImage = prefs.getString("profileImage", "")
        if (!profileImage.isNullOrEmpty()) {
            Glide.with(this).load(profileImage).placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person).circleCrop().into(imageProfile)
        } else {
            imageProfile.setImageResource(R.drawable.ic_person)
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fetchHybridDailyTip()
        }
    }

    private fun fetchHybridDailyTip() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    prefs.edit().putFloat("last_lat", location.latitude.toFloat())
                        .putFloat("last_lon", location.longitude.toFloat())
                        .apply()
                    generateDailyTip(location.latitude, location.longitude)
                } else {
                    fetchDailyTip(bearerToken)
                }
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchHybridDailyTip()
            } else {
                Toast.makeText(requireContext(), "Location permission is required for daily tips", Toast.LENGTH_SHORT).show()
                fetchDailyTip(bearerToken) // ✅ pass the token here too
            }
        }
    }

    private fun generateDailyTip(lat: Double, lon: Double) {
        val request = LatLonRequest(lat, lon)
        RetrofitClient.instance.generateDailyTip(bearerToken, request).enqueue(object : Callback<TipResponse> {
            override fun onResponse(call: Call<TipResponse>, response: Response<TipResponse>) {
                if (response.isSuccessful) {
                    val tipText = response.body()?.tip?.text ?: "No tip for today"
                    txtDailyTips.text = tipText
                    requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                        .edit().putString("last_tip", tipText).apply()
                }
            }

            override fun onFailure(call: Call<TipResponse>, t: Throwable) {
                Log.e("DailyTip", "Failed: ${t.message}")
            }
        })
    }

    private fun fetchDailyTip(token: String) {
        RetrofitClient.instance.getDailyTip(token)
            .enqueue(object : Callback<TipResponse> {
                override fun onResponse(
                    call: Call<TipResponse>,
                    response: Response<TipResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()
                        Log.d("DailyTip", "Full Response: $body")

                        val tip = body?.tip?.text ?: "No tip for today"
                        view?.findViewById<TextView>(R.id.txtview_dailytips)?.text = tip
                    } else {
                        Log.e("DailyTip", "Failed: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<TipResponse>, t: Throwable) {
                    Log.e("DailyTip", "Error: ${t.message}")
                }
            })
    }

    private fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM", "FCM token: $fcmToken") // <-- log token
                val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                val lastSentToken = prefs.getString("last_fcm_token", null)
                if (fcmToken != null && fcmToken != lastSentToken) {
                    val lat = prefs.getFloat("last_lat", 0f).toDouble()
                    val lon = prefs.getFloat("last_lon", 0f).toDouble()
                    val request = UpdateFcmRequest(userId, fcmToken, lat, lon)
                    RetrofitClient.instance.updateFcmToken(bearerToken, request)
                        .enqueue(object : Callback<FcmResponse> {
                            override fun onResponse(call: Call<FcmResponse>, response: Response<FcmResponse>) {
                                if (response.isSuccessful) {
                                    Log.d("FCM", "Token updated on server successfully")
                                    prefs.edit().putString("last_fcm_token", fcmToken).apply()
                                } else {
                                    Log.e("FCM", "Server error: ${response.code()} - ${response.message()}")
                                }
                            }

                            override fun onFailure(call: Call<FcmResponse>, t: Throwable) {
                                Log.e("FCM", "Retrofit failure: ${t.message}")
                            }
                        })
                } else {
                    Log.d("FCM", "Token not changed, no need to update")
                }
            } else {
                Log.e("FCM", "Failed to get FCM token: ${task.exception}")
            }
        }
    }


    private fun getFarmDetails(token: String) {
        if (token != null) {
            RetrofitClient.instance.getFarmDetails(token)
                .enqueue(object : Callback<FarmDetailsResponse> {
                    override fun onResponse(
                        call: Call<FarmDetailsResponse>,
                        response: Response<FarmDetailsResponse>
                    ) {
                        if (response.isSuccessful) {
                            val farm = response.body()
                            if (farm != null) {
                                // Show data in TextViews
                                farmName.text = farm.farmName
                                farmLocation.text = farm.farmLocation
                            }
                        } else {
//                            Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                            Log.e("DailyTip", "Error: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<FarmDetailsResponse>, t: Throwable) {
                        Log.e("DailyTip", "Failed: ${t.message}")
//                        Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }


    private fun getDailyAnalytics(token: String){
        RetrofitClient.instance.getDailyAnalytics(token).enqueue(object : Callback<DailyAnalyticsResponse> {
            override fun onResponse(
                call: Call<DailyAnalyticsResponse>,
                response: Response<DailyAnalyticsResponse>
            ) {
                if (!isAdded) return  // prevent crash if fragment is detached

                if (response.isSuccessful && response.body() != null) {
                    val dailyStats = response.body()!!.dailyStats
                    setupLineChart(dailyStats, requireContext())
                }
            }

            override fun onFailure(call: Call<DailyAnalyticsResponse>, t: Throwable) {
                Log.e("Chart", "API error: ${t.message}")
            }
        })
    }

    /** ---------------- CHART SETUP ---------------- **/
    private fun setupLineChart(dailyStats: List<AnalyticsStatRequest>, context: Context) {
        val diseaseMap = mutableMapOf<String, MutableList<Entry>>()
        val labels = mutableListOf<String>()

//        val limitedStats = if (dailyStats.size > 5) {
//            dailyStats.takeLast(5)
//        } else {
//            dailyStats
//        }
        val filledDailyStats = fillMissingDays(dailyStats, 5)

        filledDailyStats.forEachIndexed { index, stat ->
            val convertedDate = convertDateFormat(stat.day)
            labels.add(convertedDate)
            stat.predictions.forEach { (disease, count) ->
                diseaseMap.getOrPut(disease) { mutableListOf() }
                    .add(Entry(index.toFloat(), count.toFloat()))
            }
//            diseaseMap[disease]?.add(Entry(index.toFloat(), count.toFloat()))
        }

        val colorMap = mapOf(
            "Healthy" to Color.parseColor("#4CAF50"),
            "Anthracnose" to Color.parseColor("#F44336"),
            "Powdery Mildew" to Color.parseColor("#2196F3"),
            "Ring Spot Virus" to Color.parseColor("#FF9800")
        )

        // Get disease names in order for the marker
        val diseaseNames = diseaseMap.keys.toList()

        val lineDataSets = diseaseMap.entries.map { (key, value) ->
//            val formattedKey = key.replace("_", " ")
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

        // ✅ Add custom marker view
        val markerView =
            CustomMarkerView(context, R.layout.custom_marker_view, labels, diseaseNames)
        markerView.chartView = lineChart
        lineChart.marker = markerView

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            labelRotationAngle = -45f
            setDrawGridLines(false)
            // Limit labels to prevent crowding
            setLabelCount(Math.min(labels.size, 5), false)
            setAvoidFirstLastClipping(true)
            // Add more space between x-axis and legend
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
            //Add margin between legend and chart
            yOffset = 10f
            xOffset = 0f
            // Ensure legend has enough space
            maxSizePercent = 1.0f
            formSize = 8f
            xEntrySpace = 4f
            yEntrySpace = 2f
        }

//        lineChart.description.text = "Daily Disease Analytics"
//        lineChart.animateX(1000)
//        lineChart.setVisibleXRangeMaximum(5f)
//        lineChart.moveViewToX(0f)

//        val markerView = CustomMarkerView(context, R.layout.custom_marker)
//        markerView.chartView = lineChart
//        lineChart.marker = markerView


        // Disable chart description to save space
        lineChart.description.isEnabled = false

        // Set extra offsets to provide more space around the chart
        lineChart.setExtraOffsets(15f, 20f, 15f, 50f) // left, top, right, bottom

        lineChart.animateX(1000)

        // Disable scrolling and scaling to fit all data on screen
        lineChart.setTouchEnabled(true)

        lineChart.isDragEnabled = false
        lineChart.setScaleEnabled(false)
        lineChart.setPinchZoom(false)

        // ✅ Highlight on tap
        lineChart.isHighlightPerTapEnabled = true
        lineChart.isHighlightPerDragEnabled = false

        lineChart.invalidate()
    }

    private fun convertDateFormat(dateString: String): String {
        return try {
            // Parse the input format "Jul 24, 2025"
            val inputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ENGLISH)
            // Format to output "07/24/25"
            val outputFormat = java.text.SimpleDateFormat("MM/dd/yy", java.util.Locale.ENGLISH)

            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return dateString)
        } catch (e: Exception) {
            // If parsing fails, return original string
            dateString
        }
    }

    private fun fillMissingDays(dailyStats: List<AnalyticsStatRequest>, daysToShow: Int): List<AnalyticsStatRequest> {
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ENGLISH)
        val filledStats = mutableListOf<AnalyticsStatRequest>()

        // Create a map of existing data for quick lookup - handle various date formats
        val existingDataMap = mutableMapOf<String, AnalyticsStatRequest>()

        // Add existing data with multiple possible date formats
        dailyStats.forEach { stat ->
            // First, add the original key
            existingDataMap[stat.day] = stat

            // Try to parse and reformat the date to ensure consistency
            val standardizedDate = parseAndStandardizeDate(stat.day)
            if (standardizedDate != null) {
                existingDataMap[standardizedDate] = stat
            }
        }

        // Generate the last 'daysToShow' days including today
        for (i in (daysToShow - 1) downTo 0) {
            calendar.time = java.util.Date()
            calendar.add(java.util.Calendar.DAY_OF_MONTH, -i)
            val dateString = dateFormat.format(calendar.time)

            // Check if we have data for this date
            val existingData = existingDataMap[dateString]

            if (existingData != null) {
                // Use existing data but with standardized date format
                val standardizedData = AnalyticsStatRequest(dateString, existingData.predictions)
                filledStats.add(standardizedData)
            } else {
                // Create zero data for missing day
                val zeroPredictions = mapOf(
                    "Healthy" to 0,
                    "Anthracnose" to 0,
                    "Powdery Mildew" to 0,
                    "Ring Spot Virus" to 0
                )
                filledStats.add(AnalyticsStatRequest(dateString, zeroPredictions))
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

    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return

        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }

        // Convert dp to pixels
        val extraPaddingPx = (32 * listView.context.resources.displayMetrics.density).toInt()

        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1)) + extraPaddingPx
        listView.layoutParams = params
        listView.requestLayout()
    }

    private fun getPredictionHistory() {
        RetrofitClient.instance.getPredictionHistory(bearerToken).enqueue(object :
            Callback<List<PredictionHistoryResponse>> {
            override fun onResponse(
                call: Call<List<PredictionHistoryResponse>>,
                response: Response<List<PredictionHistoryResponse>>
            ) {

                if (!isAdded || context == null) {
                    return  // Fragment not attached anymore, just ignore result
                }

                if (response.isSuccessful && response.body() != null) {
                    val historyList = response.body()!!
                    val limitedList = historyList.take(3)

                    listViewScanHistory.adapter = HomeHistoryAdapter(requireContext(), limitedList)
                    setListViewHeightBasedOnChildren(listViewScanHistory)
                    emptyStateContainer.setVisibility(View.GONE);
                    listViewScanHistory.setVisibility(View.VISIBLE);
                    button_seemore.setVisibility(View.VISIBLE);
                } else {
//                    Toast.makeText(requireContext(), "No history found or unauthorized.", Toast.LENGTH_SHORT).show()
                    emptyStateContainer.setVisibility(View.VISIBLE);
                    listViewScanHistory.setVisibility(View.GONE);
                    button_seemore.setVisibility(View.GONE);
                }
            }

            override fun onFailure(call: Call<List<PredictionHistoryResponse>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCountScans(token: String){
        RetrofitClient.instance.getTodaysPredictionsCount(bearerToken)
            .enqueue(object : Callback<TodaysPredictionResponse> {
                override fun onResponse(
                    call: Call<TodaysPredictionResponse>,
                    response: Response<TodaysPredictionResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val todayCount = response.body()!!.count
                        todayScans.text = todayCount.toString()
                    } else {
                        todayScans.text = "0"
                        Log.e("TodayScans", "Error: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<TodaysPredictionResponse>, t: Throwable) {
                    todayScans.text = "0"
                    Log.e("TodayScans", "Network error: ${t.message}")
                }
            })
    }


    fun updateAnalytics(bearerToken: String) {
        getCountScans(bearerToken)
        getDailyAnalytics(bearerToken)
        getPredictionHistory()
    }

    private fun getStats(){
        RetrofitClient.instance.getFarmerIdentificationStats(bearerToken)
            .enqueue(object : Callback<IdentificationStatsResponse> {
                override fun onResponse(
                    call: Call<IdentificationStatsResponse>,
                    response: Response<IdentificationStatsResponse>
                ) {
                    if (!isAdded) return  // prevent crash if fragment is detached

                    if (response.isSuccessful) {
                        val stats = response.body()
                        stats?.let {
                            countHealthy.text = "${it.healthy} Healthy"
                            countDiseased.text = "${it.diseased} Diseased"

                            val percentValue = it.healthyPercentage
                                ?.replace("%", "")   // remove %
                                ?.toDoubleOrNull()  // convert to double
                                ?.roundToInt()       // round to nearest int
                                ?: 0
                            healthPercent.text = "$percentValue%"
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<IdentificationStatsResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

//    private fun setupAutoRefresh() {
//        refreshRunnable = object : Runnable {
//            override fun run() {
//                // Only refresh if fragment is visible and attached
//                if (isAdded && !isDetached && view != null && userVisibleHint) {
//                    Log.d("HomeFragment", "Auto-refreshing data...")
//                    loadAllData()
//                }
//
//                // Schedule next refresh
//                refreshHandler.postDelayed(this, refreshInterval)
//            }
//        }
//
//        // Start the auto-refresh cycle
//        refreshHandler.postDelayed(refreshRunnable!!, refreshInterval)
//    }
//
//    private fun stopAutoRefresh() {
//        refreshRunnable?.let {
//            refreshHandler.removeCallbacks(it)
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // Resume auto-refresh when fragment becomes visible
//        if (::bearerToken.isInitialized && bearerToken.isNotEmpty()) {
//            setupAutoRefresh()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        // Pause auto-refresh when fragment is not visible
//        stopAutoRefresh()
//    }
//
    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up to prevent memory leaks
//        stopAutoRefresh()

//     Unregister receiver to prevent memory leaks
            dailyTipReceiver?.let {
                try {
                    requireContext().unregisterReceiver(it)
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error unregistering receiver: ${e.message}")
                }
            }
    }
//
//    // Method to manually refresh (can be called from parent activity)
//    fun manualRefresh() {
//        if (::bearerToken.isInitialized && bearerToken.isNotEmpty()) {
//            swipeRefreshLayout.isRefreshing = true
//            refreshData()
//        }
//    }
//
//    // Method to change refresh interval
//    fun setRefreshInterval(intervalMs: Long) {
//        stopAutoRefresh()
//        refreshHandler.postDelayed(refreshRunnable!!, intervalMs)
//    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}