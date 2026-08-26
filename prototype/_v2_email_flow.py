"""
把 腾讯云后端服务开通流程.md 改成「邮箱+密码+图像验证码」方案
- Step 3 整段重写（去掉短信、地域限制）
- Step 6 LoginRepository 代码重写
- Step 7 验证清单
- Step 8 月度成本
- Step 9 常见问题
"""

import re
from pathlib import Path

PATH = Path(r"Q:\large_program\liu-flow\docs\腾讯云后端服务开通流程.md")
text = PATH.read_text(encoding="utf-8")


# ============================================================
# Step 1: TL;DR 表格调整
# ============================================================
text = text.replace(
    "| **短信下发** | CloudBase 内置短信登录（不直接买腾讯云 SMS 包） | 首月 100 条免费，单日上限 30 条 |",
    "| **身份认证** | CloudBase Auth v2 内置邮箱+密码登录 | 0 短信费，0 签名审核 |\n| **图像验证码** | CloudBase 风控触发（密码错 3-5 次后必须） | 内置能力，HTTP API 拿 captcha_token |"
)


# ============================================================
# Step 2: TL;DR 总耗时
# ============================================================
text = text.replace(
    "| **总耗时** | 30 - 60 分钟 | 含实名认证等待 |",
    "| **总耗时** | 30 分钟 | 不含实名认证等待（如果已实名，5 分钟搞定） |"
)


# ============================================================
# Step 3: TL;DR 与 PRD 差异段
# ============================================================
text = text.replace(
    "**和 V0.2.0 PRD 的差异**：原 PRD §13.6 列了 6 个云函数（sendCode / verifyLogin / syncSession / syncSettings / getUserData / batchMigrate）。本流程把这 6 个全部砍掉，**全部能力由 CloudBase Auth + PostgreSQL + RLS 直接覆盖**。客户端 SDK 直接调 HTTP API 即可，不写一行后端代码。",
    "**V0.2.0 关键变更（2026-08-26 决策）**：原 PRD 设计是「手机号 + 短信验证码」，现改为「**邮箱 + 密码 + 图像验证码（风控触发）**」。原因：CloudBase Auth v2 内置邮箱密码 + 图像验证码风控，**0 短信费、0 签名审核、0 模板申请**，30 分钟开通上线。"
)


# ============================================================
# Step 4: 整体架构图调整
# ============================================================
old_arch = """                             │ ┌──────────────────┐ │
                             │ │ 短信下发（自动） │ │
                             │ │ 走腾讯云 SMS     │ │
                             │ └──────────────────┘ │"""

new_arch = """                             │ ┌──────────────────┐ │
                             │ │ 图像验证码       │ │
                             │ │ 风控触发（自动） │ │
                             │ └──────────────────┘ │"""

text = text.replace(old_arch, new_arch)


# ============================================================
# Step 5: 2.1 选地域段 - 不再受上海地域限制
# ============================================================
old_21 = """### 2.1 开通 CloudBase

地址：https://console.cloud.tencent.com/tcb

1. 点击「立即开通」
2. 选择计费模式：**个人版 ¥19.9/月**（独立开发者推荐）或免费版（学习验证）
3. 选择地域：**必须选「上海」**（短信登录仅支持上海地域）
4. 选择数据库模式：**PostgreSQL 模式**（PostgREST + RLS，2026 年新模式）
5. 等待环境创建（约 1-2 分钟）

> ⚠️ **关键决策**：地域**必须选上海**。广州 / 北京 / 新加坡都不支持短信登录。环境创建后地域**无法修改**，选错只能新建环境。"""

new_21 = """### 2.1 开通 CloudBase

地址：https://console.cloud.tencent.com/tcb

1. 点击「立即开通」
2. 选择计费模式：**个人版 ¥19.9/月**（独立开发者推荐）或免费版（学习验证）
3. 选择地域：**任意**（邮箱密码登录全地域支持，不再受上海限制）
4. 选择数据库模式：**PostgreSQL 模式**（PostgREST + RLS，2026 年新模式）
5. 等待环境创建（约 1-2 分钟）

> 💡 **变更**：相比原短信方案（必须上海地域），邮箱密码方案**无地域限制**。广州 / 北京 / 上海 / 新加坡 / 成都任选。环境创建后地域**无法修改**，但影响不大。"""

