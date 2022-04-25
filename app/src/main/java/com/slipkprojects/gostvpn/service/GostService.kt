package com.slipkprojects.gostvpn.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.PackageInfoCompat
import com.slipkprojects.gostvpn.R
import com.slipkprojects.gostvpn.domain.model.GostSettings
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class GostService : Service() {

    interface GostCallback {
        fun onNewLogItem(logMessage: String)
        fun onNewState(isActive: Boolean)
        fun onLogsCached(logsList: Array<String>)
        fun onLogsCleared()
    }

    internal class LocalBinder(private val service: GostService): Binder() {
        val mClients: ConcurrentHashMap<Int, GostCallback> = ConcurrentHashMap()

        fun registerCallback(gostCallback: GostCallback) {
            try {
                // envia os dados recentes para o novo cliente
                with(gostCallback) {
                    onNewState(service.lastStateGost.get())

                    synchronized(service.lastLogs) {
                        if (service.lastLogs.isNotEmpty()) {
                            onLogsCached(service.lastLogs.toTypedArray())
                        }
                    }
                }
            } catch (e: RemoteException) {
                Log.e("SshCallbackManager", e.message.toString())
                // Client is dead, do not add it to the clients list
                return
            }
            mClients[gostCallback.hashCode()] = gostCallback
        }
        fun unregisterCallback(gostCallback: GostCallback) {
            mClients.remove(gostCallback.hashCode())
        }
        fun stopVpn() {
            service.stopGostClient()
        }
        fun clearLogs() {
            service.clearLogs()
        }
    }

    private var gostThread: GostThread? = null
    private var mBinder: LocalBinder? = null
    private var mNotificationShowing = false

    private lateinit var mNotificationManager: NotificationManager

    val lastLogs: MutableList<String> = Collections.synchronizedList(mutableListOf())
    val lastStateGost: AtomicBoolean = AtomicBoolean(false)

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
            Toast.makeText(this, "\"${intent?.action}\" is not implemented", Toast.LENGTH_SHORT)
                .show()
            START_NOT_STICKY
        }
    }

    override fun onBind(intent: Intent?): IBinder? =
        if (intent?.action == ACTION_BINDER_SERVICE) {
            mBinder ?: synchronized(this) {
                val binder = LocalBinder(this)
                mBinder = binder
                binder
            }
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
                    sendToClientsLog(message)
                }

                override fun onNewState(isActive: Boolean) {
                    sendToClientsState(isActive)

                    if (!isActive) {
                       stopGostClient()
                    }
                }
            }
        )
        gostThread?.start()
    }
    @Synchronized
    private fun stopGostClient() {
        Log.d("GostService", "stopGostClient()")
        gostThread?.interrupt()
        mNotificationShowing = false
        stopForeground(true)
        stopSelf()
    }


    fun sendToClientsLog(logItem: String) {
        lastLogs.add(logItem)

        mBinder?.mClients?.forEach { client ->
            val key = client.key
            val messenger = client.value

            try {
                messenger.onNewLogItem(logItem)
            } catch (e: RemoteException) {
                mBinder?.mClients?.remove(key)
            }
        }
    }
    fun sendToClientsState(state: Boolean) {
        lastStateGost.set(state)

        mBinder?.mClients?.forEach { client ->
            val key = client.key
            val messenger = client.value

            try {
                messenger.onNewState(state)
            } catch (e: RemoteException) {
                mBinder?.mClients?.remove(key)
            }
        }
    }

    fun clearLogs() {
        lastLogs.clear()

        mBinder?.mClients?.forEach { client ->
            val key = client.key
            val messenger = client.value

            try {
                messenger.onLogsCleared()
            } catch (e: RemoteException) {
                mBinder?.mClients?.remove(key)
            }
        }

        logInformation()
    }

    private fun logInformation() {

        sendToClientsLog(
            getString(R.string.mobile_info,
                Build.BOARD,
                Build.BRAND,
                Build.MODEL,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT)
        )

        val version = try {
            val packageinfo = packageManager.getPackageInfo(packageName, 0)
            String.format("%s Build %d", packageinfo.versionName, PackageInfoCompat.getLongVersionCode(packageinfo).toInt())
        } catch (ignored: PackageManager.NameNotFoundException) {
            "error getting version"
        }
        sendToClientsLog(getString(R.string.mobile_info2, version))

        // adiciona a versão do gost aos logs
        GostThread.getGostVersion(this)?.also {
            sendToClientsLog(it)
        }
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