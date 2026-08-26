package com.liuflow.app.auths

import com.google.gson.Gson
import com.liuflow.app.auths.data.ApiResult
import com.liuflow.app.auths.data.AuthRepository
import com.liuflow.app.auths.data.AuthTokenStore
import com.liuflow.app.auths.data.CloudBaseAuthApi
import com.liuflow.app.auths.data.dto.CaptchaResponse
import com.liuflow.app.auths.data.dto.CaptchaVerifyResponse
import com.liuflow.app.auths.data.dto.SignInRequest
import com.liuflow.app.auths.data.dto.SignInResponse
import com.liuflow.app.auths.data.dto.SignUpRequest
import com.liuflow.app.auths.data.dto.UserDto
import com.liuflow.app.auths.domain.AuthError
import com.liuflow.app.auths.domain.SignInResult
import com.liuflow.app.auths.domain.SignUpResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AuthRepository 核心单测。
 * 用 fake CloudBaseAuthApi（返回 ApiResult.Success/Failure）+ fake AuthTokenStore
 * （in-memory 内存模拟）覆盖 7 个关键场景。
 */
class AuthRepositoryTest {

    @Test
    fun `signIn with correct credentials returns Success and saves session`() = runTest {
        val api = FakeApi().apply {
            nextSignIn = ApiResult.Success(
                signInResponse("uid-1", "test@example.com"),
                200
            )
        }
        val store = FakeTokenStore()
        val repo = AuthRepository(api, store)

        val result = repo.signIn("test@example.com", "password123")

        assertTrue(result is SignInResult.Success)
        assertEquals("uid-1", (result as SignInResult.Success).session.user.uid)
        assertEquals("test@example.com", store.lastSaved?.user?.email)
    }

    @Test
    fun `signIn with wrong password returns Failure InvalidCredential`() = runTest {
        val api = FakeApi().apply {
            nextSignIn = ApiResult.Failure(401, AuthError.InvalidCredential)
        }
        val repo = AuthRepository(api, FakeTokenStore())

        val result = repo.signIn("test@example.com", "wrong")

        assertTrue(result is SignInResult.Failure)
        assertEquals(AuthError.InvalidCredential, (result as SignInResult.Failure).error)
    }

    @Test
    fun `signIn with captcha required returns NeedCaptcha`() = runTest {
        val api = FakeApi().apply {
            nextSignIn = ApiResult.Failure(
                401,
                AuthError.CaptchaRequired(captchaId = "cap-1", captchaUrl = "https://...")
            )
        }
        val repo = AuthRepository(api, FakeTokenStore())

        val result = repo.signIn("test@example.com", "wrong")

        assertTrue(result is SignInResult.NeedCaptcha)
        assertEquals("cap-1", (result as SignInResult.NeedCaptcha).captchaId)
    }

    @Test
    fun `signUp with new email returns Success and auto-logs-in`() = runTest {
        val api = FakeApi().apply {
            nextSignUp = ApiResult.Success(
                signInResponse("uid-2", "new@example.com"),
                200
            )
        }
        val store = FakeTokenStore()
        val repo = AuthRepository(api, store)

        val result = repo.signUp("new@example.com", "password123")

        assertTrue(result is SignUpResult.Success)
        assertEquals("uid-2", (result as SignUpResult.Success).session.user.uid)
        assertNotNull("注册后自动登录", store.lastSaved)
    }

    @Test
    fun `signUp with existing email returns Failure UserExists`() = runTest {
        val api = FakeApi().apply {
            nextSignUp = ApiResult.Failure(409, AuthError.UserExists)
        }
        val repo = AuthRepository(api, FakeTokenStore())

        val result = repo.signUp("existing@example.com", "password123")

        assertTrue(result is SignUpResult.Failure)
        assertEquals(AuthError.UserExists, (result as SignUpResult.Failure).error)
    }

