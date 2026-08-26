# 流 Flow — 产品需求文档 (PRD)

> 版本：v0.2 · 状态：待评审 · 类型：极简深度工作计时器（移动端）

---

## 0. 版本变更记录（V0.2.0 Changelog）

### 0.1 关键转向：强制登录

V0.1.0 把「不登录、零启动、全部 localStorage」当作品牌核心价值之一。**V0.2.0 正式放弃这一哲学**，转向「**username + 密码**强制登录 + 云端同步」（email 仅用于注册收验证码）。

> 决策依据：
> - 多端同步是 V0.2.0 起的产品承诺，离开账号体系无法成立
> - 数据所有权从 localStorage 转向云端，本地仅作离线缓存
> - **username + 密码**是 CloudBase Auth v2 真实方案（body 字段是 `username`），图像验证码由风控触发；email 仅用于注册收验证码
> - 防御性兜底：用户卸载重装 App 也能找回历史记录

**对老用户的影响**：首次打开 V0.2.0 版本时，App 检测到 localStorage 有历史数据，弹「数据迁移提示」，登录成功后自动把本地数据上传到云端。

### 0.2 新增章节

- 第 11 章：用户系统（**username + 密码**登录 + 图像验证码风控；email 仅注册时收验证码用）
- 第 12 章：腾讯云认证与后端架构选型
- 第 13 章：云端数据同步策略
- 第 14 章：V0.1.0 → V0.2.0 数据迁移
- 第 15 章：登录界面 UI 规范
- 第 16 章：非功能性需求（V0.2.0 增量）

### 0.3 沿用 V0.1.0 主体

- 第 1-10 章：产品定位、核心功能、9 屏架构、视觉系统、统计系统
- 4 套主题色（经典 / 静夜 / 森林 / 薄暮）
- 设置中心、统计详情、热力图、周报
- Android 客户端工程（V0.3.0 已完成的 Kotlin 实现）

---

## 1. 产品概述

### 1.1 一句话定位

**流 Flow** 是一款极简的深度工作计时器 App，让你在一个被切碎的时代，依然能拥有 25 分钟不被干扰的心流。

### 1.2 产品愿景

市面上的番茄钟 App 越来越重——任务管理、习惯打卡、好友 PK、商城……「流」反其道而行：只做一件事——计时。砍掉所有噪音，只留一个圆环、一段时间、一个状态。

### 1.3 目标用户

| 用户画像 | 核心痛点 | 使用场景 |
|---------|---------|---------|
| **独立创作者** | 容易分心，需要仪式感进入心流 | 写作 / 设计 / 编程时 |
| **研究生 / 学生** | 写论文 / 备考，需要长时间专注 | 图书馆、自习室 |
| **远程工作者** | 居家办公缺乏节奏感 | 上午 / 下午的工作时段 |
| **冥想 / 读书爱好者** | 想用番茄钟辅助阅读 | 睡前阅读、晨间冥想 |

### 1.4 核心价值主张

1. **极致克制**：只有 1 个主屏 + 3 个二级屏，绝不堆功能
2. **视觉沉浸**：液态圆环动画 + 动态背景，专注感拉满
3. **多端同步（V0.2.0 新增）**：手机 / 平板 / 未来桌面端，专注记录无缝衔接
4. **强制登录换取云端**：用 **username + 密码**的标准账号体系（email 仅作注册时收验证码用），换取换手机不丢数据的安心

---

## 2. 核心功能与优先级

### P0 — MVP 必做

| 功能 | 说明 | 技术实现 |
|------|------|---------|
| **倒计时器** | 25 分钟（可调）专注 + 5 分钟休息 | setInterval + 状态机 |
| **圆形进度** | 圆环从 0 动画到 100% | SVG `<circle>` + stroke-dashoffset |
| **任务关联** | 输入当前任务名，结束后归档 | localStorage 存 + 云端同步 |
| **完成任务历史** | 列表展示最近 30 次专注 | localStorage 存 + 云端同步 |
| **基础统计** | 今日专注次数、累计时长 | localStorage 聚合 + 云端聚合 |
| **声音 / 震动** | 结束提示音 + 震动 | Web Audio API + navigator.vibrate |
| **用户登录（V0.2.0 新增）** | **username + 密码**（图像验证码风控） | CloudBase Auth 内置 |
| **云端数据同步（V0.2.0 新增）** | Session / DailyGoal / Settings 多端同步 | CloudBase PostgreSQL + RLS |

### P0（统计专项）— 必做

| 功能 | 说明 | 技术实现 |
|------|------|---------|
| **任务分类标签** | 每次专注可打分类标签（写作/编程/学习/阅读/设计/其他） | localStorage + 云端 |
| **按分类统计** | 7 天 / 30 天各分类的次数与时长 | localStorage 聚合 + SVG 环形图 |
| **连续天数 Streak** | 连续 N 天完成 ≥1 次专注 | localStorage 比对日期 |
| **最佳纪录** | 单日最多次数、单次最长时长、累计专注天数 | localStorage 计算 |
| **完成率** | 开始 → 自然完成的百分比 | localStorage 区分放弃 vs 完成 |
| **周报柱状图** | 7 天每日专注次数与时长 | SVG 自绘 |

### P1 — 应做

| 功能 | 说明 | 技术实现 |
|------|------|---------|
| **每日目标** | 设置今日专注次数目标，显示完成进度环 | localStorage + SVG 进度环 |
| **24h 时段热力图** | 横轴 0-23h、纵轴 周一-周日，颜色 = 专注次数 | SVG 自绘 7×24 网格 |
| **工作日 vs 周末对比** | 两段时间对比 | localStorage 聚合 |
| **主题色切换** | 黑、白、蓝、绿 4 套配色 | CSS 变量 |
| **自定义时长** | 5-90 分钟可选 | 下拉选择 |
| **深色模式** | 跟随系统 / 手动切换 | prefers-color-scheme |
| **数据导出** | 导出 CSV / JSON | Blob + a.download |
| **云端数据迁移（V0.2.0 新增）** | V0.1.0 localStorage → V0.2.0 CloudBase | 一次性批量上传 |
| **退出登录（V0.2.0 新增）** | Token 失效 + 回到登录页 | JWT 主动失效 |

### P2 — 可做（远期）

| 功能 | 说明 |
|------|------|
| 白噪音（雨声、海浪、咖啡馆） |
| Apple Watch / 桌面小组件 |
| 第三方登录（微信、Apple ID）— V0.2.0 暂不做 |
| 找回密码 — V0.2.0 邮箱链接重置 |
| 注销账户 — V0.3.0 规划 |

---

## 3. 用户交互路径 (User Flows)

### 3.1 首次使用（V0.2.0 关键路径变更）

```
打开 App
  ↓
启动屏：圆环呼吸动画 1.5s（V0.2.0 新增动效）
  ↓
检测登录态
  ├─ 已登录 → 直接进入主屏
  └─ 未登录 → 进入登录页
        ↓
  ┌─ 场景 A：全新用户（localStorage 无数据）
  │    → 直接显示 **username + 密码**输入页
  │
  └─ 场景 B：V0.1.0 升级用户（localStorage 有数据）
       → 显示「数据迁移提示」
       → 用户同意后进入登录页
       → 登录成功自动迁移本地数据到云端
  ↓
登录页：输入 **username + 密码** → 风控触发时显示图像验证码 → 登录
  ↓
验证通过：Token 写入安全存储 → 触发数据拉取
  ↓
进入主屏（focus.html）
  - 任务输入框：「现在做什么？」
  - 默认 25 分钟
  - 「点击圆环开始」
```

### 3.2 专注流程（V0.2.0 保持不变）

```
主屏：输入任务 → 设置时长 → 点击圆环开始
  ↓
进入专注模式（全屏）
  - 圆环动画 + 中心倒计时
  - 顶部：「流」字 + 任务名
  - 底部：「暂停」「放弃」两个按钮
  ↓
A. 自然结束（25:00 → 00:00）
   - 震动 + 提示音
   - 自动切到「休息」状态
   - 显示「完成！」toast
   ↓
   【V0.2.0 新增】同步 Session 到云端
B. 用户暂停
   - 圆环暂停动画
   - 显示「继续 / 放弃 / 重新开始」
C. 用户放弃
   - 弹出确认：「放弃这次专注？」
   - 不计入历史
   ↓
   【V0.2.0 新增】同步 Session（status=abandoned）到云端
```

### 3.3 休息流程（V0.2.0 保持不变）

```
专注结束 → 自动进入 5 分钟休息
  - 圆环换色（灰）
  - 提示「站起来走走吧」
  - 休息结束 → 震动 → 回到主屏
  - 可选「再来一次」/「结束」
```

### 3.4 历史与统计（V0.2.0 保持不变）

```
主屏：点击「今日 X 次」入口
  ↓
进入「历史」页：
  - 今日完成次数
  - 累计时长
  - 最近专注列表（任务名 + 时长 + 时间）
  ↓
切换到「周报」：
  - 7 天专注次数柱状图
  - 每天最长一次专注时长
  - 总时长
```

