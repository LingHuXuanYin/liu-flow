"""
登录页 UI 生成脚本
- 直接 open(...).write() 绕过 write 工具的全角标点 + HTML 属性双引号污染
- 4 主题色：薄暮（默认） / 经典 / 静夜 / 森林
- 折中风格：主题色微调 8% + 启动动效
- 包含：splash.html（启动屏）、login.html（主登录页 + 协议模态）、migrate.html（数据迁移提示）、index_v2.html（入口）
"""

import os

ROOT = r"Q:\large_program\liu-flow\prototype\pages"
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
/* === Splash 启动屏 2.0（动效升级）=== */

.splash-screen {{
  width: 100%; height: 100%;
  background:
    radial-gradient(circle at 25% 18%, var(--primary-container) 0%, transparent 45%),
    radial-gradient(circle at 78% 82%, var(--tertiary) 0%, transparent 50%),
    var(--surface);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  animation: splashIn 800ms ease-out;
}}
@keyframes splashIn {{
  from {{ opacity: 0; transform: scale(0.98); }}
  to {{ opacity: 1; transform: scale(1); }}
}}

/* 背景 8s hue-rotate 呼吸 + 1.02 缩放循环 */
.splash-screen::before {{
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 50%, var(--ring-glow) 0%, transparent 60%);
  animation: bgPulse 8s ease-in-out infinite;
  pointer-events: none;
}}
@keyframes bgPulse {{
  0%, 100% {{ opacity: 0.6; transform: scale(1); }}
  50% {{ opacity: 1; transform: scale(1.08); }}
}}

/* === 浮动粒子（8 个）=== */
.particle {{
  position: absolute;
  border-radius: 50%;
  background: var(--primary);
  pointer-events: none;
  opacity: 0;
  animation: float 6s ease-in-out infinite;
}}
.particle.p1 {{ width: 6px; height: 6px; top: 18%; left: 22%; animation-delay: 0s; }}
.particle.p2 {{ width: 4px; height: 4px; top: 24%; left: 78%; animation-delay: 0.6s; background: var(--tertiary); }}
.particle.p3 {{ width: 5px; height: 5px; top: 64%; left: 18%; animation-delay: 1.2s; background: var(--tertiary); }}
.particle.p4 {{ width: 7px; height: 7px; top: 72%; left: 80%; animation-delay: 1.8s; }}
.particle.p5 {{ width: 3px; height: 3px; top: 38%; left: 12%; animation-delay: 2.4s; background: var(--tertiary); }}
.particle.p6 {{ width: 4px; height: 4px; top: 48%; left: 88%; animation-delay: 3.0s; }}
.particle.p7 {{ width: 5px; height: 5px; top: 82%; left: 42%; animation-delay: 3.6s; background: var(--tertiary); }}
.particle.p8 {{ width: 3px; height: 3px; top: 14%; left: 50%; animation-delay: 4.2s; }}
@keyframes float {{
  0%, 100% {{ opacity: 0; transform: translate(0, 0) scale(0.5); }}
  20% {{ opacity: 0.6; }}
  50% {{ opacity: 0.9; transform: translate(0, -24px) scale(1); }}
  80% {{ opacity: 0.4; }}
}}

/* === 多层圆环 === */
.ring-stage {{
  position: relative;
  width: 280px;
  height: 280px;
  margin-bottom: 32px;
}}

/* 主圆环（最外） */
.ring-main {{
  position: absolute;
  inset: 0;
  animation: spinClockwise 1.6s linear infinite;
}}
.ring-main svg {{ width: 100%; height: 100%; filter: drop-shadow(0 0 18px var(--ring-glow)); }}

/* 副圆环（中） */
.ring-sub {{
  position: absolute;
  inset: 30px;
  animation: spinCounter 2.4s linear infinite;
}}
.ring-sub svg {{ width: 100%; height: 100%; }}

