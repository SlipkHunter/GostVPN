package com.slipkprojects.gostvpn.data

import com.slipkprojects.gostvpn.data.database.PrefsStore
import com.slipkprojects.gostvpn.domain.model.GostSettings
import javax.inject.Inject

class LocalRepository @Inject constructor(private val prefsStore: PrefsStore) {
    fun getGostSettings() = prefsStore.getGostSettings()
    suspend fun updateGostSettings(gostSettings: GostSettings) = prefsStore.updateGostSettings(gostSettings)
}