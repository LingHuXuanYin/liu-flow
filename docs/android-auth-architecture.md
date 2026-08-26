# Android 端注册登录身份认证架构设计

> **模块路径**：`com.liuflow.app.auths/`
> **对应 PRD**：[V0.2.0 §11 用户系统](./prd_v0.2.0.md)
> **CloudBase 文档**：[HTTP Auth API](https://docs.cloudbase.net/http-api/auth/auth-sign-in)
> **风格基线**：MVVM + Repository + DataStore + Compose + 手动 Service Locator（对齐 `data/`、`ui/` 现有模块）

---

## 0. 架构总览

```
┌─────────────────────────────────────────────────────────────┐
│  UI 层（Compose）                                              │
│  LoginScreen / SignupScreen / CaptchaDialog                  │
│  LoginViewModel / SignupViewModel                            │
│  ↓ collectAsState()                                           │
│  暴露 StateFlow<AuthState>                                     │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│  Domain 层（auths/domain/）                                     │
│  AuthRepository —— 业务编排入口                                 │
│  Models：User / AuthSession / AuthState                       │
│          SignInResult / SignUpResult / AuthError              │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│  Data 层（auths/data/）                                          │
│  ┌──────────────────┐  ┌──────────────────────┐              │
│  │ CloudBaseAuthApi  │  │ AuthTokenStore       │              │
│  │ OkHttp + Gson     │  │ EncryptedSharedPrefs │              │
│  │ POST /auth/v1/... │  │ access/refresh/uid  │              │
│  └──────────────────┘  └──────────────────────┘              │
└──────────────────┬──────────────────────────────────────────┘
                   │ HTTPS
                   ▼
          CloudBase Auth v2
```

**关键设计决策**：
- **无 Hilt**：对齐 `AppContainer` 现有手写 Service Locator 模式，零注解处理器
- **Token 存 EncryptedSharedPreferences**：AndroidX Security Crypto，AES-256 GCM 加密
- **StateFlow + sealed class**：UI 状态用密封类表达（Idle / Loading / Success / NeedCaptcha / Error）
- **不引入第三方 SDK**：仅 OkHttp + Gson + AndroidX Security，对齐 PRD "仅 CloudBase" 原则
- **Repository 单一入口**：UI 只依赖 AuthRepository，不直接接触 CloudBaseApi / TokenStore
- **错误分类前置**：AuthError 枚举做语义化映射，UI 不需要解析错误码字符串

---

## 1. 目录结构

```
com.liuflow.app.auths/
├── data/
│   ├── CloudBaseAuthApi.kt          # HTTP 客户端（OkHttp + Gson）
│   ├── AuthTokenStore.kt            # EncryptedSharedPreferences 存 JWT
│   ├── AuthRepository.kt            # 业务编排入口（暴露给 ViewModel）
│   └── dto/
│       ├── SignInRequest.kt
│       ├── SignUpRequest.kt
│       ├── SignInResponse.kt
│       ├── CaptchaResponse.kt
│       └── ErrorResponse.kt
├── domain/
│   ├── User.kt                      # 用户数据模型
│   ├── AuthSession.kt               # access + refresh + expiresAt
│   ├── AuthState.kt                 # UI 状态机（sealed）
│   ├── SignInResult.kt              # Repository 层返回值（sealed）
│   ├── SignUpResult.kt              # Repository 层返回值（sealed）
│   └── AuthError.kt                 # 错误枚举
├── ui/
│   ├── LoginScreen.kt               # 登录页
│   ├── SignupScreen.kt              # 注册页
│   ├── CaptchaDialog.kt             # 图像验证码弹窗（Compose Dialog）
│   ├── LoginViewModel.kt            # 登录 VM
│   ├── SignupViewModel.kt           # 注册 VM
│   └── components/
│       ├── EmailField.kt            # 邮箱输入框（含 RFC 5322 校验）
│       ├── PasswordField.kt         # 密码输入框（8-32 位 + 强度提示 + 可见切换）
│       └── PrimaryButton.kt         # 主题色主按钮（loading / disabled 状态）
├── nav/
│   ├── AuthRoutes.kt                # 登录 / 注册路由常量
│   └── AuthGuard.kt                 # 路由守卫（未登录自动跳登录）
└── README.md                        # 架构说明 + 流程图（开发者 onboarding）
```

**与现有模块对齐**：
- `data/` 子包命名风格 = `db/` / `export/` / `model/` / `prefs/` / `repository/` / `stats/`
- `ui/` 子包按功能模块 = `focus/` / `history/` / `stats/`
- `domain/` 是新增（现有项目没显式 domain 层，但 Repository 充当了 domain 角色）

---

## 2. 核心数据流

### 2.1 首次启动路由守卫

```
App 启动
   │
   ▼
AuthGuard.checkOnLaunch()
   │
   ├── TokenStore 读取 accessToken + expiresAt
   │       │
   │       ├── accessToken 存在 && expiresAt > now()
   │       │      │
   │       │      └── AuthRepository.validateToken()（可选，调 `/auth/v1/me`）
   │       │              │
   │       │              ├── 有效 → NavHost 路由到 Focus (主屏)
   │       │              └── 无效 → refreshToken() → 有效 / 失败
   │       │
   │       └── 无 / 过期 → NavHost 路由到 Login
   │
   ▼
```

### 2.2 登录流程（带图像验证码风控）

```
用户输入 username + password
   │
   ▼
LoginViewModel.signIn()
   │
   ▼
AuthRepository.signIn(username, password, captchaToken=null)
   │
   ▼
CloudBaseAuthApi.signIn(...)
   │
   ├── 200 OK
   │      │
   │      ├── access_token / refresh_token / user
   │      │
   │      └── AuthTokenStore.save(session)
   │             │
   │             ▼
   │          SignInResult.Success(session)
   │             │
   │             ▼
   │          AuthState.Success(user)
   │             │
   │             ▼
   │          NavController.navigate(Focus) + 清空 backstack
   │
   ├── 401 + code = CAPTCHA_REQUIRED
   │      │
   │      └── SignInResult.NeedCaptcha(captchaId, captchaUrl)
   │             │
   │             ▼
   │          AuthState.NeedCaptcha(...)
   │             │
   │             ▼
   │          UI 弹 CaptchaDialog
   │
   ├── 401 + code = INVALID_CREDENTIALS
   │      └── SignInResult.Failure(AuthError.InvalidCredential)
   │
   ├── 429 + code = RATE_LIMIT_EXCEEDED
   │      └── SignInResult.Failure(AuthError.RateLimited)
   │
   └── 网络异常
          └── SignInResult.Failure(AuthError.Network)
```

### 2.3 注册流程

```
用户输入 email + password + 确认密码
   │
   ▼
SignupViewModel.validate()  // 客户端先校验（密码强度 + 两次一致）
   │
   ▼
AuthRepository.signUp(email, password)
   │
   ▼
CloudBaseAuthApi.signUp(...)
   │
   ├── 200 OK
   │      │
   │      ├── access_token / refresh_token / user (注册成功即登录)
   │      │
   │      └── AuthTokenStore.save(session) → SignUpResult.Success(session)
   │
   ├── 409 + code = USER_ALREADY_EXISTS
   │      └── SignUpResult.Failure(AuthError.UserExists) → UI 提示「该邮箱已注册」
   │
   └── 400 + code = PASSWORD_TOO_WEAK
          └── SignUpResult.Failure(AuthError.PasswordTooWeak)
```

### 2.4 图像验证码流程

```
CaptchaDialog.show(captchaUrl)
   │
   ▼
Coil / Glide 加载 captchaUrl 显示图片
   │
   ▼
用户输入 4 字符
   │
   ▼
CaptchaDialog.onVerify(code)
   │
   ▼
AuthRepository.verifyCaptcha(captchaId, code)
   │
   ├── 200 → captcha_token
   │      │
   │      ▼
   │   AuthRepository.signIn(username, password, captchaToken)
   │      │
   │      └── 重新走登录流程（同 §2.2）
   │
   └── 400 → AuthError.CaptchaWrong → UI 抖动 + 提示重试
```

### 2.5 Token 自动续期

```
任意 API 请求带 accessToken
   │
   ▼
CloudBase 返回 401 + code = TOKEN_EXPIRED
   │
   ▼
OkHttp Interceptor 拦截
   │
   ▼
AuthRepository.refreshToken(refreshToken)
   │
   ├── 200 → 新 access_token
   │      │
   │      ▼
   │   AuthTokenStore.save(new session)
   │      │
   │      ▼
   │   重试原请求（带新 token）
   │
   └── 401 → refreshToken 也过期
          │
          ▼
       AuthTokenStore.clear()
          │
          ▼
       AuthState.SignedOut
          │
          ▼
       NavController.navigate(Login) + 清空 backstack
```

**实现方式**：用 OkHttp `Authenticator` 而非每个请求手动处理，自动 + 透明。

### 2.6 退出登录

```
"我的"页 → 点击「退出登录」
   │
   ▼
MeViewModel.signOut()
   │
   ▼
AuthRepository.signOut()
   │
   ├── 1. CloudBaseAuthApi.signOut()  (HTTP /auth/v1/signout, 服务端失效 refreshToken)
   ├── 2. AuthTokenStore.clear()       (本地清空)
   └── 3. AuthState.SignedOut           (StateFlow 推送)
          │
          ▼
       NavController.navigate(Login) + 清空 backstack
```

---

## 3. 关键类设计

### 3.1 Domain 模型

```kotlin
// domain/User.kt
data class User(
    val uid: String,        // CloudBase JWT sub
    val email: String,
    val createdAt: Long?
)

// domain/AuthSession.kt
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,     // epoch millis
    val user: User
)

// domain/AuthError.kt
sealed class AuthError(val message: String) {
    object InvalidCredential : AuthError("邮箱或密码错误")
    object UserNotFound : AuthError("该邮箱未注册")
    object UserExists : AuthError("该邮箱已注册")
    object PasswordTooWeak : AuthError("密码需 8-32 位字母+数字")
    data class CaptchaRequired(val captchaId: String, val captchaUrl: String) :
        AuthError("需要图像验证码")
    object CaptchaWrong : AuthError("图像验证码错误")
    object RateLimited : AuthError("请求过于频繁，请稍后再试")
    object AccountLocked : AuthError("账户临时锁定，请稍后再试")
    object Network : AuthError("网络异常，请检查网络")
    object TokenExpired : AuthError("登录已过期，请重新登录")
    data class Server(val detail: String) : AuthError("服务异常：$detail")
}

// domain/AuthState.kt（UI 状态机）
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class NeedCaptcha(val captchaUrl: String, val captchaId: String) : AuthState()
    data class Error(val error: AuthError) : AuthState()
    object SignedOut : AuthState()
}

// domain/SignInResult.kt
sealed class SignInResult {
    data class Success(val session: AuthSession) : SignInResult()
    data class NeedCaptcha(val captchaId: String, val captchaUrl: String) : SignInResult()
    data class Failure(val error: AuthError) : SignInResult()
}

// domain/SignUpResult.kt
sealed class SignUpResult {
    data class Success(val session: AuthSession) : SignUpResult()
    data class Failure(val error: AuthError) : SignUpResult()
}
```

### 3.2 AuthTokenStore（加密存储）

```kotlin
// data/AuthTokenStore.kt
class AuthTokenStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(session: AuthSession) {
        prefs.edit()
            .putString(KEY_ACCESS, session.accessToken)
            .putString(KEY_REFRESH, session.refreshToken)
            .putLong(KEY_EXPIRES_AT, session.expiresAt)
            .putString(KEY_UID, session.user.uid)
            .putString(KEY_EMAIL, session.user.email)
            .apply()
    }

    fun load(): AuthSession? {
        val access = prefs.getString(KEY_ACCESS, null) ?: return null
        val refresh = prefs.getString(KEY_REFRESH, null) ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val uid = prefs.getString(KEY_UID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        return AuthSession(
            accessToken = access,
            refreshToken = refresh,
            expiresAt = expiresAt,
            user = User(uid = uid, email = email, createdAt = null)
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isValid(): Boolean {
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return expiresAt > System.currentTimeMillis() + 60_000  // 留 60s buffer
    }

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_UID = "uid"
        private const val KEY_EMAIL = "email"
    }
}
```

### 3.3 CloudBaseAuthApi（HTTP 客户端）

```kotlin
// data/CloudBaseAuthApi.kt
class CloudBaseAuthApi(
    private val envId: String,
    private val publishableKey: String,
    private val baseUrl: String = "https://$envId.api.tcloudbasegateway.com"
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * 通用 HTTP 请求（带错误码解析）
     */
    private suspend inline fun <reified T> request(
        method: String,
        path: String,
        body: Map<String, Any?>? = null,
        accessToken: String? = null
    ): Response<T> = withContext(Dispatchers.IO) {
        val url = "$baseUrl$path"
        val requestBody = body?.let {
            gson.toJson(it).toRequestBody("application/json".toMediaType())
        }

        val builder = Request.Builder().url(url)
        when (method.uppercase()) {
            "POST" -> builder.post(requestBody ?: "".toRequestBody("application/json".toMediaType()))
            "GET" -> builder.get()
        }

        builder.header("X-CloudBase-Env", envId)
        builder.header("x-client-version", "android-1.0.0")
        accessToken?.let { builder.header("Authorization", "Bearer $it") }

        try {
            val response = http.newCall(builder.build()).execute()
            val responseText = response.body?.string() ?: ""
            when {
                response.isSuccessful -> {
                    val data: T = gson.fromJson(responseText, T::class.java)
                    Response.Success(data, response.code)
                }
                else -> Response.Failure(
                    code = response.code,
                    error = parseError(responseText)
                )
            }
        } catch (e: IOException) {
            Response.Failure(code = -1, error = AuthError.Network)
        }
    }

    // 注意：/signin body 用 username 字段（注册时绑定），不是 email
    suspend fun signIn(username: String, password: String, captchaToken: String? = null): Response<SignInResponse> {
        val body = mutableMapOf<String, Any>("username" to username, "password" to password)
        captchaToken?.let { body["captcha_token"] = it }
        return request("POST", "/auth/v1/signin", body)
    }

    suspend fun signUp(email: String, password: String): Response<SignUpResponse> =
        request("POST", "/auth/v1/signup", mapOf("email" to email, "password" to password))

    suspend fun refreshToken(refreshToken: String): Response<SignInResponse> =
        request("POST", "/auth/v1/token/refresh", mapOf("refresh_token" to refreshToken))

    suspend fun signOut(refreshToken: String, accessToken: String): Response<Unit> =
        request("POST", "/auth/v1/signout", mapOf("refresh_token" to refreshToken), accessToken)

    private fun parseError(responseText: String): AuthError {
        return try {
            val err = gson.fromJson(responseText, ErrorResponse::class.java)
            when (err.code) {
                "INVALID_CREDENTIALS" -> AuthError.InvalidCredential
                "USER_NOT_FOUND" -> AuthError.UserNotFound
                "USER_ALREADY_EXISTS" -> AuthError.UserExists
                "PASSWORD_TOO_WEAK" -> AuthError.PasswordTooWeak
                "CAPTCHA_REQUIRED" -> AuthError.CaptchaRequired(err.captchaId ?: "", err.captchaUrl ?: "")
                "CAPTCHA_WRONG" -> AuthError.CaptchaWrong
                "RATE_LIMIT_EXCEEDED" -> AuthError.RateLimited
                "ACCOUNT_LOCKED" -> AuthError.AccountLocked
                else -> AuthError.Server(err.message ?: err.code ?: "未知错误")
            }
        } catch (e: Exception) {
            AuthError.Server("解析错误响应失败")
        }
    }
}

sealed class Response<out T> {
    data class Success<T>(val data: T, val code: Int) : Response<T>()
    data class Failure(val code: Int, val error: AuthError) : Response<Nothing>()
}
```

### 3.4 AuthRepository（业务编排入口）

```kotlin
// data/AuthRepository.kt
class AuthRepository(
    private val api: CloudBaseAuthApi,
    private val tokenStore: AuthTokenStore
) {
    /** 当前会话（StateFlow，UI 订阅） */
    val authState: StateFlow<AuthSession?> = tokenStore.observeSession()
        .stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = tokenStore.load()
        )

    /** 登录（username + 密码；body 字段是 username，不是 email）*/
    suspend fun signIn(username: String, password: String, captchaToken: String? = null): SignInResult {
        return when (val resp = api.signIn(username, password, captchaToken)) {
            is Response.Success -> {
                val session = resp.data.toAuthSession()
                tokenStore.save(session)
                SignInResult.Success(session)
            }
            is Response.Failure -> when (val err = resp.error) {
                is AuthError.CaptchaRequired -> SignInResult.NeedCaptcha(
                    captchaId = err.captchaId,
                    captchaUrl = err.captchaUrl
                )
                else -> SignInResult.Failure(err)
            }
        }
    }

    /** 注册（注册成功即登录） */
    suspend fun signUp(email: String, password: String): SignUpResult {
        return when (val resp = api.signUp(email, password)) {
            is Response.Success -> {
                val session = resp.data.toAuthSession()
                tokenStore.save(session)
                SignUpResult.Success(session)
            }
            is Response.Failure -> SignUpResult.Failure(resp.error)
        }
    }

    /** 验证图像验证码 */
    suspend fun verifyCaptcha(captchaId: String, code: String): String? =
        api.verifyCaptcha(captchaId, code).let { resp ->
            when (resp) {
                is Response.Success -> resp.data.captchaToken
                is Response.Failure -> null
            }
        }

    /** Token 续期（OkHttp Interceptor 自动调用） */
    suspend fun refreshToken(): AuthSession? {
        val current = tokenStore.load() ?: return null
        return when (val resp = api.refreshToken(current.refreshToken)) {
            is Response.Success -> {
                val session = resp.data.toAuthSession()
                tokenStore.save(session)
                session
            }
            is Response.Failure -> {
                tokenStore.clear()
                null
            }
        }
    }

    /** 退出登录 */
    suspend fun signOut() {
        val current = tokenStore.load() ?: return
        runCatching { api.signOut(current.refreshToken, current.accessToken) }
        tokenStore.clear()
    }
}
```

### 3.5 LoginViewModel

```kotlin
// ui/LoginViewModel.kt
class LoginViewModel(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // 注意：登录字段是 username（注册时绑定），不是 email
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun setUsername(value: String) { _username.value = value.trim() }
    fun setPassword(value: String) { _password.value = value }

    fun signIn() {
        val username = _username.value
        val password = _password.value
        if (!isFormValid(username, password)) return

        viewModelScope.launch {
            _state.value = AuthState.Loading
            when (val result = authRepo.signIn(username, password)) {
                is SignInResult.Success -> _state.value = AuthState.Success(result.session.user)
                is SignInResult.NeedCaptcha -> _state.value = AuthState.NeedCaptcha(
                    captchaUrl = result.captchaUrl,
                    captchaId = result.captchaId
                )
                is SignInResult.Failure -> _state.value = AuthState.Error(result.error)
            }
        }
    }

    fun submitCaptcha(captchaId: String, code: String) {
        viewModelScope.launch {
            val captchaToken = authRepo.verifyCaptcha(captchaId, code)
            if (captchaToken == null) {
                _state.value = AuthState.Error(AuthError.CaptchaWrong)
                return@launch
            }
            // 重新登录，附带 captchaToken
            when (val result = authRepo.signIn(_username.value, _password.value, captchaToken)) {
                is SignInResult.Success -> _state.value = AuthState.Success(result.session.user)
                is SignInResult.NeedCaptcha -> _state.value = AuthState.Error(AuthError.CaptchaWrong)
                is SignInResult.Failure -> _state.value = AuthState.Error(result.error)
            }
        }
    }

    fun dismissCaptcha() {
        _state.value = AuthState.Idle
    }

    private fun isFormValid(username: String, password: String): Boolean {
        // username: 5-24 位字母/数字/_/-
        if (!USERNAME_PATTERN.matches(username)) {
            _state.value = AuthState.Error(AuthError.Server("用户名格式不正确（5-24 位字母/数字/_/-）"))
            return false
        }
        if (password.length < 8 || password.length > 32) {
            _state.value = AuthState.Error(AuthError.PasswordTooWeak)
            return false
        }
        return true
    }

    companion object {
        private val USERNAME_PATTERN = Regex("^[A-Za-z0-9_-]{5,24}$")
    }
}
```

### 3.6 AuthGuard（路由守卫）

```kotlin
// nav/AuthGuard.kt
@Composable
fun AuthGuard(
    authState: StateFlow<AuthSession?>,
    navController: NavController,
    content: @Composable () -> Unit
) {
    val session by authState.collectAsState()

    LaunchedEffect(session) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val isAuthRoute = currentRoute in setOf(AuthRoutes.Login, AuthRoutes.Signup)

        when {
            // 未登录 + 不在登录页 → 跳登录
            session == null && !isAuthRoute -> {
                navController.navigate(AuthRoutes.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
            // 已登录 + 在登录页 → 跳主屏
            session != null && isAuthRoute -> {
                navController.navigate(Routes.Focus) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    content()
}
```

**在 `FlowNavHost` 顶层包一层**：
```kotlin
@Composable
fun FlowNavHost(
    container: AppContainer,
    navController: NavHostController
) {
    AuthGuard(
        authState = container.authRepository.authState,
        navController = navController
    ) {
        NavHost(navController, startDestination = Routes.Focus) {
            composable(AuthRoutes.Login) { LoginScreen(container, navController) }
            composable(AuthRoutes.Signup) { SignupScreen(container, navController) }
            // ... 现有 9 屏路由
        }
    }
}
```

---

## 4. OkHttp 拦截器（自动 Token 续期）

```kotlin
// data/AuthInterceptor.kt
class AuthInterceptor(
    private val tokenStore: AuthTokenStore,
    private val authRepo: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenStore.load()?.accessToken

        // 注入 accessToken
        val request = if (token != null) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else original

        val response = chain.proceed(request)

        // 401 → 自动续期
        if (response.code == 401 && token != null) {
            response.close()
            val newToken = runBlocking { authRepo.refreshToken()?.accessToken }
            if (newToken != null) {
                val retried = original.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retried)
            } else {
                // refreshToken 也过期，token 已被 clear
                // AuthGuard 监听到 authState = null，自动跳登录
            }
        }

        return response
    }
}
```

**注意**：`runBlocking` 在 OkHttp 线程上是可接受的（IO 线程），但更优是用 `Mutex` + 缓存机制避免并发续期。

---

## 5. 错误处理矩阵

| CloudBase 错误码 | AuthError | UI 提示 | 用户动作 |
|----------------|-----------|---------|---------|
| `INVALID_CREDENTIALS` | InvalidCredential | 「邮箱或密码错误，还有 N 次机会」 | 重输密码 |
| `USER_NOT_FOUND` | UserNotFound | 「该邮箱未注册，请先注册」 | 跳注册 |
| `USER_ALREADY_EXISTS` | UserExists | 「该邮箱已注册，请直接登录」 | 跳登录 |
| `PASSWORD_TOO_WEAK` | PasswordTooWeak | 「密码需 8-32 位含字母数字」 | 重设密码 |
| `CAPTCHA_REQUIRED` | CaptchaRequired | 弹 CaptchaDialog | 输入图像验证码 |
| `CAPTCHA_WRONG` | CaptchaWrong | 「验证失败，请重试」 | 重新输入 |
| `RATE_LIMIT_EXCEEDED` | RateLimited | 「请求过于频繁，请稍后再试」 | 等待 |
| `ACCOUNT_LOCKED` | AccountLocked | 「账户临时锁定，请稍后再试」 | 等待 |
| 网络异常 | Network | 「网络异常，请检查网络」 | 重试 |
| 其他 | Server | 「服务异常：xxx」 | 反馈 |

---

## 6. 安全考虑

| 维度 | 措施 |
|------|------|
| **Token 存储** | EncryptedSharedPreferences（AES-256 GCM） |
| **通信** | 全程 HTTPS / WSS（CloudBase 强制） |
| **密码** | 不在本地存储明文，不打 log（仅 TokenStore 持有加密 Token） |
| **Token 续期** | 后台自动，UI 无感知 |
| **会话失效** | 401 自动登出 + 跳登录页 |
| **错误响应** | 不在 logcat 打印原始错误响应（避免泄漏内部信息） |
| **Root 检测**（V0.3.0 评估） | SafetyNet / Play Integrity API |

---

## 7. 测试策略

### 7.1 单元测试（`app/src/test/`）

```kotlin
// AuthRepositoryTest.kt
class AuthRepositoryTest {
    private lateinit var api: FakeCloudBaseAuthApi
    private lateinit var tokenStore: FakeAuthTokenStore
    private lateinit var repo: AuthRepository

    @Test
    fun `signIn success stores session`() = runTest {
        api.mockSignInSuccess(user = User("uid-1", "test@example.com"))
        val result = repo.signIn("test_user", "password123")
        assertTrue(result is SignInResult.Success)
        assertEquals("uid-1", tokenStore.lastSavedSession?.user?.uid)
    }

    @Test
    fun `signIn with wrong password returns InvalidCredential`() = runTest {
        api.mockSignInError(code = 401, errorCode = "INVALID_CREDENTIALS")
        val result = repo.signIn("test_user", "wrong")
        assertTrue(result is SignInResult.Failure)
        assertEquals(AuthError.InvalidCredential, (result as SignInResult.Failure).error)
    }

    @Test
    fun `signIn with captcha required returns NeedCaptcha`() = runTest {
        api.mockSignInError(code = 401, errorCode = "CAPTCHA_REQUIRED", captchaId = "cap-1", captchaUrl = "...")
        val result = repo.signIn("test_user", "wrong")
        assertTrue(result is SignInResult.NeedCaptcha)
    }
}

// LoginViewModelTest.kt
class LoginViewModelTest {
    @Test
    fun `signIn with valid input emits Loading then Success`() = runTest {
        val vm = LoginViewModel(fakeAuthRepo)
        vm.setUsername("test_user")
        vm.setPassword("password123")
        vm.signIn()

        vm.state.test {
            assertEquals(AuthState.Loading, awaitItem())
            assertTrue(awaitItem() is AuthState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### 7.2 端到端测试（11 步，详见 V0.2.0 PRD §18.2）

1. 登录页输入 username + 正确密码 → 进入主屏
2. 注册流程 → 注册成功即登录
3. 注册时邮箱已存在 → USER_ALREADY_EXISTS
4. 连续输错 5 次密码 → 第 5 次返回 CAPTCHA_REQUIRED
5. 图像验证码输错 → 抖动 + 提示
6. 图像验证码输对 + 重新登录 → 登录成功
7. 完成一次专注 → sessions 表新增记录
8. 退出登录 → 跳登录页
9. 重新登录（同一 username） → 数据完整恢复
10. 用另一台手机登录（不同 username） → 看不到第一台数据
11. 直接调用 REST API 查 ?owner_id=eq.other-user → 仍然只返回自己的

---

## 8. 集成清单

按顺序执行：

- [ ] 1. `app/build.gradle.kts` 加依赖：
  - `androidx.security:security-crypto:1.1.0-alpha06`（EncryptedSharedPreferences）
  - `com.squareup.okhttp3:okhttp:4.12.0`（已有）
  - `com.google.code.gson:gson:2.10.1`（已有）
  - `io.coil-kt:coil-compose:2.5.0`（图像验证码图片加载，可选 Glide）
- [ ] 2. `data/dto/` 5 个 DTO 数据类（Kotlin data class）
- [ ] 3. `domain/` 6 个 domain 模型（User / AuthSession / AuthError / AuthState / SignInResult / SignUpResult）
- [ ] 4. `data/AuthTokenStore.kt` 实现
- [ ] 5. `data/CloudBaseAuthApi.kt` 实现（含 AuthInterceptor）
- [ ] 6. `data/AuthRepository.kt` 实现
- [ ] 7. `AppContainer.kt` 注入 auth 依赖：
  ```kotlin
  val authTokenStore = AuthTokenStore(appContext)
  val authApi = CloudBaseAuthApi(envId = BuildConfig.TCB_ENV_ID, publishableKey = BuildConfig.TCB_PUB_KEY)
  val authRepository = AuthRepository(authApi, authTokenStore)
  ```
- [ ] 8. `nav/AuthRoutes.kt` + `nav/AuthGuard.kt` 实现
- [ ] 9. `ui/FlowNavHost.kt` 包一层 AuthGuard
- [ ] 10. `ui/LoginScreen.kt` + `LoginViewModel.kt` + `components/{EmailField,PasswordField,PrimaryButton}.kt`
- [ ] 11. `ui/SignupScreen.kt` + `SignupViewModel.kt`
- [ ] 12. `ui/CaptchaDialog.kt` 实现（Compose Dialog + Coil 图片加载）
- [ ] 13. `ui/me/MeViewModel.kt` 加 signOut() + 「退出登录」按钮
- [ ] 14. `app/src/test/` 单元测试 5 个核心场景
- [ ] 15. 真机端到端测试 11 步

---

## 9. 配套资源

- **PRD**：[V0.2.0 §11 用户系统](./prd_v0.2.0.md)
- **后端流程**：[腾讯云后端服务开通流程 §6.3 LoginRepository](./腾讯云后端服务开通流程.md)
- **CloudBase HTTP Auth API**：https://docs.cloudbase.net/http-api/auth/auth-sign-in
- **CloudBase Auth v2 概述**：https://docs.cloudbase.net/en/api-reference/webv2/authentication_v2
- **现有项目风格**：
  - 依赖注入：`com.liuflow.app.AppContainer`
  - ViewModel 工厂：`com.liuflow.app.ui.FlowViewModelFactory`
  - DataStore：`com.liuflow.app.data.prefs.SettingsRepository`
  - Compose 屏：`com.liuflow.app.ui.focus.FocusScreen`

---

**文档版本**：v1.0
**创建日期**：2026-08-26
**最后更新**：2026-08-26
**作者**：Mavis（基于 V0.2.0 PRD + CloudBase Auth v2 文档）
