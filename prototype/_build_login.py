"""
登录页 UI 生成脚本
- 直接 open(...).write() 绕过 write 工具的全角标点 + HTML 属性双引号污染
- 4 主题色：薄暮（默认） / 经典 / 静夜 / 森林
- 折中风格：主题色微调 8% + 启动动效
- 包含：splash.html（启动屏）、login.html（主登录页 + 协议模态）、migrate.html（数据迁移提示）、index_v2.html（入口）
"""

import os

ROOT = r"Q:\large_program\liu-flow\prototype"
os.makedirs(ROOT, exist_ok=True)


# 4 套主题色（含微调 8% 后的登录页强调色）
THEMES = {
    "twilight": {  # 薄暮（默认）
        "name": "薄暮",
        "primary": "#7A4E7C",
        "primary_lifted": "#8E5E91",       # 微调 8%（HSL 提亮）
        "on_primary": "#FFFFFF",
        "primary_container": "#E8C5E5",
        "on_primary_container": "#2A0E2C",
        "secondary_container": "#F0EBF0",
        "tertiary": "#D89BAE",
        "surface": "#F0EBF0",
        "surface_variant": "#E0D5E0",
        "surface_container": "#F8F4F8",
        "on_surface": "#2A1A2A",
        "on_surface_variant": "#6B5B6B",
        "outline": "#A89AA8",
        "outline_variant": "#D5C8D5",
        "scrim": "#000000",
        "ring_glow": "rgba(122, 78, 124, 0.18)",
    },
    "classic": {  # 经典
        "name": "经典",
        "primary": "#6750A4",
        "primary_lifted": "#7C5DBA",
        "on_primary": "#FFFFFF",
        "primary_container": "#EADDFF",
        "on_primary_container": "#21005D",
        "secondary_container": "#E8DEF8",
        "tertiary": "#7D5260",
        "surface": "#FEF7FF",
        "surface_variant": "#E7E0EC",
        "surface_container": "#F3EDF7",
        "on_surface": "#1C1B1F",
        "on_surface_variant": "#49454F",
        "outline": "#79747E",
        "outline_variant": "#CAC4D0",
        "scrim": "#000000",
        "ring_glow": "rgba(103, 80, 164, 0.20)",
    },
    "midnight": {  # 静夜
        "name": "静夜",
        "primary": "#4A6FA5",
        "primary_lifted": "#5C82BD",
        "on_primary": "#FFFFFF",
        "primary_container": "#D3E3FD",
        "on_primary_container": "#0E1B2C",
        "secondary_container": "#1E2A38",
        "tertiary": "#9CB6D8",
        "surface": "#0F1419",
        "surface_variant": "#1A2530",
        "surface_container": "#16202B",
        "on_surface": "#E3E3E3",
        "on_surface_variant": "#A8B5C2",
        "outline": "#5A6A7A",
        "outline_variant": "#2C3845",
        "scrim": "#000000",
        "ring_glow": "rgba(74, 111, 165, 0.25)",
    },
    "forest": {  # 森林
        "name": "森林",
        "primary": "#2D6A4F",
        "primary_lifted": "#3D8060",
        "on_primary": "#FFFFFF",
        "primary_container": "#A8D5BA",
        "on_primary_container": "#0F2A1B",
        "secondary_container": "#D4E5DA",
        "tertiary": "#74A88B",
        "surface": "#F5F1E8",
        "surface_variant": "#E5E0D0",
        "surface_container": "#FAF6EC",
        "on_surface": "#1B2D1F",
        "on_surface_variant": "#4F5C50",
        "outline": "#7A8A7C",
        "outline_variant": "#C8D0C9",
        "scrim": "#000000",
        "ring_glow": "rgba(45, 106, 79, 0.20)",
    },
}


# ============ 通用：Pixel 8/9 外框 + 状态栏 + 底部导航 ============

def frame_head(theme_key: str, title: str, extra_css: str = "") -> str:
    t = THEMES[theme_key]
    return f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{title}</title>
