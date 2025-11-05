package com.example.lab_week_08

// [1] Import UI dasar
import android.os.Bundle // [1]
import android.widget.Toast // [1]
import androidx.activity.enableEdgeToEdge // [1]
import androidx.appcompat.app.AppCompatActivity // [1]
import androidx.core.view.ViewCompat // [1]
import androidx.core.view.WindowInsetsCompat // [1]

// [2] Import WorkManager
import androidx.work.Constraints // [2]
import androidx.work.Data // [2]
import androidx.work.NetworkType // [2]
import androidx.work.OneTimeWorkRequest // [2]
import androidx.work.WorkManager // [2]

// [3] Import worker kita
import com.example.lab_week_08.worker.FirstWorker // [3]
import com.example.lab_week_08.worker.SecondWorker // [3]

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
            .then(secondRequest)            // [10]
            .enqueue()                      // [10]

        // [11] Observasi status First
        workManager.getWorkInfoByIdLiveData(firstRequest.id).observe(this) { info -> // [11]
            if (info.state.isFinished) {
                showResult("First process is done") // [11]
            }
        }

        // [12] Observasi status Second
        workManager.getWorkInfoByIdLiveData(secondRequest.id).observe(this) { info -> // [12]
            if (info.state.isFinished) {
                showResult("Second process is done") // [12]
            }
        }
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
