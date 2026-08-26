package com.liuflow.app.auths.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 注册请求 DTO。
 *
 * 注册强制要求 verification_token（邮箱验证码校验后的令牌）。
 * 可选同时绑定 username + password（用于后续 username + password 登录）。
 */
data class SignUpRequest(
    val email: String,
    @SerializedName("verification_token") val verificationToken: String,
    val username: String? = null,
    val password: String? = null,
    val nickname: String? = null
)
