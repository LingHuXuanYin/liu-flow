# Android auths/ 模块测试策略

> 配套：
> - 架构设计：[android-auth-architecture.md](./android-auth-architecture.md)
> - 集成清单：[android-auth-architecture.md §8](./android-auth-architecture.md)

## 测试挑战

`AuthTokenStore` 构造函数立即调用 `MasterKey.Builder(context)` 和 `EncryptedSharedPreferences.create(context, ...)`——这两个直接访问 Android Framework，**JVM 单元测试不能实例化**。

所以单测策略必须二选一：
- A. Instrumentation Test（跑在真机/模拟器上）
- B. 重构为接口（让 JVM 单测能跑）

## 推荐方案：Instrumentation Test（首选）

零依赖改造，**直接用真实 AuthTokenStore + 真实 SharedPreferences**。跑在 `androidTest` 源集（不是 `test` 源集），用 `AndroidJUnit4` runner。

### 配置（已就绪）

`app/build.gradle.kts` 已经有：
```kotlin
testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
```

需要新增依赖到 `gradle/libs.versions.toml`：
```toml
androidx-test-runner = "1.6.1"
androidx-test-core = "1.6.1"
androidx-test-ext-junit = "1.2.1"
```

`app/build.gradle.kts`：
```kotlin
androidTestImplementation(libs.androidx.test.ext.junit)
```

### 7 个核心测试场景

```kotlin
@RunWith(AndroidJUnit4::class)
class AuthRepositoryInstrumentedTest {
    private lateinit var api: CloudBaseAuthApi
    private lateinit var store: AuthTokenStore
    private lateinit var repo: AuthRepository

    @Before fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // 每次测试前清空（避免污染）
        context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE).edit().clear()
        store = AuthTokenStore(context)
        api = CloudBaseAuthApi(envId = "test-env-id")  // 替换为测试环境
        repo = AuthRepository(api, store)
    }

    @Test fun signIn_with_correct_credentials_returns_Success() = runBlocking {
        // 注意：实际测试需先在测试 CloudBase 环境准备好测试账号
        // 或者用 MockWebServer 拦截 HTTP 请求
    }

    @Test fun signIn_with_wrong_password_returns_Failure_InvalidCredential() { ... }
    @Test fun signIn_with_captcha_required_returns_NeedCaptcha() { ... }
    @Test fun signUp_with_new_email_returns_Success_and_auto_logs_in() { ... }
    @Test fun signUp_with_existing_email_returns_Failure_UserExists() { ... }
    @Test fun refreshToken_success_saves_new_session() { ... }
    @Test fun refreshToken_failure_clears_local_session() { ... }
}
```

### MockWebServer（推荐）

为了不依赖真实 CloudBase 环境，用 `okhttp-mockwebserver` 拦截 HTTP 请求：

```kotlin
private lateinit var mockWebServer: MockWebServer

@Before fun setup() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
    // 注入到 CloudBaseAuthApi 的 baseUrl
    api = CloudBaseAuthApi(baseUrl = mockWebServer.url("/").toString())
}

@After fun teardown() { mockWebServer.shutdown() }

@Test fun signIn_with_correct_credentials() = runBlocking {
    mockWebServer.enqueue(MockResponse()
        .setResponseCode(200)
        .setBody("""
            {
              "access_token": "test-access",
              "refresh_token": "test-refresh",
              "expires_in": 7200,
              "user": { "uid": "uid-1", "email": "test@example.com" }
            }
        """.trimIndent()))

    val result = repo.signIn("test@example.com", "password123")

    assertTrue(result is SignInResult.Success)
    assertEquals("uid-1", (result as SignInResult.Success).session.user.uid)
}
```

## 备选方案：重构为接口

更彻底的解耦，但工作量大。

### Step 1：抽接口

新建 `auths/data/AuthTokenStorage.kt`：
```kotlin
package com.liuflow.app.auths.data

import com.liuflow.app.auths.domain.AuthSession
import kotlinx.coroutines.flow.StateFlow

interface AuthTokenStorage {
    val session: StateFlow<AuthSession?>
    fun load(): AuthSession?
    fun save(session: AuthSession)
    fun clear()
}
```

### Step 2：现有 TokenStore 改为实现

```kotlin
class AuthTokenStore(context: Context) : AuthTokenStorage {
    // 现有实现
}
```

### Step 3：AuthRepository 接受接口

```kotlin
class AuthRepository(
    private val api: CloudBaseAuthApi,
    private val tokenStore: AuthTokenStorage  // 改这里
)
```

### Step 4：AppContainer 兼容

```kotlin
val authTokenStore: AuthTokenStorage = AuthTokenStore(appContext)  // 多态
```

### Step 5：JVM 单测用 Fake

```kotlin
class FakeTokenStorage : AuthTokenStorage {
    private val _session = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()
    override fun load() = _session.value
    override fun save(s: AuthSession) { _session.value = s }
    override fun clear() { _session.value = null }
}
```

### 工作量

- 改 3 个文件（新建接口 + 2 个改）
- 约 30 分钟

## 第三方案：Robolectric

`app/build.gradle.kts`：
```kotlin
testImplementation("org.robolectric:robolectric:4.12.2")
```

让 JVM 单测能加载 Android Framework。**AuthTokenStore 可直接实例化**（Robolectric 提供 mock 的 MasterKey 和 SharedPreferences）。

但 Robolectric 启动慢（5-10s per test class），对纯业务逻辑测试是过度设计。

**仅推荐**：
- 需要大量 Android Framework 相关测试（Activity 启动、View 测量等）
- 项目已有 Robolectric 经验

## 单元测试范围（不依赖 Android）

以下测试**不需要 Android Framework**，纯 JVM 可跑：

| 文件 | 内容 | 难度 |
|------|------|------|
| `AuthErrorTest.kt` | 测试 sealed class 各种分支的 message 文案 | ⭐ |
| `AuthStateTest.kt` | 测试 sealed class 各状态切换 | ⭐ |
| `EmailValidatorTest.kt` | 测试 RFC 5322 邮箱校验逻辑（如果提取为独立函数） | ⭐ |
| `PasswordStrengthTest.kt` | 测试密码强度校验（8-32 + 字母 + 数字） | ⭐ |

这些可以在 `app/src/test/`（JVM 单测）直接跑，不依赖任何 Android Framework。

## 端到端测试（11 步）

详见 V0.2.0 PRD §18.2：
- 登录页输入邮箱 + 正确密码 → 进入主屏
- 注册流程 → 注册成功即登录
- 连续输错 5 次密码 → 风控触发
- 图像验证码弹窗流程
- 退出登录 → 跳登录页
- Token 自动续期（401 → refreshToken → 重试）
- 多端同步（不同手机登录看到自己的数据）

**推荐工具**：Android Studio 的 `Compose UI Test` + `Espresso`，跑在真机/模拟器上。

## 推荐实施路径

按优先级：
1. **立即做**：MockWebServer + Instrumentation Test（7 个核心场景，半天）
2. **V0.3.0**：重构为接口 + JVM 单测（更纯的业务逻辑测试）
3. **持续**：UI Test 覆盖关键流程

## 当前状态

`auths/src/test/.../TODO.md` 占位文件存在，等待用户选择测试方案。
