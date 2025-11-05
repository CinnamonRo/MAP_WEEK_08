package com.example.lab_week_08.service

// [1] Import
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

class NotificationService : Service() {

    // [2] LiveData untuk observer completion dari Activity (sesuai modul step 8)
    companion object {
        const val EXTRA_ID = "Id"              // [2]
        const val NOTIFICATION_ID = 0xCA7      // [2]
        private val mutableID = MutableLiveData<String>() // [2]
        val trackingCompletion: LiveData<String> = mutableID // [2]
    }

    private lateinit var handler: Handler                      // [3]
    private lateinit var builder: NotificationCompat.Builder   // [3]
    private var channelId: String = ""                         // [3]

    override fun onBind(intent: Intent?): IBinder? = null // [4]

    override fun onCreate() {
        super.onCreate()
        // [5] Siapkan channel & builder, sesuai modul
        channelId = createNotificationChannel() // [5]
        builder = getNotificationBuilder(getPendingIntent(), channelId) // [5]

        // [6] Mulai sebagai foreground service dengan notifikasi awal
        startForeground(NOTIFICATION_ID, builder.build()) // [6]

        // [7] HandlerThread agar countdown tidak jalan di main thread
        val ht = HandlerThread("SecondThread").apply { start() } // [7]
        handler = Handler(ht.looper) // [7]
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(EXTRA_ID) ?: "001" // [8]

        // [9] Countdown 10 → 0, update isi notifikasi tiap detik
        handler.post {
            val nm = ContextCompat.getSystemService(this, NotificationManager::class.java)
            for (t in 10 downTo 0) {
                val updated: Notification = builder
                    .setContentTitle("Tracking Background Work")
                    .setContentText("Channel id $id • Countdown: $t")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setOngoing(t != 0)
                    .build()
                nm?.notify(NOTIFICATION_ID, updated)
                Thread.sleep(1000)
            }

            // [10] Beri sinyal selesai ke Activity (modul step 8)
            mutableID.postValue(id)

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    // [11] Channel sesuai contoh modul
    private fun createNotificationChannel(): String {
        // Kembalikan ID non-empty supaya aman dipakai builder di semua versi
        val cid = "001"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val cname = "001 Channel"
            val priority = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(cid, cname, priority)
            val nm = ContextCompat.getSystemService(this, NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
        return cid
    }

    // [12] Builder sesuai contoh modul
    private fun getNotificationBuilder(
        pendingIntent: PendingIntent,
        channelId: String
    ) = NotificationCompat.Builder(this, channelId)
        .setContentTitle("Second worker process is done")
        .setContentText("Check it out!")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(pendingIntent)
        .setTicker("Second worker process is done, check it out!")
        .setOngoing(true)

    // [13] PendingIntent untuk tap notifikasi kembali ke MainActivity
    private fun getPendingIntent(): PendingIntent {
        val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), flag)
    }
}