text = text.replace(old_21, new_21)


# ============================================================
# Step 6: Step 3 整段重写（启用邮箱密码 + 图像验证码）
# ============================================================
old_step3 = """## Step 3. 启用短信登录（1 分钟）

### 3.1 进入登录方式配置

地址：https://tcb.cloud.tencent.com/dev?envId=你的环境ID#/identity/login-manage

或者：控制台 → 云开发 → 你的环境 → 身份认证 → 登录方式

### 3.2 开启「短信验证码登录」

1. 在登录方式列表中，找到「**短信验证码登录**」
2. 点击「开启」
3. 弹出配置框，确认信息无误后点「确定」

> 💡 **默认配置已够用**：
> - 验证码长度：6 位
> - 有效期：5 分钟
> - 单号码发送频率：30 秒 1 条
> - 单号码日发送量：30 条/天（可在控制台改）
> - 默认短信模板：腾讯云开发自带，不可修改

### 3.3 如需自定义短信模板（可选）

如果想用自定义签名 / 模板内容：
1. 先去腾讯云短信服务控制台申请签名（1-2 工作日）
2. 申请短信模板（1-2 工作日）
3. 回到 CloudBase → 身份认证 → 登录方式 → 短信验证码 → 选择「自定义短信通道」

**V0.2.0 推荐用默认模板**，等用户量大、要做品牌感时再改自定义。"""

new_step3 = """## Step 3. 启用邮箱 + 用户名密码登录（2 分钟）

### 3.1 进入登录方式配置

地址：https://tcb.cloud.tencent.com/dev?envId=你的环境ID#/identity/login-manage

或者：控制台 → 云开发 → 你的环境 → 身份认证 → 登录方式

### 3.2 开启「邮箱登录」和「用户名密码登录」

在登录方式列表中：

1. 找到「**邮箱登录**」→ 点击「开启」
2. 找到「**用户名密码登录**」→ 点击「开启」

> 💡 **CloudBase 默认配置已够用**：
> - 密码强度：8-32 位，必须含字母和数字
> - 密码存储：自动 bcrypt 哈希
> - 登录方式：客户端调 `auth.signIn({email, password})`
> - 注册方式：客户端调 `auth.signUp({email, password})`（**注册成功即自动登录**）

### 3.3 配置图像验证码风控（默认即开）

地址：控制台 → 身份认证 → 安全配置 → 图形验证码

> 💡 **默认行为已够用**：
> - 触发条件：同一账号密码错误 3-5 次后自动触发
> - 验证码类型：4 位字符图片
> - HTTP API：`GET /auth/v1/captcha` 拿图片，`POST /auth/v1/captcha/verify` 校验
> - **不需要每次登录都输入**（仅风控触发）

### 3.4 不需要做的事

对比短信方案，**以下步骤全部跳过**：
- ❌ 开通腾讯云 SMS 服务
- ❌ 申请短信签名（1-2 工作日）
- ❌ 申请短信模板（1-2 工作日）
- ❌ 申请自定义短信通道
- ❌ 地域必须选上海的限制

**节省时间**：短信方案需 1-3 天（审核），邮箱方案 **5 分钟完成**。"""

text = text.replace(old_step3, new_step3)


