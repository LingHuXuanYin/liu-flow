"""
同步 V0.2.0 PRD 与流程文档的字段名 / 架构变更：
- §12.4 步骤 7 移除「部署云函数」行
- §13.4 整段重写：3 张表（sessions / daily_goals / settings）+ CloudBase 内置 user
- §13.5 整段重写：RLS 用 owner_id + auth.uid() 函数
- §13.6 整段重写：云函数清单 → 「不部署」
- §18.2 验收清单调整

用 Python 直接 open().write()，避免 write 工具破坏全角标点。
"""

PATH = r"Q:\large_program\liu-flow\docs\prd_v0.2.0.md"

with open(PATH, "r", encoding="utf-8") as f:
    text = f.read()

# ============================================================
# 修改 1: §12.4 步骤 7 移除「部署云函数」
# ============================================================
old_1277 = "7. **部署云函数**：`sendCode` / `verifyLogin` / `syncSession` / `syncSettings` / `getUserData`"
new_1277 = "7. **不部署云函数**：V0.2.0 采用 CloudBase 内置 Auth + RLS 直连数据库，0 后端代码"
text = text.replace(old_1277, new_1277)

# ============================================================
# 修改 2: §13.4 整段重写（数据表设计）
# ============================================================
old_134 = """### 13.4 数据表设计（CloudBase PostgreSQL）

#### 13.4.1 users（用户表）

```sql
CREATE TABLE users (
  uid UUID PRIMARY KEY,
  phone VARCHAR(20) UNIQUE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_login_at TIMESTAMPTZ,
  nickname VARCHAR(50),
  avatar_url TEXT
);
CREATE INDEX idx_users_phone ON users(phone);
```

#### 13.4.2 sessions（专注记录表）

```sql
CREATE TABLE sessions (
  id VARCHAR(40) PRIMARY KEY,
  uid UUID NOT NULL REFERENCES users(uid),
  task TEXT,
  category VARCHAR(20),
  planned_duration INT,
  actual_duration INT,
  status VARCHAR(20),
  started_at TIMESTAMPTZ,
  ended_at TIMESTAMPTZ,
  hour INT,
  weekday INT,
  date VARCHAR(10),
  last_modified_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_sessions_uid_date ON sessions(uid, date);
CREATE INDEX idx_sessions_uid_started ON sessions(uid, started_at DESC);
```

#### 13.4.3 daily_goals（每日目标表）

```sql
CREATE TABLE daily_goals (
  uid UUID NOT NULL,
  date VARCHAR(10) NOT NULL,
  target INT NOT NULL,
  last_modified_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (uid, date)
);
```

#### 13.4.4 settings（个人设置表）

```sql
CREATE TABLE settings (
  uid UUID PRIMARY KEY,
  theme VARCHAR(20),
  dark_mode VARCHAR(20),
  default_focus_minutes INT,
  default_rest_minutes INT,
  sound_enabled BOOLEAN,
  vibration_enabled BOOLEAN,
  last_modified_at TIMESTAMPTZ
);
```"""

new_134 = """### 13.4 数据表设计（CloudBase PostgreSQL）

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
```"""

text = text.replace(old_134, new_134)

# ============================================================
# 修改 3: §13.5 整段重写（RLS 策略）
# ============================================================
old_135 = """### 13.5 安全规则（Row Level Security / RLS）

```sql
-- 用户只能访问自己的数据
ALTER TABLE sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "users_own_sessions" ON sessions
  FOR ALL
  USING (uid = current_setting('request.jwt.claims')::json->>'uid')
  WITH CHECK (uid = current_setting('request.jwt.claims')::json->>'uid');

-- 同理 daily_goals / settings
ALTER TABLE daily_goals ENABLE ROW LEVEL SECURITY;
CREATE POLICY "users_own_goals" ON daily_goals
  FOR ALL
  USING (uid = current_setting('request.jwt.claims')::json->>'uid')
  WITH CHECK (uid = current_setting('request.jwt.claims')::json->>'uid');

ALTER TABLE settings ENABLE ROW LEVEL SECURITY;
CREATE POLICY "users_own_settings" ON settings
  FOR ALL
  USING (uid = current_setting('request.jwt.claims')::json->>'uid')
  WITH CHECK (uid = current_setting('request.jwt.claims')::json->>'uid');
```"""