<script src="https://cdn.tailwindcss.com"></script>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;600;700&family=Roboto+Mono:wght@400;500&family=Noto+Sans+SC:wght@300;400;500;700&display=swap" rel="stylesheet">
<style>
:root {{
  --primary: {t['primary']};
  --primary-lifted: {t['primary_lifted']};
  --on-primary: {t['on_primary']};
  --primary-container: {t['primary_container']};
  --on-primary-container: {t['on_primary_container']};
  --secondary-container: {t['secondary_container']};
  --tertiary: {t['tertiary']};
  --surface: {t['surface']};
  --surface-variant: {t['surface_variant']};
  --surface-container: {t['surface_container']};
  --on-surface: {t['on_surface']};
  --on-surface-variant: {t['on_surface_variant']};
  --outline: {t['outline']};
  --outline-variant: {t['outline_variant']};
  --ring-glow: {t['ring_glow']};
}}
* {{ box-sizing: border-box; margin: 0; padding: 0; }}
html, body {{
  font-family: 'Roboto', 'Noto Sans SC', system-ui, sans-serif;
  background: #0a0a0a;
  color: var(--on-surface);
  -webkit-font-smoothing: antialiased;
  min-height: 100vh;
}}
.mono {{ font-family: 'Roboto Mono', 'SF Mono', monospace; }}

/* Pixel 8/9 外框 */
.android-frame {{
  width: 412px;
  height: 892px;
  background: #1a1a1a;
  border-radius: 48px;
  padding: 8px;
  box-shadow:
    0 0 0 2px rgba(255,255,255,0.05),
    0 0 0 6px #2a2a2a,
    0 30px 80px -20px rgba(0,0,0,0.6),
    0 50px 100px -20px var(--ring-glow);
  position: relative;
}}
.android-screen {{
  width: 100%; height: 100%;
  background: var(--surface);
  border-radius: 40px;
  overflow: hidden;
  position: relative;
}}
.punch-hole {{
  position: absolute;
  top: 8px; left: 50%;
  transform: translateX(-50%);
  width: 14px; height: 14px;
  background: #000;
  border-radius: 50%;
  z-index: 50;
}}

/* 状态栏 24dp */
.status-bar {{
  height: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 500;
  color: var(--on-surface);
}}
.status-bar .icons {{ display: flex; gap: 6px; align-items: center; font-size: 13px; }}

/* 底部 3-button 导航条 */
.nav-bar {{
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 56px;
  background: var(--surface);
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid var(--outline-variant);
}}
.nav-bar .nav-item {{
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: var(--on-surface-variant);
}}
.nav-bar .nav-item .icon {{
  font-size: 20px;
}}
{extra_css}
</style>
</head>
<body>
"""


def frame_tail(theme_key: str, body_class: str = "") -> str:
    t = THEMES[theme_key]
    return f"""
</body>
</html>"""


def status_bar(theme_key: str, time_str: str = "2:47", with_signal: bool = True) -> str:
    return f"""
  <div class="status-bar">
    <div>{time_str}</div>
    <div class="icons">
      <i class="fa-solid fa-signal"></i>
      <i class="fa-solid fa-wifi"></i>
      <i class="fa-solid fa-battery-three-quarters"></i>
    </div>
  </div>
"""


# ============ 启动屏 splash.html ============

SPLASH_THEME = "twilight"  # 启动屏固定用薄暮主题


def build_splash() -> str:
    t = THEMES[SPLASH_THEME]
    extra = f"""
