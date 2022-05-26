package com.slipkprojects.gostvpn.domain

import com.slipkprojects.gostvpn.domain.model.GostSettings
import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    fun getGostSettings(): Flow<GostSettings>
    suspend fun updateGostSettings(gostSettings: GostSettings)
}