"""
覆盖根目录的旧 splash/login/migrate.html 为「已迁移」提示页
避免污染 prototype/ 根目录的旧 V0.2.0 文件
"""

import os
from pathlib import Path

DEPRECATED_HTML = """<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>已迁移 · 流 Flow</title>
<style>
  body {{
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", sans-serif;
    background: #0a0a0a;
    color: #fafafa;
    margin: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100vh;
    padding: 32px;
    box-sizing: border-box;
  }}
  .card {{
    max-width: 480px;
    background: #1a1a1a;
    border: 1px solid #2a2a2a;
    border-radius: 16px;
    padding: 32px;
    text-align: center;
  }}
  h1 {{
    font-size: 24px;
    font-weight: 500;
    margin: 0 0 12px;
    color: #7A4E7C;
  }}
  p {{
    font-size: 14px;
    line-height: 1.7;
    color: #a8a8a8;
    margin: 8px 0;
  }}
  a {{
    display: inline-block;
    margin-top: 16px;
    padding: 10px 24px;
    background: #7A4E7C;
    color: white;
    text-decoration: none;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 500;
    transition: background 200ms;
  }}
  a:hover {{ background: #8E5E91; }}
  code {{
    background: #2a2a2a;
    padding: 2px 8px;
    border-radius: 4px;
    font-family: 'Roboto Mono', monospace;
    font-size: 13px;
    color: #D89BAE;
  }}
  .meta {{
    margin-top: 20px;
    font-size: 12px;
    color: #6a6a6a;
  }}
</style>
</head>
<body>
  <div class="card">
    <h1>此页面已迁移</h1>
    <p>V0.2.0 三个屏（{title}）已于 2026-08-26 迁移到 <code>pages/{name}</code> 目录。</p>
    <p>根目录的 <code>{name}</code> 仅为旧版保留，请从下方入口访问最新版。</p>
    <a href="pages/{name}">访问新版 →</a>
    <a href="index.html" style="background: transparent; border: 1px solid #7A4E7C; margin-left: 8px;">← 回到 12 屏入口</a>
    <div class="meta">V0.2.0 PRD 已切换为邮箱 + 密码 + 图像验证码方案</div>
  </div>
</body>
</html>
"""

PROTO_ROOT = Path(r"Q:\large_program\liu-flow\prototype")
FILES = {
    "splash.html": "启动屏（动效升级版）",
    "login.html": "登录页（邮箱 + 密码 + 图像验证码）",
    "migrate.html": "数据迁移提示",
}

for name, title in FILES.items():
    p = PROTO_ROOT / name
    if p.exists():
        content = DEPRECATED_HTML.format(name=name, title=title)
        p.write_text(content, encoding="utf-8")
        print(f"  [ok] {name}: 覆盖为已迁移提示页")
    else:
        print(f"  [skip] {name}: 不存在")
