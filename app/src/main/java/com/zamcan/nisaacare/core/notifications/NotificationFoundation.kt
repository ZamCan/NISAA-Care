package com.zamcan.nisaacare.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.zamcan.nisaacare.R
import com.zamcan.nisaacare.domain.model.AppPreferences

interface NotificationScheduler {
    fun configure(preferences: AppPreferences)
    fun showPrivateUpdate()
    fun cancelAll()
}

class LocalNotificationScheduler(private val context: Context) : NotificationScheduler {
    private val manager = context.getSystemService(NotificationManager::class.java)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_description)
                }
            )
        }
    }

    override fun configure(preferences: AppPreferences) {
        // The scheduling policy is intentionally conservative. A production
        // worker can call this interface after a user opts in; no reminder is
        // created implicitly during onboarding.
    }

    override fun showPrivateUpdate() {
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_private_title))
            .setContentText(context.getString(R.string.notification_private_text))
            .setAutoCancel(true)
            .build()
        manager.notify(PRIVATE_UPDATE_ID, notification)
    }

    override fun cancelAll() {
        manager.cancelAll()
    }

    companion object {
        const val CHANNEL_ID = "nisaa-care-private"
        private const val PRIVATE_UPDATE_ID = 4101
    }
}
