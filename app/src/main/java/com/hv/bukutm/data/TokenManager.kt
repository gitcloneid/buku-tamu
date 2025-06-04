package com.hv.bukutm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hv.bukutm.domain.model.Tamu
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager @Inject constructor(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TAMU_ID_KEY = intPreferencesKey("tamu_id")
        private val TAMU_NAMA_KEY = stringPreferencesKey("tamu_nama")
        private val TAMU_TELEPON_KEY = stringPreferencesKey("tamu_telepon")
        private val TAMU_KODEQR_KEY = stringPreferencesKey("tamu_kodeqr")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[ACCESS_TOKEN_KEY] }
            .firstOrNull()
    }

    val accessToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[ACCESS_TOKEN_KEY] }

    val refreshToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[REFRESH_TOKEN_KEY] }

    suspend fun saveTamu(tamu: Tamu, kodeQr: String?) {
        context.dataStore.edit { preferences ->
            preferences[TAMU_ID_KEY] = tamu.idTamu
            preferences[TAMU_NAMA_KEY] = tamu.nama
            preferences[TAMU_TELEPON_KEY] = tamu.telepon
            kodeQr?.let { preferences[TAMU_KODEQR_KEY] = it }
        }
    }

    val tamu: Flow<Tamu?> = context.dataStore.data
        .map { preferences ->
            val id = preferences[TAMU_ID_KEY]
            val nama = preferences[TAMU_NAMA_KEY]
            val telepon = preferences[TAMU_TELEPON_KEY]
            val kodeQr = preferences[TAMU_KODEQR_KEY]
            if (id != null && nama != null && telepon != null) {
                Tamu(idTamu = id, nama = nama, telepon = telepon, kodeQr = kodeQr)
            } else {
                null
            }
        }

    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}