### 3.5 退出登录（V0.2.0 新增）

```
「我的」页 → 点击「退出登录」
  ↓
弹出确认：「退出后数据仍保留在云端，下次登录可恢复」
  ↓
确认 → 清除本地 Token → 回到登录页
```

---

## 4. 页面清单与跳转关系

### 4.1 一级页面（3 个 Tab，简化）

| Tab | 名称 | 页面文件 | 主要功能 |
|-----|------|---------|---------|
| 1 | **专注** | `focus.html` | 主屏：任务输入 + 圆环 + 状态切换 |
| 2 | **记录** | `history.html` | 今日 / 7 天统计 + 历史列表 |
| 3 | **我的** | `me.html` | 设置 + 主题切换 + 数据导出 + 退出登录 |

### 4.2 二级页面

| 页面 | 文件 | 来源 | 备注 |
|------|------|------|------|
| **登录（V0.2.0 新增）** | `login.html` | 启动 / 退出登录 | **username + 密码** |
| **服务协议（V0.2.0 新增）** | `agreement.html` | 登录页内嵌 | 模态弹窗 |
| 全屏专注模式 | `running.html` | 主屏「开始」 | 全屏沉浸 |
| 休息模式 | `rest.html` | 专注结束自动跳 | 全屏 + 提示 |
| 统计详情 | `stats.html` | 记录页右上角入口 | 完整统计面板 |
| 时段热力图 | `heatmap.html` | 统计详情内 | 7×24 网格 |
| 周报详情 | `weekly.html` | 记录页入口 | 7 天图表 |
| 设置 | `settings.html` | 我的页 | 列表式 |

### 4.3 页面跳转关系图

```
                    ┌──────────────┐
                    │  启动屏 1.5s  │  (V0.2.0 动效升级)
                    └──────┬───────┘
                           ↓
                    ┌──────────────┐
              ┌────│  登录态检测   │────┐
              │    └──────────────┘    │
          已登录 ↓                  未登录 ↓
        ┌──────────┐              ┌──────────┐
        │  focus   │              │  login   │  (V0.2.0 新增)
        │  圆环+任务│              │ username+密码│
        └────┬─────┘              └────┬─────┘
             ↓                          ↓
        ┌─────────┐                 验证通过
        │ running │                     ↓
        └────┬────┘              ┌──────────┐
       自然结束↓                  │  focus   │
        ┌─────────┐               └──────────┘
        │  rest   │
        └────┬────┘
             ↓
        ┌──────────────┐
        │   focus      │
        └──────────────┘

  ┌────────────┐         ┌────────────┐
  │  history   │ ←─────→ │    me      │
  └─┬────────┬─┘         └────┬───────┘
    ↓        ↓               ↓
┌────────┐ ┌────────┐    ┌──────────┐
│ stats  │ │ weekly │    │ settings │
└───┬────┘ └────────┘    └──────────┘
    ↓
┌────────┐
│heatmap │
└────────┘
```

---

## 5. 信息架构与组件库

### 5.1 视觉系统

**主色板（4 套主题）：**

| 主题 | 主色 | 背景 | 强调 | 适用 |
|------|------|------|------|------|
| 经典 | 墨黑 `#1A1A1A` | 米白 `#FAFAF7` | 墨红 `#C73E1D` | 写作、阅读 |
| 静夜 | 钢蓝 `#2C3E50` | 深灰 `#0F1419` | 钛白 `#E8E8E8` | 编程、夜间 |
| 森林 | 松绿 `#2D4A3E` | 米杏 `#F5F1E8` | 金叶 `#D4A574` | 学习、思考 |
| 薄暮 | 暮紫 `#4A3B52` | 烟灰 `#F0EBF0` | 樱粉 `#D89BAE` | 设计、艺术 |

**字体：**
- 主字体：`Inter` + `Noto Sans SC`
- 数字字体：`Inter`（等宽特性）+ `SF Mono`（时间显示）

**圆角：**
- 圆环：无圆角（环形）
- 卡片：`rounded-2xl` (16px)
- 按钮：`rounded-full` (圆形 CTA)

### 5.2 核心组件

**圆环进度条（最重要）：**
- SVG 直径：280-320px
- 圆环粗细：6-8px
- 进度色 = 主题主色
- 背景色 = 浅灰
- 中心显示：`MM:SS` 大数字 + 任务名
- 动画：`stroke-dashoffset` 缓动

**状态指示器：**
- 准备中：浅灰圆环 + 静态
- 专注中：主题色圆环 + 顺时针动画 + 呼吸背景
- 休息中：灰色圆环 + 慢速动画
- 完成：闪动一次 + 静态

**按钮：**
- 主按钮：圆形 64x64，黑色背景，白色播放图标
- 次按钮：文字按钮（暂停 / 放弃）

### 5.3 关键文案

- 拒绝「成功学」「打鸡血」话术
- 倾向冷静、克制、留白
- 示例：「专注 25 分钟」「开始」「结束」「休息 5 分钟」

---

## 5A. 统计维度详细设计

> 这是 V0.1.0 重点，本版本保持不变。统计功能是「流」让用户「看见自己」的核心，必须做到克制 + 直观。

### 5A.1 数据模型

每次完成的专注记录（`Session`），存到 localStorage + 云端：

```js
{
  id: "uuid",                     // 唯一 id
  task: "写产品需求文档",           // 任务名（用户输入）
  category: "writing",            // 分类：writing / coding / reading / studying / design / other
  plannedDuration: 25,            // 计划时长（分钟）
  actualDuration: 25,             // 实际时长（分钟，可能短于计划 = 放弃）
  status: "completed",            // 状态：completed / abandoned
  startedAt: 1724352000000,       // 开始时间戳
  endedAt: 1724353500000,         // 结束时间戳
  hour: 14,                       // 冗余字段：开始的小时（0-23），便于热力图
  weekday: 3,                     // 冗余字段：星期几（0-6，0=周一）
  date: "2026-08-23"              // 冗余字段：YYYY-MM-DD
}
```

每日目标（`DailyGoal`）：
```js
{ date: "2026-08-23", target: 4 }  // 今日目标 4 次
```

### 5A.2 统计维度清单

| 维度 | 字段来源 | 展示形式 | 难度 |
|------|---------|---------|------|
| 今日专注次数 | `Session[date=今天].length` | 数字 | ⭐ |
| 今日累计时长 | `Σ Session.actualDuration` | 数字 + 单位 | ⭐ |
| 连续天数 Streak | 比对每日是否 ≥1 次 | 火焰图标 + 数字 | ⭐⭐ |
| 最佳单日次数 | `max(Session[date].length)` | 数字 | ⭐ |
| 最长单次专注 | `max(Session.actualDuration)` | 数字 + 单位 | ⭐ |
| 累计专注总时长 | `Σ Session.actualDuration` | 数字 | ⭐ |
| 累计专注天数 | `distinct(Session.date).length` | 数字 | ⭐ |
| 完成率 | `completed / (completed + abandoned)` | 百分比 | ⭐ |
| 按分类统计（7天） | 按 `category` 分组聚合 | 横向条形图 + 百分比 | ⭐⭐ |
| 按分类统计（30天） | 同上 | 环形图 | ⭐⭐ |
| 周报柱状图（7天） | 按 `date` 分组 | 柱状图（次数 / 时长） | ⭐⭐ |
| 每日目标完成度 | 今日次数 / 目标 | 圆环进度 | ⭐ |
| 24h 时段热力图 | 7 天 × 24 小时 = 168 格 | 颜色深浅 | ⭐⭐⭐ |
| 工作日 vs 周末 | 按 `weekday ∈ [0,4]` vs `[5,6]` 分组 | 对比条 | ⭐⭐ |

### 5A.3 统计页（stats.html）布局

```
┌─────────────────────────────┐
│  ← 统计              分享   │
├─────────────────────────────┤
│  概览卡片（4 个）           │
│  ┌─────┐┌─────┐┌─────┐┌─────┐│
│  │ 12  ││ 5h  ││ 28  ││ 92% ││
│  │次数 ││时长 ││天数 ││完成 ││
│  └─────┘└─────┘└─────┘└─────┘│
├─────────────────────────────┤
│  🔥 连续 12 天  · 最佳 28 天│
│  ────────────────────────   │
├─────────────────────────────┤
│  本周专注（柱状图）         │
│  ▁▃▅▂▇▆▄                    │
│  一二三四五六日              │
├─────────────────────────────┤
│  任务分类（7 天）           │
│  ████████ 写作   45%        │
│  █████   编程   30%          │
│  ████    阅读   15%          │
│  ██      设计   10%          │
├─────────────────────────────┤
│  每日目标       ┌──────┐     │
│  3 / 5 完成    │ ◐ 60%│     │
│                └──────┘     │
├─────────────────────────────┤
│  时段热力图（7×24）         │
│  [点击查看完整] →            │
├─────────────────────────────┤
│  工作日 vs 周末             │
│  工作日 4.2h / 周末 2.8h    │
└─────────────────────────────┘
```

