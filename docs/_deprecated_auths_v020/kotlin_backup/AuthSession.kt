package com.liuflow.app.auths.domain

/**
 * 已登录会话。包含 accessToken / refreshToken / 过期时间 / 用户信息。
 */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,    // epoch millis
    val user: User
)
