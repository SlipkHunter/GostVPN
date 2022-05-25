package com.slipkprojects.gostvpn.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.slipkprojects.gostvpn.R
import com.slipkprojects.gostvpn.domain.model.GostSettings

class GostService : Service() {

    private val gostCallbackManager: GostCallbackManager = GostCallbackManager(this)
    private var mNotificationShowing = false
    private var gostThread: GostThread? = null

    private lateinit var mNotificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = when (intent?.action) {
        ACTION_START -> {
            showToolbarNotification("Starting")

            val gostSettings = requireNotNull(intent.getParcelableExtra<GostSettings>(EXTRA_GOST_SETTINGS)) {
                "libgost.so settings is required"
            }

            showToolbarNotification("Service running")
            startGostClient(gostSettings)
            START_STICKY
        }
        ACTION_STOP -> {
            stopGostClient()
            START_NOT_STICKY
        }
        ACTION_BINDER_SERVICE -> {
            START_NOT_STICKY
        }
        else -> {
            throw NotImplementedError("${intent?.action} is not implemented")
        }
    }

    override fun onBind(intent: Intent?): IBinder? =
        if (intent?.action == ACTION_BINDER_SERVICE) {
            gostCallbackManager.mBinder
        } else null

    override fun onDestroy() {
        super.onDestroy()
        stopGostClient()
    }

    @Synchronized
    private fun startGostClient(gostSettings: GostSettings) {
        Log.d("GostService", "startGostClient()")
        gostThread = GostThread(
            application,
            gostSettings,
            object : GostThread.GostListener {
                override fun onNewLog(message: String) {
                    gostCallbackManager.sendToClientsLog(message)
                }

                override fun onNewState(isActive: Boolean) {
                    gostCallbackManager.sendToClientsState(isActive)

                    if (!isActive) {
                       stopGostClient()
                    }
                }
            }
        )
        gostThread?.start()
    }
    @Synchronized
    fun stopGostClient() {
        Log.d("GostService", "stopGostClient()")
        gostThread?.interrupt()
        mNotificationShowing = false
        stopForeground(true)
        stopSelf()
    }

    private fun showToolbarNotification(notifyMsg: String?, icon: Int = R.drawable.ic_baseline_cloud_queue_24, useChronometer: Boolean = false) {
        val context = this

        // https://stackoverflow.com/questions/67045607/how-to-resolve-missing-pendingintent-mutability-flag-lint-warning-in-android-a
        val pendingFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else PendingIntent.FLAG_UPDATE_CURRENT

        // adiciona botão parar na notificação
        val pendingIntentActionStop = PendingIntent.getService(
            context,
            0,
            Intent(context, GostService::class.java).apply {
                action = ACTION_STOP
            },
            pendingFlag
        )

        val mNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentIntent(getGraphPendingIntent(context))
            .setContentTitle(getString(R.string.app_name)) // em android antigo não mostra o nome do app, usando isso resolve o problema
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setLocalOnly(true)
            .setContentText(notifyMsg)
            .setSmallIcon(icon)
            .setTicker(notifyMsg)
            .setSilent(true)
            .addAction(R.drawable.ic_baseline_exit_to_app_24, "Desconectar", pendingIntentActionStop)
            .apply {
                if (useChronometer) setUsesChronometer(true)
            }
            .build()

        if (!mNotificationShowing) {
            startForeground(NOTIFY_ID, mNotification)
        } else {
            mNotificationManager.notify(NOTIFY_ID, mNotification)
        }

        mNotificationShowing = true
    }


    companion object {
        const val NOTIFICATION_CHANNEL_ID = "sksplus_channel_1"
        private const val NOTIFY_ID = 1

        const val ACTION_START = "actionStart::gostService"
        const val ACTION_STOP = "actionStop::gostService"
        const val ACTION_BINDER_SERVICE = "actionBinder::gostService"
        const val EXTRA_GOST_SETTINGS = "gostSettings::gostService"

        // intent utilizado pra enviar o user pra atividade principal
        fun getGraphPendingIntent(c: Context): PendingIntent {
            val intent = Intent()
            intent.component = ComponentName(c, c.packageName + ".ui.activity.MainActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

            // https://stackoverflow.com/questions/67045607/how-to-resolve-missing-pendingintent-mutability-flag-lint-warning-in-android-a
            val pendingFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else PendingIntent.FLAG_UPDATE_CURRENT

            return PendingIntent.getActivity(c, 0, intent, pendingFlag)
        }
    }
}