package com.slipkprojects.gostvpn.service.util

import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketTimeoutException


object TunnelUtils {


    fun killProcess(processName: String) {
        val list = File("/proc").listFiles { _, name -> TextUtils.isDigitsOnly(name) } ?: return

        for (process in list) {
            val exe = File(try {
                File(process, "cmdline").inputStream().bufferedReader().readText()
            } catch (_: IOException) {
                continue
            }.split(Character.MIN_VALUE, limit = 2).first())
            if (processName == exe.name) try {
                Os.kill(process.name.toInt(), OsConstants.SIGKILL)
            } catch (e: ErrnoException) {
                if (e.errno != OsConstants.ESRCH) {
                    Log.d("TunnelUtils", "SIGKILL ${exe.absolutePath} (${process.name}) failed")
                    Log.w("TunnelUtils", e)
                }
            }
        }
    }

    fun streamLogger(input: InputStream, logger: (String) -> Unit) = try {
        input.bufferedReader().forEachLine(logger)
    } catch (_: IOException) { }

    private fun isPortAvailable(hostname: String, port: Int): Boolean {
        try {
            Socket().use { socket ->
                val sockaddr: SocketAddress = InetSocketAddress(hostname, port)
                socket.connect(sockaddr, 1000)
                // The connect succeeded, so there is already something running on that port
                return false
            }
        } catch (e: SocketTimeoutException) {
            // The socket is in use, but the server didn't respond quickly enough
            return false
        } catch (e: IOException) {
            // The connect failed, so the port is available
            return true
        }
    }

    /**
     * @return porta disponível ou -1 se não encontrar nenhuma
     */
    fun findAvailablePort(hostname: String, start_port: Int, max_increment: Int): Int {
        for (port in start_port until start_port + max_increment) {
            if (isPortAvailable(hostname, port)) {
                return port
            }
        }
        return -1
    }

    /**
     * Private Address
     */
    class PrivateAddress(
        val mIpAddress: String,
        val mSubnet: String,
        val mPrefixLength: Int,
        val mRouter: String
    )

}