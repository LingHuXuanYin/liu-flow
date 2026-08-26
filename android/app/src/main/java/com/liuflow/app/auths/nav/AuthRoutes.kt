package com.liuflow.app.auths.nav

/**
 * 登录 / 注册路由。AuthGuard 用 [All] 判断是否在认证相关页面。
 *
 * 注册流程是 3 步：
 *   /auth/login         — 登录页
 *   /auth/signup        — 注册 Step 1：邮箱 + username + 密码
 *   /auth/signup/verify — 注册 Step 2：6 位验证码
 *   注册成功 → AuthGuard 自动跳主屏
 */
object AuthRoutes {
    const val Login = "auth/login"
    const val Signup = "auth/signup"
    const val SignupVerify = "auth/signup/verify"

    val All = setOf(Login, Signup, SignupVerify)
}