.splash-screen {{
  width: 100%; height: 100%;
  background: linear-gradient(180deg, var(--surface) 0%, var(--secondary-container) 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  animation: fadeIn 800ms ease-out;
}}
@keyframes fadeIn {{
  from {{ opacity: 0; }}
  to {{ opacity: 1; }}
}}
.ring-container {{
  width: 200px;
  height: 200px;
  position: relative;
  margin-bottom: 48px;
}}
.ring-container svg {{
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  filter: drop-shadow(0 0 32px var(--ring-glow));
}}
.ring-bg {{
  fill: none;
  stroke: var(--outline-variant);
  stroke-width: 4;
}}
.ring-progress {{
  fill: none;
  stroke: var(--primary);
  stroke-width: 5;
  stroke-linecap: round;
  stroke-dasharray: 565.48;
  stroke-dashoffset: 565.48;
  animation: ringSweep 1500ms cubic-bezier(0.4, 0, 0.2, 1) forwards;
}}
@keyframes ringSweep {{
  0% {{ stroke-dashoffset: 565.48; }}
  100% {{ stroke-dashoffset: 0; }}
}}
.brand-text {{
  font-family: 'Noto Sans SC', serif;
  font-size: 64px;
  font-weight: 300;
  color: var(--on-surface);
  letter-spacing: 8px;
  margin-bottom: 12px;
  animation: brandFade 800ms 200ms ease-out both;
}}
@keyframes brandFade {{
  from {{ opacity: 0; transform: translateY(8px); }}
  to {{ opacity: 1; transform: translateY(0); }}
}}
.brand-sub {{
  font-size: 13px;
  color: var(--on-surface-variant);
  letter-spacing: 4px;
  font-weight: 300;
  animation: brandFade 800ms 400ms ease-out both;
}}
.bg-glow {{
  position: absolute;
  top: 30%; left: 50%;
  transform: translate(-50%, -50%);
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, var(--ring-glow) 0%, transparent 70%);
  border-radius: 50%;
  z-index: 0;
}}
.content {{
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}}
"""
    html = frame_head(SPLASH_THEME, "流 Flow · 启动", extra)
    html += f"""
<body>
  <div style="display:flex;justify-content:center;align-items:center;min-height:100vh;padding:32px 0;">
    <div class="android-frame">
      <div class="android-screen">
        <div class="punch-hole"></div>
        {status_bar(SPLASH_THEME, "2:47")}
        <div class="splash-screen">
          <div class="bg-glow"></div>
          <div class="content">
            <div class="ring-container">
              <svg viewBox="0 0 200 200">
                <circle class="ring-bg" cx="100" cy="100" r="90"></circle>
                <circle class="ring-progress" cx="100" cy="100" r="90"></circle>
              </svg>
            </div>
            <div class="brand-text">流</div>
            <div class="brand-sub">F L O W</div>
          </div>
        </div>
        {nav_bar(SPLASH_THEME)}
      </div>
    </div>
  </div>
</body>
</html>"""
    return html


def nav_bar(theme_key: str) -> str:
    return f"""
  <div class="nav-bar">
    <div class="nav-item">
      <div class="icon"><i class="fa-solid fa-droplet"></i></div>
      <div>专注</div>
    </div>
    <div class="nav-item">
      <div class="icon"><i class="fa-regular fa-clock"></i></div>
      <div>记录</div>
    </div>
    <div class="nav-item">
      <div class="icon"><i class="fa-regular fa-user"></i></div>
      <div>我的</div>
    </div>
  </div>
"""


# ============ 登录页 login.html ============

def build_login() -> str:
    t = THEMES[SPLASH_THEME]
    extra = f"""
body {{
  background: #0a0a0a;
}}
.login-screen {{
  width: 100%; height: 100%;
  background: var(--surface);
  position: relative;
  display: flex;
  flex-direction: column;
  padding-top: 56px;
  padding-bottom: 80px;
  padding-left: 0;
  padding-right: 0;
  animation: screenFadeIn 600ms ease-out;
}}
@keyframes screenFadeIn {{
  from {{ opacity: 0; transform: translateY(8px); }}
  to {{ opacity: 1; transform: translateY(0); }}
}}

/* logo 区（带光晕） */
.logo-area {{
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 32px;
  margin-bottom: 40px;
  position: relative;
}}
.logo-ring {{
  width: 96px;
  height: 96px;
  position: relative;
  margin-bottom: 16px;
  filter: drop-shadow(0 0 32px var(--ring-glow));
}}
.logo-ring svg {{ width: 100%; height: 100%; transform: rotate(-90deg); }}
.logo-ring .bg {{ fill: none; stroke: var(--outline-variant); stroke-width: 3; }}
.logo-ring .fg {{
  fill: none;
  stroke: var(--primary);
  stroke-width: 4;
  stroke-linecap: round;
  stroke-dasharray: 263.89;
  animation: ringBreath 2.4s ease-in-out infinite;
}}
@keyframes ringBreath {{
  0%, 100% {{ stroke-dashoffset: 263.89; }}
  50% {{ stroke-dashoffset: 131.95; }}
}}
.logo-glow {{
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, var(--ring-glow) 0%, transparent 70%);
  border-radius: 50%;
  z-index: 0;
}}
.logo-area > * {{ position: relative; z-index: 1; }}