### 5A.4 任务分类标签规范

为了让统计有意义，每次开始专注时让用户**快速选一个分类**（不是必填，可跳过）：

| 分类 ID | 名称 | 图标 | 主色 |
|--------|------|------|------|
| `writing` | 写作 | ✍️ 笔 | 墨红 |
| `coding` | 编程 | ⌨️ 终端 | 钢蓝 |
| `reading` | 阅读 | 📖 书 | 金叶 |
| `studying` | 学习 | 🎓 学士帽 | 松绿 |
| `design` | 设计 | 🎨 调色板 | 樱粉 |
| `other` | 其他 | ⭕ 圆点 | 烟灰 |

**交互**：在主屏任务输入框下方，6 个分类小圆点横排，单选，点击高亮。选错可改。

### 5A.5 24h 时段热力图（heatmap.html）

7×24 网格 SVG：
- 横轴：0-23 小时（24 列）
- 纵轴：周一-周日（7 行）
- 每个格子大小：~12×12px
- 颜色：背景灰色，专注次数越多颜色越深
- 配色：单一主题色 + 不同 alpha（0.1, 0.3, 0.5, 0.7, 0.9）
- 交互：长按显示 tooltip（日期 + 小时 + 次数）

实现：用 SVG `<rect>` × 168 个 + `for` 循环 + JS 算 fill-opacity。

### 5A.6 视觉一致性

- 统计页整体用同主题色板，不引入新的颜色
- 数字用等宽字体（`SF Mono`），对齐整齐
- 图表用极简风格：无网格线、无图例边框、无 3D 效果
- 留白大于信息密度

---

## 6. 非功能性需求（V0.1.0 主体）

### 6.1 性能
- 首屏加载 < 1 秒
- 圆环动画稳定 60fps
- 切换页面 < 300ms

### 6.2 隐私
- 本地数据存 localStorage（V0.2.0 起作为离线缓存）
- V0.2.0 起数据所有权在云端
- 提供「清空所有数据」按钮（仅清本地，不清云端）

### 6.3 可用性
- 单手操作
- 全屏专注模式防误触（屏蔽返回手势）
- 后台运行时继续计时（PWA）

### 6.4 离线
- 完整 PWA，离线可用
- V0.2.0 起：登录态有效期内离线写入的 Session 在联网后自动补传

---

## 7. 原型验收标准

### 7.1 必含页面
- [ ] focus（主屏，带分类标签）
- [ ] running（全屏专注）
- [ ] rest（休息）
- [ ] history（记录，含 7 天柱状图）
- [ ] stats（统计详情）
- [ ] heatmap（24h 时段热力图）
- [ ] weekly（周报）
- [ ] me（我的）
- [ ] settings（设置）
- [ ] **login（登录页，V0.2.0 新增）**
- [ ] **agreement（服务协议模态，V0.2.0 新增）**

### 7.2 视觉品质
- 真实 Pixel 8/9 Android 外框（含前置摄像头打孔）
- Material You / Material Design 3 视觉规范
- 圆环动画流畅
- 4 套主题色可切换
- 所有文案为简体中文
- 视觉对标：Google Material 3、Tide、Forest、滴答清单

---

## 10. Material You / Material Design 3 视觉适配

### 10.1 容器：Pixel 8/9 外框

- 屏幕尺寸：412 × 892 dp（现代安卓机比例）
- 圆角：44px 外框
- 边框：黑色 4px + 中框 8px
- 顶部：前置摄像头居中打孔
- 底部：3-button 导航条（主页 / 主页 / 最近）模拟

### 10.2 Material 3 核心色板

**主色板（4 套主题对应 MD3 token）：**

| 主题 | Primary | On Primary | Primary Container | Surface | On Surface | 适用 |
|------|---------|-----------|-------------------|---------|-----------|------|
| 经典 | `#6750A4` | 白 | `#EADDFF` | `#FEF7FF` | `#1C1B1F` | 默认 |
| 静夜 | `#4A6FA5` | 白 | `#D3E3FD` | `#0F1419` | `#E3E3E3` | 编程、夜间 |
| 森林 | `#2D6A4F` | 白 | `#A8D5BA` | `#F5F1E8` | `#1B2D1F` | 学习、思考 |
| 薄暮 | `#7A4E7C` | 白 | `#E8C5E5` | `#F0EBF0` | `#2A1A2A` | 设计、艺术 |

### 10.3 MD3 组件对照

| 原 iOS 组件 | MD3 对应组件 | 关键变化 |
|------------|-------------|---------|
| 底部 TabBar | **NavigationBar** | 激活态用「Pill」形状高亮（圆角矩形）|
| 中央 + 按钮 | **Extended FAB** | 圆角 16px 矩形，含图标 + 文字 |
| AppBar | **TopAppBar (Medium)** | 高度 112dp，左标题右操作图标 |
| 卡片 | **Filled Card** | 圆角 12px，无明显边框 |
| 按钮（次） | **Text Button / Tonal Button** | 圆角 20px |
| 圆环进度 | 自绘 SVG | 同 iOS 版，填充色用 Primary |
| 状态栏 | 24dp 高 | 时间居左、图标居右 |

### 10.4 MD3 排版规范

| Token | 字号 | 字重 | 用途 |
|-------|------|------|------|
| Display Large | 57px | Regular | 数字大屏（计时器）|
| Headline Medium | 28px | Regular | 页面大标题 |
| Title Large | 22px | Medium | AppBar 标题 |
| Body Large | 16px | Regular | 正文 |
| Body Medium | 14px | Regular | 次要正文 |
| Label Large | 14px | Medium | 按钮文字 |

**字体栈**：`'Roboto', 'Noto Sans SC', system-ui, sans-serif`
**数字字体**：`'Roboto Mono', 'SF Mono'`（计时器等宽数字）

### 10.5 MD3 形状系统

| 类别 | 圆角 | 用途 |
|------|------|------|
| Extra Small | 4px | Chip 小标签 |
| Small | 8px | 小按钮 |
| Medium | 12px | 卡片 |
| Large | 16px | Extended FAB |
| Extra Large | 28px | 底部 Sheet |

### 10.6 MD3 状态层（State Layer）

- Hover：8% Primary overlay
- Pressed：12% Primary overlay
- Focus：12% Primary overlay
- 在原型中通过 `hover:scale-[0.98]` + `active:bg-primary/12` 模拟

### 10.7 与原 iOS 版的关键差异

1. **底部导航激活态**：iOS 是图标+文字变色，MD3 是整个图标区域用 Pill 形状高亮
2. **FAB 位置**：iOS 中央悬浮，MD3 仍在右下角但样式更规整
3. **圆角更大**：iOS 16px → MD3 12-28px（按层级）
4. **配色更克制**：iOS 单一墨红，MD3 用完整 Primary/Secondary/Tertiary 调色板
5. **动效语言**：iOS spring 弹性 → MD3 emphasized decelerate/accelerate 缓动

---

## 11. 用户系统（V0.2.0 新增）

### 11.1 用户对象

```typescript
interface User {
  uid: string;              // CloudBase 生成的 UUID，主键（对应 JWT sub）
  email: string;            // 标准邮箱（RFC 5322 格式）
  username?: string;        // 可选：用户绑定的 username（登录备用）
  createdAt: number;        // 首次注册时间戳
  lastLoginAt: number;      // 最近登录时间戳
  nickname?: string;        // 用户昵称（可选）
  avatarUrl?: string;       // 头像 URL（可选，V0.2.0 暂不开放上传）
}
```

**身份凭证**：
- 唯一凭证：邮箱（注册时收验证码用，**不参与登录**）
- 登录凭证：username（注册时绑定，登录主用）
- 可选凭证：username（CloudBase 用户名规则，登录备用）
- 登录方式：**username + 密码**（V0.2.0 强密码：8-32 位字母 + 数字）
- 注意：CloudBase /auth/v1/signin body 字段是 `username`（不是 email）
- 图像验证码：CloudBase 风控触发（连续登录失败 3-5 次后必须）

**Token**：
- 类型：JWT（HS256）
- 颁发方：CloudBase 身份认证
- 有效期：2 小时
- 续期：剩余有效期 < 10 分钟时自动续期

### 11.2 登录流程

#### 11.2.1 主流程（用户名 + 密码 + 图像验证码风控）

