"""看旧关键词的残留位置"""
import re
from pathlib import Path

PATH = Path(r"Q:\large_program\liu-flow\docs\prd_v0.2.0.md")
text = PATH.read_text(encoding="utf-8")
lines = text.split("\n")

CHECK = ["手机号", "短信", "腾讯云 SMS"]
for i, line in enumerate(lines, 1):
    for k in CHECK:
        if k in line:
            lt = line.strip()
            if len(lt) > 100:
                lt = lt[:100] + "..."
            print(f"L{i:4d} [{k}]: {lt}")
            break