.brand-text-lg {{
  font-family: 'Noto Sans SC', serif;
  font-size: 28px;
  font-weight: 400;
  color: var(--on-surface);
  letter-spacing: 4px;
  margin-bottom: 8px;
}}
.brand-text-en {{
  font-size: 11px;
  color: var(--on-surface-variant);
  letter-spacing: 6px;
  font-weight: 300;
}}

/* 标题与副标题 */
.title-block {{
  padding: 0 32px;
  margin-bottom: 32px;
  text-align: left;
}}
.title-block .h1 {{
  font-size: 24px;
  font-weight: 500;
  color: var(--on-surface);
  margin-bottom: 8px;
  line-height: 1.3;
}}
.title-block .h2 {{
  font-size: 14px;
  font-weight: 400;
  color: var(--on-surface-variant);
  line-height: 1.5;
}}

/* Step 1：手机号输入 */
.step {{ display: none; flex-direction: column; flex: 1; padding: 0 24px; }}
.step.active {{ display: flex; }}

.phone-row {{
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1.5px solid var(--outline-variant);
  padding: 14px 0;
  margin-bottom: 32px;
  transition: border-color 200ms;
}}
.phone-row:focus-within {{ border-bottom-color: var(--primary); }}
.phone-prefix {{
  font-size: 16px;
  font-weight: 500;
  color: var(--on-surface);
  padding-right: 12px;
  border-right: 1px solid var(--outline-variant);
}}
.phone-input {{
  flex: 1;
  font-size: 18px;
  font-weight: 400;
  font-family: 'Roboto Mono', monospace;
  color: var(--on-surface);
  background: transparent;
  border: none;
  outline: none;
  letter-spacing: 1.5px;
}}
.phone-input::placeholder {{ color: var(--on-surface-variant); font-weight: 300; letter-spacing: 0.5px; font-family: 'Roboto', sans-serif; }}

/* 主按钮 */
.btn-primary {{
  width: 100%;
  height: 52px;
  border-radius: 26px;
  background: var(--primary);
  color: var(--on-primary);
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 200ms;
  box-shadow: 0 2px 8px -2px var(--ring-glow);
}}
.btn-primary:hover {{ background: var(--primary-lifted); }}
.btn-primary:active {{ transform: scale(0.98); }}
.btn-primary:disabled {{ background: var(--outline-variant); cursor: not-allowed; box-shadow: none; }}

.btn-primary.lifted {{
  background: var(--primary-container);
  color: var(--on-primary-container);
}}

/* Step 2：验证码 */
.code-target {{
  font-size: 14px;
  color: var(--on-surface-variant);
  margin-bottom: 24px;
  text-align: center;
}}
.code-target .num {{
  font-family: 'Roboto Mono', monospace;
  color: var(--on-surface);
  font-weight: 500;
  letter-spacing: 1px;
}}
.code-target .edit {{
  color: var(--primary);
  font-size: 12px;
  margin-left: 8px;
  cursor: pointer;
  text-decoration: underline;
}}
.code-boxes {{
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-bottom: 32px;
}}
.code-box {{
  width: 48px;
  height: 56px;
  border: 1.5px solid var(--outline-variant);
  border-radius: 10px;
  background: transparent;
  font-size: 22px;
  font-family: 'Roboto Mono', monospace;
  font-weight: 500;
  text-align: center;
  color: var(--on-surface);
  outline: none;
  transition: all 150ms;
}}
.code-box:focus {{
  border-color: var(--primary);
  border-width: 2px;
  background: var(--primary-container);
  color: var(--on-primary-container);
}}
.code-box.error {{
  border-color: #BA1A1A;
  animation: shake 0.4s;
}}
@keyframes shake {{
  0%, 100% {{ transform: translateX(0); }}
  25% {{ transform: translateX(-6px); }}
  75% {{ transform: translateX(6px); }}
}}
.resend-row {{
  text-align: center;
  font-size: 13px;
  color: var(--on-surface-variant);
  margin-bottom: 32px;
}}
.resend-row .timer {{
  font-family: 'Roboto Mono', monospace;
  color: var(--on-surface-variant);
}}
.resend-row .resend {{
  color: var(--primary);
  cursor: pointer;
  font-weight: 500;
}}

