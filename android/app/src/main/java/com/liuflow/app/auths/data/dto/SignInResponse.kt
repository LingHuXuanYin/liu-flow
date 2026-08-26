package com.liuflow.app.auths.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 登录 / 注册 / 续期通用响应。返回 access_token / refresh_token / expires_in（秒）。
 */
data class SignInResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long,
    val user: UserDto
)

data class UserDto(
    val uid: String,
    val email: String? = null,
    @SerializedName("created_at") val createdAt: Long? = null
)
