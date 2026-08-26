"""验证流程文档改完状态"""
import re
from pathlib import Path

PATH = Path(r"Q:\large_program\liu-flow\docs\腾讯云后端服务开通流程.md")
text = PATH.read_text(encoding="utf-8")

bad_half = len(re.findall(r'[一-鿿],[一-鿿]|[一-鿿]\.[一-鿿]|[一-鿿]:[一-鿿]', text))

old_kw = {
    "手机号": len(re.findall(r'手机号', text)),
    "+86": len(re.findall(r'\+86', text)),
    "短信验证码": len(re.findall(r'短信验证码', text)),
    "sendCode": len(re.findall(r'sendCode', text)),
}

new_kw = {
    "邮箱": len(re.findall(r'邮箱', text)),
    "密码": len(re.findall(r'密码', text)),
    "图像验证码": len(re.findall(r'图像验证码', text)),
    "CAPTCHA_REQUIRED": len(re.findall(r'CAPTCHA_REQUIRED', text)),
    "signIn": len(re.findall(r'signIn', text)),
    "signUp": len(re.findall(r'signUp', text)),
}

print(f"流程文档大小: {len(text):,} chars ({len(text)/1024:.1f} KB)")
print(f"中文上下文半角错误: {bad_half}  {'OK' if bad_half == 0 else 'FAIL'}")
print("\n旧关键词残留（应接近 0）:")
for k, v in old_kw.items():
    print(f"  {k}: {v}")
print("\n新关键词覆盖（应 > 0）:")
for k, v in new_kw.items():
    print(f"  {k}: {v}")
