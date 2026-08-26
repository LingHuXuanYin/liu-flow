package com.liuflow.app.auths.data

import com.liuflow.app.auths.data.dto.SignInRequest
import com.liuflow.app.auths.data.dto.SignInResponse
import com.liuflow.app.auths.data.dto.SignUpRequest
import com.liuflow.app.auths.data.dto.UserDto
import com.liuflow.app.auths.domain.AuthError
import com.liuflow.app.auths.domain.AuthSession
import com.liuflow.app.auths.domain.SignInResult
import com.liuflow.app.auths.domain.SignUpResult
import com.liuflow.app.auths.domain.User
import kotlinx.coroutines.flow.StateFlow

/**
 * 认证业务编排入口。ViewModel 只依赖 Repository，不直接接触 CloudBaseAuthApi / TokenStore。
 */
class AuthRepository(
    private val api: CloudBaseAuthApi,
    private val tokenStore: AuthTokenStore
) {
    /** 当前会话 StateFlow，UI 层订阅以感知登出 / Token 失效 */
    val session: StateFlow<AuthSession?> = tokenStore.session

    // ===== 登录（username + password）=====
    // 注意：CloudBase /signin body 用 username 字段（注册时绑定），不是 email

    suspend fun signIn(username: String, password: String, captchaToken: String? = null): SignInResult {
        return when (val resp = api.signIn(SignInRequest(username, password, captchaToken))) {
            is ApiResult.Success -> {
                val s = resp.data.toAuthSession()
                tokenStore.save(s)
                api.updateAccessToken(s.accessToken)  // 同步新 token 到 OkHttp 拦截器
                SignInResult.Success(s)
            }
            is ApiResult.Failure -> when (val err = resp.error) {
                is AuthError.CaptchaRequired -> SignInResult.NeedCaptcha(err.captchaId, err.captchaUrl)
                else -> SignInResult.Failure(err)
            }
        }
    }

    // ===== 注册（V0.2.0 真实流程：邮箱+验证码 3 步）=====

    /**
     * Step 1：发送邮箱验证码。返回 verificationId 供 Step 2 使用。
     */
    suspend fun sendSignupCode(email: String): RequestCodeResult {
        return when (val resp = api.getVerification(email, target = "NON_USER")) {
            is ApiResult.Success -> RequestCodeResult.Success(resp.data.verificationId)
            is ApiResult.Failure -> RequestCodeResult.Failure(resp.error)
        }
    }

    /**
     * Step 2 + 3：校验验证码 + 注册（同时绑定 username + password）。
     * 一次调用完成注册 + 登录（注册成功即返回 accessToken）。
     */
    suspend fun signUpWithCode(
        email: String,
        verificationId: String,
        code: String,
        username: String,
        password: String
    ): SignUpResult {
        // 步骤1：校验验证码
        val verifyResp = api.verifyVerification(verificationId, code)
        if (verifyResp is ApiResult.Failure) {
            return SignUpResult.Failure(verifyResp.error)
        }
        val verificationToken = (verifyResp as ApiResult.Success).data.verificationToken

        // 步骤2：注册（带 username + password）
        val signUpReq = SignUpRequest(
            email = email,
            verificationToken = verificationToken,
            username = username,
            password = password
        )
        return when (val resp = api.signUp(signUpReq)) {
            is ApiResult.Success -> {
                val s = resp.data.toAuthSession()
                tokenStore.save(s)
                api.updateAccessToken(s.accessToken)  // 同步新 token 到 OkHttp 拦截器
                SignUpResult.Success(s)
            }
            is ApiResult.Failure -> SignUpResult.Failure(resp.error)
        }
    }

    /** 验证图像验证码（登录风控） */
    suspend fun verifyCaptcha(captchaId: String, code: String): String? {
        return when (val r = api.verifyCaptcha(captchaId, code)) {
            is ApiResult.Success -> r.data.captchaToken
            is ApiResult.Failure -> null
        }
    }

    /** Token 续期：被 AuthInterceptor 在 401 时自动调用 */
    suspend fun refreshToken(): AuthSession? {
        val current = tokenStore.load() ?: return null
        return when (val resp = api.refreshToken(current.refreshToken)) {
            is ApiResult.Success -> {
                val s = resp.data.toAuthSession()
                tokenStore.save(s)
                api.updateAccessToken(s.accessToken)  // 同步新 token 到 OkHttp 拦截器
                s
            }
            is ApiResult.Failure -> {
                tokenStore.clear()
                api.updateAccessToken(null)  // 清掉过期 token
                null
            }
        }
    }

    /** 退出登录 */
    suspend fun signOut() {
        val current = tokenStore.load() ?: return
        runCatching { api.signOut(current.refreshToken, current.accessToken) }
        tokenStore.clear()
        api.updateAccessToken(null)  // 清掉 token
    }
}

sealed class RequestCodeResult {
    data class Success(val verificationId: String) : RequestCodeResult()
    data class Failure(val error: AuthError) : RequestCodeResult()
}

private fun SignInResponse.toAuthSession(): AuthSession {
    val expiresAt = System.currentTimeMillis() + expiresIn * 1000
    return AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAt = expiresAt,
        user = user.toUser()
    )
}

private fun UserDto.toUser(): User =
    User(uid = uid, email = email.orEmpty(), createdAt = createdAt)
