package com.liuflow.app.ui.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.BuildConfig
import com.liuflow.app.auths.data.AuthManager
import com.liuflow.app.data.prefs.SettingsRepository
import com.liuflow.app.data.prefs.UserSettings
import com.liuflow.app.data.repository.FlowRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 「我」页 ViewModel。
 *
 * V0.2.1 简化：去掉 AuthSession，邮箱 / 用户名直接从 AuthManager 读（SharedPreferences）。
 * V0.2.2：登录判定改用 `AuthManager.loggedIn`（看本地是否有 accessToken），
 *        不依赖 email 字段——CloudBase `/signin` 响应里 email 是可选的，
 *        登录成功但响应不带 email 时「退出登录」按钮就不显示。
 */
class MeViewModel(
    private val repo: FlowRepository,
    private val settings: SettingsRepository,
    private val auth: AuthManager,
) : ViewModel() {

    val version: String get() = BuildConfig.VERSION_NAME

    val settingsState: StateFlow<UserSettings> = settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    /** 当前是否已登录（本地有 accessToken）—— UI 用这个判定显示「退出登录」 */
    val isLoggedIn: StateFlow<Boolean> = auth.loggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, auth.accessToken() != null)

    /**
     * 当前登录用户标识（用于「退出登录」按钮的二级文案）
     * fallback 链：email → username → sub → ""
     */
    val accountLabel: String get() = auth.email()?.takeIf { it.isNotBlank() }
        ?: auth.username()?.takeIf { it.isNotBlank() }
        ?: auth.sub()?.let { "uid: $it" }
        ?: ""

    fun clearAllData() = viewModelScope.launch { repo.deleteAll() }

    fun signOut() {
        auth.signOut()
    }
}

