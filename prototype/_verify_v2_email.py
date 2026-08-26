"""验证 V0.2.0 PRD 改完后的状态"""
import re
from pathlib import Path

PATH = Path(r"Q:\large_program\liu-flow\docs\prd_v0.2.0.md")
text = PATH.read_text(encoding="utf-8")

# 中文上下文半角错误
bad_half = len(re.findall(r'[一-鿿],[一-鿿]|[一-鿿]\.[一-鿿]|[一-鿿]:[一-鿿]', text))

# 关键关键词残留
keywords_old = {
    "手机号": len(re.findall(r'手机号', text)),
    "+86": len(re.findall(r'\+86', text)),
    "短信": len(re.findall(r'短信', text)),
    "腾讯云 SMS": len(re.findall(r'腾讯云 SMS', text)),
    "sendCode": len(re.findall(r'sendCode', text)),
    "verifyLogin": len(re.findall(r'verifyLogin', text)),
    "sms_codes": len(re.findall(r'sms_codes', text)),
}

keywords_new = {
    "邮箱": len(re.findall(r'邮箱', text)),
    "密码": len(re.findall(r'密码', text)),
    "图像验证码": len(re.findall(r'图像验证码', text)),
    "CloudBase Auth": len(re.findall(r'CloudBase Auth', text)),
    "CAPTCHA_REQUIRED": len(re.findall(r'CAPTCHA_REQUIRED', text)),
}

print(f"PRD 大小: {len(text):,} chars ({len(text)/1024:.1f} KB)")
print(f"中文上下文半角错误: {bad_half}  {'OK' if bad_half == 0 else 'FAIL'}")
print("\n旧关键词残留（应该接近 0）:")
for k, v in keywords_old.items():
    flag = "OK" if v == 0 else "WARN"
    print(f"  {k}: {v}  {flag}")
print("\n新关键词覆盖（应该 > 0）:")
for k, v in keywords_new.items():
    print(f"  {k}: {v}")
