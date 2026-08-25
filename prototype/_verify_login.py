"""
验证脚本：全角中文标点 + HTML 属性双引号 + 占位符残留
按 user profile pitfall 条目：write 工具会破坏 HTML 属性半角双引号
"""

import re
import sys
from pathlib import Path

ROOT = Path(r"Q:\large_program\liu-flow\prototype")
FILES = ["splash.html", "login.html", "migrate.html", "index_v2.html"]


def main():
    print("=" * 60)
    print("  V0.2.0 登录原型 - 验证报告")
    print("=" * 60)

    all_ok = True
    for name in FILES:
        path = ROOT / name
        if not path.exists():
            print(f"  [FAIL] {name}: 文件不存在")
            all_ok = False
            continue
        text = path.read_text(encoding="utf-8")
        size_kb = len(text) / 1024

        # 1) 全角中文标点（中文上下文中应保留）
        fullwidth_punct = len(re.findall(r"[\uff0c\u3002\uff1a\uff1b\uff01\uff1f\u201c\u201d\u2018\u2019\uff08\uff09]", text))

        # 2) 半角标点错误出现于中文上下文（不应有）
        bad_halfwidth = len(re.findall(r"[一-鿿],|[一-鿿]\.|[一-鿿]:|[一-鿿]\?|[一-鿿]!|[一-鿿];", text))

        # 3) HTML 属性双引号：必须为半角
        half_quote_in_attrs = len(re.findall(r'="[^"]*"', text))
        full_quote_in_attrs = len(re.findall(r'\u201c="|\u201d="', text))

        # 4) 占位符残留
        placeholder = re.findall(r"\{[^{}]*\}", text)
        lorem = re.findall(r"(?i)lorem ipsum|placeholder\.com", text)

        # 5) <a href="..."> 链接是否完整
        hrefs = re.findall(r'<a\s+href="([^"]+)"', text)
        empty_href = [h for h in hrefs if h in ("#", "", "javascript:void(0)")]

        print(f"\n  [{name}]  {size_kb:.1f} KB")
        print(f"    全角标点数:         {fullwidth_punct}")
        print(f"    中文上下文半角错误: {bad_halfwidth}  {'OK' if bad_halfwidth == 0 else 'FAIL'}")
        print(f"    HTML 半角双引号对:  {half_quote_in_attrs}")
        print(f"    HTML 全角双引号污染: {full_quote_in_attrs}  {'OK' if full_quote_in_attrs == 0 else 'FAIL'}")
        print(f"    {{}} 占位符残留:     {len(placeholder)}")
        print(f"    Lorem/placeholder:   {len(lorem)}  {'OK' if len(lorem) == 0 else 'FAIL'}")

        if bad_halfwidth or full_quote_in_attrs or lorem:
            all_ok = False

    print("\n" + "=" * 60)
    if all_ok:
        print("  [ALL OK] 4 个文件验证通过")
    else:
        print("  [ISSUE] 存在问题，需修复")
        sys.exit(1)
    print("=" * 60)


if __name__ == "__main__":
    main()