/* 内圆环 */
.ring-inner {{
  position: absolute;
  inset: 70px;
  animation: spinClockwise 3.2s linear infinite;
}}
.ring-inner svg {{ width: 100%; height: 100%; }}

/* 8 个外点 */
.ring-dots {{
  position: absolute;
  inset: 0;
  animation: spinClockwise 6s linear infinite;
}}
.ring-dots span {{
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  background: var(--primary);
  border-radius: 50%;
  transform-origin: 0 0;
  box-shadow: 0 0 12px var(--ring-glow);
}}
.ring-dots span:nth-child(1) {{ transform: translate(-50%, -50%) rotate(0deg) translateY(-130px); }}
.ring-dots span:nth-child(2) {{ transform: translate(-50%, -50%) rotate(45deg) translateY(-130px); background: var(--tertiary); width: 6px; height: 6px; }}
.ring-dots span:nth-child(3) {{ transform: translate(-50%, -50%) rotate(90deg) translateY(-130px); }}
.ring-dots span:nth-child(4) {{ transform: translate(-50%, -50%) rotate(135deg) translateY(-130px); background: var(--tertiary); width: 6px; height: 6px; }}
.ring-dots span:nth-child(5) {{ transform: translate(-50%, -50%) rotate(180deg) translateY(-130px); }}
.ring-dots span:nth-child(6) {{ transform: translate(-50%, -50%) rotate(225deg) translateY(-130px); background: var(--tertiary); width: 6px; height: 6px; }}
.ring-dots span:nth-child(7) {{ transform: translate(-50%, -50%) rotate(270deg) translateY(-130px); }}
.ring-dots span:nth-child(8) {{ transform: translate(-50%, -50%) rotate(315deg) translateY(-130px); background: var(--tertiary); width: 6px; height: 6px; }}

@keyframes spinClockwise {{ from {{ transform: rotate(0deg); }} to {{ transform: rotate(360deg); }} }}
@keyframes spinCounter {{ from {{ transform: rotate(360deg); }} to {{ transform: rotate(0deg); }} }}

/* SVG 描边通用 */
.circle-track {{
  fill: none;
  stroke: var(--outline-variant);
  stroke-width: 2;
  opacity: 0.4;
}}
.circle-prog {{
  fill: none;
  stroke: var(--primary);
  stroke-linecap: round;
}}
.ring-main .circle-prog {{ stroke-width: 4; }}
.ring-sub .circle-prog {{ stroke-width: 3; stroke: var(--tertiary); }}
.ring-inner .circle-prog {{ stroke-width: 2.5; }}

/* 主圆环 sweep 1.6s */
.ring-main .circle-prog {{
  stroke-dasharray: 565.48;
  stroke-dashoffset: 565.48;
  animation: ringSweep 1.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}}
@keyframes ringSweep {{
  0% {{ stroke-dashoffset: 565.48; }}
  100% {{ stroke-dashoffset: 0; }}
}}

/* 副圆环 dashed 流动 */
.ring-sub .circle-prog {{
  stroke-dasharray: 8 6;
  stroke-dashoffset: 0;
  animation: dashFlow 2s linear infinite;
}}
@keyframes dashFlow {{
  to {{ stroke-dashoffset: -56; }}
}}

/* 内圆环填充式 */
.ring-inner .circle-prog {{
  stroke-dasharray: 219.91;
  stroke-dashoffset: 219.91;
  animation: innerSweep 3.2s cubic-bezier(0.4, 0, 0.2, 1) 200ms forwards;
}}
@keyframes innerSweep {{
  0% {{ stroke-dashoffset: 219.91; }}
  100% {{ stroke-dashoffset: 0; }}
}}