# ============================================================
# Step 7: Step 6.3 整体重写（LoginRepository）
# ============================================================
old_63 = """### 6.3 实现登录

新建 `LoginRepository.kt`：

```kotlin
package com.liuflow.app.cloud

import com.google.gson.reflect.TypeToken

class LoginRepository(private val cloudbase: CloudBaseClient) {

    // ========== Step 1：发送验证码 ==========
    suspend fun sendCode(phone: String): String? {
        val formattedPhone = if (phone.startsWith(\"+86\")) phone else \"+86$phone\"
        val result = cloudbase.request<Map<String, Any>>(
            method = \"POST\",
            path = \"/auth/v1/verification\",
            body = mapOf(
                \"phone_number\" to formattedPhone,
                \"target\" to \"ANY\"  // 任何用户都可以发（首次登录即注册）
            ),
            typeToken = object : TypeToken<Map<String, Any>>() {}
        )
        return result?.get(\"verification_id\") as? String
    }

    // ========== Step 2：验证 + 登录 ==========
    suspend fun loginWithCode(phone: String, verificationId: String, code: String): Boolean {
        // 验证验证码
        val verifyResult = cloudbase.request<Map<String, Any>>(
            method = \"POST\",
            path = \"/auth/v1/verification/verify\",
            body = mapOf(
                \"verification_id\" to verificationId,
                \"verification_code\" to code
            ),
            typeToken = object : TypeToken<Map<String, Any>>() {}
        ) ?: return false

        val verificationToken = verifyResult[\"verification_token\"] as? String ?: return false

        // 用 verification_token 登录（自动注册）
        val loginResult = cloudbase.request<Map<String, Any>>(
            method = \"POST\",
            path = \"/auth/v1/signin\",
            body = mapOf(
                \"phone_number\" to if (phone.startsWith(\"+86\")) phone else \"+86$phone\",
                \"verification_token\" to verificationToken
            ),
            typeToken = object : TypeToken<Map<String, Any>>() {}
        ) ?: return false

        // 拿到 access_token，写入客户端
        val accessToken = loginResult[\"access_token\"] as? String ?: return false
        cloudbase.updateAccessToken(accessToken)
        return true
    }

    // ========== 退出登录 ==========
    suspend fun logout() {
        cloudbase.updateAccessToken(\"\")
    }
}
```"""

new_63 = """### 6.3 实现登录（邮箱 + 密码 + 图像验证码风控）

新建 `LoginRepository.kt`：

```kotlin
package com.liuflow.app.cloud

import com.google.gson.reflect.TypeToken

class LoginRepository(private val cloudbase: CloudBaseClient) {

    /**
     * 邮箱 + 密码注册（注册成功即自动登录）
     * @param email 标准邮箱格式
     * @param password 8-32 位字母+数字
     */
    suspend fun signUp(email: String, password: String): Boolean {
        val result = cloudbase.request<Map<String, Any>>(
            method = \"POST\",
            path = \"/auth/v1/signup\",
            body = mapOf(
                \"email\" to email,
                \"password\" to password
            ),
            typeToken = object : TypeToken<Map<String, Any>>() {}
        ) ?: return false

        val accessToken = result[\"access_token\"] as? String ?: return false
        cloudbase.updateAccessToken(accessToken)
        return true
    }

    /**
     * 邮箱 + 密码登录
     * @return Result.success / Result.failure(\"CAPTCHA_REQUIRED\") / Result.failure(其他错误)
     */
    suspend fun signIn(email: String, password: String, captchaToken: String? = null): SignInResult {
        val body = mutableMapOf<String, Any>(
            \"email\" to email,
            \"password\" to password
        )
        captchaToken?.let { body[\"captcha_token\"] = it }

        val response = cloudbase.requestRaw(
            method = \"POST\",
            path = \"/auth/v1/signin\",
            body = body
        )

        return when (response.code) {
            200 -> {
                val data = response.json ?: return SignInResult.Failure(\"EMPTY_RESPONSE\")
                val accessToken = data[\"access_token\"] as? String
                    ?: return SignInResult.Failure(\"NO_TOKEN\")
                cloudbase.updateAccessToken(accessToken)
                SignInResult.Success
            }
            401 -> {
                // 密码错误，可能触发 CAPTCHA_REQUIRED
                val errCode = response.json?.get(\"code\") as? String
                if (errCode == \"CAPTCHA_REQUIRED\") {
                    SignInResult.CaptchaRequired(
                        captchaUrl = response.json?.get(\"captcha_url\") as? String ?: \"\"
                    )
                } else {
                    SignInResult.Failure(\"INVALID_CREDENTIALS\")
                }
            }
            429 -> SignInResult.Failure(\"RATE_LIMIT_EXCEEDED\")
            else -> SignInResult.Failure(\"SERVER_ERROR\")
        }
    }

    /**
     * 获取图像验证码图片 URL（风控触发后调用）
     */
    suspend fun getCaptchaImage(): String? {
        val result = cloudbase.request<Map<String, Any>>(
            method = \"GET\",
            path = \"/auth/v1/captcha\",
            typeToken = object : TypeToken<Map<String, Any>>() {}
        )
        return result?.get(\"captcha_url\") as? String
    }

    /**
     * 校验图像验证码，拿到 captcha_token
     */
    suspend fun verifyCaptcha(captchaId: String, code: String): String? {
        val result = cloudbase.request<Map<String, Any>>(
            method = \"POST\",
            path = \"/auth/v1/captcha/verify\",
            body = mapOf(
                \"captcha_id\" to captchaId,
                \"code\" to code
            ),
            typeToken = object : TypeToken<Map<String, Any>>() {}
        )
        return result?.get(\"captcha_token\") as? String
    }

    // ========== 退出登录 ==========
    suspend fun logout() {
        cloudbase.request<Map<String, Any>>(
            method = \"POST\",
            path = \"/auth/v1/signout\",
            typeToken = object : TypeToken<Map<String, Any>>() {}
        )
        cloudbase.updateAccessToken(\"\")
    }
}

sealed class SignInResult {
    object Success : SignInResult()
    data class CaptchaRequired(val captchaUrl: String) : SignInResult()
    data class Failure(val code: String) : SignInResult()
}
```

**关键设计**：
- `SignInResult` 用 sealed class 表达三种结果：成功 / 需验证码 / 失败
- 客户端用 `when` 模式匹配处理不同分支
- 图像验证码流程：失败 → 弹窗 → 渲染图片 → 用户输入 → 调 verify → 拿到 token → 重新登录
- 不写云函数，全部走 CloudBase HTTP API"""

