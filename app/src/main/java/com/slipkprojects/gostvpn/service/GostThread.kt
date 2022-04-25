package com.slipkprojects.gostvpn.service

import android.app.Application
import android.content.Context
import com.slipkprojects.gostvpn.domain.model.GostSettings
import com.slipkprojects.gostvpn.service.util.TunnelUtils
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

// nome do binário
private const val GOST = "libgost.so"

class GostThread(
    private val application: Application,
    private val settings: GostSettings,
    private val listener: GostListener
): Thread() {

    interface GostListener {
        fun onNewLog(message: String)
        fun onNewState(isActive: Boolean)
    }

    private var process: Process? = null

    override fun run() {
        super.run()

        listener.onNewLog("Started GostThread")
        listener.onNewState(true)

        try {
            val filePathBin = File(application.applicationInfo.nativeLibraryDir, GOST)
                .also { if (!it.exists()) throw IOException("Bin $GOST is not found") }
                .absolutePath

            // TODO("Uma validação aqui seria interessante")
            val filePathSettings = makeFileSettings(settings)
            val cmd = "-C $filePathSettings"//settings.settings.trim()

            process = Runtime.getRuntime().exec("$filePathBin $cmd")

            process?.also {
                val cmdName = File(filePathBin).nameWithoutExtension
                val logger = { message: String ->
                    listener.onNewLog(message)
                }

                // daemon threads são fechadas quando a thread que os criou é encerrada
                thread(name = "stderr-$cmdName", isDaemon = true) {
                    TunnelUtils.streamLogger(it.errorStream, logger)
                }
                thread(name = "stdout-$cmdName", isDaemon = true) {
                    TunnelUtils.streamLogger(it.inputStream, logger)
                }

                // aguarda até o processo encerrar
                it.waitFor()

                listener.onNewLog("Gost process stopped")
            }

        } catch (e: Exception) {
            listener.onNewLog("Error: ${e.stackTraceToString()}")
        }

        listener.onNewState(false)
        listener.onNewLog("Stopped GostThread")
    }

    override fun interrupt() {
        super.interrupt()

        TunnelUtils.killProcess(GOST)

        process?.also {
            it.destroy()
            process = null
        }
    }

    private fun makeFileSettings(gostSettings: GostSettings): String {
        val settings = gostSettings.settings
        val fileName = "gost.json"
        val fileOut = File(application.filesDir, fileName)

        fileOut.outputStream().use {
            it.write(settings.toByteArray())
            it.flush()
        }

        return fileOut.absolutePath
    }

    companion object {
        fun getGostVersion(context: Context): String? = try {
            val filePathBin = File(context.applicationInfo.nativeLibraryDir, GOST)
                .also { if (!it.exists()) throw IOException("Bin $GOST is not found") }
                .absolutePath

            // show gost version
            val cmd = "-V"

            val process = Runtime.getRuntime().exec("$filePathBin $cmd")
            process.inputStream.bufferedReader().readText().trim()
        } catch (e: InterruptedException) {
            null
        }
    }
}