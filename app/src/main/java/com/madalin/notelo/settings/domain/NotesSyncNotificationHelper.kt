package com.madalin.notelo.settings.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.madalin.notelo.R

/**
 * Helper class for displaying synchronization notifications.
 */
class NotesSyncNotificationHelper(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "notes_synchronization_channel"
        const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    /**
     * Creates the notification channel for displaying synchronization notifications.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Notes Synchronization"
            val channelDescription = "Channel used to display notes synchronization status"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a notification with the given [title] and [text].
     */
    fun showNotification(title: String, text: String, ongoing: Boolean = false) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notes)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(ongoing)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Updates the notification to show progress.
     */
    fun showProgressNotification(progress: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Sync In Progress")
            .setContentText("Synchronizing data...")
            .setSmallIcon(R.drawable.ic_notes)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(100, progress, false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Dismisses the notification.
     */
    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}