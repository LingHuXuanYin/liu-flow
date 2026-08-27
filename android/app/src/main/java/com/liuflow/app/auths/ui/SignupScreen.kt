package com.liuflow.app.auths.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.liuflow.app.auths.domain.AuthState
import com.liuflow.app.auths.ui.components.EmailField
import com.liuflow.app.auths.ui.components.PasswordField
import com.liuflow.app.auths.ui.components.PrimaryButton
import com.liuflow.app.auths.ui.components.UsernameField

/**
 * 注册单页（V0.2.4）。
 *
 * UI 全在一页：
 *   邮箱 / 用户名 / 密码 / 确认密码 /
 *   [ 验证码输入框       ] [ 获取验证码 / 60s 倒计时 ]   ← 左右并排
 *   [ 注册 ]
 *
 * verificationId / verificationToken 在 ViewModel 内部流转，**不出现在 UI**。
 */
@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    navController: NavController,
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val email by viewModel.email.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirm by viewModel.confirm.collectAsState()
    val code by viewModel.verificationCode.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        when (state) {
            is AuthState.Registered -> {
                // 注册成功：不自动登录，弹 Toast + popBackStack 回登录页
                Toast.makeText(context, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is AuthState.Success -> onSuccess()  // 兼容旧路径（注册目前不再触发 Success）
            else -> Unit
        }
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
            Spacer(Modifier.height(32.dp))
            Text("创建你的", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "流 Flow 账号",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "邮箱验证后即可开始专注",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            EmailField(
                value = email,
                onValueChange = viewModel::setEmail,
                enabled = state !is AuthState.Loading
            )
            OutlinedTextField(
                value = username,
                onValueChange = viewModel::setUsername,
                label = { Text("用户名（5-24 位字母/数字/_/-）") },
                singleLine = true,
                enabled = state !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
            PasswordField(
                value = password,
                onValueChange = viewModel::setPassword,
                label = "密码",
                helperText = "8-32 位字母 + 数字，区分大小写",
                enabled = state !is AuthState.Loading
            )
            PasswordField(
                value = confirm,
                onValueChange = viewModel::setConfirm,
                label = "确认密码",
                enabled = state !is AuthState.Loading
            )

            // ===== 验证码输入行（输入框 + 获取验证码按钮 并排）=====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = viewModel::setCode,
                    label = { Text("邮箱验证码") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = state !is AuthState.Loading,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        android.util.Log.i("AuthFlow", "[UI] SignupScreen 获取验证码 button clicked")
                        viewModel.fetchVerificationCode()
                    },
                    enabled = email.isNotBlank()
                        && countdown == 0
                        && state !is AuthState.Loading
                ) {
                    Text(
                        text = if (countdown > 0) "${countdown}s" else "获取验证码",
                        fontSize = 13.sp
                    )
                }
            }

            // ===== 错误信息 =====
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
                text = "注册",
                onClick = {
                    android.util.Log.i("AuthFlow", "[UI] SignupScreen 注册 button clicked")
                    viewModel.signUp()
                },
                loading = state is AuthState.Loading,
                enabled = email.isNotBlank() && username.isNotBlank()
                    && password.isNotBlank() && confirm.isNotBlank()
                    && code.length == 6
            )

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
            ) {
                Text("已有账号？立即登录")
            }
        }
    }
}
