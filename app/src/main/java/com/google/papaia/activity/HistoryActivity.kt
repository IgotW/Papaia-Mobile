package com.google.papaia.activity

import android.content.Intent
import android.gesture.Prediction
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        listView = findViewById(R.id.listViewPredictions)
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

        getPredictionHistory()

        button_back.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("navigateTo", "home")
            startActivity(intent)
            finish()
        }
    }
    private fun getPredictionHistory() {
        RetrofitClient.instance.getPredictionHistory(userId, bearerToken).enqueue(object :
            Callback<List<PredictionHistoryResponse>> {
            override fun onResponse(
                call: Call<List<PredictionHistoryResponse>>,
                response: Response<List<PredictionHistoryResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val historyList = response.body()!!
                    listView.adapter = HistoryAdapter(this@HistoryActivity, historyList)
                } else {
                    Toast.makeText(this@HistoryActivity, "No history found or unauthorized.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PredictionHistoryResponse>>, t: Throwable) {
                Toast.makeText(this@HistoryActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}