text = text.replace(old_63, new_63)


# ============================================================
# Step 8: Step 7.2 验证清单
# ============================================================
old_72 = """### 7.2 端到端验证清单

| 步骤 | 验证项 | 预期结果 |
|------|--------|---------|
| 1 | 登录页输入手机号 `13800000000`，点「获取验证码」 | 1 秒内收到短信 |
| 2 | 输入收到的 6 位验证码，点「登录」 | 自动进入主屏，accessToken 写入内存 |
| 3 | 完成一次专注 | `sessions` 表新增一条记录，`owner_id` = 自己的 uid |
| 4 | 退出登录 | accessToken 清空，回到登录页 |
| 5 | 重新登录（同一手机号） | 数据完整恢复（RLS 自动过滤） |
| 6 | 用另一台手机登录（不同手机号） | 看不到第一台手机的数据（RLS 隔离生效） |
| 7 | 直接调用 REST API 查 `?owner_id=eq.other-user` | 仍然只返回自己的（RLS USING 子句过滤） |"""

new_72 = """### 7.2 端到端验证清单

| 步骤 | 验证项 | 预期结果 |
|------|--------|---------|
| 1 | 登录页输入邮箱 `test@example.com` + 正确密码 | 1 秒内进入主屏，accessToken 写入内存 |
| 2 | 注册流程：邮箱 + 8-32 位密码 | 注册成功即自动登录 |
| 3 | 注册时邮箱已存在 | 报错 `USER_ALREADY_EXISTS`，提示「该邮箱已注册」 |
| 4 | 连续输错 5 次密码 | 第 5 次返回 `CAPTCHA_REQUIRED`，弹图像验证码弹窗 |
| 5 | 图像验证码输错 | 抖动 + 提示「验证失败」 |
| 6 | 图像验证码输对 + 重新登录 | 登录成功，进入主屏 |
| 7 | 完成一次专注 | `sessions` 表新增一条记录，`owner_id` = 自己的 uid |
| 8 | 退出登录 | accessToken 清空，回到登录页 |
| 9 | 重新登录（同一邮箱） | 数据完整恢复（RLS 自动过滤） |
| 10 | 用另一台手机登录（不同邮箱） | 看不到第一台的数据（RLS 隔离生效） |
| 11 | 直接调用 REST API 查 `?owner_id=eq.other-user` | 仍然只返回自己的（RLS USING 子句过滤） |"""

text = text.replace(old_72, new_72)


# ============================================================
# Step 9: Step 8 月度成本估算 - 删除短信费用
# ============================================================
old_81 = """### 8.1 假设场景

- 注册用户 1000 人
- 日活 100 人（10%）
- 每人每天登录 1 次（1 条短信）
- 每人每天完成 3 个 Session（每个 1 次写入 + 偶尔 1 次读）

### 8.2 月度成本

| 资源 | 用量 | 单价 | 月度费用 |
|------|------|------|---------|
| 短信验证码 | 100 × 30 = 3000 条 | 0.045 元/条 | ¥135 |
| CloudBase 个人版 | 1 套 | ¥19.9/月 | ¥19.9 |
| 合计 | - | - | **≈ ¥155/月** |

**首月优惠**：100 条免费短信（CloudBase 新环境赠送）。"""

