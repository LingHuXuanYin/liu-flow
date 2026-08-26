package com.liuflow.app.auths.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 图像验证码获取 / 校验响应。
 *
 * 路径（按文档示范）：
 *   - GET  /auth/v1/captcha         → 拿 captcha_id + captcha_url
 *   - POST /auth/v1/captcha/verify  → 拿 captcha_token
 */
data class CaptchaResponse(
    @SerializedName("captcha_id") val captchaId: String,
    @SerializedName("captcha_url") val captchaUrl: String
)

data class CaptchaVerifyResponse(
    @SerializedName("captcha_token") val captchaToken: String
)
