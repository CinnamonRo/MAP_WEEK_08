package com.example.lab_week_08.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.lab_week_08.MainActivity
import com.example.lab_week_08.R

class SecondNotificationService : Service() {

    companion object {
        const val EXTRA_ID = "Id2"
        const val NOTIFICATION_ID = 0xBEEF
        private const val CHANNEL_ID = "map08_channel_2"
        private const val CHANNEL_NAME = "MAP 08 Tracking 2"

        private val _done = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = _done
    }

    private lateinit var handler: Handler
    private lateinit var builder: NotificationCompat.Builder

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannelIfNeeded()
        builder = baseBuilder(getPendingIntent())
        // tampilkan notifikasi awal seketika
        startForeground(NOTIFICATION_ID, builder
            .setContentTitle("Second Notification")
            .setContentText("Channel 2 • Countdown: 10")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build())

        val ht = HandlerThread("SecondNotificationThread").apply { start() }
        handler = Handler(ht.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(EXTRA_ID) ?: "001"

        handler.post {
            val nm = ContextCompat.getSystemService(this, NotificationManager::class.java)
            for (t in 9 downTo 0) {
                val n: Notification = builder
                    .setContentTitle("Second Notification")
                    .setContentText("Channel 2 id $id • Countdown: $t")
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setOngoing(t != 0)
                    .build()
                nm?.notify(NOTIFICATION_ID, n)
                Thread.sleep(1000)
            }
            _done.postValue(id)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    // util
    private fun baseBuilder(pi: PendingIntent) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentIntent(pi)
        .setTicker("Third worker process is done, check it out!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            val nm = ContextCompat.getSystemService(this, NotificationManager::class.java)
            nm?.createNotificationChannel(ch)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), flag)
    }
}
