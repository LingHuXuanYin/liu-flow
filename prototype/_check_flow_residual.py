"""看流程文档旧关键词的残留位置"""
from pathlib import Path

PATH = Path(r"Q:\large_program\liu-flow\docs\腾讯云后端服务开通流程.md")
text = PATH.read_text(encoding="utf-8")
lines = text.split("\n")

for i, line in enumerate(lines, 1):
    for k in ["手机号", "短信验证码"]:
        if k in line:
            lt = line.strip()
            if len(lt) > 100:
                lt = lt[:100] + "..."
            print(f"L{i:4d} [{k}]: {lt}")
            break
