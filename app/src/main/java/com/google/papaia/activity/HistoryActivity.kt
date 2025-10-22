package com.google.papaia.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.gesture.Prediction
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.papaia.R
import com.google.papaia.adapter.HistoryAdapter
import com.google.papaia.response.PredictionHistoryResponse
import com.google.papaia.response.PredictionResponse
import com.google.papaia.utils.ApiService
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var bearerToken: String  // Get this from your session/auth manager
    private lateinit var userId: String

    private lateinit var dateFilterDropdown: AutoCompleteTextView
    private lateinit var diseaseFilterDropdown: AutoCompleteTextView

    private lateinit var layout_disease: TextInputLayout
    private lateinit var layout_date: TextInputLayout

    private var historyAdapter: HistoryAdapter? = null
    private var historyList: List<PredictionHistoryResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        listView = findViewById(R.id.listViewPredictions)

        dateFilterDropdown = findViewById(R.id.dropdown_date_filter)
        diseaseFilterDropdown = findViewById(R.id.dropdown_disease_filter)
        layout_disease = findViewById(R.id.layout_disease)
        layout_date = findViewById(R.id.layout_date)

        layout_disease.defaultHintTextColor = ColorStateList.valueOf(Color.BLACK)
        layout_date.defaultHintTextColor = ColorStateList.valueOf(Color.BLACK)

        val button_back = findViewById<ImageView>(R.id.imageview_arrowback_history)

        val prefs = getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
        val user = prefs.getString("userId", "User")
        val token = prefs.getString("token", "")
        bearerToken = "Bearer $token"
        userId = user ?: ""
        // Replace this with the actual token from your login/session manager


        if (userId.isEmpty() || token.isNullOrEmpty()) {
            Toast.makeText(this, "Missing user session data.", Toast.LENGTH_SHORT).show()
            return
        }

        setupFilters()
        getPredictionHistory()

        button_back.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("navigateTo", "home")
            startActivity(intent)
            finish()
        }
    }

    private fun setupFilters() {
        val dateOptions = listOf(
            "All",
            "Today",
            "Yesterday",
            "Last 7 Days",
            "Last 30 Days",
            "This Month",
            "Last Month",
            "This Year",
            "Last Year"
        )

        val diseaseOptions = listOf(
            "All",
            "Healthy",
            "Ring Spot Virus",
            "Anthracnose",
            "Powdery Mildew"
        )

        val dateAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dateOptions)
        val diseaseAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, diseaseOptions)

        dateFilterDropdown.setAdapter(dateAdapter)
        diseaseFilterDropdown.setAdapter(diseaseAdapter)

        // Default = All
        dateFilterDropdown.setText("All", false)
        diseaseFilterDropdown.setText("All", false)

        dateFilterDropdown.setOnItemClickListener { _, _, _, _ -> applyFilters() }
        diseaseFilterDropdown.setOnItemClickListener { _, _, _, _ -> applyFilters() }
    }

    private fun getPredictionHistory() {
        RetrofitClient.instance.getPredictionHistory(bearerToken).enqueue(object :
            Callback<List<PredictionHistoryResponse>> {
            override fun onResponse(
                call: Call<List<PredictionHistoryResponse>>,
                response: Response<List<PredictionHistoryResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val historyList = response.body()!!
                    historyAdapter = HistoryAdapter(this@HistoryActivity, historyList)
                    listView.adapter = historyAdapter

                    applyFilters()
                } else {
                    Toast.makeText(this@HistoryActivity, "No history found or unauthorized.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PredictionHistoryResponse>>, t: Throwable) {
                Toast.makeText(this@HistoryActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun applyFilters() {
        val selectedDate = dateFilterDropdown.text.toString()
        val selectedDisease = diseaseFilterDropdown.text.toString()

        historyAdapter?.applyFilter(selectedDisease, selectedDate)
    }
}