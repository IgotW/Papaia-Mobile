package com.google.papaia.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecurePrefsHelper {

    private const val PREFS_FILENAME = "secure_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"

    private fun getPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            PREFS_FILENAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? =
        getPrefs(context).getString(KEY_AUTH_TOKEN, null)

    fun clearToken(context: Context) {
        getPrefs(context).edit().remove(KEY_AUTH_TOKEN).apply()
    }
}