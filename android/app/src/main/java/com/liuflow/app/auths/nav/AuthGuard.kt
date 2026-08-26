package com.liuflow.app.auths.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.liuflow.app.auths.data.AuthManager
import com.liuflow.app.ui.nav.Routes

/**
 * 路由守卫。
 * - 未登录 + 不在认证页 → 跳 [AuthRoutes.Login]
 * - 已登录 + 在认证页 → 跳 [Routes.Focus]（主屏）
 *
 * V0.2.1 简化：判定登录态直接看 AuthManager.loggedIn（SharedPreferences 是否有 token），
 * 不再订阅 AuthRepository.session。
 *
 * 用法：包在 FlowNavHost 顶层。
 */
@Composable
fun AuthGuard(
    auth: AuthManager,
    navController: NavController,
    content: @Composable () -> Unit
) {
    val loggedIn by auth.loggedIn.collectAsState()
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val isAuthRoute = currentRoute in AuthRoutes.All

    LaunchedEffect(loggedIn, currentRoute) {
        when {
            !loggedIn && !isAuthRoute && currentRoute != null -> {
                navController.navigate(AuthRoutes.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
            loggedIn && isAuthRoute -> {
                navController.navigate(Routes.Focus) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
    content()
}
