package com.slipkprojects.gostvpn.data

import com.slipkprojects.gostvpn.domain.LocalRepository
import com.slipkprojects.gostvpn.domain.model.GostSettings
import com.slipkprojects.gostvpn.data.database.PrefsStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalRepositoryImpl @Inject constructor(private val prefsStore: PrefsStore): LocalRepository {
    override fun getGostSettings(): Flow<GostSettings> = prefsStore.getGostSettings()
    override suspend fun updateGostSettings(gostSettings: GostSettings) = prefsStore.updateGostSettings(gostSettings)
}