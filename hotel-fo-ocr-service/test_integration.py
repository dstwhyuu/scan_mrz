import httpx
import sys

# 1. Login to get JWT
login_data = {
    "username": "frontoffice",
    "password": "FrontOffice@12345"
}
try:
    print("1. Logging in...")
    login_resp = httpx.post("http://localhost:8080/api/v1/auth/login", json=login_data)
    login_resp.raise_for_status()
    token = login_resp.json().get("accessToken")
    print(f"   Login success. Token: {token[:20]}...")
except Exception as e:
    print(f"Login failed: {e}")
    sys.exit(1)

# 2. Upload dummy passport to /api/v1/scans
print("\n2. Uploading dummy_passport.jpg to backend...")
headers = {
    "Authorization": f"Bearer {token}"
}
files = {'passportImage': ('dummy_passport.jpg', open('dummy_passport.jpg', 'rb'), 'image/jpeg')}

try:
    scan_resp = httpx.post("http://localhost:8080/api/v1/scans", headers=headers, files=files, timeout=20.0)
    print(f"   Response status: {scan_resp.status_code}")
    print("   Response JSON:")
    import json
    print(json.dumps(scan_resp.json(), indent=2))
except Exception as e:
    print(f"Scan upload failed: {e}")
