package com.liuflow.app.auths.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 错误响应统一格式。错误码在 [code] 里，消息在 [message] 里。
 */
data class ErrorResponse(
    val code: String? = null,
    val message: String? = null,
    @SerializedName("captcha_id") val captchaId: String? = null,
    @SerializedName("captcha_url") val captchaUrl: String? = null
)
