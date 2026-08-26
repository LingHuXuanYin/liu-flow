package com.liuflow.app.auths.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 发送邮箱验证码响应。
 * 路径：POST /auth/v1/verification，body: { email, target }
 *   target = "ANY"（实测只接受这个值）
 */
data class VerificationResponse(
    @SerializedName("verification_id") val verificationId: String
)

/**
 * 校验邮箱验证码响应，返回 [verificationToken] 用于后续注册 / 登录。
 */
data class VerificationTokenResponse(
    @SerializedName("verification_token") val verificationToken: String
)
