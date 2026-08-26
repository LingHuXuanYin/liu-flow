# auths/ 模块测试 —— TODO 占位

> **当前状态**：占位文件，AuthRepositoryTest.kt 暂未提交（编译期问题：AuthTokenStore 构造依赖 Android Framework，JVM 单测实例化需 Robolectric 或重构为接口）。
>
> 详见 [`docs/android-auth-testing.md`](../../../../../../docs/android-auth-testing.md)。

## 推荐的测试策略（按优先级）

### 1. Instrumentation Test（首选，零依赖改造）
跑在真机 / 模拟器上，直接用真实 `AuthTokenStore`：

```kotlin
@RunWith(AndroidJUnit4::class)
class AuthRepositoryInstrumentedTest {
    @Test fun signIn_with_correct_credentials_returns_Success() {
        runBlocking {
            val store = AuthTokenStore(ApplicationProvider.getApplicationContext())
            // 真实环境 + 真实 SharedPreferences
        }
    }
}
```

### 2. 重构 TokenStore 为接口（更彻底的方案）
新建 `AuthTokenStorage` 接口，把 `AuthTokenStore` 改为实现，测试用 in-memory Fake。

修改 3 个文件：
- 新建 `auths/data/AuthTokenStorage.kt`（接口）
- 改 `auths/data/AuthTokenStore.kt`（实现接口）
- 改 `auths/data/AuthRepository.kt`（接受接口）

之后可写纯 JVM 单测。

### 3. Robolectric（中等方案）
在 `app/build.gradle.kts` 加：
```kotlin
testImplementation("org.robolectric:robolectric:4.12.2")
```

让 JVM 单测能跑 Android Framework，AuthTokenStore 可直接实例化。

---

**当前建议**：先用 Instrumentation Test 跑通 7 个核心场景（详见 docs/android-auth-testing.md）。
