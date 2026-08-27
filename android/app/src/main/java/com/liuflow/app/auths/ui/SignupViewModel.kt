package com.liuflow.app.auths.ui

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.auths.data.AuthManager
import com.liuflow.app.auths.domain.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 注册 ViewModel（V0.2.4 单页一体化）。
 *
 * UI 只有一个页面：邮箱 + 账号 + 密码 + 确认密码 + 验证码 + 获取验证码按钮 + 注册按钮。
 * `pendingVerificationId` 是 ViewModel 内部流转数据，不出现在 UI。
 *
 * 流程（用户视角）：
 *   1. 填邮箱 / 账号 / 密码 / 确认密码
 *   2. 点"获取验证码" → 发码 → 启动 60s 倒计时
 *   3. 用户在邮箱收到 6 位码，输到验证码框
 *   4. 点"注册" → 验码 + 注册 → 跳主屏
 */
class SignupViewModel(
    private val auth: AuthManager
) : ViewModel() {

    // ===== 通用 loading / error =====
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // ===== 表单字段（全部暴露给 UI）=====
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirm = MutableStateFlow("")
    val confirm: StateFlow<String> = _confirm.asStateFlow()

    private val _verificationCode = MutableStateFlow("")
    val verificationCode: StateFlow<String> = _verificationCode.asStateFlow()

    // ===== 倒计时（"获取验证码"按钮文案）=====
    private val _countdown = MutableStateFlow(0)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    /**
     * 发码成功后存 verificationId，验码时用。
     * 初始 null（用户还没点过"获取验证码"）。
     * ViewModel 内部流转，**不暴露给 UI**。
     */
    private var pendingVerificationId: String? = null

    fun setEmail(v: String) { _email.value = v.trim() }
    fun setUsername(v: String) { _username.value = v.trim() }
    fun setPassword(v: String) { _password.value = v }
    fun setConfirm(v: String) { _confirm.value = v }
    fun setCode(v: String) {
        if (v.length <= 6 && v.all { it.isDigit() || it.isLetter() }) {
            _verificationCode.value = v.uppercase()
        }
    }

    /** "获取验证码"按钮：60s 内 disabled + 倒计时；0 时 enabled + 文案"重新发送" */
    fun canFetchCode(): Boolean = _countdown.value == 0

    /** 点"获取验证码"：发码 → 存 vid → 启动 60s 倒计时 */
    fun fetchVerificationCode() {
        val email = _email.value
        android.util.Log.i("AuthFlow", "[VM] SignupViewModel.fetchVerificationCode() entry, email='$email'")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = AuthState.Error("邮箱格式不正确")
            return
        }
        if (_countdown.value > 0) return  // 倒计时未结束，UI 也应 disable，这里兜底

        viewModelScope.launch {
            android.util.Log.i("AuthFlow", "[VM] fetchVerificationCode coroutine started, state=Loading")
            _state.value = AuthState.Loading
            auth.sendSignupCode(email)
                .also { android.util.Log.i("AuthFlow", "[VM] sendSignupCode returned, success=${it.isSuccess}") }
                .onSuccess { vid ->
                    android.util.Log.i("AuthFlow", "[VM] sendSignupCode onSuccess, vid='$vid'")
                    pendingVerificationId = vid
                    _state.value = AuthState.Idle
                    startCountdown()
                }
                .onFailure { e ->
                    android.util.Log.e("AuthFlow", "[VM] sendSignupCode onFailure: ${e.javaClass.simpleName}: ${e.message}", e)
                    _state.value = AuthState.Error(e.message ?: "发送验证码失败")
                }
        }
    }

    /** "注册"按钮：先验码 → 再注册（带 username + password） */
    fun signUp() {
        val code = _verificationCode.value
        val vid = pendingVerificationId
        val email = _email.value
        val username = _username.value
        val pwd = _password.value
        val cfm = _confirm.value

        android.util.Log.i("AuthFlow", "[VM] SignupViewModel.signUp() entry, codeLen=${code.length} vidNull=${vid == null} email='$email' u='$username'")
        if (!isFormValid(email, username, pwd, cfm)) return
        if (code.length != 6) {
            _state.value = AuthState.Error("请输入 6 位验证码")
            return
        }
        if (vid.isNullOrEmpty()) {
            _state.value = AuthState.Error("请先点「获取验证码」按钮")
            return
        }

        viewModelScope.launch {
            android.util.Log.i("AuthFlow", "[VM] signUp coroutine started, state=Loading")
            _state.value = AuthState.Loading
            // 步骤 1：验码
            android.util.Log.i("AuthFlow", "[VM] signUp step 1: verifyCode")
            val vtResult = auth.verifyCode(vid, code)
            android.util.Log.i("AuthFlow", "[VM] signUp verifyCode returned, success=${vtResult.isSuccess}")
            val verificationToken = vtResult.getOrElse {
                android.util.Log.e("AuthFlow", "[VM] signUp verifyCode failed: ${it.javaClass.simpleName}: ${it.message}", it)
                _state.value = AuthState.Error(it.message ?: "验证码错误")
                return@launch
            }
            // 步骤 2：注册
            android.util.Log.i("AuthFlow", "[VM] signUp step 2: register with verificationToken len=${verificationToken.length}")
            auth.signUp(email, verificationToken, username, pwd)
                .also { android.util.Log.i("AuthFlow", "[VM] signUp register returned, success=${it.isSuccess}") }
                .onSuccess {
                    android.util.Log.i("AuthFlow", "[VM] signUp onSuccess → state=Registered")
                    _state.value = AuthState.Registered
                }
                .onFailure { e ->
                    android.util.Log.e("AuthFlow", "[VM] signUp onFailure: ${e.javaClass.simpleName}: ${e.message}", e)
                    _state.value = AuthState.Error(e.message ?: "注册失败")
                }
        }
    }

    fun clearError() {
        if (_state.value is AuthState.Error) _state.value = AuthState.Idle
    }

    // ===== 私有 =====

    private fun startCountdown() {
        _countdown.value = 60
        viewModelScope.launch {
            while (_countdown.value > 0) {
                kotlinx.coroutines.delay(1000)
                _countdown.value = _countdown.value - 1
            }
        }
    }

    private fun isFormValid(email: String, username: String, pwd: String, cfm: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.value = AuthState.Error("邮箱格式不正确")
            return false
        }
        // username 规则：5-24 位字母/数字/_/-
        if (username.length !in 5..24 || !username.all { it.isLetterOrDigit() || it == '_' || it == '-' }) {
            _state.value = AuthState.Error("用户名格式不正确（5-24 位字母/数字/_/-，不含中文）")
            return false
        }
        if (pwd != cfm) {
            _state.value = AuthState.Error("两次密码输入不一致")
            return false
        }
        if (pwd.length !in 8..32 || !pwd.any { it.isLetter() } || !pwd.any { it.isDigit() }) {
            _state.value = AuthState.Error("密码需 8-32 位含字母和数字")
            return false
        }
        return true
    }
}
