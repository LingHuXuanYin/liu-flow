# 流 Flow · Android 部署指南

> 把 `docs/prd.md` 和 `prototype/` 里的 HTML 原型落地成 Kotlin + Jetpack Compose + Material 3 的原生 App。本指南**不依赖**你本机已有的任何环境——会从零带你把工具链装齐。

---

## 0. 当前你机器上的状态

我在 `Q:\large_program\liu-flow` 里检查到：

- ❌ **JDK** — 未安装
- ❌ **Android Studio** — 未安装
- ❌ **Android SDK** — 未找到
- ✅ **adb** — `D:\Program Files (Portable)\android-platform-tools\adb.exe`（能连真机/模拟器）
- ❌ **Gradle** — 未安装（无所谓，Android Studio 自带，且项目用 wrapper）

> 一切都要装。下面是建议路径。

---

## 1. 安装顺序（不要跳步）

### 1.1 装 JDK 17

**推荐：** Microsoft OpenJDK 17（Windows MSI 一键装，自动配 `JAVA_HOME`）
<https://learn.microsoft.com/en-us/java/openjdk/download#openjdk-17>

或者 Temurin 17（Eclipse Adoptium）：<https://adoptium.net/temurin/releases/?version=17>

> 选 **JDK 17** 不要 21。AGP 8.5 + Kotlin 2.0 在 17 上跑得最稳。

装完后验证（PowerShell）：

```powershell
java -version
$env:JAVA_HOME
```

期望看到 `openjdk version "17.x.x"`。

### 1.2 装 Android Studio Koala (2024.1.1) 或更新

<https://developer.android.com/studio>

勾选安装：
- **Android Studio** 主程序
- **Android Virtual Device**（模拟器）
- 装到非系统盘的建议位置，例如 `D:\Android\Android Studio`

首次启动它会拉取：
- Android SDK Platform 34
- Build-Tools 34.0.0
- Platform-Tools
- Gradle 8.9（**不要**取消，它会自动下）

### 1.3 打开 SDK Manager，把下面这些勾上

```
SDK Platforms:
  ✅ Android 14 (API 34)

SDK Tools:
  ✅ Android SDK Build-Tools 34.0.0
  ✅ Android SDK Platform-Tools
  ✅ Android Emulator
  ✅ Intel x86 Emulator Accelerator (HAXM)   ← 或者你 AMD 装 WHPX
```

如果物理机是 AMD CPU，HAXM 不能用——勾上 **Windows Hypervisor Platform (WHPX)**，在「启用或关闭 Windows 功能」里打开 Hyper-V 那一坨。

### 1.4 接受 license

SDK Manager → SDK Tools 标签页 → 顶部会显示 **Accept licenses**，点接受。

---

## 2. 打开项目

1. 启动 Android Studio
2. `File` → `Open` → 选 `Q:\large_program\liu-flow\android`
3. 第一次打开时 Android Studio 会**自动**：
   - 创建 `gradle/wrapper/gradle-wrapper.jar` 和 `gradlew`/`gradlew.bat`
   - 同步 Gradle 8.9（下载依赖会花几分钟）
   - 生成 `.idea/` 配置目录

> 如果同步失败：左上角 `File` → `Sync with File System`；或 `Build` → `Clean Project`，再 `Rebuild Project`。

### 2.1 项目结构（你应该看到）

```
android/
├── app/                          # 唯一 module
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/liuflow/app/  # 全部 Kotlin 源码
│       └── res/                   # 资源
├── gradle/
│   ├── libs.versions.toml         # Version Catalog
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts               # 根级
├── settings.gradle.kts
├── gradle.properties
├── SETUP.md                       # 本文件
└── README.md
```

---

## 3. 跑起来

### 3.1 模拟器（如果你没真机）

1. `Tools` → `Device Manager` → `Create Device`
2. 选 **Pixel 8**（412 × 892 dp，正好对应 PRD §10.1 的容器尺寸）
3. System Image 选 **API 34 (UpsideDownCake)** — 下载
4. 启动模拟器
5. 工具栏 ▶ 点 Run

### 3.2 真机（推荐 — 你已有 adb）

1. 手机进入 **开发者选项 → USB 调试**
2. 用数据线连电脑
3. 第一次会弹窗「是否允许 USB 调试」，勾「总是允许」后点确认
4. 验证：

   ```powershell
   adb devices
   # 应该看到一行:  xxxxxx  device
   ```

5. Android Studio 顶栏设备列表里会出你的真机，选中 → ▶ Run

---

## 4. 第一次跑会看到什么

冷启动后直接进 **Focus** 主屏：

- 顶部状态栏 24dp（默认 9:41）
- TopAppBar Medium 风格：日期 + 标题「准备专注」+ 右上铃铛
- 280dp 圆环 + 中心 `25:00` 大数字
- 任务输入框（`现在做什么？`）
- 6 个分类 Chip：写作 / 编程 / 阅读 / 学习 / 设计 / 其他
- 时长选择：15 / 25 / 45 / 60 / 自定义
- 右下角 **Extended FAB**「▶ 开始」

点 FAB → 全屏 **Running**，圆环动起来。结束时震动（设置里能关），自动进 **Rest**。

底部三 Tab：专注 / 记录 / 我的。

---

