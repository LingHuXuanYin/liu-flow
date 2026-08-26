package com.liuflow.app.auths.domain

/**
 * 注册流程的内部状态机。SignupViewModel 用它驱动 Step 1 / Step 2 切换。
 *
 * V0.2.1 简化：去掉 data class 字段（email / username / password / verificationId），
 * 这些字段直接存在 SignupViewModel 内部的 StateFlow 里，避免双份状态。
 *
 * 流程：
 *   EmailForm → 输邮箱 + username + 密码 + 点「发送验证码」
 *     ↓
 *   CodeSent → 邮箱已发，verificationId 存于 SignupViewModel._verificationId
 *     ↓ （用户切到 VerificationScreen 输 6 位验证码）
 *     ↓ 点「注册」
 *     ↓
 *   Success → 注册成功，自动登录，AuthGuard 跳主屏
 */
sealed class SignupStep {
    object EmailForm : SignupStep()
    object CodeSent : SignupStep()
    object Success : SignupStep()
}