```
[用户]                 [App]                    [CloudBase Auth v2]
  │                     │                            │
  │ 输入用户名 + 密码    │                            │
  ├───────────────────→│ 校验用户名格式              │
  │                     ├──── POST /auth/v1/signin ──→│
  │                     │   {username, password}     │
  │                     │                            │
  │                     │←─ {success: true,         ─┤
  │                     │     access_token,          │
  │                     │     refresh_token,         │
  │                     │     user: {uid, email,    │
  │                     │      username}}            │
  │                     │                            │
  │←── 登录成功 ────────│ 写入安全存储                │
  │                     │ 触发数据拉取                │
  ↓                     ↓                            ↓

─── 失败流程（密码错误 3-5 次后）─────────────────────────

  │ 输入用户名 + 错误密码│                            │
  ├───────────────────→│                            │
  │                     ├──── POST /auth/v1/signin ──→│
  │                     │   {username, password}     │
  │                     │                            │
  │                     │←─ error.code =             ─┤
  │                     │   CAPTCHA_REQUIRED         │
  │                     │   (返回 captcha_url)        │
  │                     │                            │
  │ 弹出图像验证码弹窗   │                            │
  │ 看到 4 位字符图片    │                            │
  │ 用户输入图片字符     │                            │
  │                     │                            │
  │                     ├─ POST HTTP 图形验证码 API ─→│
  │                     │   (拿 captcha_token)       │
  │                     │                            │
  │                     ├─ POST /auth/v1/signin ────→│
  │                     │   {username, password,     │
  │                     │    captcha_token}           │
  │                     │                            │
  │                     │←─ {success: true,         ─┤
  │                     │     access_token, ...}     │
  │←── 登录成功 ────────│                            │
  ↓                     ↓                            ↓
```

**关键点**：
- 正常登录无需图像验证码，**只在密码错 3-5 次后风控触发**
- 验证码图片由 CloudBase 通过 HTTP API 提供，客户端渲染
- 验证通过后拿到 `captcha_token`，再次登录时附带即可
- 完全 CloudBase 原生，不写云函数

#### 11.2.2 限流与风控规则

| 维度 | 阈值 | 超出处理 |
|------|------|---------|
| 登录 |  |  |
| 同一邮箱 | 1 分钟 5 次 | 提示「请稍候」+ 倒计时 |
| 同一邮箱 | 1 小时 20 次 | 提示「请求过于频繁，请稍后再试」 |
| 同一 IP | 1 小时 30 次 | 触发风控 → 强制图像验证码 |
| 密码错误次数 | 连续 3-5 次 | 触发 `CAPTCHA_REQUIRED` → 必须输入图像验证码 |
| 同一账号累计失败 | 1 天 10 次 | 临时锁定 1 小时（CloudBase 风控默认） |
| 注册邮箱已存在 | - | 提示「该邮箱已注册，请直接登录」 |
| **注册流程（邮箱验证码）** |  |  |
| 同一邮箱发送验证码 | 1 分钟 1 次 | 倒计时 60s（ViewModel 强制） |
| 同一邮箱发送验证码 | 1 小时 5 次 | 提示「请求过于频繁，请稍后再试」 |
| 同一邮箱发送验证码 | 1 天 10 次 | 提示「已达今日上限，请明天再试」 |
| 验证码错误次数 | 连续 5 次 | 当前验证码作废，需重新发送 |
| 验证码有效期 | 5 分钟 | 过期后报错 `VERIFICATION_CODE_EXPIRED` |

#### 11.2.3 错误处理

| 错误码 | 含义 | 用户提示 |
|--------|------|---------|
| `INVALID_EMAIL` | 邮箱格式错误 | 「请输入正确的邮箱」 |
| `USER_NOT_FOUND` | 邮箱未注册 | 「该邮箱未注册，请先注册」 |
| `USER_ALREADY_EXISTS` | 邮箱已注册 | 「该邮箱已注册，请直接登录」 |
| `INVALID_PASSWORD` | 密码错误 | 「密码错误，还有 N 次机会」 |
| `PASSWORD_TOO_WEAK` | 注册密码强度不够 | 「密码需 8-32 位，含字母和数字」 |
| `RATE_LIMIT_EXCEEDED` | 触发限流 | 「请求过于频繁，请稍后再试」 |
| `CAPTCHA_REQUIRED` | 风控触发，需图像验证码 | 「请输入图像验证码」 |
| `CAPTCHA_WRONG` | 图像验证码错误 | 「图像验证码错误，请重试」 |
| `ACCOUNT_LOCKED` | 账户临时锁定 | 「尝试次数过多，请 1 小时后再试」 |
| `VERIFICATION_CODE_INVALID` | 邮箱验证码错误 | 「验证码错误」 |
| `VERIFICATION_CODE_EXPIRED` | 邮箱验证码过期 | 「验证码已过期，请重新获取」 |
| `VERIFICATION_RATE_LIMITED` | 验证码请求限流 | 「请求过于频繁，请稍后再试」 |
| `USERNAME_INVALID` | 用户名格式错误 | 「用户名 5-24 位，字母/数字/_/-」 |
| `USERNAME_EXISTS` | 用户名已被占用 | 「该用户名已被占用」 |
| `NETWORK_ERROR` | 网络异常 | 「网络异常，请检查网络后重试」 |
| `SERVER_ERROR` | 服务异常 | 「服务暂时不可用，请稍后再试」 |

### 11.3 注册（邮箱 + 验证码 3 步流程）

> **关键**：CloudBase Auth v2 **不允许**直接用「用户名 + 密码」注册（这是 CloudBase 的硬限制），必须先用其他方式（邮箱验证码）注册，再绑定 username + 密码。

**完整 3 步流程**：

**Step 1：输入基本信息**
- 用户在登录页点击「没有账号？立即注册」→ 进入 `SignupScreen`
- 输入：邮箱 + username（5-24 位字母/数字/_/-）+ 密码（8-32 位字母+数字）+ 确认密码
- 客户端校验：邮箱格式 / username 格式 / 两次密码一致 / 密码强度
- 校验通过 → 调 `auth.sendSignupCode(email, target="NON_USER")`

**Step 2：发送邮箱验证码**
- 服务端发邮件（含 6 位数字验证码，5 分钟有效）
- 返回 `verification_id`（客户端不持久化，存在 ViewModel 内存）
- 客户端 UI 跳到 `VerificationScreen`（带 email 脱敏显示「yo****@example.com」）
- 启动 60s 倒计时（防止用户重复点「重新发送」）

**Step 3：输入验证码 + 注册**
- 用户在 6 个独立方框输入 6 位验证码
- 点「注册」→ 客户端顺序调 2 个 API：
  1. `auth.verifyVerification(verification_id, code)` → 返回 `verification_token`
  2. `auth.signUp({ email, verification_token, username, password })` → 返回 `access_token` + `user`
- 注册成功 = 自动登录（access_token 写入 EncryptedSharedPreferences）
- **AuthGuard 监听 session 变化，自动跳主屏**

**重复注册处理**：
- 邮箱已存在 → Step 1 报错 `USER_ALREADY_EXISTS`，提示「该邮箱已注册，请直接登录」
- username 已存在 → Step 3 报错 `USERNAME_EXISTS`
- 验证码错 5 次 → 当前验证码作废，需重新发送
- 验证码 5 分钟过期 → 提示「验证码已过期，请重新获取」

**同意服务条款**：
- 不需要单独步骤
- 「登录/注册」按钮下方以小字标注「登录即代表同意《服务协议》《隐私政策》」

### 11.4 退出与切换

**退出登录**：
- 「我的」页 → 「退出登录」入口
- 弹出确认：「退出后数据仍保留在云端，下次登录可恢复」
- 确认后：清除本地 Token、清除登录态、下次启动回到登录页
- **不**清空 localStorage / 云端数据

**切换账号**：
- 「我的」页 → 「切换账号」
- 退出当前账号 → 回到登录页 → 输入新邮箱登录
- 旧账号数据保留在云端

### 11.5 强制登录的产品决策说明

V0.1.0 哲学「零启动成本、不用注册」被正式放弃。理由：

| 理由 | 说明 |
|------|------|
| 多端同步 | V0.2.0 起核心承诺，没有账号无法实现 |
| 数据所有权 | 从 localStorage 转向云端，本地仅作缓存 |
| 用户体验 | 验证码即密码，免去「忘记密码」一类问题 |
| 防御性 | 卸载重装 / 换手机不丢数据 |
| 商业化铺垫 | V0.3.0+ 可能引入会员体系，账号是基础 |

**对老用户的影响**：
- 首次升级 V0.2.0：弹「数据迁移提示」
- 登录成功后：自动把 localStorage 中所有数据上传到云端
- 迁移完成后：localStorage 数据可手动清空（建议保留作离线缓存）

---

## 12. 腾讯云认证与后端架构（V0.2.0 新增）

### 12.1 技术选型总览

| 层级 | 选型 | 理由 |
|------|------|------|
| **身份认证** | CloudBase Auth v2 | 内置 **username + 密码**登录、图像验证码风控、JWT 颁发，0 部署 |
| **后端 BaaS** | 腾讯云 CloudBase | 一体化：身份认证 + 数据库 + 存储，对齐 Supabase 心智，RLS 天然支持 |
| **数据库** | CloudBase PostgreSQL | 关系型，事务、索引、行级权限（RLS）开箱即用 |
| **后端函数** | **不部署** | V0.2.0 客户端直连数据库 + RLS，0 后端代码 |
| **存储** | CloudBase 云存储 | 用于头像、备份导出文件（V0.2.0 暂不启用） |
| **CDN** | CloudBase 静态托管 | V0.2.0 暂不部署 Web 端，预留 |

