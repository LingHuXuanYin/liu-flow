package com.liuflow.app.auths.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.auths.data.AuthManager
import com.liuflow.app.auths.domain.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 登录 ViewModel。
 *
 * V0.2.1 完全按 docs/腾讯云开发接入指引2026.md §1.1.4.2 文档示范重写：
 *   - 调 AuthManager.signIn(username, password)
 *   - 成功 → AuthState.Success
 *   - 失败 → AuthState.Error(message)
 *   - 密码错 3 次以上 CloudBase 会返 CAPTCHA_REQUIRED（暂时 UI 弹通用错误，
 *     不做复杂 captcha 弹窗逻辑——v0.2.1 范围外）
 */
class LoginViewModel(
    private val auth: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun setUsername(value: String) { _username.value = value.trim() }
    fun setPassword(value: String) { _password.value = value }

    fun signIn() {
        val u = _username.value
        val p = _password.value
        android.util.Log.i("AuthFlow", "[VM] LoginViewModel.signIn() entry, u='$u' pLen=${p.length}")
        if (u.isBlank() || p.isBlank()) {
            _state.value = AuthState.Error("请输入用户名和密码")
            return
        }

        viewModelScope.launch {
            android.util.Log.i("AuthFlow", "[VM] LoginViewModel.signIn() coroutine started, state=Loading")
            _state.value = AuthState.Loading
            auth.signIn(u, p)
                .also { android.util.Log.i("AuthFlow", "[VM] LoginViewModel.signIn() auth.signIn returned, success=${it.isSuccess}") }
                .onSuccess {
                    android.util.Log.i("AuthFlow", "[VM] LoginViewModel.signIn() onSuccess → state=Success")
                    _state.value = AuthState.Success
                }
                .onFailure { e ->
                    android.util.Log.e("AuthFlow", "[VM] LoginViewModel.signIn() onFailure: ${e.javaClass.simpleName}: ${e.message}", e)
                    _state.value = AuthState.Error(e.message ?: "登录失败")
                }
        }
    }

    fun clearError() {
        if (_state.value is AuthState.Error) _state.value = AuthState.Idle
    }
}
