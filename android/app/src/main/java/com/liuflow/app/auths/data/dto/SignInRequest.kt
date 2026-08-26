package com.liuflow.app.auths.data.dto

/**
 * 登录请求 DTO。
 *
 * 真实 body：`{ username, password }`
 * （username 是注册时绑定的字段，**不是 email**）
 *
 * 验证码触发时附带 [captchaToken]，否则不传。
 */
data class SignInRequest(
    val username: String,
    val password: String,
    val captchaToken: String? = null
)
