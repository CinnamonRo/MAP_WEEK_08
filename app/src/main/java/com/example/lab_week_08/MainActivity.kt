package com.example.lab_week_08

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.*

import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker
import com.example.lab_week_08.worker.ThirdWorker
import com.example.lab_week_08.service.NotificationService
import com.example.lab_week_08.service.SecondNotificationService

class MainActivity : AppCompatActivity() {

    private val workManager by lazy { WorkManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        // Android 13+ permission untuk notifikasi
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        val firstRequest = OneTimeWorkRequest.Builder(FirstWorker::class.java)
            .setConstraints(constraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id))
            .build()

        val secondRequest = OneTimeWorkRequest.Builder(SecondWorker::class.java)
            .setConstraints(constraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id))
            .build()

        // 1) First → 2) Second
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()

        // First done
        workManager.getWorkInfoByIdLiveData(firstRequest.id).observe(this) { info ->
            if (info.state.isFinished) showResult("First worker executed")
        }

        // Second done → 3) Jalankan NotificationService
        workManager.getWorkInfoByIdLiveData(secondRequest.id).observe(this) { info ->
            if (info.state.isFinished) {
                showResult("Second worker executed")
                launchNotificationService() // ini memicu step ke-3
            }
        }

        // 3) NotificationService selesai → 4) Enqueue ThirdWorker
        NotificationService.trackingCompletion.observe(this) { channelId ->
            showResult("NotificationService executed (channel $channelId)")
            enqueueThirdWorker(constraints, id)
        }

        // 5) SecondNotificationService selesai → info penutup
        SecondNotificationService.trackingCompletion.observe(this) { channelId ->
            showResult("SecondNotificationService executed (channel $channelId)")
        }
    }

    // Enqueue ThirdWorker, lalu observe selesai → jalankan SecondNotificationService
    private fun enqueueThirdWorker(constraints: Constraints, id: String) {
        val third = OneTimeWorkRequest.Builder(ThirdWorker::class.java)
            .setConstraints(constraints)
            .setInputData(getIdInputData(ThirdWorker.INPUT_DATA_ID, id))
            .build()

        workManager.enqueue(third)

        workManager.getWorkInfoByIdLiveData(third.id).observe(this) { info ->
            if (info.state.isFinished) {
                showResult("Third worker executed")
                launchSecondNotificationService(id) // step ke-5
            }
        }
    }

    // Step 3
    private fun launchNotificationService() {
        val it = Intent(this, NotificationService::class.java)
            .putExtra(NotificationService.EXTRA_ID, "001")
        ContextCompat.startForegroundService(this, it)
    }

    // Step 5
    private fun launchSecondNotificationService(id: String) {
        val it = Intent(this, SecondNotificationService::class.java)
            .putExtra(SecondNotificationService.EXTRA_ID, id)
        ContextCompat.startForegroundService(this, it)
    }

    private fun getIdInputData(idKey: String, idValue: String): Data =
        Data.Builder().putString(idKey, idValue).build()

    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
