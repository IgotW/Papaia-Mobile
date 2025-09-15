package com.google.papaia.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.papaia.R
import com.google.papaia.request.LatLonRequest

class DailyTipWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return Result.failure()

        // Get stored location
        val lat = prefs.getFloat("last_lat", 0f).toDouble()
        val lon = prefs.getFloat("last_lon", 0f).toDouble()
        val request = LatLonRequest(lat, lon)

        return try {
            val response = RetrofitClient.instance.generateDailyTip("Bearer $token", request).execute()
            if (response.isSuccessful) {
                val tip = response.body()?.tip?.text ?: "Here’s your tip!"
                showNotification("Daily Farm Tip", tip)
                Result.success()
            } else {
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

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.papaialogo) // replace with your logo
            .setAutoCancel(true)
            .build()

        nm.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}
