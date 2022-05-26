package com.slipkprojects.gostvpn.service.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.slipkprojects.gostvpn.domain.model.GostSettings
import com.slipkprojects.gostvpn.service.GostService

object GostHelper {
    fun startService(context: Context, gostSettings: GostSettings) {
        val service = Intent(context, GostService::class.java).apply {
            action = GostService.ACTION_START
            putExtra(GostService.EXTRA_GOST_SETTINGS, gostSettings)
        }
        ContextCompat.startForegroundService(context, service)
    }
}