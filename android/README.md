# 流 Flow · Android

一款**极简的深度工作计时器 App**——只做一件事，让你拥有 25 分钟不被干扰的心流。

> 详细产品需求：`../docs/prd.md` ｜ HTML 原型：`../prototype/` ｜ 环境部署：[SETUP.md](./SETUP.md)

## 技术栈

| 维度 | 选型 | 备注 |
|------|------|------|
| 语言 | Kotlin 2.0.20 | |
| UI | Jetpack Compose (BOM 2024.09) + Material 3 | MD3 颜色/形状/排版 token 全套 |
| 架构 | MVVM + StateFlow | 手动 DI（`AppContainer`），无 Hilt |
| 数据库 | Room 2.6.1（KSP） | `SessionEntity` / `DailyGoalEntity` |
| 设置 | DataStore Preferences 1.1.1 | |
| 导航 | Navigation Compose 2.8.1 | |
| 序列化 | kotlinx-serialization | 数据导出 JSON |
| CSV | opencsv 5.9 | 数据导出 CSV |
| 最低 SDK | 26 (Android 8.0) | |
| 目标 SDK | 34 (Android 14) | |

## 目录结构

```
app/src/main/java/com/liuflow/app/
├── FlowApp.kt                  # Application：创建 AppContainer
├── MainActivity.kt             # ComponentActivity，启用 edge-to-edge
├── AppContainer.kt             # 手动 DI（DB / Repository / Settings / Timer）
├── data/
│   ├── db/                     # Room: AppDatabase, SessionEntity, DailyGoalEntity, DAOs
│   ├── prefs/                  # DataStore: SettingsRepository + UserSettings
│   ├── model/                  # Category 枚举 / FlowTheme 枚举 / DarkMode
│   ├── repository/             # FlowRepository（业务层）
│   ├── stats/                  # StatsCalculator（纯函数，所有 KPI 算这里）
│   └── export/                 # DataExporter（CSV / JSON）
├── timer/
│   └── TimerController.kt      # 倒计时状态机：IDLE→RUNNING→PAUSED→COMPLETED
├── ui/
│   ├── theme/                  # Color / Type / Shape / Theme (4 主题 × 2)
│   ├── components/             # FocusRing / StatusBar / BottomNavBar / PhoneFrame
│   ├── nav/                    # Routes + FlowNavHost
│   ├── focus/                  # 主屏
│   ├── running/                # 全屏专注
│   ├── rest/                   # 休息
│   ├── history/                # 记录
│   ├── stats/                  # 完整统计
│   ├── heatmap/                # 7×24 时段热力图
│   ├── weekly/                 # 周报
│   ├── me/                     # 我的
│   ├── settings/               # 设置
│   └── FlowViewModelFactory.kt # 单一工厂
└── util/                       # DateUtils / TimeFormat / VibrateUtil
```

## 9 个页面（按 PRD §4.1-4.2）

| Tab / 二级 | 路由 | 关键能力 |
|------------|------|---------|
| 主屏 Focus | `focus` | 任务输入 + 6 分类 + 时长选择 + FAB |
| 全屏专注 Running | `running` | 倒计时圆环 + 暂停/继续/放弃 |
| 休息 Rest | `rest` | 自动 5min，结束提示 |
| 记录 History | `history` | 今日卡 + 7 天柱状图 + 最近列表 |
| 完整统计 Stats | `stats` | 4 KPI + Streak + 周柱 + 分类条 + 目标环 + 工作日/周末 + 最佳记录 |
| 时段热力图 Heatmap | `heatmap` | 7×24 SVG 网格，alpha 渐变 |
| 周报 Weekly | `weekly` | 7 天分钟柱状图 |
| 我的 Me | `me` | 数据导出（JSON/CSV）+ 清空数据 + 关于 |
| 设置 Settings | `settings` | 4 主题 + 浅/深 + 默认时长 + 休息时长 + 每日目标 + 声音/震动 |

## 4 套主题

| ID | 名称 | Primary |
|----|------|---------|
| `classic` | 经典 | `#6750A4`（Material Default） |
| `night` | 静夜 | `#4A6FA5`（钢蓝） |
| `forest` | 森林 | `#2D6A4F`（松绿） |
| `twilight` | 薄暮 | `#7A4E7C`（暮紫） |

每套都有 light + dark 两套配色，跟随系统 / 强制浅色 / 强制深色由 `DarkMode` 控制。

## 6 个任务分类

| ID | 名称 | 强调色 |
|----|------|--------|
| `writing` | 写作 | 墨红 |
| `coding` | 编程 | 钢蓝 |
| `reading` | 阅读 | 金叶 |
| `studying` | 学习 | 松绿 |
| `design` | 设计 | 樱粉 |
| `other` | 其他 | 烟灰 |

## 核心数据模型（`SessionEntity`）

```kotlin
data class SessionEntity(
    val id: String,                 // uuid
    val task: String,
    val category: String?,          // 上述 6 个 id 之一
    val plannedDuration: Int,       // 分钟
    val actualDuration: Int,        // 分钟
    val status: String,             // "completed" | "abandoned"
    val startedAt: Long,
    val endedAt: Long,
    val hour: Int,                  // 0..23，本地时间冗余
    val weekday: Int,               // 0..6，0=周一
    val date: String,               // "YYYY-MM-DD" 本地
)
```

所有数据**只在本地 Room / DataStore 存储**，不联网。

## 跑起来

见 **[SETUP.md](./SETUP.md)**。

```powershell
# 1. 装好 JDK 17 + Android Studio Koala+
# 2. AS 打开本目录
# 3. 等 Gradle sync 完
# 4. 选设备 → ▶ Run
```

## 测试

```powershell
.\gradlew :app:testDebugUnitTest
```

`StatsCalculatorTest` 覆盖了核心统计函数（overview / streak / heatmap / last7Days）。

## 已知边界

- 后台时计时可能受 Doze 模式影响（PRD §6.3 要求 PWA 不在本项目范围）
- 7×24 热力图只显示最近 7 天
- 数据导出走 SAF（Storage Access Framework），依赖系统文件选择器
