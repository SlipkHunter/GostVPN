package com.slipkprojects.gostvpn.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Permite uma melhor interação entre serviço e cliente
 */
class GostInteractor(
    private val context: Context,
    listener: Listener
) {

    private var iRemoteService: LocalBinder? = null
    private var mBound = AtomicBoolean(false)
    private val awaitBound = CountDownLatch(1)

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            iRemoteService = (service as LocalBinder).apply {
                // We want to monitor the service for as long as we are
                // connected to it.
                try {
                    registerCallback(iStatusCallbacks)
                } catch (e: RemoteException) {
                    // In this case the service has crashed before we could even
                    // do anything with it; we can count on soon being
                    // disconnected (and then reconnected if it can be restarted)
                    // so there is no need to do anything here.
                }
            }

            mBound.set(true)
            awaitBound.countDown()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mBound.set(false)
            iRemoteService = null
            awaitBound.countDown()
        }
    }

    fun startBound() {
        Intent(context, GostService::class.java).apply {
            action = GostService.ACTION_BINDER_SERVICE
            context.bindService(this, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun stopBound() {
        if (mBound.getAndSet(false)) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            iRemoteService?.apply {
                try {
                    unregisterCallback(iStatusCallbacks)
                } catch (e: RemoteException) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
            context.unbindService(mServiceConnection)

            awaitBound.countDown()
        }
    }

    suspend fun stopVpn() {
        withContext(Dispatchers.IO) {
            awaitBound.await()

            iRemoteService?.apply {
                try {
                    stopVpn()
                } catch (e: RemoteException) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
        }
    }

    suspend fun clearLogsFromService() {
        withContext(Dispatchers.IO) {
            awaitBound.await()

            iRemoteService?.apply {
                try {
                    clearLogs()
                } catch (e: RemoteException) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
        }
    }

    interface Listener {
        fun onNewIsActive(isActiveVpn: Boolean) {}
        fun onNewLog(logItem: String) {}
        fun onLogsCached(logsList: Array<String>) {}
        fun onLogsCleared() {}
    }

    private val iStatusCallbacks = object : GostCallback {
        override fun onNewLogItem(logMessage: String) {
            listener.onNewLog(logMessage)
        }

        override fun onNewState(isActive: Boolean) {
            listener.onNewIsActive(isActive)
        }

        override fun onLogsCached(logsList: Array<String>) {
            listener.onLogsCached(logsList)
        }

        override fun onLogsCleared() {
            listener.onLogsCleared()
        }
    }
}