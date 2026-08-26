package com.liuflow.app.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.liuflow.app.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    container: AppContainer,
    onBack: () -> Unit,
) {
    val vm: DebugViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DebugViewModel(container.authApi) }
        }
    )
    val lastResult by vm.lastResult.collectAsState()
    val running by vm.running.collectAsState()
    // 自动级联填入：6 号发码成功 → 自动填到 7/8 号；7 号验码成功 → 自动填到 8 号
    val autoVerificationId by vm.lastVerificationId.collectAsState()
    val autoEmail by vm.lastEmail.collectAsState()
    val autoVerificationToken by vm.lastVerificationToken.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CloudBase 调试面板") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // 固定顶部：状态条 + 结果区（永远可见）
            StatusBar(container)
            lastResult?.let { result -> ResultPanel(result) }
            if (running) {
                Text("⏳ 请求中…", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 4.dp))
            }

            // 可滚动卡片区
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "🗂 PG CRUD（§1.1.3）",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                PgSection(vm)

                Spacer(Modifier.height(8.dp))
                Text(
                    "🔐 身份认证（§1.1.4）",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                AuthSection(
                    vm = vm,
                    autoEmail = autoEmail,
                    autoVerificationId = autoVerificationId,
                    autoVerificationToken = autoVerificationToken,
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ResultPanel(result: String) {
    val isSuccess = result.contains("✅")
    val isRunning = result.contains("⏳")
    val container = when {
        isRunning -> MaterialTheme.colorScheme.primaryContainer
        isSuccess -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(
            text = result,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
    }
}

@Composable
private fun StatusBar(container: AppContainer) {
    // 实时反映 cloudbase 实例状态：envId 是否配 / accessToken 是否已注入
    val envId = com.liuflow.app.BuildConfig.TCB_ENV_ID
    val token by container.authApi.accessTokenFlow.collectAsState()
    val tokenLen = token?.length ?: 0

    val envOk = envId.isNotBlank()
    val tokenOk = !token.isNullOrBlank()

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                "cloudbase 实例",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
            Spacer(Modifier.size(4.dp))
            Text(
                "  ${if (envOk) "✓" else "❌"} ENV_ID  ${if (envOk) envId else "(未配置)"}",
                fontSize = 11.sp
            )
            Text(
                "  ${if (tokenOk) "✓" else "❌"} accessToken  ${if (tokenOk) "($tokenLen 字符，已注入 OkHttp 拦截器)" else "(未登录，需要 6→7→8 或 9 号拿到)"}",
                fontSize = 11.sp
            )
            Text(
                "  增删改查后续都走这个 cloudbase 实例（api.request<T>()）",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PgSection(vm: DebugViewModel) {
    var table by remember { mutableStateOf("test") }
    var limit by remember { mutableStateOf("10") }
    var id by remember { mutableStateOf("") }
    // key-value 字段（默认 1 个 key/value，可加多个）
    var kvKey by remember { mutableStateOf("run") }
    var kvVal by remember { mutableStateOf("com2") }

    TestCard("1) PG 查询 (GET) - 列表 List<Map>") {
        OutlinedTextField(
            value = table, onValueChange = { table = it },
            label = { Text("table name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = limit, onValueChange = { limit = it },
            label = { Text("limit") },
            modifier = Modifier.fillMaxWidth()
        )
        ExecButton("执行查询", enabled = table.isNotBlank()) {
            vm.pgQuery(table.trim(), limit.toIntOrNull() ?: 10)
        }
    }

    TestCard("1b) PG 按 id 查单条 (GET ?id=eq.X) - 单个 Map") {
        OutlinedTextField(
            value = table, onValueChange = { table = it },
            label = { Text("table name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = id, onValueChange = { id = it },
            label = { Text("id") },
            modifier = Modifier.fillMaxWidth()
        )
        ExecButton("按 id 查", enabled = table.isNotBlank() && id.isNotBlank()) {
            vm.pgQueryById(table.trim(), id.trim())
        }
    }

    TestCard("2) PG 新增 (POST) - mapOf(key to value)") {
        OutlinedTextField(
            value = table, onValueChange = { table = it },
            label = { Text("table name") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = kvKey, onValueChange = { kvKey = it },
                label = { Text("key") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = kvVal, onValueChange = { kvVal = it },
                label = { Text("value") },
                modifier = Modifier.weight(1f)
            )
        }
        ExecButton("执行新增", enabled = table.isNotBlank() && kvKey.isNotBlank()) {
            vm.pgInsert(table.trim(), mapOf(kvKey.trim() to kvVal.trim()))
        }
    }

    TestCard("3) PG 更新 (PATCH id=eq.X) - mapOf(key to value)") {
        OutlinedTextField(
            value = table, onValueChange = { table = it },
            label = { Text("table name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = id, onValueChange = { id = it },
            label = { Text("id") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = kvKey, onValueChange = { kvKey = it },
                label = { Text("key") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = kvVal, onValueChange = { kvVal = it },
                label = { Text("new value") },
                modifier = Modifier.weight(1f)
            )
        }
        ExecButton("执行更新", enabled = table.isNotBlank() && id.isNotBlank() && kvKey.isNotBlank()) {
            vm.pgUpdate(table.trim(), id.trim(), mapOf(kvKey.trim() to kvVal.trim()))
        }
    }

    TestCard("4) PG upsert (POST) - mapOf(key to value)") {
        OutlinedTextField(
            value = table, onValueChange = { table = it },
            label = { Text("table name") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = kvKey, onValueChange = { kvKey = it },
                label = { Text("key") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = kvVal, onValueChange = { kvVal = it },
                label = { Text("value") },
                modifier = Modifier.weight(1f)
            )
        }
        ExecButton("执行 upsert", enabled = table.isNotBlank() && kvKey.isNotBlank()) {
            vm.pgUpsert(table.trim(), mapOf(kvKey.trim() to kvVal.trim()))
        }
    }

    TestCard("5) PG 删除 (DELETE id=eq.X)") {
        OutlinedTextField(
            value = table, onValueChange = { table = it },
            label = { Text("table name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = id, onValueChange = { id = it },
            label = { Text("id") },
            modifier = Modifier.fillMaxWidth()
        )
        ExecButton("执行删除", enabled = table.isNotBlank() && id.isNotBlank()) {
            vm.pgDelete(table.trim(), id.trim())
        }
    }
}

@Composable
private fun AuthSection(
    vm: DebugViewModel,
    autoEmail: String?,
    autoVerificationId: String?,
    autoVerificationToken: String?,
) {
    var email by remember(autoEmail) { mutableStateOf(autoEmail ?: "") }
    var verificationId by remember(autoVerificationId) { mutableStateOf(autoVerificationId ?: "") }
    var code by remember { mutableStateOf("") }
    var verificationToken by remember(autoVerificationToken) { mutableStateOf(autoVerificationToken ?: "") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    TestCard("6) 发邮箱验证码 → 自动填到 7/8 号") {
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("email") },
            modifier = Modifier.fillMaxWidth()
        )
        ExecButton("发送", enabled = email.isNotBlank()) {
            vm.sendCode(email.trim())
        }
    }

    TestCard("7) 验邮箱验证码 → 自动填到 8 号") {
        OutlinedTextField(
            value = verificationId, onValueChange = { verificationId = it },
            label = { Text("verification_id (6 号自动填)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = code, onValueChange = { code = it },
            label = { Text("6 位 code (邮件里看)") },
            modifier = Modifier.fillMaxWidth()
        )
        ExecButton("验证", enabled = verificationId.isNotBlank() && code.length == 6) {
            vm.verifyCode(verificationId.trim(), code.trim())
        }
    }

    TestCard("8) 注册 (email + verificationToken + username + password)") {
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("email (6 号自动填)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = verificationToken, onValueChange = { verificationToken = it },
            label = { Text("verificationToken (7 号自动填)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text("username (5-24 位字母/数字/_/-, 不含中文)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("password (8-32 位含字母+数字)") },
            modifier = Modifier.fillMaxWidth()
        )
        ExecButton("注册", enabled = email.isNotBlank() && verificationToken.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
            vm.signUpTest(email.trim(), verificationToken.trim(), username.trim(), password.trim())
        }
    }

    TestCard("9) 账号密码登录 (username + password) → accessToken + refreshToken 即访问 App 的全部令牌") {
        OutlinedTextField(
            value = username, onValueChange = { username = it },
            label = { Text("username") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("password") },
            modifier = Modifier.fillMaxWidth()
        )
        ExecButton("登录", enabled = username.isNotBlank() && password.isNotBlank()) {
            vm.signInTest(username.trim(), password.trim())
        }
    }
}

@Composable
private fun TestCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Medium)
            content()
        }
    }
}

@Composable
private fun ExecButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled) {
        Text(text)
    }
}
