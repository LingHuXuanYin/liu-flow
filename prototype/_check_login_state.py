"""检查两个 login.html 哪个是新版"""
import re
from pathlib import Path

ROOT_LOGIN = Path(r"Q:\large_program\liu-flow\prototype\login.html")
PAGES_LOGIN = Path(r"Q:\large_program\liu-flow\prototype\pages\login.html")

for name, path in [("根目录", ROOT_LOGIN), ("pages/", PAGES_LOGIN)]:
    text = path.read_text(encoding="utf-8")
    phone = len(re.findall(r'手机号|短信|验证码|138 ', text))
    plus86 = text.count("+86")
    email = len(re.findall(r'邮箱|password|signIn|auth\.signIn', text))
    print(f"=== {name} {path.name} ({len(text)} chars) ===")
    print(f"  手机/验证码关键字: {phone}")
    print(f"  +86 出现: {plus86}")
    print(f"  邮箱/密码关键字: {email}")
    print(f"  首 200 字: {text[:200]}")
    print()