new_81 = """### 8.1 假设场景

- 注册用户 1000 人
- 日活 100 人（10%）
- 每人每天登录 1 次（**0 短信费**）
- 每人每天完成 3 个 Session（每个 1 次写入 + 偶尔 1 次读）

### 8.2 月度成本

| 资源 | 用量 | 单价 | 月度费用 |
|------|------|------|---------|
| 短信验证码 | 0 | - | **¥0** |
| 图像验证码 | 1000 次/月 | 包含在 CloudBase 免费额度 | **¥0** |
| CloudBase 个人版 | 1 套 | ¥19.9/月 | ¥19.9 |
| 合计 | - | - | **≈ ¥20/月** |

**对比短信方案节省 ¥135/月**（短信 ¥135 + CloudBase ¥19.9 = ¥155 → 仅 CloudBase ¥19.9）。"""

text = text.replace(old_81, new_81)


# ============================================================
# Step 10: Step 9 常见问题 - Q1 短信收不到改成 Q1 密码错
# ============================================================
old_91 = """### Q1. 短信收不到？

排查顺序：
1. **手机号格式**：必须 `+86 13800000000`，加空格也可，但必须有 `+86`
2. **地域是否上海**：非上海地域**完全不支持**短信登录
3. **单日次数**：默认 30 条/天，超出后当天不再下发
4. **30 秒内重复发**：30 秒频率限制，等一下
5. **控制台测试短信**：身份认证 → 登录方式 → 短信验证码 → 「测试」按钮，看是否到"""

new_91 = """### Q1. 密码输错几次会触发图像验证码？

- CloudBase Auth v2 默认行为：同一邮箱密码错误 3-5 次后触发
- 控制台可调：身份认证 → 安全配置 → 图形验证码 → 调整触发阈值
- 风控触发后，客户端需要：
  1. 捕获 `CAPTCHA_REQUIRED` 错误码
  2. 调 `GET /auth/v1/captcha` 拿图片 URL
  3. 渲染图片
  4. 用户输入图片字符
  5. 调 `POST /auth/v1/captcha/verify` 拿 `captcha_token`
  6. 重新发起登录，附带 `captcha_token`"""

text = text.replace(old_91, new_91)


# ============================================================
# Step 11: Q4 REST API 示例 - 改登录接口
# ============================================================
old_q4 = """### Q4. 客户端能否直接调 REST API，不用 SDK？

可以。CloudBase 提供 PostgREST 接口：

```bash
# 1. 登录拿 access_token
curl -X POST \"https://<envId>.api.tcloudbasegateway.com/auth/v1/signin\" \\
  -H \"Content-Type: application/json\" \\
  -d '{\"phone_number\":\"+8613800000000\",\"verification_token\":\"...\"}'

# 2. 查数据
curl \"https://<envId>.api.tcloudbasegateway.com/v1/rdb/rest/sessions?select=*\" \\
  -H \"Authorization: Bearer <access_token>\"
```"""

new_q4 = """### Q4. 客户端能否直接调 REST API，不用 SDK？

可以。CloudBase 提供完整 HTTP API：

```bash
# 1. 邮箱注册（注册即登录）
curl -X POST \"https://<envId>.api.tcloudbasegateway.com/auth/v1/signup\" \\
  -H \"Content-Type: application/json\" \\
  -d '{\"email\":\"test@example.com\",\"password\":\"password123\"}'

# 2. 邮箱登录
curl -X POST \"https://<envId>.api.tcloudbasegateway.com/auth/v1/signin\" \\
  -H \"Content-Type: application/json\" \\
  -d '{\"email\":\"test@example.com\",\"password\":\"password123\"}'

# 3. 登录失败触发图像验证码时，附带 captcha_token 重试
curl -X POST \"https://<envId>.api.tcloudbasegateway.com/auth/v1/signin\" \\
  -H \"Content-Type: application/json\" \\
  -d '{\"email\":\"test@example.com\",\"password\":\"password123\",\"captcha_token\":\"...\"}'

# 4. 查数据（带 access_token）
curl \"https://<envId>.api.tcloudbasegateway.com/v1/rdb/rest/sessions?select=*\" \\
  -H \"Authorization: Bearer <access_token>\"
```"""

