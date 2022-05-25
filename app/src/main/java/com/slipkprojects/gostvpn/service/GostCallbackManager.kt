package com.slipkprojects.gostvpn.service

import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.RemoteException
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import com.slipkprojects.gostvpn.R
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class GostCallbackManager(private val gostService: GostService) {
    val mBinder: LocalBinder = object : LocalBinder() {
        override fun registerCallback(gostCallback: GostCallback) {
            try {
                // envia os dados recentes para o novo cliente
                with(gostCallback) {
                    onNewState(lastStateGost.get())

                    synchronized(lastLogs) {
                        if (lastLogs.isNotEmpty()) {
                            onLogsCached(lastLogs.toTypedArray())
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
        override fun unregisterCallback(gostCallback: GostCallback) {
            mClients.remove(gostCallback.hashCode())
        }
        override fun stopVpn() {
            gostService.stopGostClient()
        }
        override fun clearLogs() {
            this@GostCallbackManager.clearLogs()
        }
    }

    private val lastLogs: MutableList<String> = Collections.synchronizedList(mutableListOf())
    private val lastStateGost: AtomicBoolean = AtomicBoolean(false)

    fun sendToClientsLog(logItem: String) {
        lastLogs.add(logItem)

        mBinder.mClients.forEach { client ->
            val key = client.key
            val messenger = client.value

            try {
                messenger.onNewLogItem(logItem)
            } catch (e: RemoteException) {
                mBinder.mClients.remove(key)
            }
        }
    }
    fun sendToClientsState(state: Boolean) {
        lastStateGost.set(state)

        mBinder.mClients.forEach { client ->
            val key = client.key
            val messenger = client.value

            try {
                messenger.onNewState(state)
            } catch (e: RemoteException) {
                mBinder.mClients.remove(key)
            }
        }
    }

    fun clearLogs() {
        lastLogs.clear()

        mBinder.mClients.forEach { client ->
            val key = client.key
            val messenger = client.value

            try {
                messenger.onLogsCleared()
            } catch (e: RemoteException) {
                mBinder.mClients.remove(key)
            }
        }

        logInformation()
    }

    private fun logInformation() {

        sendToClientsLog(
            gostService.getString(
                R.string.mobile_info,
                Build.BOARD,
                Build.BRAND,
                Build.MODEL,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT)
        )

        val version = try {
            val packageinfo = gostService.packageManager.getPackageInfo(gostService.packageName, 0)
            String.format("%s Build %d", packageinfo.versionName, PackageInfoCompat.getLongVersionCode(packageinfo).toInt())
        } catch (ignored: PackageManager.NameNotFoundException) {
            "error getting version"
        }
        sendToClientsLog(gostService.getString(R.string.mobile_info2, version))

        // adiciona a versão do gost aos logs
        GostThread.getGostVersion(gostService)?.also {
            sendToClientsLog(it)
        }
    }
}

interface GostCallback {
    fun onNewLogItem(logMessage: String)
    fun onNewState(isActive: Boolean)
    fun onLogsCached(logsList: Array<String>)
    fun onLogsCleared()
}

abstract class LocalBinder : Binder() {
    val mClients: ConcurrentHashMap<Int, GostCallback> = ConcurrentHashMap()

    abstract fun registerCallback(gostCallback: GostCallback)
    abstract fun unregisterCallback(gostCallback: GostCallback)
    abstract fun stopVpn()
    abstract fun clearLogs()
}