package com.liuflow.app.auths.domain

/**
 * 认证错误类型。UI 层根据 sealed class 分支显示对应提示。
 * [message] 是面向用户的中文文案。
 */
sealed class AuthError(val message: String) {
    object InvalidCredential : AuthError("邮箱或密码错误")
    object UserNotFound : AuthError("该邮箱未注册")
    object UserExists : AuthError("该邮箱已注册")
    object PasswordTooWeak : AuthError("密码需 8-32 位含字母和数字")
    object UsernameInvalid : AuthError("用户名格式不正确（5-24 位字母/数字/下划线/连字符）")
    object UsernameExists : AuthError("该用户名已被占用")

    // 邮箱验证码错误（V0.2.0 注册流程用）
    object VerificationCodeInvalid : AuthError("验证码错误")
    object VerificationCodeExpired : AuthError("验证码已过期，请重新获取")
    object VerificationRateLimited : AuthError("请求过于频繁，请稍后再试")

    data class CaptchaRequired(val captchaId: String, val captchaUrl: String) :
        AuthError("需要图像验证码")
    object CaptchaWrong : AuthError("图像验证码错误，请重试")
    object RateLimited : AuthError("请求过于频繁，请稍后再试")
    object AccountLocked : AuthError("账户临时锁定，请稍后再试")
    object Network : AuthError("网络异常，请检查网络")
    object TokenExpired : AuthError("登录已过期，请重新登录")
    /** 通用参数错误（CloudBase INVALID_ARGUMENT）。请检查邮箱/用户名/密码格式。 */
    object InvalidArgument : AuthError("参数错误：邮箱需标准格式、用户名 5-24 位字母/数字/_/-（不支持中文）、密码 8-32 位含字母+数字")
    data class Server(val detail: String) : AuthError("服务异常：$detail")
    object NotConfigured : AuthError("CloudBase 配置缺失（.env 文件未填写）")
}