/* 协议 */
.agreement {{
  margin-top: auto;
  padding: 16px 32px 0;
  text-align: center;
  font-size: 11px;
  color: var(--on-surface-variant);
  line-height: 1.6;
}}
.agreement a {{
  color: var(--on-surface-variant);
  text-decoration: underline;
}}

/* 协议模态弹窗 */
.modal {{
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: none;
  z-index: 100;
  align-items: flex-end;
  animation: modalFade 200ms;
}}
@keyframes modalFade {{ from {{ opacity: 0; }} to {{ opacity: 1; }} }}
.modal.active {{ display: flex; }}
.sheet {{
  width: 100%;
  background: var(--surface);
  border-top-left-radius: 28px;
  border-top-right-radius: 28px;
  max-height: 70%;
  display: flex;
  flex-direction: column;
  animation: sheetUp 300ms cubic-bezier(0.4, 0, 0.2, 1);
}}
@keyframes sheetUp {{
  from {{ transform: translateY(100%); }}
  to {{ transform: translateY(0); }}
}}
.sheet-handle {{
  width: 32px;
  height: 4px;
  background: var(--outline-variant);
  border-radius: 2px;
  margin: 12px auto 0;
}}
.sheet-title {{
  font-size: 18px;
  font-weight: 500;
  color: var(--on-surface);
  padding: 16px 24px 8px;
}}
.sheet-body {{
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px 24px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--on-surface-variant);
}}
.sheet-body h3 {{ color: var(--on-surface); font-size: 14px; font-weight: 500; margin: 16px 0 8px; }}
.sheet-body p {{ margin-bottom: 12px; }}
.sheet-body ul {{ padding-left: 20px; margin-bottom: 12px; }}
.sheet-body li {{ margin-bottom: 6px; }}
"""
    html = frame_head(SPLASH_THEME, "登录 · 流 Flow", extra)
    html += """
<body>
  <div style="display:flex;justify-content:center;align-items:center;min-height:100vh;padding:32px 0;">
    <div class="android-frame">
      <div class="android-screen">
        <div class="punch-hole"></div>
