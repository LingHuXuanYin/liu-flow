package com.liuflow.app.auths.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.liuflow.app.auths.domain.AuthSession
import com.liuflow.app.auths.domain.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 加密 Token 存储。
 * - 用 EncryptedSharedPreferences（AES-256 GCM）防止本地明文泄漏
 * - 加密 IO（Keystore + EncryptedSharedPreferences）放后台线程异步加载，
 *   避免 Application 启动时主线程阻塞导致 ANR
 * - [session] 初始为 null，加载完成后才更新
 */
class AuthTokenStore(
    context: Context,
    private val appScope: CoroutineScope
) {

    private val appContext = context.applicationContext

    /**
     * EncryptedSharedPreferences 延迟初始化。
     * - 第一次访问（IO 线程）时触发 Keystore + 加密 FS IO
     * - 用 SYNCHRONIZED 避免多线程同时触发
     * - 主线程访问 [save] / [load] / [clear] 时会短暂阻塞，但只触发一次
     */
    private val prefs: SharedPreferences by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _session = MutableStateFlow<AuthSession?>(null)
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    init {
        // 异步加载：避免 Application 启动时主线程阻塞
        appScope.launch(Dispatchers.IO) {
            try {
                val loaded = withContext(Dispatchers.IO) { loadInternal() }
                _session.value = loaded
                Log.i(TAG, "Token store ready, session=${if (loaded == null) "null" else "expired-or-valid"}")
            } catch (e: Exception) {
                // 关键：吞掉异常，让 App 至少能启动（用户可重新登录）
                Log.e(TAG, "Failed to load encrypted token store, falling back to logged-out state", e)
                _session.value = null
            }
        }
    }

    fun save(session: AuthSession) {
        try {
            prefs.edit()
                .putString(KEY_ACCESS, session.accessToken)
                .putString(KEY_REFRESH, session.refreshToken)
                .putLong(KEY_EXPIRES_AT, session.expiresAt)
                .putString(KEY_UID, session.user.uid)
                .putString(KEY_EMAIL, session.user.email)
                .apply()
            _session.value = session
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save session", e)
        }
    }

    fun load(): AuthSession? {
        return try {
            val access = prefs.getString(KEY_ACCESS, null) ?: return null
            val refresh = prefs.getString(KEY_REFRESH, null) ?: return null
            val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
            val uid = prefs.getString(KEY_UID, null) ?: return null
            val email = prefs.getString(KEY_EMAIL, null) ?: return null
            if (expiresAt <= System.currentTimeMillis()) null
            else AuthSession(
                accessToken = access,
                refreshToken = refresh,
                expiresAt = expiresAt,
                user = User(uid = uid, email = email, createdAt = null)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load session", e)
            null
        }
    }

    /** 内部用：初始化时调用（已知在 IO 线程） */
    private fun loadInternal(): AuthSession? = load()

    /** 剩余有效期 > 60s 视为有效（留 buffer 给 Token 续期） */
    fun isValid(): Boolean {
        val s = _session.value ?: return false
        return s.expiresAt > System.currentTimeMillis() + 60_000
    }

    fun clear() {
        try {
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear prefs", e)
        }
        _session.value = null
    }

    companion object {
        private const val TAG = "AuthTokenStore"
        private const val FILE_NAME = "auth_tokens"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_UID = "uid"
        private const val KEY_EMAIL = "email"
    }
}
