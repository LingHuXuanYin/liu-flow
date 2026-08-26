package com.liuflow.app.auths.data

import com.liuflow.app.auths.domain.AuthSession
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp 拦截器：自动注入 Authorization header，并在 401 时自动 refreshToken + 重试。
 *
 * 注意：runBlocking 在 OkHttp 线程上可接受（IO 线程），但生产环境建议用
 * Mutex 缓存 refreshToken 请求避免并发续期（V0.3.0 优化项）。
 */
class AuthInterceptor(
    private val accessTokenProvider: () -> String?,
    private val refresher: suspend () -> AuthSession?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = accessTokenProvider()
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else original

        val response = chain.proceed(request)

        // 401 → 自动 refreshToken + 重试一次
        if (response.code == 401 && token != null) {
            response.close()
            val newSession = runBlocking { refresher() }
            if (newSession != null) {
                val retried = original.newBuilder()
                    .header("Authorization", "Bearer ${newSession.accessToken}")
                    .build()
                return chain.proceed(retried)
            }
            // refreshToken 也过期 → tokenStore.session 变 null → AuthGuard 自动跳登录
        }

        return response
    }
}
