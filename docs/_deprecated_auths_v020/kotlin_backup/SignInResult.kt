package com.liuflow.app.auths.domain

/**
 * 登录结果。Repository 层返回值；UI 层只关心 Success / NeedCaptcha / Failure。
 */
sealed class SignInResult {
    data class Success(val session: AuthSession) : SignInResult()
    data class NeedCaptcha(val captchaId: String, val captchaUrl: String) : SignInResult()
    data class Failure(val error: AuthError) : SignInResult()
}