## 5. 常见问题排查

### 5.1 `SDK location not found`

在 `Q:\large_program\liu-flow\android` 下新建 `local.properties`：

```properties
sdk.dir=C:\\Users\\<你的用户名>\\AppData\\Local\\Android\\Sdk
```

把 `<你的用户名>` 替换成你 Windows 用户名。

### 5.2 Gradle sync 失败 / 网络问题

- 关闭代理：Settings → Appearance & Behavior → System Settings → HTTP Proxy → **No proxy**
- 或者给 Gradle 配代理：`%USERPROFILE%\.gradle\gradle.properties` 加：

  ```properties
  systemProp.http.proxyHost=127.0.0.1
  systemProp.http.proxyPort=7890
  systemProp.https.proxyHost=127.0.0.1
  systemProp.https.proxyPort=7890
  ```

### 5.3 KSP 报错 `JavaVersion 17 not supported`

你的 JAVA_HOME 指向了 11 或 21。改回 17（见 1.1）。Android Studio → `File` → `Project Structure` → SDK Location → Gradle Settings → Gradle JDK 选 17。

### 5.4 模拟器黑屏 / 卡 logo

打开任务管理器 → 结束所有 `qemu-system-x86_64.exe` → 重启模拟器。
或者直接走真机，Pixel 8 / 三星 / 一加 都行。

### 5.5 编译报 `Unresolved reference: BuildConfig`

`BuildConfig` 在 `defaultConfig` 没启用时不会生成。我们 `app/build.gradle.kts` 里已经 `buildConfig = true` 打开，确认同步成功即可。

---

## 6. 验证 DataStore 备份恢复（Android 11+）

App 启动时会在 Logcat 打「FlowInit」标签，把 DataStore 和 Room 实际路径贴出来，方便对照 `backup_rules.xml` 确认覆盖。

### 6.1 查看实际路径

```powershell
# 1. 启动 App
adb logcat -s FlowInit:I

# 应看到：
# FlowInit: DataStore: /data/data/com.liuflow.app.debug/files/datastore/flow_settings.preferences_pb (exists=true)
# FlowInit: Room:     /data/data/com.liuflow.app.debug/databases/flow.db (exists=true)
# FlowInit: Backup coverage: domain=file path=datastore/ -> covers DataStore
# FlowInit: Backup coverage: domain=database path=.   -> covers Room
```

### 6.2 触发 Auto Backup（需要真机 + Google 账户开启备份）

```powershell
# 1. 在 App 里改主题 / 默认时长 / 每日目标
# 2. 触发备份
adb shell bmgr backupnow com.liuflow.app.debug

# 3. 模拟重装
adb uninstall com.liuflow.app.debug

# 4. 重新安装
adb install app\build\outputs\apk\debug\app-debug.apk

# 5. 启动 App，检查主题 / 默认时长 / 每日目标是否恢复
```

> 注意：`bmgr backupnow` 需要设备已登录 Google 账户且开启了 Auto Backup。云端还原要联网且可能延迟几分钟。

### 6.3 存储格式速查

| 类别     | 库                   | 文件格式                  | 路径                                                              |
| -------- | -------------------- | ------------------------- | ----------------------------------------------------------------- |
| 设置项   | DataStore Preferences | Protobuf KV（`.preferences_pb`） | `/data/data/<pkg>/files/datastore/flow_settings.preferences_pb`     |
| 专注记录 | Room SQLite          | SQLite 3（`.db`）         | `/data/data/<pkg>/databases/flow.db`                              |
| 通知图标 | 资源                 | 矢量（`.xml`）            | 编译进 APK，无需文件系统路径                                       |

两个目录都已被 `backup_rules.xml` 覆盖。设置项的 KV 性质决定了单条值最多几 KB，全量备份/还原几乎瞬时。

---

## 7. 打包 Debug APK（不签名）

```powershell
cd Q:\large_program\liu-flow\android
.\gradlew assembleDebug
```

产物：`app\build\outputs\apk\debug\app-debug.apk`

安装到设备：

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 8. 跑单元测试

```powershell
.\gradlew :app:testDebugUnitTest
```

测试覆盖在 `app/src/test/java/com/liuflow/app/data/stats/StatsCalculatorTest.kt`：
- overview 聚合
- 连续天数计算
- 7×24 热力图
- last7Days 缺日补零

---

## 9. 目录约定（之后怎么改）

- **加新屏** → `app/src/main/java/com/liuflow/app/ui/<name>/` 建一个 `<Name>Screen.kt` + `<Name>ViewModel.kt`，再到 `ui/nav/FlowNavHost.kt` 注册路由
- **加新主题色** → 改 `ui/theme/Color.kt` + `data/model/FlowTheme.kt`
- **加新统计维度** → 改 `data/stats/StatsCalculator.kt`（纯函数，测试友好），然后在 `StatsScreen` 里加一张卡
- **加新分类** → 改 `data/model/Category.kt`（注意 chip 横向滚动够了，不要超过 7 个）

---

## 10. 不在范围内（已经明确不做）

- 用户系统 / 登录 / 云端同步（PRD §8）
- 真正的支付流程
- iOS 平台（不在本任务范围）
- PWA / Service Worker

如果之后要加，参见 PRD 对应章节做迭代。