### 12.2 CloudBase Auth v2 邮箱 + 验证码注册

V0.2.0 注册用「**邮箱 + 验证码**」3 步流程（email 收验证码），登录用「**username + 密码**」1 步。**0 短信费、0 签名审核**，但**邮件模板需审核**（1-2 工作日）。

#### 12.2.1 邮箱规则

- 格式：标准 RFC 5322（CloudBase 自动校验）
- 唯一：每个邮箱在 CloudBase 唯一
- 注册时：必须先发邮箱验证码，验证邮箱所有权后才能完成注册
- 登录时：**username + 密码**（body 字段是 `username`，不是 email；CloudBase 自动校验密码强度）

#### 12.2.2 密码规则

- **长度**：8-32 位
- **复杂度**：必须同时包含字母和数字
- **存储**：CloudBase 自动使用 bcrypt 哈希存储，开发者不可见明文
- **传输**：全程 HTTPS
- **找回**：V0.2.0 暂不开放「忘记密码」邮件链接（V0.3.0 规划）

#### 12.2.3 邮箱验证码规则

- **长度**：6 位数字
- **有效期**：5 分钟
- **使用次数**：1 次（验证成功后立即失效）
- **存储**：CloudBase 后端
- **限流**：同 §11.2.2

#### 12.2.4 邮件模板

- 模板示例：`【流 Flow】您的验证码是 {1}，5 分钟内有效，请勿泄露。`
- 签名需在腾讯云后台单独申请「流 Flow」
- 审核周期：1-2 个工作日
- V0.2.0 推荐用默认模板，等用户量大、要做品牌感时再改自定义

#### 12.2.5 图像验证码（登录风控）

**默认行为**：
- 正常登录不需要图像验证码
- 同一账号（username）密码错误 3-5 次后，CloudBase 自动返回 `CAPTCHA_REQUIRED` 错误码
- 客户端拿到 `captcha_url`，渲染图像验证码
- 用户输入图片字符后，客户端调 HTTP API 拿到 `captcha_token`
- 附带 `captcha_token` 重新发起登录请求

### 12.3 CloudBase 资源消耗估算

#### 12.3.1 假设场景

- 注册用户：1000 人
- 日活：100 人（10%）
- 每人每天登录 1 次
- 每人每天完成 3 个 Session（每次 1 次数据库写入 + 偶尔 1 次读）
- 每人每天打开 5 次 App（每次 0.5 次数据库读）

#### 12.3.2 月度消耗估算

| 资源 | 单价 | 月度用量 | 月度费用 |
|------|------|---------|---------|
| 短信验证码 | - | **0**（V0.2.0 不使用） | **¥0** |
| CloudBase 个人版 | ¥19.9/月 | 1 套 | ¥19.9 |
| 合计 | - | - | **≈ ¥20/月** |

**对比 V0.2.0 PRD 原短信方案节省 ¥135/月**（短信 ¥135 + CloudBase ¥19.9 = ¥155 → 仅 CloudBase ¥19.9）。

#### 12.3.3 套餐选型建议

| 阶段 | 套餐 | 资源点 | 月费 |
|------|------|--------|------|
| MVP（V0.2.0 验证期） | FREE 免费版 | 3000 点 | ¥0 |
| 内测（< 100 用户） | PERSONAL 个人版 | 40000 点 | ¥19.9 |
| 公测（100-1000 用户） | STANDARD 标准版 | 330000 点 | ¥179.1 |
| 商业化 | ENTERPRISE 企业版 | 1500000 点 | ¥799.2 |

**V0.2.0 推荐起手**：FREE 免费版（3000 资源点/月）。**username + 密码**登录 0 短信费，比短信方案节省一大笔。

### 12.4 腾讯云账号准备清单

1. **实名认证**：个人认证 / 企业认证（推荐企业，后续可商用）
2. **开通 CloudBase**：控制台 → 云开发 → 新建环境，**地域必须上海**（邮箱验证码仅支持上海）
3. **创建数据库**：PostgreSQL 模式
4. **启用邮箱登录**：控制台 → 身份认证 → 登录方式 → 邮箱（必须开启，否则注册流程跑不通）
5. **配置邮件模板**：控制台 → 身份认证 → 邮件模板（默认模板即可，1-2 工作日审核）
6. **不部署云函数**：V0.2.0 采用 CloudBase 内置 Auth + RLS 直连数据库，0 后端代码

**总耗时**：
- 实名认证：1-3 天（如已实名可跳过）
- 邮件模板审核：1-2 工作日（如用默认模板可缩短到 0）
- CloudBase 开通 + 数据库表 + RLS：30 分钟
- **总计：1-3 天（主要等模板审核）**

---

## 13. 云端数据同步策略（V0.2.0 新增）

### 13.1 同步范围

| 数据 | 同步策略 | 频率 |
|------|---------|------|
| **Session（专注记录）** | 完成后立即上传 | 实时 |
| **DailyGoal（每日目标）** | 设置/修改时上传 | 实时 |
| **Settings（个人偏好）** | 修改时上传 | 实时 |
| **Theme（主题色）** | 修改时上传 | 实时 |
| **Streak 等聚合数据** | 不存云端，本地计算 | - |
| **导出文件（JSON/CSV）** | V0.2.0 暂不同步 | - |

### 13.2 同步策略

**写策略**：
1. 本地立即写入（保证 UI 即时响应）
2. 立即触发云端同步（Best-effort，不阻塞 UI）
3. 同步失败：本地标记为「待同步」，下次联网/下次启动时重试

**读策略**：
1. 登录成功 → 拉取云端所有数据
2. 与本地数据合并（以云端时间戳为准）
3. 合并完成后刷新 UI

**离线场景**：
- 本地正常写入、统计、显示
- 登录态有效期内联网后自动补传
- Token 失效时：仅本地可用，云端同步挂起

### 13.3 数据冲突处理

**策略**：以云端 `last_modified_at` 为准

| 场景 | 行为 |
|------|------|
| 本地有，云端无 | 上传 |
| 云端有，本地无 | 下载 |
| 都有，云端更新 | 覆盖本地 |
| 都有，本地更新 | 上传并覆盖云端 |
| 时间戳相同（误差 < 1s） | 视为一致，不操作 |

### 13.4 数据表设计（CloudBase PostgreSQL）

> **设计原则**：用户体系用 CloudBase 内置 user，业务表 3 张即可。`owner_id` 默认从 JWT 读取，从源头杜绝身份伪造。
>
> **配套实操**：完整的建表 SQL + RLS 策略见 [`docs/腾讯云后端服务开通流程.md` §4-5](./腾讯云后端服务开通流程.md)。

#### 13.4.1 users（用户表）

**使用 CloudBase 内置 user，无需自建**。

CloudBase Auth 在用户注册时自动管理 `uid`（对应 JWT `sub` 字段）、`phone_number`、`created_at` 等。可在控制台 → 身份认证 → 用户管理可视化查看。

#### 13.4.2 sessions（专注记录表）

```sql
CREATE TABLE public.sessions (
  id varchar(40) PRIMARY KEY,
  owner_id varchar(64) NOT NULL DEFAULT (current_setting('request.jwt.claims', true)::json->>'sub'),
  task text,
  category varchar(20),
  planned_duration int,
  actual_duration int,
  status varchar(20),
  started_at timestamptz,
  ended_at timestamptz,
  hour int,
  weekday int,
  date varchar(10),
  last_modified_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_sessions_owner_date ON public.sessions(owner_id, date);
CREATE INDEX idx_sessions_owner_started ON public.sessions(owner_id, started_at DESC);
```

**字段变更说明**：
- `uid UUID` → `owner_id varchar(64)`：对齐 CloudBase RLS 标准约定，varchar(64) 存 UUID 字符串更紧凑
- `public.` schema：CloudBase PostgreSQL 默认 schema
- `DEFAULT (current_setting('request.jwt.claims', true)::json->>'sub')`：客户端无需传 owner_id，自动从 JWT 读取

#### 13.4.3 daily_goals（每日目标表）

```sql
CREATE TABLE public.daily_goals (
  owner_id varchar(64) NOT NULL DEFAULT (current_setting('request.jwt.claims', true)::json->>'sub'),
  date varchar(10) NOT NULL,
  target int NOT NULL,
  last_modified_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (owner_id, date)
);
```

#### 13.4.4 settings（个人设置表）

```sql
CREATE TABLE public.settings (
  owner_id varchar(64) PRIMARY KEY DEFAULT (current_setting('request.jwt.claims', true)::json->>'sub'),
  theme varchar(20),
  dark_mode varchar(20),
  default_focus_minutes int,
  default_rest_minutes int,
  sound_enabled boolean,
  vibration_enabled boolean,
  last_modified_at timestamptz
);
```

