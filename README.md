# App Builder

اپلیکیشن اندروید WebView با تنظیمات کاملاً پویا.

## نحوه استفاده

1. فایل `config/app01.json` رو عوض کن
2. عکس‌ها رو توی `assets/` بذار (`icon-144.png` و `splash-logo.png`)
3. `git push` کن
4. GitHub Actions خودش APK می‌سازه
5. از تب Actions دانلود کن

## فایل‌های مهم

- `config/app01.json` → همه‌ی تنظیمات
- `assets/icon-144.png` → آیکون اپ
- `assets/splash-logo.png` → لوگوی اسپلش
- `scripts/inject.py` → تبدیل JSON به Config.java

## ساختار

- `app/src/main/java/app/vista/Config.java` → خودکار ساخته میشه از JSON
- `app/src/main/java/app/vista/*.java` → کدهای UI
- `app/build.gradle` → خودکار ساخته میشه از JSON