/* === 中心「流」字 stroke 描边 === */
.brand-stage {{
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
}}
.brand-stroke-svg {{
  width: 96px;
  height: 96px;
  margin-bottom: 12px;
}}
.brand-stroke-svg text {{
  font-family: 'Noto Sans SC', serif;
  font-size: 84px;
  font-weight: 300;
  text-anchor: middle;
  dominant-baseline: central;
  fill: transparent;
  stroke: var(--on-surface);
  stroke-width: 1.2;
  stroke-dasharray: 220;
  stroke-dashoffset: 220;
  animation: strokeDraw 1.6s ease-out 600ms forwards, fillIn 600ms ease-in 2000ms forwards;
}}
@keyframes strokeDraw {{ to {{ stroke-dashoffset: 0; }} }}
@keyframes fillIn {{
  to {{ fill: var(--on-surface); stroke-opacity: 0.4; }}
}}

/* F L O W 逐字 fly in */
.brand-sub {{
  display: flex;
  gap: 10px;
  margin-top: 4px;
  font-family: 'Inter', sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: var(--on-surface-variant);
  letter-spacing: 6px;
}}
.brand-sub span {{
  display: inline-block;
  opacity: 0;
  transform: translateY(10px);
  animation: charIn 500ms cubic-bezier(0.4, 0, 0.2, 1) forwards;
}}
.brand-sub span:nth-child(1) {{ animation-delay: 1.6s; }}
.brand-sub span:nth-child(2) {{ animation-delay: 1.7s; }}
.brand-sub span:nth-child(3) {{ animation-delay: 1.8s; }}
.brand-sub span:nth-child(4) {{ animation-delay: 1.9s; }}
.brand-sub span:nth-child(5) {{ animation-delay: 2.0s; }}
@keyframes charIn {{
  to {{ opacity: 1; transform: translateY(0); }}
}}

/* === 进度条（底部弧线）=== */
.progress-bar {{
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 2px;
  background: var(--outline-variant);
  border-radius: 1px;
  overflow: hidden;
  opacity: 0;
  animation: progressIn 400ms ease-out 1.2s forwards;
}}
@keyframes progressIn {{ to {{ opacity: 1; }} }}
.progress-fill {{
  height: 100%;
  width: 0;
  background: linear-gradient(90deg, var(--primary), var(--tertiary));
  border-radius: 1px;
  animation: progressFill 1.6s cubic-bezier(0.4, 0, 0.2, 1) 1.4s forwards;
}}
@keyframes progressFill {{
  0% {{ width: 0; }}
  100% {{ width: 100%; }}
}}

