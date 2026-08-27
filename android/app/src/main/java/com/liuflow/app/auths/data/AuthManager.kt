package com.liuflow.app.auths.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * 极简版身份认证（按 `docs/腾讯云开发接入指引2026.md` §1.1.2 + §1.1.4 文档示范风格）。
 *
 * 设计原则（与文档完全一致）：
 * - 用一个 `CloudBaseAuthApi.request<Map<String, Any>>()` 通杀所有 Auth 端点
 *   （登录 / 发码 / 验码 / 注册 / 刷新都直接拼 path + body）
 * - 错误用 `kotlin.Result<T>` 表达，不自定义 sealed class
 * - 本地 token 存 `SharedPreferences`（不加 `EncryptedSharedPreferences`、不存 User 对象）
 * - 不做 401 自动续期（文档示范里就没演示），需要时手动 `refresh()`
 *
 * 与 Debug 入口（`DebugViewModel`）共用同一个 `CloudBaseAuthApi` 实例，但 Debug 不
 * 走本类（不存本地）；主流程走本类（存本地 + 跳主屏）。
 */
class AuthManager(
    context: Context,
    private val api: CloudBaseAuthApi,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mapType = object : TypeToken<Map<String, Any>>() {}

    private val _loggedIn = MutableStateFlow(hasToken())
    val loggedIn: StateFlow<Boolean> = _loggedIn.asStateFlow()

    // ==================== 本地 token 访问 ====================

    fun accessToken(): String? = prefs.getString(KEY_ACCESS, null)
    fun refreshToken(): String? = prefs.getString(KEY_REFRESH, null)
    fun sub(): String? = prefs.getString(KEY_SUB, null)
    fun username(): String? = prefs.getString(KEY_USERNAME, null)
    fun email(): String? = prefs.getString(KEY_EMAIL, null)

    private fun hasToken(): Boolean = !accessToken().isNullOrEmpty()

    /** 退出登录：清本地 + 清 cloudbase 实例的 token */
    fun signOut() {
        prefs.edit().clear().apply()
        api.updateAccessToken(null)
        _loggedIn.value = false
    }

    /**
     * App 启动时调用一次：把本地存的 token 注入到 cloudbase 实例，
     * 让后续业务请求（PG CRUD 等）能自动带 Authorization header。
     */
    fun restoreAccessToken() {
        api.updateAccessToken(accessToken())
    }

    // ==================== 业务方法（对齐文档 §1.1.4）====================

    /** 文档 §1.1.4.1 步骤 1：发邮箱验证码 */
    suspend fun sendSignupCode(email: String): Result<String> = withContext(Dispatchers.IO) {
        android.util.Log.i("AuthFlow", "[Mgr] AuthManager.sendSignupCode() entered, email='$email'")
        // 文档示范 target="NON_USER"；实测当前目标端只接受 "ANY"，其他值都 400 enum invalid
        val body = mapOf("email" to email, "target" to "ANY")
        when (val r = api.request<Map<String, Any>>(
            method = "POST",
            path = "/auth/v1/verification",
            body = body,
            typeToken = mapType,
        )) {
            is ApiResult.Success -> {
                android.util.Log.i("AuthFlow", "[Mgr] sendSignupCode Success, response keys=${r.data.keys}")
                val vid = r.data["verification_id"] as? String
                if (vid == null) Result.failure(IllegalStateException("响应无 verification_id"))
                else Result.success(vid)
            }
            is ApiResult.Failure -> {
                android.util.Log.e("AuthFlow", "[Mgr] sendSignupCode Failure code=${r.code} msg=${r.message}")
                Result.failure(AuthException(r.code, r.message))
            }
        }
    }

    /** 文档 §1.1.4.1 步骤 2：验邮箱验证码，拿到 verificationToken */
    suspend fun verifyCode(verificationId: String, code: String): Result<String> = withContext(Dispatchers.IO) {
        android.util.Log.i("AuthFlow", "[Mgr] AuthManager.verifyCode() entered, vid='$verificationId' codeLen=${code.length}")
        val body = mapOf("verification_id" to verificationId, "verification_code" to code)
        when (val r = api.request<Map<String, Any>>(
            method = "POST",
            path = "/auth/v1/verification/verify",
            body = body,
            typeToken = mapType,
        )) {
            is ApiResult.Success -> {
                android.util.Log.i("AuthFlow", "[Mgr] verifyCode Success, response keys=${r.data.keys}")
                val vt = r.data["verification_token"] as? String
                if (vt == null) Result.failure(IllegalStateException("响应无 verification_token"))
                else Result.success(vt)
            }
            is ApiResult.Failure -> {
                android.util.Log.e("AuthFlow", "[Mgr] verifyCode Failure code=${r.code} msg=${r.message}")
                Result.failure(AuthException(r.code, r.message))
            }
        }
    }

    /**
     * 文档 §1.1.4.1 步骤 3：注册（带 username + password）。
     *
     * **注册成功不自动登录**——不写本地 token、不置 `_loggedIn = true`。
     * UI 监听到 `AuthState.Registered` 后弹 Toast + popBackStack 回登录页，
     * 让用户用新账号密码登录（见 `SignupViewModel.signUp()` + `SignupScreen`）。
     *
     * 为什么不自动登录：
     * - 用户体验上，注册是「开账号」动作，登录是「进系统」动作，应该分开。
     * - 即使 `/signup` 响应里返回了 accessToken，把它写本地会让 `AuthManager.loggedIn = true`，
     *   `AuthGuard` 监听到立刻跳主屏（`Routes.Focus`），用户失去"刚刚注册的成就感 + 输密码的明确反馈"。
     * - 真实业务里这种设计也更清晰：注册流程即"提交注册信息"，登录流程即"用账号进系统"。
     *
     * 如果未来需要给注册响应里的 token 写本地，调用方应显式调 `persistFromResponse(r.data, email)`。
     */
    suspend fun signUp(
        email: String,
        verificationToken: String,
        username: String,
        password: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        android.util.Log.i("AuthFlow", "[Mgr] AuthManager.signUp() entered, email='$email' u='$username' vtLen=${verificationToken.length}")
        val body = mapOf(
            "email" to email,
            "verification_token" to verificationToken,
            "username" to username,
            "password" to password,
        )
        when (val r = api.request<Map<String, Any>>(
            method = "POST",
            path = "/auth/v1/signup",
            body = body,
            typeToken = mapType,
        )) {
            is ApiResult.Success -> {
                android.util.Log.i("AuthFlow", "[Mgr] signUp Success code=${r.code}")
                // 注意：这里故意不调 persistFromResponse / 不写 _loggedIn。
                // 响应里的 access_token / refresh_token 仅用于让接口层 200 校验通过，
                // 不下沉到本地存储 / 触发 AuthGuard 跳主屏。
                Result.success(Unit)
            }
            is ApiResult.Failure -> {
                android.util.Log.e("AuthFlow", "[Mgr] signUp Failure code=${r.code} msg=${r.message}")
                Result.failure(AuthException(r.code, r.message))
            }
        }
    }

    /**
     * 文档 §1.1.4.2：账号密码登录。
     * body 是 `{ username, password }`（**username 不是 email**）。
     *
     * 关键：signin 必须用账号密码向云端申请**新的** access_token + refresh_token，
     * 绝不能携带过期的旧 Authorization 头。
     *
     * - App 启动时 `AppContainer.restoreAccessToken()` 会把本地存的 accessToken 注入
     *   `CloudBaseAuthApi.accessToken`，而 `CloudBaseAuthApi.request()` 对**所有**端点
     *   都自动附加 `Authorization: Bearer <token>`。如果用户带着过期 token 进入登录页
     *   再点登录，这里就必须先把 `api.accessToken` 临时清空，否则后端会返
     *   `HTTP 400 failed_precondition / token expiry`（见 2026-08-28 00:11 Logcat 抓的 case）。
     * - 登录失败时清掉本地旧 token，避免下次启动又用过期 token「伪登录」成功、
     *   但所有业务请求都 401 的灰色状态。
     */
    suspend fun signIn(
        username: String,
        password: String,
        captchaToken: String? = null,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        android.util.Log.i("AuthFlow", "[Mgr] AuthManager.signIn() entered, u='$username' pLen=${password.length} hasCaptcha=${captchaToken != null}")
        // 临时把 api 的 accessToken 清空，signin 请求不带 Authorization 头
        api.updateAccessToken(null)
        val body = mutableMapOf<String, Any>("username" to username, "password" to password)
        val custom = if (captchaToken != null) mapOf("x-captcha-token" to captchaToken) else emptyMap()
        when (val r = api.request<Map<String, Any>>(
            method = "POST",
            path = "/auth/v1/signin",
            body = body,
            customHeaders = custom,
            typeToken = mapType,
        )) {
            is ApiResult.Success -> {
                android.util.Log.i("AuthFlow", "[Mgr] signIn Success code=${r.code}, response keys=${r.data.keys}")
                persistFromResponse(r.data, emailOverride = null)
                Result.success(Unit)
            }
            is ApiResult.Failure -> {
                android.util.Log.e("AuthFlow", "[Mgr] signIn Failure code=${r.code} msg=${r.message}")
                // 登录失败：清掉本地旧 token（accessToken / refreshToken），状态回到「未登录」，
                // 强制用户重新登录，避免下次启动又用过期 token 走通 `hasToken()` 但请求全 401
                signOut()
                Result.failure(AuthException(r.code, r.message))
            }
        }
    }

    /**
     * 文档未演示但 path 存在：用 refreshToken 换新 accessToken。
     * accessToken 过期时主流程可手动调（不再做 401 自动续期拦截器）。
     */
    suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        val rt = refreshToken()
            ?: return@withContext Result.failure(IllegalStateException("本地无 refresh_token"))
        android.util.Log.i("AuthFlow", "[Mgr] AuthManager.refresh() entered")
        val body = mapOf("refresh_token" to rt)
        when (val r = api.request<Map<String, Any>>(
            method = "POST",
            path = "/auth/v1/token/refresh",
            body = body,
            typeToken = mapType,
        )) {
            is ApiResult.Success -> {
                android.util.Log.i("AuthFlow", "[Mgr] refresh Success")
                persistFromResponse(r.data, emailOverride = null)
                Result.success(Unit)
            }
            is ApiResult.Failure -> {
                android.util.Log.e("AuthFlow", "[Mgr] refresh Failure code=${r.code} msg=${r.message}")
                // 续期失败 → 清本地，强制重新登录
                signOut()
                Result.failure(AuthException(r.code, r.message))
            }
        }
    }

    /**
     * 图像验证码：登录风控触发后，先 GET 拿 captcha_url + captcha_id，
     * 用户输入字符后 POST 验，拿到 captcha_token 再调 [signIn]。
     * 文档 §1.1.4 没列路径，但 Debug 入口确认是 `/auth/v1/captcha` + `/auth/v1/captcha/verify`。
     */
    suspend fun getCaptcha(): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        android.util.Log.i("AuthFlow", "[Mgr] AuthManager.getCaptcha() entered")
        when (val r = api.request<Map<String, Any>>(
            method = "GET",
            path = "/auth/v1/captcha",
            typeToken = mapType,
        )) {
            is ApiResult.Success -> {
                android.util.Log.i("AuthFlow", "[Mgr] getCaptcha Success")
                val id = r.data["captcha_id"] as? String
                val url = r.data["captcha_url"] as? String
                if (id == null || url == null)
                    Result.failure(IllegalStateException("响应缺 captcha_id / captcha_url"))
                else Result.success(id to url)
            }
            is ApiResult.Failure -> {
                android.util.Log.e("AuthFlow", "[Mgr] getCaptcha Failure code=${r.code} msg=${r.message}")
                Result.failure(AuthException(r.code, r.message))
            }
        }
    }

    suspend fun verifyCaptcha(captchaId: String, code: String): Result<String> = withContext(Dispatchers.IO) {
        android.util.Log.i("AuthFlow", "[Mgr] AuthManager.verifyCaptcha() entered, captchaId='$captchaId' code='$code'")
        val body = mapOf("captcha_id" to captchaId, "code" to code)
        when (val r = api.request<Map<String, Any>>(
            method = "POST",
            path = "/auth/v1/captcha/verify",
            body = body,
            typeToken = mapType,
        )) {
            is ApiResult.Success -> {
                android.util.Log.i("AuthFlow", "[Mgr] verifyCaptcha Success")
                val token = r.data["captcha_token"] as? String
                if (token == null) Result.failure(IllegalStateException("响应无 captcha_token"))
                else Result.success(token)
            }
            is ApiResult.Failure -> {
                android.util.Log.e("AuthFlow", "[Mgr] verifyCaptcha Failure code=${r.code} msg=${r.message}")
                Result.failure(AuthException(r.code, r.message))
            }
        }
    }

    // ==================== 私有：持久化 token ====================

    /**
     * 从响应中提取 access_token / refresh_token / sub / username / email 写到本地
     * SharedPreferences，并把 accessToken 同步到 `CloudBaseAuthApi` 实例（让后续
     * 业务请求自动带 Authorization header）。
     *
     * 响应字段（按文档 §1.1.4.2 第 388-401 行示范）：
     *   - `access_token`   必有
     *   - `refresh_token`  必有
     *   - `sub`            用户 ID
     *   - `email` / `username` 可选
     */
    private fun persistFromResponse(data: Map<String, Any>, emailOverride: String?) {
        val access = data["access_token"] as? String ?: return
        val refresh = (data["refresh_token"] as? String).orEmpty()
        val sub = data["sub"] as? String
        val u = data["username"] as? String
        val e = (data["email"] as? String) ?: emailOverride

        val edit = prefs.edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
        if (sub != null) edit.putString(KEY_SUB, sub)
        if (u != null) edit.putString(KEY_USERNAME, u)
        if (e != null) edit.putString(KEY_EMAIL, e)
        edit.apply()

        api.updateAccessToken(access)
        _loggedIn.value = true
    }

    companion object {
        private const val PREFS_NAME = "auth_tokens"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_SUB = "sub"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }
}

/**
 * 认证相关异常的轻量包装：带 HTTP code + 接口响应 message 文案。
 * 文档风格不自定义 sealed class，UI 层直接 `e.message` 显示。
 */
class AuthException(val code: Int, message: String) : RuntimeException(message)
