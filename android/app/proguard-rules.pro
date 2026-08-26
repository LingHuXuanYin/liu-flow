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

# ====== OkHttp / Okio ======
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**

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