/* 整体微缩放循环（动效期内） */
.content {{
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: contentBreathe 4s ease-in-out infinite;
}}
@keyframes contentBreathe {{
  0%, 100% {{ transform: scale(1); }}
  50% {{ transform: scale(1.02); }}
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
          <!-- 8 个浮动粒子 -->
          <div class="particle p1"></div>
          <div class="particle p2"></div>
          <div class="particle p3"></div>
          <div class="particle p4"></div>
          <div class="particle p5"></div>
          <div class="particle p6"></div>
          <div class="particle p7"></div>
          <div class="particle p8"></div>

          <div class="content">
            <!-- 多层圆环 -->
            <div class="ring-stage">
              <!-- 主圆环（最外 1.6s sweep） -->
              <div class="ring-main">
                <svg viewBox="0 0 200 200">
                  <circle class="circle-track" cx="100" cy="100" r="90"></circle>
                  <circle class="circle-prog" cx="100" cy="100" r="90"></circle>
                </svg>
              </div>
              <!-- 副圆环（中 dashed 流动） -->
              <div class="ring-sub">
                <svg viewBox="0 0 200 200">
                  <circle class="circle-track" cx="100" cy="100" r="70"></circle>
                  <circle class="circle-prog" cx="100" cy="100" r="70"></circle>
                </svg>
              </div>
              <!-- 内圆环（3.2s sweep） -->
              <div class="ring-inner">
                <svg viewBox="0 0 200 200">
                  <circle class="circle-track" cx="100" cy="100" r="35"></circle>
                  <circle class="circle-prog" cx="100" cy="100" r="35"></circle>
                </svg>
              </div>
              <!-- 8 个外点公转 -->
              <div class="ring-dots">
                <span></span><span></span><span></span><span></span>
                <span></span><span></span><span></span><span></span>
              </div>
            </div>

            <!-- 中心「流」字 stroke 描边 -->
            <div class="brand-stage">
              <svg class="brand-stroke-svg" viewBox="0 0 100 100">
                <text x="50" y="50">流</text>
              </svg>
              <div class="brand-sub">
                <span>F</span><span>L</span><span>O</span><span>W</span><span>·</span>
              </div>
            </div>
          </div>

          <!-- 底部进度条 -->
          <div class="progress-bar">
            <div class="progress-fill"></div>
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

/* Tab 切换：登录 / 注册 */
.auth-tabs {{
  display: flex;
  margin: 0 24px 24px;
  background: var(--surface-container);
  border-radius: 24px;
  padding: 4px;
  gap: 4px;
}}
.auth-tab {{
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--on-surface-variant);
  border-radius: 20px;
  cursor: pointer;
  transition: all 200ms;
}}
.auth-tab.active {{
  background: var(--surface);
  color: var(--on-surface);
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}}

/* Auth 表单 */
.auth-form {{ display: none; flex-direction: column; flex: 1; padding: 0 24px; }}
.auth-form.active {{ display: flex; }}

.form-title {{
  font-size: 22px;
  font-weight: 500;
  color: var(--on-surface);
  margin-bottom: 4px;
}}
.form-subtitle {{
  font-size: 13px;
  color: var(--on-surface-variant);
  margin-bottom: 24px;
}}

/* Input 字段（Material 3 Outlined TextField 简化） */
.input-field {{
  margin-bottom: 16px;
  position: relative;
}}
.input-field label {{
  display: block;
  font-size: 12px;
  color: var(--on-surface-variant);
  margin-bottom: 6px;
  font-weight: 500;
}}
.input-field input {{
  width: 100%;
  height: 48px;
  padding: 0 16px;
  border: 1.5px solid var(--outline-variant);
  border-radius: 12px;
  background: transparent;
  font-size: 15px;
  color: var(--on-surface);
  outline: none;
  transition: all 200ms;
  font-family: inherit;
}}
.input-field input:focus {{
  border-color: var(--primary);
  background: var(--primary-container);
  color: var(--on-primary-container);
}}
.input-field input::placeholder {{
  color: var(--on-surface-variant);
  font-weight: 300;
}}
.input-with-icon {{
  position: relative;
}}
.input-with-icon input {{ padding-right: 44px; }}
.input-with-icon i {{
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--on-surface-variant);
  cursor: pointer;
  font-size: 16px;
}}
.input-helper {{
  font-size: 11px;
  color: var(--on-surface-variant);
  margin-top: 4px;
  margin-left: 4px;
}}

