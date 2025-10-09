package com.google.papaia.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.papaia.R
import com.google.papaia.request.LatLonRequest
import com.google.papaia.response.TipResponse
import retrofit2.Response

class DailyTipWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return Result.failure()

        // STEP 1: Show cached tip immediately
        val cachedTip = prefs.getString("last_tip", "Your daily tip will appear here.")
        showNotification("Daily Farm Tip", cachedTip ?: "Stay tuned for a new tip!")

        // STEP 2: Get stored location
        val lat = prefs.getFloat("last_lat", 0f).toDouble()
        val lon = prefs.getFloat("last_lon", 0f).toDouble()

        return try {
            val response: Response<TipResponse> = if (lat != 0.0 && lon != 0.0) {
                // Location-based tip
                val request = LatLonRequest(lat, lon)
                RetrofitClient.instance.generateDailyTip("Bearer $token", request).execute()
            } else {
                // Fallback to simple daily tip
                RetrofitClient.instance.getDailyTip("Bearer $token").execute()
            }

            if (response.isSuccessful && response.body() != null) {
                val tip = response.body()?.tip?.text ?: "Here’s your tip!"
                showNotification("Daily Farm Tip", tip)

                // Save for next time
                prefs.edit().putString("last_tip", tip).apply()

                Result.success()
            } else {
                Log.e("DailyTipWorker", "Error: ${response.code()} - ${response.message()}")
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "daily_tip_channel"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Daily Tips", NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.papaialogo) // replace with your logo
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // support long text
            .setAutoCancel(true)
            .build()

        nm.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}
