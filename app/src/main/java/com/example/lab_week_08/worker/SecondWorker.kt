package com.example.lab_week_08.worker

import android.content.Context // [1]
import androidx.work.Data // [1]
import androidx.work.Worker // [1]
import androidx.work.WorkerParameters // [1]

class SecondWorker( // [2] Worker #2
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result { // [3]
        val id = inputData.getString(INPUT_DATA_ID) // [4]
        Thread.sleep(3000L) // [5]
        val outputData = Data.Builder() // [6]
            .putString(OUTPUT_DATA_ID, id) // [6]
            .build() // [6]
        return Result.success(outputData) // [7]
    }

    companion object { // [8]
        const val INPUT_DATA_ID = "inId" // [8]
        const val OUTPUT_DATA_ID = "outId" // [8]
    }
}
