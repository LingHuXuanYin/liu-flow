package com.liuflow.app.auths.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.liuflow.app.BuildConfig
import com.liuflow.app.auths.domain.AuthState
import com.liuflow.app.auths.nav.AuthRoutes
import com.liuflow.app.auths.ui.components.PasswordField
import com.liuflow.app.auths.ui.components.PrimaryButton
import com.liuflow.app.auths.ui.components.UsernameField
import com.liuflow.app.debug.DebugActivity

/**
 * 登录页：username + password → 调 AuthManager.signIn()。
 *
 * V0.2.1 简化：
 *   - 去掉图像验证码弹窗（v0.2.1 范围外，留 TODO 后续接入 AuthManager.getCaptcha/verifyCaptcha）
 *   - 错误用 String 文案直接显示
 *   - 成功 → AuthState.Success → AuthGuard 自动跳主屏
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navController: NavController,
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        if (state is AuthState.Success) onSuccess()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Text("欢迎回到", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "流 Flow",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "登录后数据云端同步，多端互通",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            UsernameField(
                value = username,
                onValueChange = viewModel::setUsername,
                enabled = state !is AuthState.Loading
            )
            PasswordField(
                value = password,
                onValueChange = viewModel::setPassword,
                helperText = "8-32 位字母 + 数字",
                enabled = state !is AuthState.Loading
            )

            (state as? AuthState.Error)?.let { err ->
                Text(
                    text = err.message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "登录",
                onClick = viewModel::signIn,
                loading = state is AuthState.Loading,
                enabled = username.isNotBlank() && password.isNotBlank()
            )

            TextButton(
                onClick = { navController.navigate(AuthRoutes.Signup) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("没有账号？立即注册")
            }

            // 调试入口（仅 DEBUG build 可见）
            if (BuildConfig.DEBUG) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        context.startActivity(Intent(context, DebugActivity::class.java))
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("CloudBase 调试面板")
                }
            }
        }
    }
}