new_135 = """### 13.5 安全规则（Row Level Security / RLS）

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
- 客户端用 service_role key → `BYPASSRLS` 全权限（仅后端使用）"""

text = text.replace(old_135, new_135)

# ============================================================
# 修改 4: §13.6 整段重写（云函数清单 → 不部署）
# ============================================================
old_136 = """### 13.6 云函数清单

| 函数名 | 触发方式 | 入参 | 出参 |
|--------|---------|------|------|
| `sendCode` | HTTP | `{ phone }` | `{ success, errorCode }` |
| `verifyLogin` | HTTP | `{ phone, code }` | `{ success, token, user }` |
| `syncSession` | HTTP | `{ session }` | `{ success }` |
| `syncSettings` | HTTP | `{ settings }` | `{ success }` |
| `getUserData` | HTTP | - | `{ sessions, dailyGoals, settings }` |
| `batchMigrate` | HTTP | `{ sessions, dailyGoals, settings }` | `{ success, count }` |"""

new_136 = """### 13.6 后端函数：**不部署云函数**

按「不部署后端应用」原则，V0.2.0 不写任何云函数。客户端直接通过 SDK 调 CloudBase HTTP API，RLS 自动鉴权。

| 业务能力 | 原云函数方案 | V0.2.0 实际方案 |
|---------|------------|---------------|
| 发送验证码 | `sendCode` | CloudBase Auth 内置：客户端调 `POST /auth/v1/verification` |
| 验证码登录 | `verifyLogin` | CloudBase Auth 内置：客户端调 `POST /auth/v1/signin` |
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
- 客户端可见表结构（RLS 保护数据，但不能隐藏 schema）"""

text = text.replace(old_136, new_136)

# ============================================================
# 修改 5: §18.2 验收清单调整
# ============================================================
old_182 = """### 18.2 后端

- [ ] 腾讯云 SMS 短信模板通过审核
- [ ] CloudBase 环境开通 + 5 张表创建（users / sessions / daily_goals / settings / sms_codes）
- [ ] 云函数实现：`sendCode` / `verifyLogin` / `syncSession` / `syncSettings` / `getUserData` / `batchMigrate`
- [ ] RLS 策略验证：跨用户访问被拒
- [ ] 限流逻辑验证：1 分钟 1 次 / 1 小时 5 次 / 1 天 10 次"""

new_182 = """### 18.2 后端

- [ ] 腾讯云账号实名认证（个人/企业）
- [ ] CloudBase 环境开通，**地域 = 上海**，模式 = PostgreSQL
- [ ] Publishable Key 已保存到本地配置
- [ ] 短信登录方式已开启（控制台 → 身份认证 → 登录方式）
- [ ] 3 张业务表创建（`sessions` / `daily_goals` / `settings`）
- [ ] 抽象函数 `auth.uid()` / `auth.role()` 已创建
- [ ] RLS 启用 + 表级 GRANT + 4 套 Policy 每表齐备
- [ ] 端到端验证 7 步全部通过（详见流程文档 §7.2）
- [ ] 不部署任何云函数（0 后端代码）

**配套实操文档**：[`docs/腾讯云后端服务开通流程.md`](./腾讯云后端服务开通流程.md)"""

text = text.replace(old_182, new_182)

# ============================================================
# 写入
# ============================================================
with open(PATH, "w", encoding="utf-8") as f:
    f.write(text)

print(f"[ok] {PATH}")
print(f"     {len(text):,} chars, {len(text) / 1024:.1f} KB")