### 13.5 安全规则（Row Level Security / RLS）

> **核心**：CloudBase 配合 PostgreSQL RLS 实现「客户端直连数据库 + 自动权限隔离」，**无需写任何后端鉴权代码**。

#### 13.5.1 抽象函数（首次配置执行一次）

```sql
-- 返回当前登录用户 ID
CREATE OR REPLACE FUNCTION auth.uid() RETURNS text
LANGUAGE SQL STABLE AS $$
  SELECT current_setting('request.jwt.claims', true)::json->>'sub'
$$;

-- 返回当前角色
CREATE OR REPLACE FUNCTION auth.role() RETURNS text
LANGUAGE SQL STABLE AS $$
  SELECT current_setting('request.jwt.claims', true)::json->>'role'
$$;
```

#### 13.5.2 启用 RLS + 表级授权

```sql
ALTER TABLE public.sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.settings ENABLE ROW LEVEL SECURITY;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.sessions TO authenticated;
GRANT USAGE, SELECT ON SEQUENCE public.sessions_id_seq TO authenticated;
GRANT ALL ON public.sessions TO service_role;
GRANT USAGE, SELECT ON SEQUENCE public.sessions_id_seq TO service_role;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.daily_goals TO authenticated;
GRANT ALL ON public.daily_goals TO service_role;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.settings TO authenticated;
GRANT ALL ON public.settings TO service_role;
```

#### 13.5.3 行级 Policy（每表 4 套：SELECT / INSERT / UPDATE / DELETE）

以 `sessions` 为例（`daily_goals` / `settings` 模板相同）：

```sql
-- SELECT：只能读自己的
CREATE POLICY sessions_select_own ON public.sessions
  FOR SELECT TO authenticated
  USING (owner_id = (select auth.uid()));

-- INSERT：只能插入 owner_id = 自己的
CREATE POLICY sessions_insert_own ON public.sessions
  FOR INSERT TO authenticated
  WITH CHECK (owner_id = (select auth.uid()));

-- UPDATE：只能改自己的，且不能改 owner_id
CREATE POLICY sessions_update_own ON public.sessions
  FOR UPDATE TO authenticated
  USING (owner_id = (select auth.uid()))
  WITH CHECK (owner_id = (select auth.uid()));

-- DELETE：只能删自己的
CREATE POLICY sessions_delete_own ON public.sessions
  FOR DELETE TO authenticated
  USING (owner_id = (select auth.uid()));
```

**效果**：
- 客户端不带 token → 查不到任何数据
- 客户端伪造 `owner_id` → `WITH CHECK` 拒绝
- 客户端 A 查 B 的数据 → `USING` 过滤掉
- 客户端用 service_role key → `BYPASSRLS` 全权限（仅后端使用）

### 13.6 后端函数：**不部署云函数**

按「不部署后端应用」原则，V0.2.0 不写任何云函数。客户端直接通过 SDK 调 CloudBase HTTP API，RLS 自动鉴权。

| 业务能力 | 原云函数方案 | V0.2.0 实际方案 |
|---------|------------|---------------|
| 用户注册 | - | CloudBase Auth 内置：客户端调 `auth.signUp({email, verification_token, username, password})` |
| 用户登录 | - | CloudBase Auth 内置：客户端调 `auth.signIn({username, password})`（**注意：不是 email**）|
| 图像验证码 | - | CloudBase Auth 风控触发：客户端调 `auth.signIn({username, password, captcha_token})` |
| 写入 Session | `syncSession` | 客户端直调 `POST /v1/rdb/rest/sessions`，RLS 自动校验 owner_id |
| 读取所有数据 | `getUserData` | 客户端直调 `GET /v1/rdb/rest/sessions?select=*` |
| 写入 Settings | `syncSettings` | 客户端直调 `POST /v1/rdb/rest/settings` |
| 批量迁移 | `batchMigrate` | 客户端循环 `POST /v1/rdb/rest/sessions`，或一次性 bulk insert |

**优势**：
- 零部署：所有能力由 CloudBase 托管，开通即用
- 零维护：无云函数冷启动、无版本管理
- 0 元成本：V0.2.0 数据量小，个人版 ¥19.9/月 足够

**代价**：
- 复杂业务逻辑（如聚合统计）不适合直接 RLS 暴露，未来需要时再上云函数
- 客户端可见表结构（RLS 保护数据，但不能隐藏 schema）

### 13.7 客户端 SDK 选型

- **Android 原生**：CloudBase Android SDK（`com.tencent.tcb：tcb-sdk-android`）
- **Web 原型（V0.2.0 演示用）**：CloudBase JS SDK + Web 端 JWT 直接调用

---

## 14. 数据迁移（V0.2.0 新增）

### 14.1 V0.1.0 → V0.2.0 升级路径

```
[首次启动 V0.2.0]
  ↓
检测 localStorage
  ├─ 无数据 → 直接进入登录页
  └─ 有数据 → 显示「数据迁移提示」
        ↓
  ┌─ 弹窗内容：
  │  「检测到本地有 X 条专注记录，登录后自动同步到云端，是否继续？」
  │  [稍后再说] [立即登录]
  ↓
用户选择
  ├─ 稍后再说 → 进入登录页，本地数据保留（下次登录时可再次提示）
  └─ 立即登录 → 进入登录页
        ↓
  登录成功 → 触发 batchMigrate 云函数
        ↓
  迁移完成 toast：「已迁移 X 条记录到云端」
        ↓
  进入主屏
```

### 14.2 字段映射

| localStorage 字段 | CloudBase 字段 | 转换 |
|------------------|----------------|------|
| `sessions[]` | `sessions` 表 | 直接映射，保留 id |
| `daily_goals[date]` | `daily_goals` 表 | 按 (owner_id, date) 主键 upsert |
| `settings` (单个对象) | `settings` 表（按 owner_id 一行） | 单行 upsert |

### 14.3 冲突处理

**迁移时**：
- 云端无数据 → 直接插入
- 云端有数据 → 提示用户「云端已有 N 条记录，是否合并？」
  - 合并：按 `id` 去重，保留时间戳更新的版本
  - 覆盖：清空云端，重新上传本地
  - 取消：保留云端，丢弃本地

### 14.4 迁移代码示例（伪代码）

```typescript
async function migrateFromV1() {
  const localSessions = localStorage.getItem('sessions') || '[]';
  const localDailyGoals = localStorage.getItem('daily_goals') || '{}';
  const localSettings = localStorage.getItem('settings') || '{}';

  if (!localSessions.length && !localDailyGoals.length && !localSettings.length) {
    return; // 无数据，跳过
  }

  await callFunction('batchMigrate', {
    sessions: JSON.parse(localSessions),
    dailyGoals: JSON.parse(localDailyGoals),
    settings: JSON.parse(localSettings),
  });

  // 标记本地为「已迁移」，不再二次提示
  localStorage.setItem('migrated_to_v2', 'true');
}
```

---

## 15. 登录界面规范（V0.2.0 新增）

### 15.1 页面清单

| 页面 | 文件 | 状态 | 备注 |
|------|------|------|------|
| 启动屏 | `splash.html` | 新增 | 1.5s 圆环呼吸动画 |
| 登录页 | `login.html`（主） | 新增 | **username + 密码** |
| 注册页 | `login.html`（切换） | 新增 | **email + username + 密码 + 确认**（3 步注册） |
| 图像验证码弹窗 | `login.html`（同页） | 新增 | 风控触发后显示 |
| 服务协议 | `agreement.html` | 新增 | 模态弹窗 |
| 数据迁移提示 | `migrate.html` | 新增 | V0.1.0 升级用户专属 |

### 15.2 视觉规范

**整体沿用 V0.1.0 MD3 风格**：
- Pixel 8/9 外框（412 × 892 dp）
- 状态栏 24dp（时间居左、信号/电量居右）
- 3-button 底部导航
- Roboto 字体栈 + Roboto Mono 数字
- 4 套主题色均适配，薄暮紫粉为默认

**主题色微调（折中策略）**：
- 登录页强调色比设置中心明亮 8%（HSL 提亮）
- logo 区添加主题色 6% alpha 光晕（`box-shadow: 0 0 60px -10px ${primary}66`）
- 「登录」按钮使用 `primary` 主题色；图像验证码弹窗用 `primary-container` 浅色

**专属启动动效**：
- 启动屏 1.5s：圆环从 0 动画到 360°（`stroke-dashoffset` 缓出）
- 圆环下方 logo 「流」字淡入 800ms
- 背景从 `#FAFAF7` 到主题色 5% alpha 渐变
- 进入主页面后圆环停留 200ms 再淡出

### 15.3 关键交互

#### 15.3.1 登录页（login.html 主视图）

