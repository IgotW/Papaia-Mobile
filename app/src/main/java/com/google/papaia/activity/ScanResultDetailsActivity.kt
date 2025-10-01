package com.google.papaia.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.papaia.R
import com.google.papaia.response.ScanResult
import com.google.papaia.utils.ApiService
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanResultDetailsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var shareButton: ImageView
    private lateinit var plantImage: ImageView
    private lateinit var diseaseStatus: TextView
    private lateinit var diseaseName: TextView
    private lateinit var confidenceText: TextView
    private lateinit var treatmentButton: MaterialButton
    private lateinit var treatmentTextView: TextView
    private lateinit var treatmentCard: androidx.cardview.widget.CardView
    private lateinit var scanAgainButton: MaterialButton
    private lateinit var viewHistoryButton: MaterialButton

    private var currentScanResult: ScanResult? = null
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan_result_details)

        initViews()
        setupClickListeners()

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val username = prefs.getString("username", "User")
        val token = prefs.getString("token", "")
        val user = prefs.getString("userId", "User")
        val bearerToken = "Bearer $token"

        // Get prediction ID from intent and fetch data
        val predictionId = intent.getStringExtra("analysis_id")
        if (predictionId != null) {
            fetchPredictionData(predictionId, bearerToken)
        } else {
            Toast.makeText(this, "Error: No prediction ID provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        shareButton = findViewById(R.id.shareButton)
        plantImage = findViewById(R.id.plantImage)
        diseaseStatus = findViewById(R.id.diseaseStatus)
        diseaseName = findViewById(R.id.diseaseName)
        confidenceText = findViewById(R.id.confidenceText)
        treatmentButton = findViewById(R.id.treatmentButton)
        treatmentTextView = findViewById(R.id.treatmentTextView)
        treatmentCard = findViewById(R.id.treatmentCard)
        scanAgainButton = findViewById(R.id.scanAgainButton)
        viewHistoryButton = findViewById(R.id.viewHistoryButton)
    }

    private fun fetchPredictionData(predictionId: String, tokenId: String) {
        // Show loading state
        diseaseStatus.text = "Loading prediction data..."

        RetrofitClient.instance.getPredictionById(predictionId, tokenId).enqueue(object :
            Callback<ScanResult> {
            override fun onResponse(call: Call<ScanResult>, response: Response<ScanResult>) {
                if (response.isSuccessful) {
                    response.body()?.let { scanResult ->
                        currentScanResult = scanResult
                        setupData(scanResult)
                    } ?: run {
                        showError("Empty response from server")
                    }
                } else {
                    when (response.code()) {
                        404 -> showError("Prediction not found")
                        500 -> showError("Server error occurred")
                        else -> showError("Error: ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<ScanResult>, t: Throwable) {
                Log.e("ScanResult", "API call failed", t)
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun setupData(scanResult: ScanResult) {

        Log.d("ScanResult", "Full scan result: $scanResult")
        Log.d("ScanResult", "Image URL: '${scanResult.imageUrl}'")
        // Load image from server URL
        loadImageFromUrl(scanResult.imageUrl)

        // Set disease status
        diseaseStatus.text = "⚠ Diseased"
        diseaseStatus.setTextColor(ContextCompat.getColor(this, R.color.tertiary))

        diseaseName.text = scanResult.prediction

        // Determine severity level based on confidence
        val severity = when {
            scanResult.confidence >= 0.8 -> "High"
            scanResult.confidence >= 0.6 -> "Medium"
            else -> "Low"
        }

//        confidenceText.text = severity
//        confidenceText.setTextColor(when(severity) {
//            "High" -> ContextCompat.getColor(this, R.color.red)
//            "Medium" -> ContextCompat.getColor(this, R.color.orange)
//            else -> ContextCompat.getColor(this, R.color.green)
//        })

        confidenceText.setTextColor(
            when {
                scanResult.confidence >= 0.9 -> ContextCompat.getColor(this, R.color.green)
                scanResult.confidence >= 0.7 -> ContextCompat.getColor(this, R.color.orange)
                else -> ContextCompat.getColor(this, R.color.red)
            }
        )

        // Extract description and show confidence
        val description = extractDescription(scanResult.suggestion)
        val confidencePercentage = (scanResult.confidence * 100).toInt()
//        confidenceText.text = "${description.joinToString("\n")}\n\nConfidence: $confidencePercentage%"
            confidenceText.text = "$confidencePercentage%"
    }

    private fun loadImageFromUrl(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            // fallback if no image is provided
            plantImage.setImageResource(R.drawable.image_no_content)
            return
        }

//        val fullUrl = "https://papaiaapi.onrender.com${imageUrl}"
//
//        Glide.with(this)
//            .load(fullUrl)
//            .placeholder(R.drawable.image_no_content)
//            .error(R.drawable.image_no_content)
//            .into(plantImage)

        // Image with Glide
        val fullUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            "https://papaiaapi.onrender.com${imageUrl}"
        }

        Glide.with(this)
            .load(fullUrl)
            .placeholder(R.drawable.image_no_content)
            .error(R.drawable.image_no_content)
            .centerCrop()
            .into(plantImage)

    }

    private fun extractDescription(suggestion: String?): List<String> {
        if (suggestion.isNullOrBlank()) {
            // return a default or empty list
            return listOf("No suggestions available.")
        }

        // Example: split into lines or sections
        return suggestion.split("\n\n")
    }

    private fun setupTreatmentText() {
        currentScanResult?.let { scanResult ->
            val formattedSuggestions = formatSuggestions(scanResult.suggestion)
            treatmentTextView.text = formattedSuggestions
        }
    }

//    private fun formatSuggestions(suggestion: String?): String {
//        if (suggestion.isNullOrBlank()) {
//            return "No treatment suggestions available."
//        }
//
//        // Example formatting
//        return suggestion
//            .replace("## ", "")   // safe now
//            .replace("*", "•")
//    }

    //String -> CharSequence
    private fun formatSuggestions(suggestion: String?): CharSequence {
        if (suggestion.isNullOrBlank()) {
            return "No treatment suggestions available."
        }

        // Split by '*' (used as bullet points in your backend response)
        val steps = suggestion.split("*")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

//        return steps.mapIndexed { index, step ->
//            "${getCircledNumber(index + 1)} $step"
//        }.joinToString("\n\n")
        // Format each step with bold "Step ①" then the content
        val formatted = steps.mapIndexed { index, step ->
            "<b>🌱 Step ${getCircledNumber(index + 1)}</b><br>${step}"
        }.joinToString("<br><br>")

        // Convert HTML string to styled text
        return Html.fromHtml(formatted, Html.FROM_HTML_MODE_LEGACY)
    }


    private fun formatSection(section: String, sectionIndex: Int): String {
        val lines = section.split("\n").filter { it.trim().isNotEmpty() }

        if (lines.isEmpty()) return ""

//        val title = lines.first().trim()
        val content = lines.drop(1)
//
//        // Format section title with modern emoji indicators
        val formattedTitle = when {
            title.contains("Understanding", ignoreCase = true) ||
                    title.contains("Enemy", ignoreCase = true) -> "🔍 $title"

            title.contains("Immediate", ignoreCase = true) ||
                    title.contains("Action", ignoreCase = true) -> "⚡ $title"

            title.contains("Tips", ignoreCase = true) ||
                    title.contains("Manage", ignoreCase = true) -> "💡 $title"

            else -> "📋 $title"
        }

        // Format content with modern bullet points and numbering
        val formattedContent = if (content.isNotEmpty()) {
            content.mapIndexed { index, line ->
                val cleanLine = line.trim()
                    .removePrefix("•")
                    .removePrefix("-")
                    .removePrefix("*")
                    .trim()

                // Number every item (1, 2, 3…)
                if (cleanLine.isNotEmpty()) {
                    "${index + 1}. $cleanLine"
                } else {
                    cleanLine
                }
            }.joinToString("\n")
        } else ""

        return if (formattedContent.isNotEmpty()) {
            "$formattedTitle\n$formattedContent"
        } else {
            formattedTitle
        }
    }

    private fun getCircledNumber(number: Int): String {
        return when (number) {
            1 -> "①"
            2 -> "②"
            3 -> "③"
            4 -> "④"
            5 -> "⑤"
            6 -> "⑥"
            7 -> "⑦"
            8 -> "⑧"
            9 -> "⑨"
            10 -> "⑩"
            else -> "⊙" // Generic circle for numbers > 10
        }
    }

//    private fun createStepDrawable(context: Context, number: Int): Drawable {
//        val size = 80 // px
//        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//
//        // Draw circle
//        val paintCircle = Paint().apply {
//            color = Color.parseColor("#0B6E4F") // dark green
//            isAntiAlias = true
//        }
//        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintCircle)
//
//        // Draw number
//        val paintText = Paint().apply {
//            color = Color.WHITE
//            textSize = 36f
//            textAlign = Paint.Align.CENTER
//            isAntiAlias = true
//        }
//        val yPos = (canvas.height / 2 - (paintText.descent() + paintText.ascent()) / 2)
//        canvas.drawText(number.toString(), size / 2f, yPos, paintText)
//
//        return BitmapDrawable(context.resources, bitmap)
//    }


    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        shareButton.setOnClickListener {
            currentScanResult?.let {
                shareResult(it)
            } ?: run {
                Toast.makeText(this, "No data to share", Toast.LENGTH_SHORT).show()
            }
        }

//        treatmentButton.setOnClickListener {
//            // Toggle treatment suggestions visibility
//            if (treatmentCard.visibility == View.VISIBLE) {
//                treatmentCard.visibility = View.GONE
//                treatmentButton.text = "⚡ Suggested Treatment"
//            } else {
//                treatmentCard.visibility = View.VISIBLE
//                treatmentButton.text = "⚡ Hide Treatment"
//                setupTreatmentText()
//            }
//        }
        treatmentButton.setOnClickListener {
            toggleTreatmentCard()
        }



        scanAgainButton.setOnClickListener {
            // Navigate back to camera/scan activity
            finish()
        }

        viewHistoryButton.setOnClickListener {
            // Navigate to history activity
            Toast.makeText(this, "Opening scan history...", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun toggleTreatmentCard() {
        isExpanded = !isExpanded

        if (isExpanded) {
            treatmentCard.visibility = View.VISIBLE
            setupTreatmentText()
            // Change to up arrow when expanded
            treatmentButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_up)
        } else {
            treatmentCard.visibility = View.GONE
            // Change to down arrow when collapsed
            treatmentButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_down)
        }
    }

    private fun shareResult(scanResult: ScanResult) {
        val shareText = """
            Plant Disease Detection Result
            
            Disease: ${scanResult.prediction}
            Confidence: ${(scanResult.confidence * 100).toInt()}%
            Detected on: ${scanResult.timestamp}
            
            Scan ID: ${scanResult.id}
        """.trimIndent()

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share scan result"))
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        diseaseStatus.text = "Error loading data"
        diseaseStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
    }
}