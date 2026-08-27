package com.liuflow.app.auths.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liuflow.app.BuildConfig
import com.liuflow.app.auths.data.dto.CaptchaResponse
import com.liuflow.app.auths.data.dto.CaptchaVerifyResponse
import com.liuflow.app.auths.data.dto.ErrorResponse
import com.liuflow.app.auths.data.dto.SignInRequest
import com.liuflow.app.auths.data.dto.SignInResponse
import com.liuflow.app.auths.data.dto.SignUpRequest
import com.liuflow.app.auths.data.dto.VerificationResponse
import com.liuflow.app.auths.data.dto.VerificationTokenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * 通用 CloudBase HTTP 客户端（对齐 `docs/腾讯云开发接入指引2026.md` §1.1.2 文档示范）。
 *
 * 特性：
 * - 通用 [request] 方法，支持 GET / POST / PUT / PATCH / DELETE
 * - 30s 全 timeout（connect / read / write）
 * - [updateAccessToken] 手动更新 token；OkHttp client **不挂 AuthInterceptor**（按文档示范）
 * - 错误模型 [ApiResult] 与 Debug 面板共享：Success / Failure
 *
 * 线程安全：所有 setter / getter 用 @Volatile，OkHttp 自身线程安全。
 */
class CloudBaseAuthApi(
    private val envId: String = BuildConfig.TCB_ENV_ID,
    @Suppress("unused") private val region: String = BuildConfig.TCB_REGION,
    private val baseUrl: String = "https://$envId.api.tcloudbasegateway.com",
) {

    private val gson = Gson()

    /** 当前 accessToken。由 [updateAccessToken] 写入；请求时塞到 Authorization header。 */
    @Volatile
    var accessToken: String? = null
        private set

    /** 当前 accessToken 的 StateFlow，UI 订阅显示状态条对勾。 */
    private val _accessTokenFlow = MutableStateFlow<String?>(null)
    val accessTokenFlow: StateFlow<String?> = _accessTokenFlow.asStateFlow()

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)   // 30s→10s：R8 挂死时让用户少等点
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * 更新当前 accessToken。登录 / refreshToken 成功后由 [AuthManager] 调用，
     * 避免后续请求仍用旧 token。同时同步推送给 [accessTokenFlow] 订阅者。
     */
    fun updateAccessToken(newToken: String?) {
        accessToken = newToken
        _accessTokenFlow.value = newToken
    }

    // ===== 通用 request 方法（对齐文档 §1.1.2）=====

    /**
     * 通用 HTTP 请求（严格对齐腾讯云开发接入指引 2026 §1.1.2）。
     *
     * @param method  GET / POST / PUT / PATCH / DELETE
     * @param path    API 路径，如 `/auth/v1/signin` 或 `/v1/rdb/rest/sessions`
     * @param body    请求体（任意对象，Gson 序列化；GET 时忽略）
     * @param customHeaders 自定义 headers（X-CloudBase-Env / x-client-version / Authorization 由本类自动注入）
     * @param type    响应类型（Class 形式，原始类型用）；与 [typeToken] 互斥，优先用 [typeToken]
     * @param typeToken 响应类型（TypeToken 形式，支持泛型如 `List<Map<String, Any>>`）；与 [type] 互斥
     */
    suspend fun <T> request(
        method: String,
        path: String,
        body: Any? = null,
        customHeaders: Map<String, String> = emptyMap(),
        type: Class<T>? = null,
        typeToken: TypeToken<T>? = null,
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        if (envId.isBlank()) {
            // 之前这个分支没 Log.e，release 模式下命中完全静默 —— V0.6.1 修
            Log.e(TAG, "TCB_ENV_ID is empty（BuildConfig.TCB_ENV_ID=\"\"）— .env 文件未读出 / 键名拼错 / 文件被 git 钩子改回去了")
            return@withContext ApiResult.Failure(0, "CloudBase 配置缺失（.env 文件未填写）")
        }
        Log.i(TAG, "[Api] request entry: $method $path, envId.len=${envId.length}, hasToken=${accessToken != null}")

        val url = "$baseUrl$path"
        val upper = method.uppercase()
        val isGet = upper == "GET"
        val requestBody = if (!isGet) {
            val jsonBody = (if (body != null) gson.toJson(body) else "{}")
            Log.i(TAG, "[Api] request body (${jsonBody.length} bytes): ${jsonBody.take(500)}")
            jsonBody.toRequestBody(JSON)
        } else null

        val builder = Request.Builder().url(url)
        when (upper) {
            "GET" -> builder.get()
            "POST" -> builder.post(requestBody!!)
            "PUT" -> builder.put(requestBody!!)
            "PATCH" -> builder.patch(requestBody!!)
            "DELETE" -> if (requestBody != null) builder.delete(requestBody) else builder.delete()
            else -> return@withContext ApiResult.Failure(-4, "Unsupported method: $method")
        }

        // 默认 headers（对齐文档 §1.1.2 第 82-84 行）
        builder.header("Content-Type", "application/json")
        builder.header("Accept", "application/json")
        builder.header("X-CloudBase-Env", envId)
        builder.header("x-client-version", "android-${BuildConfig.VERSION_NAME}")
        // Authorization：登录成功后由 [AuthManager] 注入；自定义 headers 可覆盖
        accessToken?.let { builder.header("Authorization", "Bearer $it") }
        // PostgREST：写入操作默认 Prefer=representation，强制返新行（否则返空 body 解析失败）
        if (upper in setOf("POST", "PATCH", "PUT")) {
            builder.header("Prefer", "return=representation")
        }
        // 自定义 headers（最后写入覆盖前面的同名 header；调用方可用来覆盖默认 header）
        customHeaders.forEach { (k, v) -> builder.header(k, v) }

        try {
            Log.i(TAG, "[Api] about to http.newCall(...).execute() for $upper $url")
            http.newCall(builder.build()).execute().use { resp ->
                val text = resp.body?.string() ?: ""
                Log.i(TAG, "[Api] $upper $url → ${resp.code}, bodyLen=${text.length}, body[:200]=${text.take(200)}")
                if (resp.isSuccessful) {
                    val wantsTyped = type != null || typeToken != null
                    if (!wantsTyped) {
                        // 调用方未指定响应类型：返 Unit（典型如 DELETE）
                        @Suppress("UNCHECKED_CAST")
                        ApiResult.Success(Unit as T, resp.code)
                    } else if (text.isEmpty()) {
                        // 响应 body 空但调用方期望类型：返合理的空容器（空 List / 空 Map）
                        val empty: Any = when {
                            typeToken != null && typeToken.rawType == List::class.java -> emptyList<Any>()
                            typeToken != null && typeToken.rawType == Map::class.java -> emptyMap<String, Any>()
                            type == List::class.java -> emptyList<Any>()
                            type == Map::class.java -> emptyMap<String, Any>()
                            else -> Unit
                        }
                        @Suppress("UNCHECKED_CAST")
                        ApiResult.Success(empty as T, resp.code)
                    } else {
                        try {
                            val data: T = when {
                                typeToken != null -> gson.fromJson(text, typeToken.type)
                                type != null -> gson.fromJson(text, type)
                                else -> error("unreachable")
                            }
                            ApiResult.Success(data, resp.code)
                        } catch (e: com.google.gson.JsonSyntaxException) {
                            Log.e(TAG, "JSON parse error on $upper $url: ${text.take(200)}", e)
                            ApiResult.Failure(resp.code, "服务器返回格式异常")
                        }
                    }
                } else {
                    // 任何端点失败都打完整响应，方便 Debug 面板定位
                    Log.e(TAG, "HTTP ${resp.code} on $upper $url\n  body: ${text.take(500)}")
                    ApiResult.Failure(resp.code, parseErrorMessage(text))
                }
            }
        } catch (e: IOException) {
            ApiResult.Failure(-1, "网络异常，请检查网络")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Bad request on $upper $url", e)
            ApiResult.Failure(-2, "请求参数非法")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error on $upper $url", e)
            ApiResult.Failure(-3, "网络请求失败：${e.javaClass.simpleName}")
        }
    }

    // ===== Auth 端点方法（封装具体 path + body）=====

    suspend fun signIn(req: SignInRequest): ApiResult<SignInResponse> =
        request("POST", "/auth/v1/signin", body = req, type = SignInResponse::class.java)

    suspend fun signOut(refreshToken: String, accessToken: String): ApiResult<Unit> =
        request(
            "POST",
            "/auth/v1/signout",
            body = mapOf("refresh_token" to refreshToken),
            customHeaders = mapOf("Authorization" to "Bearer $accessToken"),
            type = null
        )

    /**
     * 发送邮箱验证码。
     * @param target "NON_USER"（注册用，账号不存在才发） / "ANY" / "USER"
     */
    suspend fun getVerification(
        email: String,
        target: String = "NON_USER",
        captchaToken: String? = null
    ): ApiResult<VerificationResponse> {
        val body = mutableMapOf<String, Any>("email" to email, "target" to target)
        captchaToken?.let { body["captcha_token"] = it }
        return request(
            "POST",
            "/auth/v1/verification",
            body = body,
            type = VerificationResponse::class.java
        )
    }

    /** 校验邮箱验证码，拿到 verificationToken。 */
    suspend fun verifyVerification(
        verificationId: String,
        code: String
    ): ApiResult<VerificationTokenResponse> {
        val body = mapOf("verification_id" to verificationId, "verification_code" to code)
        if (BuildConfig.DEBUG_LOG_NETWORK) {
            Log.d(TAG, "verifyVerification body: $body")
        }
        return request(
            "POST",
            "/auth/v1/verification/verify",
            body = body,
            type = VerificationTokenResponse::class.java
        )
    }

    /** 用 verificationToken 注册，可同时绑定 username + password。 */
    suspend fun signUp(req: SignUpRequest): ApiResult<SignInResponse> {
        if (BuildConfig.DEBUG_LOG_NETWORK) {
            Log.d(TAG, "signUp body: email=${req.email} username=${req.username} password.length=${req.password?.length} verificationToken.length=${req.verificationToken.length}")
        }
        return request("POST", "/auth/v1/signup", body = req, type = SignInResponse::class.java)
    }

    /** 用 refreshToken 换新的 accessToken。 */
    suspend fun refreshToken(refreshToken: String): ApiResult<SignInResponse> =
        request(
            "POST",
            "/auth/v1/token/refresh",
            body = mapOf("refresh_token" to refreshToken),
            type = SignInResponse::class.java
        )

    /** 获取图像验证码。 */
    suspend fun getCaptcha(): ApiResult<CaptchaResponse> =
        request("GET", "/auth/v1/captcha", type = CaptchaResponse::class.java)

    /** 校验图像验证码，拿到 captcha_token。 */
    suspend fun verifyCaptcha(captchaId: String, code: String): ApiResult<CaptchaVerifyResponse> =
        request(
            "POST",
            "/auth/v1/captcha/verify",
            body = mapOf("captcha_id" to captchaId, "code" to code),
            type = CaptchaVerifyResponse::class.java
        )

    // ===== 错误消息提取（按文档示范，直接用响应里的 message / code 字符串）=====

    /**
     * 从错误响应 body 里提取给用户看的消息。优先用 [ErrorResponse.message]，
     * 没有就用 [ErrorResponse.code]，再没有就用响应原文。
     */
    private fun parseErrorMessage(text: String): String {
        return try {
            val err = gson.fromJson(text, ErrorResponse::class.java)
            when {
                !err.message.isNullOrBlank() -> err.message
                !err.code.isNullOrBlank() -> "错误：${err.code}"
                else -> text.takeIf { it.isNotBlank() } ?: "未知错误"
            }
        } catch (e: Exception) {
            text.takeIf { it.isNotBlank() } ?: "解析错误响应失败"
        }
    }

    companion object {
        private const val TAG = "CloudBaseAuth"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T, val code: Int) : ApiResult<T>()
    data class Failure(val code: Int, val message: String) : ApiResult<Nothing>()
}
