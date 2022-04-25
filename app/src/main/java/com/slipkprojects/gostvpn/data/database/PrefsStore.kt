package com.slipkprojects.gostvpn.data.database

import android.content.Context
import androidx.datastore.core.DataStore
import com.slipkprojects.gostvpn.AppPreferences
import com.slipkprojects.gostvpn.domain.model.GostSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PrefsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<AppPreferences> = context.settingsDataStore

    fun getGostSettings(): Flow<GostSettings> = dataStore
        .data.catch { exception -> // 1
            // dataStore.data throws an IOException if it can't read the data
            if (exception is IOException) { // 2
                emit(AppPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }.map { GostSettings(it.gostSettings) }

    suspend fun updateGostSettings(gostSettings: GostSettings) {
        dataStore.updateData { preferences ->
            preferences.toBuilder().setGostSettings(
                gostSettings.settings
            ).build()
        }
    }
}