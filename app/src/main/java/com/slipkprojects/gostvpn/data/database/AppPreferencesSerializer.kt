package com.slipkprojects.gostvpn.data.database

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.slipkprojects.gostvpn.AppPreferences
import java.io.InputStream
import java.io.OutputStream

object AppPreferencesSerializer : Serializer<AppPreferences> {

    override val defaultValue: AppPreferences = AppPreferences.newBuilder()
        .setGostSettings("")
        .build()

    override suspend fun readFrom(input: InputStream): AppPreferences {
        try {
            return AppPreferences.parseFrom(input)
        } catch (exception: IllegalStateException) {
            throw CorruptionException("Cannot read proto.", exception)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AppPreferences, output: OutputStream) {
        t.writeTo(output)
    }
}
val Context.settingsDataStore: DataStore<AppPreferences> by dataStore(
    fileName = "appprefrences.pb",
    serializer = AppPreferencesSerializer
)