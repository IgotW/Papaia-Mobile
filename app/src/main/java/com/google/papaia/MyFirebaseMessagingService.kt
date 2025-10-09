package com.google.papaia

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.papaia.activity.DashboardActivity
import com.google.papaia.request.UpdateFcmRequest
import com.google.papaia.response.FcmResponse
import com.google.papaia.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val jwt = prefs.getString("token", null)
        val userId = prefs.getString("id", null)

        if (!jwt.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            fetchLocationAndSendToken(token, userId, jwt)
        }
    }

    private fun fetchLocationAndSendToken(token: String, userId: String, jwt: String) {
        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, send only token without location
            sendFcmTokenToServer(token, null, null, userId, jwt)
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                val lat = location?.latitude
                val lon = location?.longitude
                sendFcmTokenToServer(token, lat, lon, userId, jwt)
            }.addOnFailureListener {
                Log.e("FCM", "Failed to get location: ${it.message}")
                sendFcmTokenToServer(token, null, null, userId, jwt)
            }
        } catch (e: SecurityException) {
            Log.e("FCM", "Location permission missing: ${e.message}")
            sendFcmTokenToServer(token, null, null, userId, jwt)
        }
    }

    private fun sendFcmTokenToServer(fcmToken: String, lat: Double?, lon: Double?, userId: String, jwt: String) {
        val request = UpdateFcmRequest(userId, fcmToken, lat ?: 0.0, lon ?: 0.0)

        RetrofitClient.instance.updateFcmToken("Bearer $jwt", request)
            .enqueue(object : Callback<FcmResponse> {
                override fun onResponse(call: Call<FcmResponse>, response: Response<FcmResponse>) {
                    if (response.isSuccessful) {
                        Log.d("FCM", "Token updated on server successfully")
                    } else {
                        Log.e("FCM", "Server error: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<FcmResponse>, t: Throwable) {
                    Log.e("FCM", "Token update failed: ${t.message}")
                }
            })
    }



    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title ?: "🌱 Daily Tip"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Open the app for today's tip"

        // Save to SharedPreferences
        getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("last_tip", body)
            .apply()

        // Broadcast to update the UI if app is open
        val intent = Intent("DAILY_TIP_RECEIVED")
        intent.putExtra("tip_text", body)
        sendBroadcast(intent)

        showNotification(title, body)

    }

    private fun showNotification(title: String, body: String) {
        val channelId = "daily_tip_channel"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Tips",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }

        // ✅ Intent to open MainActivity when user clicks the notification
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.papaialogo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // ✅ show full text
            .setAutoCancel(true) // ✅ dismiss after clicking
            .setContentIntent(pendingIntent) // ✅ open app on click
            .build()

        nm.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}