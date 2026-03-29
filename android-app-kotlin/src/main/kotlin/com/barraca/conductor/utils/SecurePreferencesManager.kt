package com.barraca.conductor.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manager para SharedPreferences encriptadas
 * Almacena datos sensibles de forma segura
 */
class SecurePreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        Constants.PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ==================== TOKEN ====================

    fun saveToken(token: String) {
        prefs.edit().putString(Constants.PREF_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(Constants.PREF_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(Constants.PREF_TOKEN).apply()
    }

    // ==================== USUARIO ====================

    fun saveUserId(userId: String) {
        prefs.edit().putString(Constants.PREF_USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString(Constants.PREF_USER_ID, null)
    }

    fun saveUserName(name: String) {
        prefs.edit().putString(Constants.PREF_USER_NAME, name).apply()
    }

    fun getUserName(): String? {
        return prefs.getString(Constants.PREF_USER_NAME, null)
    }

    // ==================== GENERAL ====================

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun hasToken(): Boolean {
        return getToken() != null
    }
}
