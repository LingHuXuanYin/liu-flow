"""扫 V0.2.0 PRD 里的关键关键词（手机号/短信/SMS 等），定位要改的位置"""
import re
from pathlib import Path

PATH = Path(r"Q:\large_program\liu-flow\docs\prd_v0.2.0.md")
text = PATH.read_text(encoding="utf-8")
lines = text.split("\n")

KEYWORDS = ["手机号", "短信", "SMS", "验证码", "11 位", "+86", "sms_codes", "腾讯云 SMS", "sendCode", "verifyLogin"]

for i, line in enumerate(lines, 1):
    if any(k in line for k in KEYWORDS):
        line_trim = line.strip()
        if len(line_trim) > 110:
            line_trim = line_trim[:110] + "..."
        print(f"L{i:4d}: {line_trim}")
