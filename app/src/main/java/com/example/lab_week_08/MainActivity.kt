package com.example.lab_week_08

// [1] Import UI dasar
import android.os.Bundle // [1]
import android.widget.Toast // [1]
import androidx.activity.enableEdgeToEdge // [1]
import androidx.appcompat.app.AppCompatActivity // [1]
import androidx.core.view.ViewCompat // [1]
import androidx.core.view.WindowInsetsCompat // [1]

// [1b] Import untuk service/intent
import android.content.Intent // [1b]
import androidx.core.content.ContextCompat // [1b]

// [2] Import WorkManager
import androidx.work.Constraints // [2]
import androidx.work.Data // [2]
import androidx.work.NetworkType // [2]
import androidx.work.OneTimeWorkRequest // [2]
import androidx.work.WorkManager // [2]

// [3] Import worker & service
import com.example.lab_week_08.worker.FirstWorker // [3]
import com.example.lab_week_08.worker.SecondWorker // [3]
import com.example.lab_week_08.service.NotificationService // [3]

class MainActivity : AppCompatActivity() {

    // [4] Instance WorkManager
    private val workManager by lazy { WorkManager.getInstance(this) } // [4]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // [5]
        enableEdgeToEdge() // [5]
        setContentView(R.layout.activity_main) // [5]
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets -> // [5]
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()) // [5]
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom) // [5]
            insets // [5]
        }

        // [A] Runtime permission untuk notifikasi (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // [6] Constraint: butuh koneksi internet
        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // [6]
            .build() // [6]

        val id = "001" // [7] ID dummy untuk input/output

        // [8] Request untuk FirstWorker + input
        val firstRequest = OneTimeWorkRequest.Builder(FirstWorker::class.java) // [8]
            .setConstraints(networkConstraints) // [8]
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id)) // [8]
            .build() // [8]

        // [9] Request untuk SecondWorker + input
        val secondRequest = OneTimeWorkRequest.Builder(SecondWorker::class.java) // [9]
            .setConstraints(networkConstraints) // [9]
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id)) // [9]
            .build() // [9]

        // [10] Rantai eksekusi: First → Second
        workManager.beginWith(firstRequest) // [10]
            .then(secondRequest) // [10]
            .enqueue() // [10]

        // [11] Observasi status First (cukup sekali)
        workManager.getWorkInfoByIdLiveData(firstRequest.id).observe(this) { info -> // [11]
            if (info.state.isFinished) {
                showResult("First process is done") // [11]
            }
        }

        // [12] Observasi status Second, lalu panggil service
        workManager.getWorkInfoByIdLiveData(secondRequest.id).observe(this) { info -> // [12]
            if (info.state.isFinished) {
                showResult("Second process is done") // [12]
                launchNotificationService() // [12]
            }
        }
    }

    // [B] Fungsi launch service + observe completion (modul step 8)
    private fun launchNotificationService() {
        // Observe service completion → Toast final dari modul
        NotificationService.trackingCompletion.observe(this) { Id -> // [B]
            showResult("Process for Notification Channel ID $Id is done!") // [B]
        }

        // Start service dengan EXTRA_ID "001" (modul)
        val serviceIntent = Intent(this, NotificationService::class.java).apply { // [B]
            putExtra(NotificationService.EXTRA_ID, "001") // [B]
        }
        ContextCompat.startForegroundService(this, serviceIntent) // [B]
    }

    // [13] Helper: bungkus input Data
    private fun getIdInputData(idKey: String, idValue: String): Data = // [13]
        Data.Builder()
            .putString(idKey, idValue) // [13]
            .build() // [13]

    // [14] Helper: tampilkan Toast
    private fun showResult(message: String) { // [14]
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show() // [14]
    }
}
