# Flow ProGuard rules
# Keep kotlinx-serialization metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }

# ====== GSON 反射（V0.6.0 release 崩溃修复） ======
# 错误：TypeToken must be created with a type argument; when using code shrinkers
#       make sure that generic signatures are preserved.
# 原因：R8 默认会把泛型签名 Signature 字段削掉，导致 TypeToken<Map<String, Any>> 拿不到类型参数
# 修法：保留 Signature + Annotation 属性 + TypeToken 类本身
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# GSON DTO（auths/data/dto/）— 字段名 / 类型不能被改名
-keep class com.liuflow.app.auths.data.dto.** { *; }

# ====== OkHttp / Okio / Conscrypt =====
# V0.6.1 release 登录注册卡死修复：
# 症状：release 模式点登录/注册 → UI 永远 Loading，按钮转圈不结束，
#       30s 后也不抛任何异常，Logcat 看不到 CloudBaseAuth / AndroidRuntime 日志
# 根因：OkHttp 内部用了大量反射 / 方法句柄 / ServiceLoader（TaskRunner、
#       RealCall、ConnectionPool、Dispatcher 这些 internal 类互相引用），
#       R8 混淆后方法名 / 类名被改，反射找不到原方法 → 整个调用链静默挂死
#       ——既不抛异常也不返回，30s socket 超时也走不到
# 修法：保留 okhttp3 / okio 全部类 + 成员 + 接口 + Conscrypt 替代
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep enum okhttp3.** { *; }
-keep class okhttp3.internal.** { *; }
-keep class okio.** { *; }
-keep interface okio.** { *; }
-keep class org.conscrypt.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class org.openjsse.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**

# ====== Compose Runtime + Coroutines Flow ======
# V0.6.1 release 跳转不触发修复：
# 现象：登录成功 _loggedIn.value = true 写了，VM 切 Success 打了，
#       但 AuthGuard 的 LaunchedEffect 协程没执行（[Guard] log 全部缺失），
#       navigate 不触发，UI 留在登录页
# 根因：R8 混淆了 Compose runtime / coroutines flow internal 类的协程调度逻辑，
#       collectAsState 收不到 StateFlow 新值 / LaunchedEffect 协程不被调度
# 修法：保 androidx.compose.runtime + kotlinx.coroutines.flow 关键类
-keep class androidx.compose.runtime.** { *; }
-keep interface androidx.compose.runtime.** { *; }
-keep class androidx.compose.runtime.snapshots.** { *; }
-keep class androidx.compose.runtime.saveable.** { *; }
-keep class androidx.compose.ui.platform.** { *; }
-keep class androidx.compose.ui.text.** { *; }
-dontwarn androidx.compose.**

# Kotlin Function interfaces（LaunchedEffect / collectAsState 内部用 Function1/2/3）
-keep class kotlin.jvm.functions.** { *; }
-keep interface kotlin.jvm.functions.** { *; }

# Coroutines flow（保 internal 状态机 + StateFlowImpl + FlowCollector）
-keep class kotlinx.coroutines.flow.** { *; }
-keep class kotlinx.coroutines.flow.internal.** { *; }
-keepclassmembers class kotlinx.coroutines.flow.** { *; }
-dontwarn kotlinx.coroutines.flow.**

# ====== Coil（验证码图片加载） ======
-dontwarn coil.**

# ====== Kotlinx Coroutines ======
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ====== AuthManager / FlowApp 内部反射边界 ======
# 保留 Application 子类的 onCreate 不被重命名（logcat 调试能看清）
-keep class com.liuflow.app.FlowApp { *; }

# ====== BuildConfig ======
-keep class com.liuflow.app.BuildConfig { *; }
