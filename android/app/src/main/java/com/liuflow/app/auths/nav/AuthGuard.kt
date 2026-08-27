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
        android.util.Log.i("AuthFlow", "[Guard] LaunchedEffect fired: loggedIn=$loggedIn isAuthRoute=$isAuthRoute currentRoute=$currentRoute")
        when {
            !loggedIn && !isAuthRoute && currentRoute != null -> {
                android.util.Log.i("AuthFlow", "[Guard] → navigate(AuthRoutes.Login)")
                try {
                    navController.navigate(AuthRoutes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                    android.util.Log.i("AuthFlow", "[Guard] navigate Login returned")
                } catch (e: Exception) {
                    android.util.Log.e("AuthFlow", "[Guard] navigate Login threw", e)
                }
            }
            loggedIn && isAuthRoute -> {
                android.util.Log.i("AuthFlow", "[Guard] → navigate(Routes.Focus), currentRoute=$currentRoute")
                try {
                    navController.navigate(Routes.Focus) {
                        popUpTo(0) { inclusive = true }
                    }
                    android.util.Log.i("AuthFlow", "[Guard] navigate Focus returned (post-popUpTo)")
                } catch (e: Exception) {
                    android.util.Log.e("AuthFlow", "[Guard] navigate Focus threw", e)
                }
            }
            else -> {
                android.util.Log.i("AuthFlow", "[Guard] no-op branch (loggedIn=$loggedIn isAuthRoute=$isAuthRoute)")
            }
        }
    }
    content()
}
