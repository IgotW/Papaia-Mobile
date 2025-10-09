package com.google.papaia

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.papaia.utils.DailyTipWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createChannel()
//        scheduleDailyTipWorker(this) // auto-schedule once app is opened
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "daily_tip_channel",
                "Daily Tips",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

//    companion object {
//        fun scheduleDailyTipWorker(context: android.content.Context) {
//            val now = Calendar.getInstance()
//            val target = Calendar.getInstance().apply {
//                set(Calendar.HOUR_OF_DAY, 6)
//                set(Calendar.MINUTE, 0)
//                set(Calendar.SECOND, 0)
//                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
//            }
//            val initialDelay = target.timeInMillis - now.timeInMillis
//
//            val workRequest = PeriodicWorkRequestBuilder<DailyTipWorker>(1, TimeUnit.DAYS)
//                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
//                .build()
//
//            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//                "DailyTipWork",
//                ExistingPeriodicWorkPolicy.UPDATE,
//                workRequest
//            )
//        }
//
//        fun cancelDailyTipWorker(context: android.content.Context) {
//            WorkManager.getInstance(context).cancelUniqueWork("DailyTipWork")
//        }
//    }
}