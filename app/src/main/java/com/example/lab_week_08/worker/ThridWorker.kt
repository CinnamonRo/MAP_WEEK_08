package com.example.lab_week_08.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class ThirdWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val id = inputData.getString(INPUT_DATA_ID) ?: "001"  // [1]
        Thread.sleep(3000L)                                   // [2] simulasi kerja 3 detik
        val out = Data.Builder().putString(OUTPUT_DATA_ID, id).build() // [3]
        return Result.success(out)                            // [4]
    }

    companion object {
        const val INPUT_DATA_ID = "inId"
        const val OUTPUT_DATA_ID = "outId"
    }
}
