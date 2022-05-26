package com.slipkprojects.gostvpn.service.util

import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream


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

}