```
┌──────────────────────────────┐
│  ←                          │
│                              │
│         [圆环 logo]           │  ← 主题色光晕
│                              │
│      欢迎回到 流 Flow         │  ← Headline 28px
│    登录后数据云端同步，多端互通 │  ← Body 14px 灰
│                              │
│  ┌────────────────────────┐  │
│  │ 邮箱                   │  │  ← Outlined TextField
│  │ your@email.com         │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ 密码              👁    │  │  ← Outlined TextField
│  │ ••••••••               │  │  ← 密码可见切换
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │       登录             │  │  ← Filled Button (primary)
│  └────────────────────────┘  │
│                              │
│     没有账号？立即注册         │  ← Text Button
│                              │
│  登录即代表同意《服务协议》    │  ← Body 12px
│  《隐私政策》                │
└──────────────────────────────┘
```

**交互细节**：
- 邮箱输入框自动 trim 空格、自动 RFC 5322 格式校验
- 密码输入框支持显示/隐藏切换（眼睛图标）
- 「登录」按钮在 **username + 密码**均非空时高亮（username 5-24 位、密码 8-32 位）
- 点击 → loading 态 → 成功后进入主屏
- 密码错误 3-5 次后弹图像验证码（见 15.3.3）

#### 15.3.2 注册 Step 1：输入基本信息（Android SignupScreen）

```
┌──────────────────────────────┐
│  ←                          │
│                              │
│      创建你的 流 Flow 账号     │  ← Headline 32px
│    邮箱验证后即可开始专注     │  ← Body 13px
│                              │
│  ┌────────────────────────┐  │
│  │ 📧 邮箱                │  │
│  │ your@email.com         │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ 用户名 5-24 位字母数字_-  │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ 密码    8-32 位字母+数字 👁 │  │  ← 强度 helper text
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │ 确认密码            👁  │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │      发送验证码        │  │  ← Filled Button (primary)
│  └────────────────────────┘  │
│                              │
│      已有账号？立即登录       │
└──────────────────────────────┘
```

**交互细节**：
- 邮箱格式实时校验（RFC 5322）
- 用户名规则提示（5-24 位，字母/数字/_/-）
- 密码强度实时提示：弱 / 中 / 强
- 两次密码不一致：抖动 + 错误提示
- 客户端先校验全部字段，校验通过才发请求
- 「发送验证码」按钮在 loading 态变 spinner
- 成功 → ViewModel 切到 `CodeSent` 状态 → 跳到 VerificationScreen

#### 15.3.3 注册 Step 2：输入邮箱验证码（Android VerificationScreen）

```
┌──────────────────────────────┐
│  ←                          │
│                              │
│      输入验证码               │  ← Headline 22px
│   我们已发送 6 位验证码到你的邮箱 │
│                              │
│      yo****@example.com      │  ← 脱敏显示
│                              │
│  ┌──┐┌──┐┌──┐┌──┐┌──┐┌──┐  │
│  │ 1││ 2││ 3││ 4││ 5││  │  │  ← 6 个独立方框
│  └──┘└──┘└──┘└──┘└──┘└──┘  │
│                              │
│      60s 后可重新发送         │  ← 倒计时
│      (倒计时结束后显示「重新发送」)
│                              │
│  ┌────────────────────────┐  │
│  │       注册              │  │  ← 6 位都填了才能点
│  └────────────────────────┘  │
│                              │
│      修改邮箱                 │  ← Text Button
└──────────────────────────────┘
```

**交互细节**：
- 6 个独立方框，自动 focus 第一格
- 支持粘贴自动填充（识别 6 位连续字符）
- 单格输入后自动 focus 下一格
- 倒计时 60s（持久化到 ViewModel 内存，不跨进程）
- 倒计时结束后切换为「重新发送验证码」按钮
- 错误时方框抖动 + 红色边框 + 错误提示
- 「修改邮箱」返回 Step 1（重新填写）

#### 15.3.3 图像验证码弹窗（风控触发）

```
┌──────────────────────────────┐
│  ╳                          │
│  请完成安全验证                │  ← Headline 18px
│                              │
│  ┌──────────┐               │
│  │ [验证码图片]│               │  ← 4 字符图片
│  │   a8K2   │               │  ← 来自 CloudBase HTTP API
│  └──────────┘                │
│      换一张  ↻                │  ← 刷新
│                              │
│  ┌────────────────────────┐  │
│  │ 请输入图片中的字符      │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │       验证             │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

**触发条件**：
- 同一账号（username）密码错误 3-5 次后自动弹出
- 客户端捕获 `CAPTCHA_REQUIRED` 错误码
- 调用 `GET /auth/v1/captcha` 拿图片 URL
- 渲染图片，用户输入字符
- 调用 `POST /auth/v1/captcha/verify` 拿 `captcha_token`
- 附带 `captcha_token` 重新发起登录

**设计原则**：
- 弹窗半透明遮罩 + 居中卡片，圆角 20px
- 图片背景随机干扰线，4 位大小写字母+数字
- 错误时抖动 + 提示「验证失败，请重试」
- 验证成功后自动关闭弹窗，继续登录

#### 15.3.4 服务协议（agreement.html，模态弹窗）

- 从底部弹起（Bottom Sheet，28px 顶部圆角）
- 高度：屏幕 70%
- 内容：服务协议 + 隐私政策（Markdown 渲染）
- 顶部下拉手势可关闭

### 15.4 文案规范

| 场景 | 文案 |
|------|------|
| 登录主标题 | 欢迎回到 流 Flow |
| 登录副标题 | 登录后数据云端同步，多端互通 |
| 登录邮箱 placeholder | 请输入邮箱 |
| 登录密码 placeholder | 请输入密码 |
| 注册主标题 | 创建你的 流 Flow 账号 |
| 注册邮箱 placeholder | 请输入邮箱 |
| 注册密码 placeholder | 8-32 位字母 + 数字 |
| 注册确认密码 placeholder | 请再次输入密码 |
| 倒计时（注册） | 59s 后重新发送验证码（邮箱注册无需验证码） |
| 主按钮 | 登录 / 注册 |
| 错误提示 | 密码错误，还有 N 次机会 / 请输入正确的邮箱 |
| 协议文案 | 登录即代表同意《服务协议》《隐私政策》 |
| 图像验证码弹窗 | 请完成安全验证 / 请输入图片中的字符 |

### 15.5 4 主题色适配示例

**经典主题（紫）**：
- 主色 `#6750A4` / Container `#EADDFF` / On Container `#21005D`
- 强调按钮文字色 `#FFFFFF`（on-primary）

**静夜主题（蓝）**：
- 主色 `#4A6FA5` / Container `#D3E3FD` / On Container `#0E1B2C`
- 强调按钮文字色 `#FFFFFF`

**森林主题（绿）**：
- 主色 `#2D6A4F` / Container `#A8D5BA` / On Container `#0F2A1B`
- 强调按钮文字色 `#FFFFFF`

**薄暮主题（紫粉）**：
- 主色 `#7A4E7C` / Container `#E8C5E5` / On Container `#2A0E2C`
- 强调按钮文字色 `#FFFFFF`

---

## 16. 非功能性需求（V0.2.0 增量）

### 16.1 安全

| 项 | 规范 |
|----|------|
| **密码** | 8-32 位字母+数字，CloudBase 自动 bcrypt 哈希存储 |
| **密码传输** | 全程 HTTPS，前端不存储明文 |
| **密码错误** | 连续 3-5 次触发图像验证码，1 天 10 次后临时锁定 1 小时 |
| **通信** | 全程 HTTPS / WSS |
| **Token** | JWT 2 小时有效期 + Refresh Token 30 天，SDK 自动续期 |
| **邮箱展示** | 列表中脱敏 `yo****@example.com` |
| **Token 存储** | Android EncryptedSharedPreferences / iOS Keychain |
| **登出** | 主动调用 `auth.signOut()` + 本地清除 |
| **图像验证码** | CloudBase 风控触发，4 位字符图片，HTTP API 校验 |
| **注册防刷** | 同邮箱 1 分钟 5 次，1 小时 20 次（同 IP） |

### 16.2 性能

| 指标 | 目标 |
|------|------|
| 启动屏到登录页 | < 800ms |
| **username + 密码**登录端到端 | < 1.5s |
| 图像验证码图片获取 | < 1s |
| 图像验证码校验 + 重新登录 | < 2s |
| 数据拉取（1000 条 Session） | < 2s |
| 倒计时刷新 | localStorage 持久化，防止刷新绕过 |
| Token 续期阈值 | 剩余有效期 < 10 分钟时自动续期 |
| 首页加载（含登录态） | < 1.5s |

### 16.3 隐私