"""
    html += status_bar(SPLASH_THEME, "2:48")
    html += f"""
        <div class="login-screen">
          <!-- logo 区（带主题色光晕 + 圆环呼吸动效） -->
          <div class="logo-area">
            <div class="logo-glow"></div>
            <div class="logo-ring">
              <svg viewBox="0 0 100 100">
                <circle class="bg" cx="50" cy="50" r="42"></circle>
                <circle class="fg" cx="50" cy="50" r="42"></circle>
              </svg>
            </div>
            <div class="brand-text-lg">流 Flow</div>
            <div class="brand-text-en">F O C U S &nbsp;·&nbsp; F L O W</div>
          </div>

          <!-- Step 1：手机号输入 -->
          <div class="step active" id="step-1">
            <div class="title-block">
              <div class="h1">欢迎回到 流 Flow</div>
              <div class="h2">登录后数据云端同步，多端互通</div>
            </div>
            <div style="padding: 0 24px;">
              <div class="phone-row">
                <span class="phone-prefix">+86</span>
                <input class="phone-input" type="tel" maxlength="11" placeholder="请输入手机号" value="138 0000 0000" />
              </div>
              <button class="btn-primary" onclick="goStep(2)">获取验证码</button>
            </div>
          </div>

          <!-- Step 2：验证码输入 -->
          <div class="step" id="step-2">
            <div class="title-block">
              <div class="h1">输入验证码</div>
              <div class="h2">已发送 6 位数字验证码</div>
            </div>
            <div class="code-target">
              验证码已发送至 <span class="num">138 **** 0000</span>
              <span class="edit" onclick="goStep(1)">修改</span>
            </div>
            <div style="padding: 0 16px;">
              <div class="code-boxes">
                <input class="code-box" type="tel" maxlength="1" value="1" />
                <input class="code-box" type="tel" maxlength="1" value="2" />
                <input class="code-box" type="tel" maxlength="1" value="3" />
                <input class="code-box" type="tel" maxlength="1" value="4" />
                <input class="code-box" type="tel" maxlength="1" value="" />
                <input class="code-box" type="tel" maxlength="1" value="" />
              </div>
              <div class="resend-row">
                <span class="timer"><span class="mono">59</span>s 后重新发送</span>
              </div>
              <button class="btn-primary">登录</button>
            </div>
          </div>

          <!-- 协议 -->
          <div class="agreement">
            登录即代表同意<a href="#" onclick="showAgreement(); return false;">《服务协议》</a>
            <a href="#" onclick="showAgreement(); return false;">《隐私政策》</a>
          </div>
        </div>

        <!-- 协议模态弹窗 -->
        <div class="modal" id="agreement-modal">
          <div class="sheet">
            <div class="sheet-handle"></div>
            <div class="sheet-title">服务协议与隐私政策</div>
            <div class="sheet-body">
              <h3>一、服务说明</h3>
              <p>流 Flow（以下简称"本服务"）是一款极简的深度工作计时器，由个人开发者运营。本服务通过手机号短信验证码完成身份验证，所有专注记录可选择同步至云端。</p>
              <h3>二、我们收集的信息</h3>
              <ul>
                <li>手机号：用于登录身份验证</li>
                <li>专注记录：包括任务名、时长、分类、时间戳</li>
                <li>个人偏好：包括主题色、默认时长、声音设置</li>
              </ul>
              <h3>三、信息使用</h3>
              <p>上述信息仅用于为您提供专注记录与多端同步服务，不会用于广告投放、用户画像或转售给第三方。</p>
              <h3>四、您的权利</h3>
              <ul>
                <li>随时退出登录，本地与云端数据保留</li>
                <li>随时导出数据为 JSON / CSV 文件</li>
                <li>随时切换账号或修改个人偏好</li>
              </ul>
              <h3>五、短信服务</h3>
              <p>本服务使用腾讯云短信服务下发验证码，可能产生 0.045 元/条的费用，由开发者承担，不向用户收费。</p>
              <p style="margin-top:24px; font-size: 11px; color: var(--on-surface-variant);">最后更新：2026-08-25</p>
            </div>
          </div>
        </div>
"""
    html += nav_bar(SPLASH_THEME)
    html += f"""
      </div>
    </div>
  </div>
  <script>
    function goStep(n) {{
      document.querySelectorAll('.step').forEach(s => s.classList.remove('active'));
      document.getElementById('step-' + n).classList.add('active');
    }}
    function showAgreement() {{
      document.getElementById('agreement-modal').classList.add('active');
    }}
    document.getElementById('agreement-modal').addEventListener('click', function(e) {{
      if (e.target === this) this.classList.remove('active');
    }});

    // 验证码自动跳格
    document.querySelectorAll('.code-box').forEach((box, i, boxes) => {{
      box.addEventListener('input', function(e) {{
        if (e.target.value && i < boxes.length - 1) boxes[i + 1].focus();
      }});
      box.addEventListener('keydown', function(e) {{
        if (e.key === 'Backspace' && !e.target.value && i > 0) boxes[i - 1].focus();
      }});
    }});
  </script>
</body>
</html>"""
    return html


# ============ 数据迁移提示 migrate.html ============

def build_migrate() -> str:
    t = THEMES[SPLASH_THEME]
    extra = f"""
