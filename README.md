
## چطور استفاده کنم؟

## استفاده

۱. تو پنل، تنظیمات رو انجام بده
۲. **ذخیره** بزن، بعد **بارگیری** — فایل `app.json` دانلود می‌شه
۳. تو گیت‌هاب، `config/app.json` رو **جایگزین کن**
۴. Commit/Push بزن
۵. Actions خودکار شروع می‌شه
۶. از تب **Actions** → **Artifacts** → APK دانلود کن

## قواعد

- هر چیزی تو `app.json` → از `app.json` خونده می‌شه
- اگه `app.json` ناقص باشه → بیلد fail
- **فقط ۲ فایل آپلود می‌کنی:** `assets/icon.png` و `assets/round.png`

## Secrets موردنیاز

تو `Settings` → `Secrets and variables` → `Actions`:

| نام | مقدار |
|---|---|
| `KEYSTORE_PASS` | پسورد `release.jks` |
| `KEY_ALIAS` | نام کلید |
| `KEY_PASS` | پسورد کلید |
۱. تو پنل اپ‌ساز، تنظیمات رو انجام بده
۲. ذخیره بزن، بعد بارگیری کن — فایل `app.json` دانلود می‌شه
۳. تو گیت‌هاب، برو به `config/app.json` و **جایگزینش کن**
۴. Commit/Push بزن
۵. Actions خودکار شروع می‌شه
۶. از تب **Actions** → **Artifacts** → APK رو دانلود کن

## قوانین

- **هر چیزی تو `app.json` هست → از `app.json` خونده می‌شه (نه هاردکد)**
- **اگه `app.json` ناقص باشه → بیلد fail، اپ ساخته نمی‌شه**
- **فقط تصاویر (مثل آیکون PNG) می‌تونن پیش‌فرض باشن**

## Secrets موردنیاز

تو `Settings` → `Secrets and variables` → `Actions`:

| نام | مقدار |
|---|---|
| `KEYSTORE_PASS` | پسورد فایل `release.jks` |
| `KEY_ALIAS` | نام کلید |
| `KEY_PASS` | پسورد کلید |