text = text.replace(old_q4, new_q4)


# ============================================================
# Step 12: Q5 回滚 + Q6 migration - 保留
# ============================================================


# ============================================================
# Step 13: Step 11 对照表 - 字段名调整
# ============================================================
old_11 = """| V0.2.0 PRD 章节 | 本流程的实现 |
|---------------|------------|
| §11 用户系统 | CloudBase Auth 内置，3 行 SDK |
| §12.1 腾讯云 SMS | CloudBase 内置短信，首月 100 条免费 |
| §12.2 短信计费 | 0.045 元/条，详见 Step 8 |
| §12.3 CloudBase 资源估算 | Step 8.2 月度成本表 |
| §12.4 腾讯云账号准备清单 | Step 1-2 |
| §13.1-13.3 同步策略 | **客户端直连数据库 + RLS**，无云函数 |
| §13.4 数据表设计 | Step 4.2 SQL 模板，**字段名 uid → owner_id** |
| §13.5 RLS 策略 | Step 5.3 完整 SQL 模板 |
| §13.6 云函数清单 | **不部署，全部删除** |
| §13.7 客户端 SDK | Step 6.2-6.4 Kotlin 代码 |
| §14 数据迁移 | Step 6.4 `batchUploadSessions` 方法 |
| §15 登录界面规范 | 见 `prototype/pages/login.html`（已实现） |"""

new_11 = """| V0.2.0 PRD 章节 | 本流程的实现 |
|---------------|------------|
| §11 用户系统 | CloudBase Auth v2 内置，邮箱+密码 |
| §12.1 选型 | CloudBase Auth + PostgreSQL + RLS |
| §12.2 CloudBase Auth v2 | Step 3 启用邮箱密码 + 图像验证码 |
| §12.3 资源消耗 | Step 8 月度成本表，**0 短信费** |
| §12.4 账号准备清单 | Step 1-2 |
| §13.1-13.3 同步策略 | 客户端直连数据库 + RLS，无云函数 |
| §13.4 数据表设计 | Step 4.2 SQL 模板，字段名 owner_id |
| §13.5 RLS 策略 | Step 5.3 完整 SQL 模板 |
| §13.6 后端函数 | 不部署，0 后端代码 |
| §13.7 客户端 SDK | Step 6.2-6.4 Kotlin 代码 |
| §14 数据迁移 | Step 6.4 `batchUploadSessions` 方法 |
| §15 登录界面规范 | 见 `prototype/pages/login.html`（已实现邮箱+密码版） |"""

text = text.replace(old_11, new_11)


# ============================================================
# Step 14: Step 12 验证报告 - 改登录方式
# ============================================================
text = text.replace(
    "- [ ] 短信登录已开启",
    "- [ ] 邮箱密码登录已开启（控制台 → 身份认证 → 登录方式）"
)
text = text.replace(
    "- [ ] 3 张数据表（sessions / daily_goals / settings）已创建",
    "- [ ] 3 张数据表（sessions / daily_goals / settings）已创建"
)
text = text.replace(
    "- [ ] 端到端测试 7 步全部通过",
    "- [ ] 端到端测试 11 步全部通过（详见 Step 7.2）"
)


# ============================================================
# Step 15: Step 13 数据来源
# ============================================================
text = text.replace(
    "- CloudBase 短信验证码登录：https://docs.cloudbase.net/authentication-v2/method/sms-login\n- CloudBase 登录认证概述：https://cloud.tencent.com/document/product/1301/67238",
    "- CloudBase 邮箱密码登录：https://docs.cloudbase.net/authentication/method/username-login\n- CloudBase 登录认证 v2 API：https://docs.cloudbase.net/en/api-reference/webv2/authentication_v2\n- CloudBase 登录认证概述：https://cloud.tencent.com/document/product/1301/67238"
)
text = text.replace(
    "- 腾讯云 SMS 定价：https://buy.cloud.tencent.com/pricing/sms",
    "- 腾讯云 SMS 定价（仅参考，V0.2.0 已不用）：https://buy.cloud.tencent.com/pricing/sms"
)


# ============================================================
# 写入
# ============================================================
PATH.write_text(text, encoding="utf-8")
print(f"[ok] {PATH}")
print(f"     {len(text):,} chars, {len(text) / 1024:.1f} KB")