    @Test
    fun `refreshToken success saves new session`() = runTest {
        val api = FakeApi().apply {
            nextRefresh = ApiResult.Success(
                signInResponse("uid-1", "test@example.com", expiresIn = 7200),
                200
            )
        }
        val store = FakeTokenStore().apply {
            seedSession("uid-1", "test@example.com", refreshToken = "old-refresh")
        }
        val repo = AuthRepository(api, store)

        val newSession = repo.refreshToken()

        assertNotNull(newSession)
        assertEquals("test@example.com", newSession!!.user.email)
    }

    @Test
    fun `refreshToken failure clears local session`() = runTest {
        val api = FakeApi().apply {
            nextRefresh = ApiResult.Failure(401, AuthError.TokenExpired)
        }
        val store = FakeTokenStore().apply {
            seedSession("uid-1", "test@example.com")
        }
        val repo = AuthRepository(api, store)

        val newSession = repo.refreshToken()

        assertNull(newSession)
        assertNull("续期失败清空本地", store.lastSaved)
    }

    // ============== Test Doubles ==============

    private fun signInResponse(
        uid: String,
        email: String,
        expiresIn: Long = 7200
    ): SignInResponse = SignInResponse(
        accessToken = "access-$uid",
        refreshToken = "refresh-$uid",
        expiresIn = expiresIn,
        user = UserDto(uid = uid, email = email)
    )

    private class FakeApi : CloudBaseAuthApi() {
        var nextSignIn: ApiResult<SignInResponse>? = null
        var nextSignUp: ApiResult<SignInResponse>? = null
        var nextRefresh: ApiResult<SignInResponse>? = null

        override suspend fun signIn(req: SignInRequest): ApiResult<SignInResponse> {
            return nextSignIn ?: ApiResult.Failure(500, AuthError.Server("未配置"))
        }

        override suspend fun signUp(req: SignUpRequest): ApiResult<SignInResponse> {
            return nextSignUp ?: ApiResult.Failure(500, AuthError.Server("未配置"))
        }

        override suspend fun refreshToken(refreshToken: String): ApiResult<SignInResponse> {
            return nextRefresh ?: ApiResult.Failure(500, AuthError.Server("未配置"))
        }
    }

    /**
     * 内存版 TokenStore，模拟 EncryptedSharedPreferences 行为。
     * 注：CloudBaseAuthApi 的构造函数有默认值（读 BuildConfig），
     * 所以可以无参直接继承。
     */
    private class FakeTokenStore : AuthTokenStore(MockContext()) {
        var lastSaved: com.liuflow.app.auths.domain.AuthSession? = null

        init {
            // 用反射 mock 内部 MutableStateFlow（简化测试）
        }

        override fun save(session: com.liuflow.app.auths.domain.AuthSession) {
            lastSaved = session
        }

        fun seedSession(uid: String, email: String, refreshToken: String = "old-refresh") {
            // 模拟：注入一个有效 session
            val session = com.liuflow.app.auths.domain.AuthSession(
                accessToken = "old-access",
                refreshToken = refreshToken,
                expiresAt = System.currentTimeMillis() + 60_000,
                user = com.liuflow.app.auths.domain.User(uid = uid, email = email, createdAt = null)
            )
            lastSaved = session
        }

        override fun load(): com.liuflow.app.auths.domain.AuthSession? = lastSaved

        override fun clear() {
            lastSaved = null
        }
    }
}

/** 不依赖 Android Framework 的 Context 占位（仅用于构造 AuthTokenStore） */
private class MockContext : android.content.Context() {
    override fun assets(): android.content.res.AssetManager = throw NotImplementedError()
    override fun getResources(): android.content.res.Resources = throw NotImplementedError()
    override fun getPackageName(): String = "mock"
    override fun applicationContext: android.content.Context = this
    override fun getSharedPreferences(name: String?, mode: Int): android.content.SharedPreferences =
        throw NotImplementedError()
}
