package com.liuflow.app.auths.domain

/**
 * UI 层 AuthState 状态机。ViewModel 暴露 StateFlow<AuthState>，Screen 用 collectAsState 订阅。
 *
 * V0.2.1 简化：去掉 `User` 字段（CloudBase `/signin` 实际返回的是 `sub`，不是嵌套 `user.uid`），
 * 错误统一用 String 文案（按文档示范，不自定义 sealed class）。
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    /** 登录成功（AuthGuard 监 AuthManager.loggedIn 跳主屏） */
    object Success : AuthState()
    /**
     * 注册成功（**不**自动登录）。
     * UI 监听后弹「注册成功，请登录」Toast + popBackStack 回登录页，
     * 让用户用刚注册的账号密码登录。不会触发 AuthGuard 跳主屏。
     */
    object Registered : AuthState()
    /** 错误提示（直接显示给用户） */
    data class Error(val message: String) : AuthState()
}
