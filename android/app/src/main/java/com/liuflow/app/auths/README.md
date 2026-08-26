# 流 Flow · Android 端身份认证模块（auths/）

> 配套文档：
> - 接入指引：[腾讯云开发接入指引 2026](../../../docs/腾讯云开发接入指引2026.md) §1.1.2 + §1.1.4
> - PRD：[V0.2.0 §11 用户系统](../../../docs/prd_v0.2.0.md)

## 原则

完全按 `docs/腾讯云开发接入指引2026.md` §1.1.2 + §1.1.4 文档示范风格：
- 一个 `CloudBaseAuthApi`（OkHttp + Gson 通用客户端，**不挂 AuthInterceptor**）
- 一个 `AuthManager`（SharedPreferences 存 token + 业务方法）
- 错误用 `kotlin.Result<T>` + `AuthException(code, message)`，**不自定义 sealed class**
- 错误消息直接取自接口响应的 `message` 字段，不做枚举映射

## 模块结构

```
com.liuflow.app.auths/
├── data/
│   ├── CloudBaseAuthApi.kt        # OkHttp + Gson HTTP 客户端（按 §1.1.2）
│   ├── AuthManager.kt             # 业务编排：SharedPreferences 存 token + 登录/注册/刷新
│   └── dto/                       # DTO（仅 Debug 面板在用）
├── domain/
│   ├── AuthState.kt                # UI 状态机（Idle / Loading / Success / Error）
│   └── SignupStep.kt               # 注册 3 步状态机
├── ui/
│   ├── LoginScreen.kt / LoginViewModel.kt
│   ├── SignupScreen.kt / SignupViewModel.kt
│   ├── VerificationScreen.kt        # 注册 Step 2：邮箱验证码
│   ├── CaptchaDialog.kt              # 图像验证码弹窗（登录风控，v0.2.1 暂未启用）
│   └── components/                   # 通用输入组件
└── nav/
    ├── AuthRoutes.kt                 # 路由常量
    └── AuthGuard.kt                  # 未登录自动跳登录页
```

## 关键设计要点

### 注册 vs 登录字段差异

| 阶段 | 输入字段 | 接口 |
|------|---------|------|
| **注册** | email + 验证码 + username + password | 3 步：发码 / 验码 / 注册 |
| **登录** | **username + password** | 1 步：`POST /signin` body 是 `{username, password}` |

> `/signin` 真实 body 是 `{username, password}`，**不是 email**。

### `/signin` 真实响应字段

```json
{
  "access_token": "...",
  "refresh_token": "...",
  "expires_in": 7200,
  "sub": "user-uid-xxxxx"
}
```

> 用户 ID 在 `sub` 字段（不是嵌套 `user.uid`）。`email` / `username` 是可选字段，可能没有。

### 完整 token 链路

```
用户输入 username + password
    ↓
LoginViewModel.signIn() → AuthManager.signIn()
    ↓
CloudBaseAuthApi.request("POST", "/auth/v1/signin", body = mapOf(...))
    ↓
    ├─ 200 → 解析 access_token / refresh_token / sub
    │       → SharedPreferences.put(access_token, refresh_token, sub)
    │       → api.updateAccessToken(access)  // 注入 OkHttp Authorization
    │       → AuthState.Success → AuthGuard 跳主屏
    │
    └─ 非 200 → 提取响应 message → AuthException(code, message) → AuthState.Error(message)
```

App 启动时 `AppContainer` 构造 `AuthManager` 调 `restoreAccessToken()`，把本地存的 token 注入，业务请求自动带 Authorization。

## 集成清单

1. **配置**（已完成）：
   - `app/src/main/.env.example`（样板，可 commit）
   - `app/src/main/.env`（实际配置，gitignore 排除）
   - `app/build.gradle.kts` 读 .env 注入 BuildConfig
2. **AppContainer 注入**：
   ```kotlin
   val authApi = CloudBaseAuthApi()
   val authManager = AuthManager(appContext, authApi).also { it.restoreAccessToken() }
   ```
3. **FlowNavHost 包 AuthGuard**：
   ```kotlin
   AuthGuard(auth = container.authManager, navController = nav) { ... }
   ```
4. **MeScreen 退出登录**：调 `container.authManager.signOut()`，AuthGuard 自动跳登录

## 调试

- `.env` 文件留空时启动会看到 `CloudBase 配置缺失（.env 文件未填写）` 错误
- `DEBUG_LOG_NETWORK=true` 会在 logcat 看到 CloudBase API 请求 / 响应
- 启动入口 → 登录页底部有「CloudBase 调试面板」按钮（仅 DEBUG build 可见），可调 12 个原始接口（PG CRUD + 认证）
- 主流程登录调通后 token 存在 `SharedPreferences/auth_tokens.xml`（明文，**生产建议改 EncryptedSharedPreferences**）
- accessToken 过期目前需要手动调 `AuthManager.refresh()`（v0.2.1 范围外，没接 401 自动续期）

## 安全考虑

- **Token 加密**：v0.2.1 用普通 SharedPreferences（明文），**生产建议改 EncryptedSharedPreferences**
- **通信**：全程 HTTPS（CloudBase gateway 强制）
- **密码**：不存本地明文，不打 log

## 历史

- V0.2.1：按 `docs/腾讯云开发接入指引2026.md` 文档示范风格重写，砍掉所有"生产加强"封装
- V0.2.0：引入 AuthRepository / AuthTokenStore / AuthInterceptor / EncryptedSharedPreferences / sealed class 错误模型（有 NPE bug）
- 旧代码备份：`docs/_deprecated_auths_v020/kotlin_backup/`