.migrate-screen {{
  width: 100%; height: 100%;
  background: var(--surface);
  display: flex;
  flex-direction: column;
  position: relative;
  padding-top: 56px;
}}
.migrate-content {{
  flex: 1;
  padding: 64px 32px 0;
  display: flex;
  flex-direction: column;
}}
.migrate-icon {{
  width: 96px;
  height: 96px;
  background: var(--primary-container);
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: var(--on-primary-container);
  margin-bottom: 32px;
  box-shadow: 0 4px 20px -8px var(--ring-glow);
}}
.migrate-title {{
  font-size: 26px;
  font-weight: 500;
  color: var(--on-surface);
  margin-bottom: 12px;
  line-height: 1.3;
}}
.migrate-desc {{
  font-size: 14px;
  color: var(--on-surface-variant);
  line-height: 1.7;
  margin-bottom: 32px;
}}
.migrate-stats {{
  background: var(--surface-container);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 32px;
}}
.stat-row {{
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}}
.stat-row .label {{
  color: var(--on-surface-variant);
  display: flex;
  align-items: center;
  gap: 8px;
}}
.stat-row .value {{
  color: var(--on-surface);
  font-family: 'Roboto Mono', monospace;
  font-weight: 500;
}}
.stat-divider {{ height: 1px; background: var(--outline-variant); margin: 4px 0; }}
.migrate-actions {{
  margin-top: auto;
  padding: 24px 0 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}}
.btn-secondary {{
  width: 100%;
  height: 48px;
  border-radius: 24px;
  background: transparent;
  color: var(--primary);
  font-size: 14px;
  font-weight: 500;
  border: 1.5px solid var(--outline-variant);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}}
.btn-secondary:hover {{ border-color: var(--primary); }}
"""
    html = frame_head(SPLASH_THEME, "数据迁移 · 流 Flow", extra)
    html += f"""
<body>
  <div style="display:flex;justify-content:center;align-items:center;min-height:100vh;padding:32px 0;">
    <div class="android-frame">
      <div class="android-screen">
        <div class="punch-hole"></div>
"""
    html += status_bar(SPLASH_THEME, "2:48")
    html += f"""
        <div class="migrate-screen">
          <div class="migrate-content">
            <div class="migrate-icon"><i class="fa-solid fa-cloud-arrow-up"></i></div>
            <div class="migrate-title">检测到本地有<br/>V0.1.0 数据</div>
            <div class="migrate-desc">V0.2.0 起启用云端同步，登录后会自动把本地记录上传到云端。换手机、卸载重装都能找回。</div>
            <div class="migrate-stats">
              <div class="stat-row">
                <div class="label"><i class="fa-regular fa-circle-check" style="color: var(--primary);"></i> 专注记录</div>
                <div class="value">28 条</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-row">
                <div class="label"><i class="fa-regular fa-calendar" style="color: var(--tertiary);"></i> 连续天数</div>
                <div class="value">12 天</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-row">
                <div class="label"><i class="fa-regular fa-clock" style="color: var(--tertiary);"></i> 累计时长</div>
                <div class="value">14.2 h</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-row">
                <div class="label"><i class="fa-solid fa-sliders" style="color: var(--on-surface-variant);"></i> 个人设置</div>
                <div class="value">6 项</div>
              </div>
            </div>
            <div class="migrate-actions">
              <button class="btn-primary">立即登录并迁移</button>
              <button class="btn-secondary">稍后再说</button>
            </div>
          </div>
        </div>
{nav_bar(SPLASH_THEME)}
      </div>
    </div>
  </div>
</body>
</html>"""
    return html


# ============ 入口 index_v2.html ============

def build_index() -> str:
    """入口：手机壳 iframe 展示所有 V0.2.0 新增页面 + 4 主题切换"""
    extra = f"""
