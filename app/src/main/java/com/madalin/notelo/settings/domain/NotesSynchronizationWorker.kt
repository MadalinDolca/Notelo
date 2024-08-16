package com.madalin.notelo.settings.domain

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.madalin.notelo.R

/**
 * Worker class for performing notes synchronization.
 */
class NotesSynchronizationWorker(
    private val appContext: Context,
    private val params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val notificationHelper = NotesSyncNotificationHelper(appContext)

    companion object {
        const val WORK_NAME = "NotesSynchronizationWorker"
    }

    override suspend fun doWork(): Result {
        notificationHelper.showNotification(
            title = appContext.getString(R.string.notes_synchronization),
            text = appContext.getString(R.string.synchronization_has_started),
            ongoing = true
        )

        when (val result = NotesSynchronizer.start()) {
            NotesSynchronizer.SynchronizationResult.Success -> {
                notificationHelper.showNotification(
                    appContext.getString(R.string.notes_synchronization),
                    appContext.getString(R.string.synchronization_has_succeeded)
                )
                return Result.success()
            }

            is NotesSynchronizer.SynchronizationResult.Error -> {
                Log.e("NotesSynchronizationWorker", "Synchronization failed: ${result.message}")
                notificationHelper.showNotification(
                    appContext.getString(R.string.notes_synchronization),
                    appContext.getString(R.string.synchronization_has_failed)
                )
                return Result.failure()
            }
        }
    }
}