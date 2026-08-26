package com.liuflow.app.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.liuflow.app.auths.data.ApiResult
import com.liuflow.app.auths.data.CloudBaseAuthApi
import com.liuflow.app.auths.data.dto.SignInRequest
import com.liuflow.app.auths.data.dto.SignUpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * CloudBase 调试面板 ViewModel。
 * 12 个功能对应腾讯云开发接入指引 2026 §1.1.3 (PG CRUD) + §1.1.4 (Auth)。
 *
 * 每个方法把结果写入 [lastResult]（StateFlow），UI 用 LaunchedEffect 监听显示。
 */
class DebugViewModel(
    private val api: CloudBaseAuthApi,
) : ViewModel() {

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    private val _lastResult = MutableStateFlow<String?>(null)
    val lastResult: StateFlow<String?> = _lastResult.asStateFlow()

    // ===== 自动级联填入（Debug 面板用）=====

    private val _lastVerificationId = MutableStateFlow<String?>(null)
    val lastVerificationId: StateFlow<String?> = _lastVerificationId.asStateFlow()

    private val _lastEmail = MutableStateFlow<String?>(null)
    val lastEmail: StateFlow<String?> = _lastEmail.asStateFlow()

    private val _lastVerificationToken = MutableStateFlow<String?>(null)
    val lastVerificationToken: StateFlow<String?> = _lastVerificationToken.asStateFlow()

    private val pretty = GsonBuilder().setPrettyPrinting().create()
    private val compact = Gson()

    // ===== PG CRUD（§1.1.3）=====

    /** §1.1.3.1 PG 查询 —— 严格对齐文档示例：typeToken = object : TypeToken<List<Map<String, Any>>>() {} */
    fun pgQuery(table: String, limit: Int = 10) = exec("PG 查询 $table (limit=$limit)") {
        val path = "/v1/rdb/rest/$table?select=*&limit=$limit"
        val resp = api.request(
            method = "GET",
            path = path,
            typeToken = object : TypeToken<List<Map<String, Any>>>() {}
        )
        when (resp) {
            is ApiResult.Success -> "✅ 200 共 ${resp.data.size} 条\n" + pretty.toJson(resp.data)
            is ApiResult.Failure -> "❌ ${resp.code} ${resp.message}"
        }
    }

    /** §1.1.3.1b PG 按 id 查单条 —— 返 Map<String, Any>，适合简单测试 */
    fun pgQueryById(table: String, id: String) = exec("PG 按 id 查 $table#$id") {
        val path = "/v1/rdb/rest/$table?id=eq.$id&select=*"
        val resp = api.request<Map<String, Any>>(
            method = "GET",
            path = path,
            typeToken = object : TypeToken<Map<String, Any>>() {}
        )
        when (resp) {
            is ApiResult.Success -> {
                if (resp.data.isEmpty()) "✅ 200 (空：id=$id 不存在)"
                else "✅ 200\n" + pretty.toJson(resp.data)
            }
            is ApiResult.Failure -> "❌ ${resp.code} ${resp.message}"
        }
    }

    /** §1.1.3.2 PG 新增 —— 直接接 Map<String, Any?>，UI 端用 mapOf("run" to "com2") */
    fun pgInsert(table: String, body: Map<String, Any?>) = exec("PG 新增 $table") {
        val resp = api.request(
            method = "POST",
            path = "/v1/rdb/rest/$table",
            body = body,
            typeToken = object : TypeToken<List<Map<String, Any>>>() {}
        )
        when (resp) {
            is ApiResult.Success -> "✅ 200 新增成功（${resp.data.size} 条）\n" + pretty.toJson(resp.data)
            is ApiResult.Failure -> "❌ ${resp.code} ${resp.message}"
        }
    }

    /** §1.1.3.3 PG 更新（按 id） */
    fun pgUpdate(table: String, id: String, body: Map<String, Any?>) = exec("PG 更新 $table#$id") {
        val resp = api.request(
            method = "PATCH",
            path = "/v1/rdb/rest/$table?id=eq.$id",
            body = body,
            typeToken = object : TypeToken<List<Map<String, Any>>>() {}
        )
        when (resp) {
            is ApiResult.Success -> "✅ 200 更新成功（${resp.data.size} 条）\n" + pretty.toJson(resp.data)
            is ApiResult.Failure -> "❌ ${resp.code} ${resp.message}"
        }
    }

    /** §1.1.3.4 PG upsert */
    fun pgUpsert(table: String, body: Map<String, Any?>) = exec("PG upsert $table") {
        val resp = api.request(
            method = "POST",
            path = "/v1/rdb/rest/$table",
            body = body,
            typeToken = object : TypeToken<List<Map<String, Any>>>() {}
        )
        when (resp) {
            is ApiResult.Success -> "✅ 200 upsert 成功（${resp.data.size} 条）\n" + pretty.toJson(resp.data)
            is ApiResult.Failure -> "❌ ${resp.code} ${resp.message}"
        }
    }

    /** §1.1.3.5 PG 删除（按 id）—— DELETE 通常返空 body */
    fun pgDelete(table: String, id: String) = exec("PG 删除 $table#$id") {
        val resp = api.request<Map<String, Any>>(
            method = "DELETE",
            path = "/v1/rdb/rest/$table?id=eq.$id",
            typeToken = object : TypeToken<Map<String, Any>>() {}
        )
        when (resp) {
            is ApiResult.Success -> "✅ 200 删除成功"
            is ApiResult.Failure -> "❌ ${resp.code} ${resp.message}"
        }
    }

    // ===== 身份认证（§1.1.4）=====

    /** §1.1.4.1 步骤 1：发邮箱验证码 */
    fun sendCode(email: String) = exec("发邮箱验证码 $email") {
        val resp = api.getVerification(email = email, target = "ANY")
        when (resp) {
            is ApiResult.Success -> {
                // 自动级联：把 verificationId 暴露给 UI 填到 7/8 号卡片
                _lastVerificationId.value = resp.data.verificationId
                _lastEmail.value = email
                "✅ 200 verification_id=${resp.data.verificationId}"
            }
            is ApiResult.Failure -> "❌ ${resp.message}"
        }
    }

    /** §1.1.4.1 步骤 2：验邮箱验证码 */
    fun verifyCode(verificationId: String, code: String) = exec("验邮箱验证码") {
        val resp = api.verifyVerification(verificationId, code)
        when (resp) {
            is ApiResult.Success -> {
                // 自动级联：把 verificationToken 暴露给 UI 填到 8 号卡片
                _lastVerificationToken.value = resp.data.verificationToken
                "✅ 200 verificationToken=${resp.data.verificationToken.take(40)}..."
            }
            is ApiResult.Failure -> "❌ ${resp.message}"
        }
    }

    /** §1.1.4.1 步骤 3：注册 → 同样返回 accessToken + refreshToken */
    fun signUpTest(email: String, verificationToken: String, username: String, password: String) =
        exec("注册 $email / $username") {
            val resp = api.signUp(SignUpRequest(email, verificationToken, username, password))
            when (resp) {
                is ApiResult.Success -> {
                    val s = resp.data
                    // Debug 路径不走 AuthRepository，需手动把 token 写回 cloudbase 实例
                    api.updateAccessToken(s.accessToken)
                    "✅ 200 已注册\n\n" +
                        "  ✓ accessToken  (${s.accessToken.length} 字符)\n" +
                        "  ✓ refreshToken (${s.refreshToken.length} 字符)\n\n" +
                        "两个 token 都已存到 cloudbase 实例，可用于访问 App 业务接口"
                }
                is ApiResult.Failure -> "❌ ${resp.message}"
            }
        }

    /** §1.1.4.2 账号密码登录 → accessToken + refreshToken 即访问 App 的全部令牌 */
    fun signInTest(username: String, password: String) = exec("登录 $username") {
        val resp = api.signIn(SignInRequest(username, password, null))
        when (resp) {
            is ApiResult.Success -> {
                val s = resp.data
                // Debug 路径不走 AuthRepository，需手动把 token 写回 cloudbase 实例
                api.updateAccessToken(s.accessToken)
                "✅ 200 已登录\n\n" +
                    "  ✓ accessToken  (${s.accessToken.length} 字符)\n" +
                    "  ✓ refreshToken (${s.refreshToken.length} 字符)\n\n" +
                    "两个 token 都已存到 cloudbase 实例，可用于访问 App 业务接口"
            }
            is ApiResult.Failure -> "❌ ${resp.message}"
        }
    }

    /** 清空结果区 */
    fun clearResult() {
        _lastResult.value = null
    }

    // ===== 工具方法 =====

    private fun exec(label: String, block: suspend () -> String) {
        if (_running.value) return
        viewModelScope.launch {
            _running.value = true
            val start = System.currentTimeMillis()
            val result = try {
                withContext(Dispatchers.IO) { block() }
            } catch (e: Throwable) {
                "❌ 异常: ${e.javaClass.simpleName}: ${e.message}"
            }
            val elapsed = System.currentTimeMillis() - start
            _lastResult.value = "▶ $label (${elapsed}ms)\n$result"
            _running.value = false
        }
    }
}