body {{ background: #0a0a0a; color: #fafafa; }}
header {{
  text-align: center;
  padding: 40px 24px 24px;
}}
header h1 {{
  font-size: 32px;
  font-weight: 400;
  color: #fafafa;
  margin-bottom: 8px;
  letter-spacing: 4px;
}}
header .sub {{
  font-size: 13px;
  color: #888;
  letter-spacing: 4px;
  font-weight: 300;
}}
.theme-tabs {{
  display: flex;
  justify-content: center;
  gap: 8px;
  padding: 16px;
}}
.theme-tab {{
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  background: rgba(255,255,255,0.05);
  color: #aaa;
  cursor: pointer;
  transition: all 200ms;
  border: 1px solid transparent;
}}
.theme-tab.active {{
  background: rgba(122, 78, 124, 0.2);
  color: #fafafa;
  border-color: #7A4E7C;
}}
.theme-tab:hover {{ background: rgba(255,255,255,0.1); }}

.page-tabs {{
  display: flex;
  justify-content: center;
  gap: 4px;
  padding: 8px 16px 24px;
  flex-wrap: wrap;
}}
.page-tab {{
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 11px;
  background: rgba(255,255,255,0.05);
  color: #aaa;
  cursor: pointer;
  transition: all 200ms;
  border: none;
  font-family: inherit;
}}
.page-tab.active {{
  background: rgba(255,255,255,0.95);
  color: #0a0a0a;
}}

.showcase {{
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 0 0 40px;
  min-height: 1000px;
}}
.showcase iframe {{
  width: 412px;
  height: 892px;
  border: none;
  border-radius: 48px;
  box-shadow:
    0 0 0 2px rgba(255,255,255,0.05),
    0 0 0 6px #2a2a2a,
    0 30px 80px -20px rgba(0,0,0,0.6);
  background: transparent;
}}

footer {{
  text-align: center;
  padding: 40px 24px;
  color: #666;
  font-size: 12px;
}}
footer a {{ color: #999; text-decoration: none; }}
"""
    html = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>流 Flow · V0.2.0 登录原型</title>
<script src="https://cdn.tailwindcss.com"></script>
<style>
{extra}
</style>
</head>
<body>
<header>
  <h1>流 Flow</h1>
  <div class="sub">V 0 . 2 . 0 &nbsp; · &nbsp; 登录原型</div>
</header>

<div class="theme-tabs">
  <div class="theme-tab active" data-theme="twilight">薄暮</div>
  <div class="theme-tab" data-theme="classic">经典</div>
  <div class="theme-tab" data-theme="midnight">静夜</div>
  <div class="theme-tab" data-theme="forest">森林</div>
</div>

<div class="page-tabs">
  <button class="page-tab active" data-page="splash.html">启动屏</button>
  <button class="page-tab" data-page="login.html">登录</button>
  <button class="page-tab" data-page="migrate.html">数据迁移</button>
</div>

<div class="showcase">
  <iframe id="preview" src="splash.html"></iframe>
</div>

<footer>
  <div>本原型为 V0.2.0 PRD 设计稿 · 配套 <a href="../docs/prd_v0.2.0.md">PRD v0.2.0</a></div>
  <div style="margin-top: 8px;">后端：腾讯云 SMS + 腾讯云 CloudBase</div>
</footer>

<script>
  // 主题切换
  const themeTabs = document.querySelectorAll('.theme-tab');
  const pageTabs = document.querySelectorAll('.page-tab');
  const preview = document.getElementById('preview');
  let currentTheme = 'twilight';
  let currentPage = 'splash.html';

  function updatePreview() {{
    const sep = preview.src.includes('?') ? '&' : '?';
    preview.src = currentPage + sep + 'theme=' + currentTheme;
  }}

  themeTabs.forEach(tab => {{
    tab.addEventListener('click', () => {{
      themeTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      currentTheme = tab.dataset.theme;
      updatePreview();
    }});
  }});

  pageTabs.forEach(tab => {{
    tab.addEventListener('click', () => {{
      pageTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      currentPage = tab.dataset.page;
      updatePreview();
    }});
  }});

  // 从 URL hash 读取初始页面
  const hash = window.location.hash.replace('#', '');
  if (hash) {{
    const target = document.querySelector('[data-page="' + hash + '"]');
    if (target) target.click();
  }}
</script>
</body>
</html>"""
    return html


# ============ 写入所有文件 ============

if __name__ == "__main__":
    files = {
        "splash.html": build_splash(),
        "login.html": build_login(),
        "migrate.html": build_migrate(),
        "index_v2.html": build_index(),
    }
    for name, content in files.items():
        path = os.path.join(ROOT, name)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"  [ok] {name}: {len(content):,} chars")
    print(f"\n[完成] 4 个文件写入 {ROOT}")
