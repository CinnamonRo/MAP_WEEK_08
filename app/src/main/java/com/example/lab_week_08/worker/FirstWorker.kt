package com.example.lab_week_08.worker

// [1] Import inti WorkManager
import android.content.Context // [1]
import androidx.work.Data // [1]
import androidx.work.Worker // [1]
import androidx.work.WorkerParameters // [1]

class FirstWorker( // [2] Worker #1
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) { // [2]

    override fun doWork(): Result { // [3] Titik eksekusi background
        // [4] Ambil input (kalau ada)
        val id = inputData.getString(INPUT_DATA_ID) // [4]

        // [5] Simulasi kerja berat 3 detik
        Thread.sleep(3000L) // [5]

        // [6] Bangun output
        val outputData = Data.Builder() // [6]
            .putString(OUTPUT_DATA_ID, id) // [6]
            .build() // [6]

        // [7] Sukses, lempar output
        return Result.success(outputData) // [7]
    }

    companion object { // [8] Kunci input/output
        const val INPUT_DATA_ID = "inId" // [8]
        const val OUTPUT_DATA_ID = "outId" // [8]
    }
}
