package com.liuflow.app.auths.ui

import androidx.navigation.NavController

/**
 * V0.2.3 标记为 deprecated：注册流程已合并到单页 `SignupScreen`（按 `SignupStep`
 * 切 UI），不再走独立路由 `AuthRoutes.SignupVerify`。
 *
 * 原因：之前用 NavController 跳到 SignupVerify 时，新路由的 `viewModel(factory = factory)`
 * 拿到的是新 BackStackEntry 的新实例，状态全丢（verificationId / pendingEmail / step
 * 都没了），所以跳过去立刻被弹回。
 *
 * 本文件保留仅为历史参考；新 UI 逻辑请看 `SignupScreen.kt`。
 */
@Deprecated(
    message = "注册流程已合并到 SignupScreen（单页，按 step 切 UI），不再使用独立路由",
    replaceWith = ReplaceWith("SignupScreen(viewModel, navController, onSuccess)")
)
@androidx.compose.runtime.Composable
fun VerificationScreen(
    viewModel: SignupViewModel,
    navController: NavController,
    onSuccess: () -> Unit
) {
    // 简单重定向到 SignupScreen（共用同一个 viewModel 时才工作；不通过 NavController 路由）
    SignupScreen(
        viewModel = viewModel,
        navController = navController,
        onSuccess = onSuccess,
    )
}