/* 表单底部链接 */
.form-footer {{
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: var(--on-surface-variant);
}}
.form-footer .link {{
  color: var(--primary);
  cursor: pointer;
  font-weight: 500;
}}
.form-footer .link:hover {{ text-decoration: underline; }}

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
  margin-top: 8px;
}}
.btn-primary:hover {{ background: var(--primary-lifted); }}
.btn-primary:active {{ transform: scale(0.98); }}
.btn-primary:disabled {{ background: var(--outline-variant); cursor: not-allowed; box-shadow: none; }}
.btn-primary.lifted {{
  background: var(--primary-container);
  color: var(--on-primary-container);
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

/* 图像验证码弹窗（居中卡片） */
.captcha-dialog {{
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 86%;
  max-width: 360px;
  background: var(--surface);
  border-radius: 20px;
  padding: 28px 24px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
  animation: captchaPop 280ms cubic-bezier(0.4, 0, 0.2, 1);
}}
@keyframes captchaPop {{
  from {{ opacity: 0; transform: translate(-50%, -50%) scale(0.92); }}
  to {{ opacity: 1; transform: translate(-50%, -50%) scale(1); }}
}}
.captcha-close {{
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: var(--on-surface-variant);
  cursor: pointer;
  font-size: 18px;
  transition: background 150ms;
}}
.captcha-close:hover {{ background: var(--surface-variant); }}
.captcha-title {{
  font-size: 18px;
  font-weight: 500;
  color: var(--on-surface);
  margin-bottom: 4px;
}}
.captcha-subtitle {{
  font-size: 12px;
  color: var(--on-surface-variant);
  margin-bottom: 20px;
}}
.captcha-image-box {{
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}}
.captcha-image {{
  position: relative;
  flex: 1;
  height: 60px;
  background: linear-gradient(135deg, #F8F4F8 0%, #F0EBF0 100%);
  border-radius: 8px;
  border: 1px solid var(--outline-variant);
  overflow: hidden;
}}
.captcha-refresh {{
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 11px;
  color: var(--primary);
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 150ms;
}}
.captcha-refresh:hover {{ background: var(--surface-variant); }}
.captcha-refresh i {{
  font-size: 16px;
  margin-bottom: 2px;
}}
.captcha-input {{
  margin-bottom: 16px;
}}
.captcha-input input {{
  font-family: 'Roboto Mono', monospace;
  font-size: 18px;
  letter-spacing: 4px;
  text-align: center;
  text-transform: uppercase;
}}
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

          <!-- 登录 Tab 切换（登录 / 注册） -->
          <div class="auth-tabs">
            <div class="auth-tab active" data-tab="login" onclick="switchTab('login')">登录</div>
            <div class="auth-tab" data-tab="signup" onclick="switchTab('signup')">注册</div>
          </div>

          <!-- 登录表单 -->
          <div class="auth-form active" id="form-login">
            <div class="form-title">欢迎回到 流 Flow</div>
            <div class="form-subtitle">登录后数据云端同步，多端互通</div>

            <div class="input-field">
              <label>邮箱</label>
              <input type="email" placeholder="请输入邮箱" value="reader@liuflow.app" />
            </div>

            <div class="input-field">
              <label>密码</label>
              <div class="input-with-icon">
                <input type="password" placeholder="请输入密码" value="flow2026" id="login-password" />
                <i class="fa-regular fa-eye" onclick="togglePassword('login-password', this)"></i>
              </div>
            </div>

            <button class="btn-primary" onclick="goMain()">登录</button>

            <div class="form-footer">
              <span class="link" onclick="switchTab('signup')">没有账号？立即注册</span>
            </div>
          </div>

          <!-- 注册表单 -->
          <div class="auth-form" id="form-signup">
            <div class="form-title">创建你的 流 Flow 账号</div>
            <div class="form-subtitle">几秒钟即可开始专注</div>

            <div class="input-field">
              <label>邮箱</label>
              <input type="email" placeholder="请输入邮箱" />
            </div>

            <div class="input-field">
              <label>密码</label>
              <div class="input-with-icon">
                <input type="password" placeholder="8-32 位字母 + 数字" id="signup-password" />
                <i class="fa-regular fa-eye" onclick="togglePassword('signup-password', this)"></i>
              </div>
              <div class="input-helper">8-32 位字母 + 数字，区分大小写</div>
            </div>

            <div class="input-field">
              <label>确认密码</label>
              <div class="input-with-icon">
                <input type="password" placeholder="请再次输入密码" id="signup-password-2" />
                <i class="fa-regular fa-eye" onclick="togglePassword('signup-password-2', this)"></i>
              </div>
            </div>

            <button class="btn-primary" onclick="goMain()">注册</button>

            <div class="form-footer">
              <span class="link" onclick="switchTab('login')">已有账号？立即登录</span>
            </div>
          </div>

          <!-- 协议 -->
          <div class="agreement">
            登录即代表同意<a href="#" onclick="showAgreement(); return false;">《服务协议》</a>
            <a href="#" onclick="showAgreement(); return false;">《隐私政策》</a>
          </div>
        </div>

        <!-- 协议模态弹窗（Bottom Sheet） -->
        <div class="modal" id="agreement-modal">
          <div class="sheet">
            <div class="sheet-handle"></div>
            <div class="sheet-title">服务协议与隐私政策</div>
            <div class="sheet-body">
              <h3>一、服务说明</h3>
              <p>流 Flow（以下简称"本服务"）是一款极简的深度工作计时器，由个人开发者运营。本服务通过邮箱 + 密码完成身份验证，所有专注记录可同步至云端。</p>
              <h3>二、我们收集的信息</h3>
              <ul>
                <li>邮箱：用于登录身份验证</li>
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
              <h3>五、密码安全</h3>
              <p>本服务使用 bcrypt 算法加密存储您的密码，开发者无法获取明文。请使用 8-32 位含字母与数字的强密码，并定期更换。</p>
              <p style="margin-top:24px; font-size: 11px; color: var(--on-surface-variant);">最后更新：2026-08-26</p>
            </div>
          </div>
        </div>

        <!-- 图像验证码弹窗（风控触发） -->
        <div class="modal" id="captcha-modal">
          <div class="captcha-dialog">
            <div class="captcha-close" onclick="closeCaptcha()">
              <i class="fa-solid fa-xmark"></i>
            </div>
            <div class="captcha-title">请完成安全验证</div>
            <div class="captcha-subtitle">连续输错密码后需要进行验证</div>

            <div class="captcha-image-box">
              <div class="captcha-image">
                <span style="position:absolute;top:14px;left:18px;font-size:24px;font-family:'Roboto Mono';font-weight:700;color:#2A1A2A;transform:rotate(-8deg);">a</span>
                <span style="position:absolute;top:18px;left:48px;font-size:28px;font-family:'Roboto Mono';font-weight:700;color:#6750A4;transform:rotate(5deg);">8</span>
                <span style="position:absolute;top:12px;right:54px;font-size:26px;font-family:'Roboto Mono';font-weight:700;color:#2D6A4F;transform:rotate(-3deg);">K</span>
                <span style="position:absolute;top:22px;right:18px;font-size:24px;font-family:'Roboto Mono';font-weight:700;color:#BA1A1A;transform:rotate(7deg);">2</span>
                <!-- 干扰线 -->
                <svg style="position:absolute;top:0;left:0;width:100%;height:100%;pointer-events:none;" viewBox="0 0 160 60">
                  <line x1="10" y1="20" x2="150" y2="40" stroke="#7A4E7C" stroke-width="1" opacity="0.4"/>
                  <line x1="20" y1="50" x2="140" y2="10" stroke="#4A6FA5" stroke-width="1" opacity="0.3"/>
                </svg>
              </div>
              <div class="captcha-refresh" onclick="refreshCaptcha()">
                <i class="fa-solid fa-rotate"></i>
                <span>换一张</span>
              </div>
            </div>

            <div class="input-field captcha-input">
              <input type="text" placeholder="请输入图片中的字符" maxlength="4" id="captcha-code" />
            </div>

            <button class="btn-primary" onclick="verifyCaptcha()">验证</button>
          </div>
        </div>
"""
    html += nav_bar(SPLASH_THEME)
    html += f"""
      </div>
    </div>
  </div>
  <script>
    // Tab 切换
    function switchTab(tab) {{
      document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
      document.querySelector('[data-tab="' + tab + '"]').classList.add('active');
      document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
      document.getElementById('form-' + tab).classList.add('active');
    }}

    // 密码可见切换
    function togglePassword(inputId, icon) {{
      const input = document.getElementById(inputId);
      if (input.type === 'password') {{
        input.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
      }} else {{
        input.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
      }}
    }}

    // 协议弹窗
    function showAgreement() {{
      document.getElementById('agreement-modal').classList.add('active');
    }}
    document.getElementById('agreement-modal').addEventListener('click', function(e) {{
      if (e.target === this) this.classList.remove('active');
    }});

    // 图像验证码弹窗
    function showCaptcha() {{
      document.getElementById('captcha-modal').classList.add('active');
    }}
    function closeCaptcha() {{
      document.getElementById('captcha-modal').classList.remove('active');
    }}
    function refreshCaptcha() {{
      // 实际项目中这里重新请求 captcha 图片
      // 演示用：刷新字符内容
      const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
      const spans = document.querySelectorAll('.captcha-image span');
      for (let i = 0; i < spans.length; i++) {{
        spans[i].textContent = chars[Math.floor(Math.random() * chars.length)];
      }}
    }}
    function verifyCaptcha() {{
      // 演示用：直接跳主屏
      goMain();
    }}
    document.getElementById('captcha-modal').addEventListener('click', function(e) {{
      if (e.target === this) closeCaptcha();
    }});

    // 演示用：点击登录按钮模拟风控触发
    function goMain() {{
      // 演示用：直接显示主屏（实际项目中跳主屏或显示 captcha）
      // 这里直接 alert 提示
      alert('登录成功（演示）\n\n实际流程：\n1. 邮箱+密码 → 调 CloudBase signIn\n2. 密码错误 3-5 次 → 弹图像验证码\n3. 验证通过 → 跳主屏');
    }}

    // 演示用：5 秒后自动弹 captcha 演示
    setTimeout(() => {{
      const notice = document.createElement('div');
      notice.style.cssText = 'position:fixed;bottom:24px;left:50%;transform:translateX(-50%);background:rgba(0,0,0,0.85);color:white;padding:10px 18px;border-radius:8px;font-size:12px;z-index:1000;box-shadow:0 4px 12px rgba(0,0,0,0.3);';
      notice.innerHTML = '💡 演示提示：点击登录按钮 5 次后，会触发图像验证码弹窗';
      document.body.appendChild(notice);
      setTimeout(() => notice.remove(), 6000);
    }}, 2000);

    // 演示用：5 次点击后弹 captcha
    let loginAttempts = 0;
    document.querySelectorAll('.btn-primary').forEach(btn => {{
      if (btn.textContent.trim() === '登录') {{
        btn.addEventListener('click', function(e) {{
          e.preventDefault();
          e.stopImmediatePropagation();
          loginAttempts++;
          if (loginAttempts >= 2) {{
            showCaptcha();
          }} else {{
            alert('登录成功（演示）');
          }}
        }}, true);
      }}
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
    PAGES_ROOT = r"Q:\large_program\liu-flow\prototype\pages"
    PROTO_ROOT = r"Q:\large_program\liu-flow\prototype"

    # 3 个 V0.2.0 屏 → pages/ 目录（与 V0.1.0 9 屏同目录）
    pages_files = {
        "splash.html": build_splash(),
        "login.html": build_login(),
        "migrate.html": build_migrate(),
    }
    for name, content in pages_files.items():
        path = os.path.join(PAGES_ROOT, name)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"  [ok] pages/{name}: {len(content):,} chars")

    # V0.2.0 独立入口 → 根目录（备份用，V0.1.0 入口已整合 V0.2.0 tab）
    index_v2 = build_index()
    path = os.path.join(PROTO_ROOT, "index_v2.html")
    with open(path, "w", encoding="utf-8") as f:
        f.write(index_v2)
    print(f"  [ok] index_v2.html: {len(index_v2):,} chars")

    print(f"\n[完成] 4 个文件写入（3 个 pages/ + 1 个根目录）")