| 项 | 说明 |
|----|------|
| **强制登录** | 邮箱是必填项（V0.2.0 政策决定） |
| **退出登录** | local 数据保留、云端数据保留、Token 失效 |
| **注销账户（V0.2.0 暂不开放）** | V0.3.0 规划：清空云端所有数据、不可恢复 |
| **数据导出** | 仍支持 JSON / CSV 导出，导出后用户自行保管 |
| **服务协议 + 隐私政策** | 必须可访问、明确说明数据使用范围 |
| **第三方 SDK** | 仅腾讯云 CloudBase（含内置图像验证码 HTTP API），无其他第三方 |
| **广告 / 追踪** | 零广告、零追踪、零统计 SDK |

### 16.4 可用性

| 场景 | 处理 |
|------|------|
| 网络异常 | 登录失败友好提示，按钮 loading 态恢复 |
| 密码错误 | 抖动反馈 + 错误提示「密码错误，还有 N 次机会」 |
| 忘记密码 | V0.2.0 暂不支持邮箱链接重置（V0.3.0 规划），可联系客服 |
| 邮箱已注册 | 提示「该邮箱已注册，请直接登录」并自动跳转 |
| Token 失效 | 自动跳登录页、保留本地数据 |
| 国际邮箱 | 全支持（Gmail / Outlook / Yahoo 等） |
| 老年用户 | 大字号模式（V0.3.0 规划） |

### 16.5 合规

- **工信部备案**：如上架国内市场需 ICP 备案 + 公安备案
- **隐私政策**：必须包含邮箱收集说明、数据使用范围、用户权利
- **服务协议 + 隐私政策**：明确说明邮箱作为账号使用
- **未成年人保护**：默认不向 14 岁以下用户开放（V0.2.0 简化处理）

---

## 17. 范围之外（V0.2.0）

以下功能 V0.2.0 不做，明确边界：

- ❌ 邮箱注册 / 第三方登录（微信扫码、QQ、Apple ID、GitHub）
- ❌ 找回密码（验证码即密码，无需找回）
- ❌ 注销账户（V0.3.0 规划）
- ❌ 多设备同时在线冲突的复杂合并
- ❌ 商业化（会员、Pro 等级）
- ❌ 语音验证码（V0.2.0 后期评估）
- ❌ 手机号登录（V0.2.0 仅支持邮箱）
- ❌ 大字号 / 老年模式
- ❌ 头像上传（V0.2.0 暂不开放）
- ❌ 用户昵称（V0.2.0 用邮箱脱敏展示）

---

## 18. V0.2.0 验收标准

### 18.1 必含页面

- [ ] **splash.html**（启动屏 1.5s 动效）
- [ ] **login.html**（**username + 密码** + 协议）
- [ ] **agreement.html**（服务协议模态）
- [ ] **migrate.html**（V0.1.0 升级用户数据迁移提示）
- [ ] focus.html（首次启动需登录后才能进入）
- [ ] 「我的」页新增「退出登录」「切换账号」入口
- [ ] 「我的」页顶部显示当前登录邮箱（脱敏）

### 18.2 后端

- [ ] 腾讯云账号实名认证（个人/企业）
- [ ] CloudBase 环境开通，**地域 = 上海**，模式 = PostgreSQL
- [ ] Publishable Key 已保存到本地配置
- [ ] 邮箱 + 用户名密码登录方式已开启（控制台 → 身份认证 → 登录方式）
- [ ] 3 张业务表创建（`sessions` / `daily_goals` / `settings`）
- [ ] 抽象函数 `auth.uid()` / `auth.role()` 已创建
- [ ] RLS 启用 + 表级 GRANT + 4 套 Policy 每表齐备
- [ ] 图像验证码风控测试：密码错 5 次后必触发
- [ ] 端到端验证 7 步全部通过（详见流程文档 §7.2）
- [ ] 不部署任何云函数（0 后端代码）

**配套实操文档**：[`docs/腾讯云后端服务开通流程.md`](./腾讯云后端服务开通流程.md)

### 18.3 同步

- [ ] 登录后 Session 实时同步
- [ ] 离线场景下本地正常写入、登录恢复后批量补传
- [ ] 切换设备登录：数据完整拉取
- [ ] V0.1.0 → V0.2.0 数据迁移：localStorage → CloudBase 无丢失

### 18.4 登录 UI

- [ ] Pixel 8/9 外框适配
- [ ] 4 套主题色全部适配（薄暮为默认）
- [ ] 启动屏圆环呼吸 1.5s 动效流畅
- [ ] 邮箱 / 密码输入流畅，密码可见切换工作正常
- [ ] 倒计时 60s 不被刷新绕过
- [ ] 服务协议 / 隐私政策可访问

---

## 19. 风险与依赖

### 19.1 风险

| 风险 | 影响 | 缓解 |
|------|------|------|
| 密码撞库攻击 | 账户被盗 | 强制 8-32 位密码 + 图像验证码风控 + bcrypt 哈希 |
| 用户抗拒强制登录 | 老用户流失 | 引导文案 + 服务协议透明化 + 数据迁移说明 |
| CloudBase 资源点耗尽 | 服务不可用 | 设置 80% 阈值告警、自动降级为本地模式 |
| 实名认证审核拖延 | 发布延期 | 提前 1 周开始准备材料 |
| RLS 策略漏洞 | 数据泄露 | 安全测试 + 渗透测试 + 第三方审计 |
| 并发同步冲突 | 数据错乱 | lastModifiedAt 时间戳 + 用户级锁 |
| 图像验证码被 OCR 绕过 | 风险降低 | CloudBase 风控会动态升级（滑块 / 拼图） |

### 19.2 依赖

| 依赖 | 状态 | 备注 |
|------|------|------|
| 腾讯云账号实名认证 | 待办 | 个人认证 1 天 / 企业认证 3-7 天 |
| CloudBase 环境开通 | 待办 | 实名后即时，**地域不再受限**（账号密码登录全地域支持） |
| PostgreSQL 数据库创建 | 待办 | CloudBase 控制台一键 |
| 3 张表结构创建 | 待办 | SQL 脚本一次性执行 |
| **username + 密码**登录方式开启 | 待办 | 控制台勾选，5 分钟完成 |
| 图像验证码风控配置 | 待办 | 控制台默认配置，无需操作 |
| 0 个云函数部署 | - | V0.2.0 不部署 |
| App 备案（如上架） | 待办 | ICP 备案 7-20 天 |

### 19.3 数据来源

- CloudBase 邮箱密码登录：https://docs.cloudbase.net/authentication/method/username-login
- CloudBase 登录认证 v2：https://docs.cloudbase.net/en/api-reference/webv2/authentication_v2
- CloudBase 身份认证概述：https://cloud.tencent.com/document/product/876/121347
- CloudBase 图形验证码处理：https://docs.cloudbase.net/service/authentication
- CloudBase 产品介绍：https://docs.cloudbase.net/
- CloudBase 定价：https://www.cloudbase.net/pricing

---

## 20. V0.2.0 关键决策记录

| 决策 | 选项 | 选定 | 理由 |
|------|------|------|------|
| 认证方式 | 微信扫码 / 手机号+短信 / 邮箱+验证码 / 邮箱+密码 | **邮箱+验证码注册 + username+密码登录** | CloudBase Auth v2 真实流程（/signin body 用 `username` 字段） |
| 范围边界 | 只登录 / 登录+同步 / 同步+社交 / 同步+商业化 | **登录+同步** | V0.2.0 聚焦多端同步，社交/商业化后续版本 |
| 定位哲学 | 可选 / 引导 / 强制 / 渐进 | **强制登录** | V0.2.0 正式放弃零启动哲学 |
| UI 风格 | 完全沿用 / 独立设计 / 折中 | **折中（主题色微调+启动动效）** | 视觉一致 + 产品仪式感 |

---

## 21. 下一步计划

| 优先级 | 计划 | 预计耗时 | 依赖 |
|--------|------|---------|------|
| P0 | 腾讯云账号实名 + CloudBase 环境开通 | 1-2 天 | 无 |
| P0 | CloudBase 环境开通 + 数据库表创建 | 0.5 天 | 实名完成 |
| P0 | 云函数开发（6 个） | 3-4 天 | 表结构完成 |
| P0 | Android 客户端接入 CloudBase SDK | 2-3 天 | 云函数上线 |
| P0 | 登录页 UI 实现 | 1-2 天 | 设计稿定稿 |
| P0 | V0.1.0 → V0.2.0 数据迁移测试 | 1 天 | 全链路联调 |
| P1 | 安全测试（RLS / 限流 / Token） | 1-2 天 | 全链路联调 |
| P1 | Release APK 签名 + 上架 | 1 天 | 用户决定 |
| P2 | 注销账户功能 | 1 天 | V0.2.0 上线后 |
| P2 | 微信扫码登录（V0.2.1） | 3 天 | 用户决定 |

---

**文档版本**：v0.2.0
**创建日期**：2026-08-25
**最后更新**：2026-08-25
**关联文档**：
- V0.1.0 PRD：`docs/prd.md`（保留作历史档案）
- V0.3.0 迭代记录：`docs/迭代记录-2026-08-23.md`
- 项目交付执行情况：`docs/项目交付执行情况.md`
