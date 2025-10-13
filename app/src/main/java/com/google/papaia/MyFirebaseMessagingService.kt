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

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "daily_tip_channel"
        private const val CHANNEL_NAME = "Daily Papaya Tips"
    }

    // ✅ UPDATED: Called when FCM token is refreshed
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: ${token.take(20)}...")

        // Save token locally first
        getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("last_fcm_token", token)
            .apply()

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val jwt = prefs.getString("token", null)
        val userId = prefs.getString("userId", null)

        if (!jwt.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            Log.d(TAG, "User authenticated, updating token on server")
            fetchLocationAndSendToken(token, userId, jwt)
        } else {
            Log.w(TAG, "User not logged in, token will be sent on next login")
        }
    }

    // ✅ UPDATED: Better location fetching with fallback
    private fun fetchLocationAndSendToken(token: String, userId: String, jwt: String) {
        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Location permission not granted, sending token without location")
            sendFcmTokenToServer(token, null, null, userId, jwt)
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d(TAG, "Location obtained: ${location.latitude}, ${location.longitude}")
                        sendFcmTokenToServer(token, location.latitude, location.longitude, userId, jwt)
                    } else {
                        Log.w(TAG, "Location is null, sending token without location")
                        sendFcmTokenToServer(token, null, null, userId, jwt)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get location: ${e.message}")
                    sendFcmTokenToServer(token, null, null, userId, jwt)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception getting location: ${e.message}")
            sendFcmTokenToServer(token, null, null, userId, jwt)
        }
    }

    // ✅ UPDATED: Better error handling
    private fun sendFcmTokenToServer(
        fcmToken: String,
        lat: Double?,
        lon: Double?,
        userId: String,
        jwt: String
    ) {
        val request = UpdateFcmRequest(
            userId,
            fcmToken,
            lat ?: 10.338743,
            lon ?: 123.911991
        )

        Log.d(TAG, "Sending token to server for user: $userId")

        RetrofitClient.instance.updateFcmToken("Bearer $jwt", request)
            .enqueue(object : Callback<FcmResponse> {
                override fun onResponse(call: Call<FcmResponse>, response: Response<FcmResponse>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "✅ Token successfully updated on server")
                    } else {
                        Log.e(TAG, "❌ Server error: ${response.code()} - ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<FcmResponse>, t: Throwable) {
                    Log.e(TAG, "❌ Network error updating token: ${t.message}")
                }
            })
    }

    // ✅ UPDATED: Better notification handling
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Get notification data
        val title = remoteMessage.notification?.title ?: "🌱 Daily Tip"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Open the app for today's farming tip"

        Log.d(TAG, "Notification - Title: $title, Body: ${body.take(50)}...")

        // Save tip to SharedPreferences for app to use
        getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("last_tip", body)
            .apply()

        Log.d(TAG, "Tip saved to SharedPreferences")

        // Broadcast to update UI if app is open
        try {
            val intent = Intent("DAILY_TIP_RECEIVED").apply {
                putExtra("tip_text", body)
            }
            sendBroadcast(intent)
            Log.d(TAG, "Broadcast sent to update UI")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending broadcast: ${e.message}")
        }

        // Show notification to user
        showNotification(title, body)
    }

    // ✅ UPDATED: Better notification with proper channel handling
    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Receive daily papaya farming tips based on weather conditions"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            }
        }

        // Create intent to open app when notification is tapped
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("openHome", true) // Optional: to navigate to home fragment
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.papaialogo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        // Show notification with unique ID based on timestamp
        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "✅ Notification displayed with ID: $notificationId")
    }
}