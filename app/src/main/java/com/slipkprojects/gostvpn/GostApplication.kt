package com.slipkprojects.gostvpn

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.slipkprojects.gostvpn.service.GostService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GostApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= 26) createNotificationChannel(this)
    }

    /**
     * Notificação
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val name: CharSequence = context.getString(R.string.app_name) // The user-visible name of the channel.
        val description = context.getString(R.string.app_description) // The user-visible description of the channel.
        val importance = NotificationManager.IMPORTANCE_LOW

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel(com.slipkprojects.gostvpn.service.GostService.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                this.description = description
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
    }
}