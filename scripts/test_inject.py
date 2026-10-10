#!/usr/bin/env python3
# =========================================================
# test_inject.py — مقایسه app01.json با Config.java
# مسیر: scripts/test_inject.py
# =========================================================
# 📌 app01.json رو می‌خونه
# 📌 Config.java که inject.py ساخت رو می‌خونه
# 📌 مقایسه می‌کنه — کدوم فیلد رسیده و کدوم نرسیده
# =========================================================

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONFIG_FILE = ROOT / "config" / "app01.json"
JAVA_SRC_DIR = ROOT / "app" / "src" / "main" / "java"


# =========================================================
# لیست کامل فیلدها با نام معادل در Config.java
# =========================================================
# (json_path, java_const_name, converter)
# converter: 'str', 'int', 'bool', 'float', 'color'
FIELDS = [
    # BRANDING
    ("branding.app_name", "APP_NAME", "str"),
    ("branding.app_name_en", "APP_NAME_EN", "str"),
    ("branding.package_name", "PACKAGE_NAME", "str"),
    ("branding.version_code", "VERSION_CODE", "int"),
    ("branding.version_name", "VERSION_NAME", "str"),
    ("branding.is_new_app", "IS_NEW_APP", "bool"),

    # COLORS
    ("colors.color_primary", "COLOR_PRIMARY", "color"),
    ("colors.color_background", "COLOR_BACKGROUND", "color"),
    ("colors.color_text", "COLOR_TEXT", "color"),
    ("colors.color_text_secondary", "COLOR_TEXT_SECONDARY", "color"),
    ("colors.color_button", "COLOR_BUTTON", "color"),
    ("colors.color_button_text", "COLOR_BUTTON_TEXT", "color"),
    ("colors.color_accent", "COLOR_ACCENT", "color"),
    ("colors.bg_type", "BG_TYPE", "str"),
    ("colors.bg_color_solid", "BG_COLOR_SOLID", "color"),
    ("colors.bg_color_1", "BG_COLOR_1", "color"),
    ("colors.bg_color_2", "BG_COLOR_2", "color"),
    ("colors.bg_gradient_angle", "BG_GRADIENT_ANGLE", "int"),
    ("colors.bg_effect", "BG_EFFECT", "str"),
    ("colors.bg_effect_opacity", "BG_EFFECT_OPACITY", "float"),

    # FONTS
    ("fonts.font_family", "FONT_FAMILY", "str"),
    ("fonts.font_weight", "FONT_WEIGHT", "int"),
    ("fonts.font_size_base", "FONT_SIZE_BASE", "int"),
    ("fonts.title_size", "TITLE_SIZE", "int"),
    ("fonts.title_weight", "TITLE_WEIGHT", "int"),
    ("fonts.subtitle_size", "SUBTITLE_SIZE", "int"),
    ("fonts.subtitle_weight", "SUBTITLE_WEIGHT", "int"),
    ("fonts.body_size", "BODY_SIZE", "int"),
    ("fonts.body_weight", "BODY_WEIGHT", "int"),
    ("fonts.button_size", "BUTTON_SIZE", "int"),
    ("fonts.button_weight", "BUTTON_WEIGHT", "int"),
    ("fonts.anim_title", "ANIM_TITLE", "str"),
    ("fonts.anim_text", "ANIM_TEXT", "str"),
    ("fonts.anim_btn", "ANIM_BTN", "str"),
    ("fonts.anim_duration", "ANIM_DURATION", "int"),
    ("fonts.anim_delay", "ANIM_DELAY", "int"),

    # SPLASH
    ("splash.enabled", "SPLASH_ENABLED", "bool"),
    ("splash.duration", "SPLASH_DURATION", "int"),
    ("splash.bg_type", "SPLASH_BG_TYPE", "str"),
    ("splash.bg_color_solid", "SPLASH_BG_COLOR_SOLID", "color"),
    ("splash.bg_color_1", "SPLASH_BG_COLOR_1", "color"),
    ("splash.bg_color_2", "SPLASH_BG_COLOR_2", "color"),
    ("splash.logo_size", "SPLASH_LOGO_SIZE", "int"),
    ("splash.logo_anim", "SPLASH_LOGO_ANIM", "str"),
    ("splash.title", "SPLASH_TITLE", "str"),
    ("splash.title_size", "SPLASH_TITLE_SIZE", "int"),
    ("splash.title_color", "SPLASH_TITLE_COLOR", "color"),
    ("splash.title_anim", "SPLASH_TITLE_ANIM", "str"),
    ("splash.subtitle", "SPLASH_SUBTITLE", "str"),
    ("splash.subtitle_size", "SPLASH_SUBTITLE_SIZE", "int"),
    ("splash.subtitle_color", "SPLASH_SUBTITLE_COLOR", "color"),
    ("splash.subtitle_anim", "SPLASH_SUBTITLE_ANIM", "str"),
    ("splash.loader_type", "SPLASH_LOADER_TYPE", "str"),
    ("splash.loader_color", "SPLASH_LOADER_COLOR", "color"),
    ("splash.show_loader", "SPLASH_SHOW_LOADER", "bool"),

    # ONBOARDING
    ("onboarding.enabled", "ONB_ENABLED", "bool"),
    ("onboarding.skip_text", "ONB_SKIP_TEXT", "str"),
    ("onboarding.next_text", "ONB_NEXT_TEXT", "str"),
    ("onboarding.prev_text", "ONB_PREV_TEXT", "str"),
    ("onboarding.start_text", "ONB_START_TEXT", "str"),
    ("onboarding.btn_bg", "ONB_BTN_BG", "color"),
    ("onboarding.btn_text_color", "ONB_BTN_TEXT_COLOR", "color"),
    ("onboarding.dot_active", "ONB_DOT_ACTIVE", "color"),
    ("onboarding.dot_inactive", "ONB_DOT_INACTIVE", "color"),

    # WELCOME
    ("welcome.enabled", "WELCOME_ENABLED", "bool"),
    ("welcome.title", "WELCOME_TITLE", "str"),
    ("welcome.subtitle", "WELCOME_SUBTITLE", "str"),
    ("welcome.icon", "WELCOME_ICON", "str"),
    ("welcome.button_text", "WELCOME_BUTTON_TEXT", "str"),
    ("welcome.button_bg", "WELCOME_BUTTON_BG", "color"),
    ("welcome.button_text_color", "WELCOME_BUTTON_TEXT_COLOR", "color"),
    ("welcome.bg_color", "WELCOME_BG_COLOR", "color"),
    ("welcome.title_color", "WELCOME_TITLE_COLOR", "color"),
    ("welcome.subtitle_color", "WELCOME_SUBTITLE_COLOR", "color"),

    # VPN
    ("vpn.enabled", "VPN_ENABLED", "bool"),
    ("vpn.top_title", "VPN_TOP_TITLE", "str"),
    ("vpn.top_subtitle", "VPN_TOP_SUBTITLE", "str"),
    ("vpn.footnote", "VPN_FOOTNOTE", "str"),
    ("vpn.show_recheck", "VPN_SHOW_RECHECK", "bool"),

    # WEBVIEW
    ("webview.url", "WV_URL", "str"),
    ("webview.url_type", "WV_URL_TYPE", "str"),
    ("webview.url_home", "WV_URL_HOME", "str"),
    ("webview.user_agent", "WV_USER_AGENT", "str"),
    ("webview.user_agent_custom", "WV_USER_AGENT_CUSTOM", "str"),
    ("webview.progress_bar", "WV_PROGRESS_BAR", "bool"),
    ("webview.progress_color", "WV_PROGRESS_COLOR", "color"),
    ("webview.progress_height", "WV_PROGRESS_HEIGHT", "int"),
    ("webview.zoom_enabled", "WV_ZOOM", "bool"),
    ("webview.js_enabled", "WV_JS", "bool"),
    ("webview.dom_storage", "WV_DOM", "bool"),
    ("webview.database", "WV_DATABASE", "bool"),
    ("webview.geolocation", "WV_GEOLOCATION", "bool"),
    ("webview.file_upload", "WV_FILE_UPLOAD", "bool"),
    ("webview.camera", "WV_CAMERA", "bool"),
    ("webview.microphone", "WV_MIC", "bool"),
    ("webview.pull_to_refresh", "WV_PULL_REFRESH", "bool"),
    ("webview.pull_text", "WV_PULL_TEXT", "str"),
    ("webview.pull_release", "WV_PULL_RELEASE", "str"),
    ("webview.pull_loading", "WV_PULL_LOADING", "str"),
    ("webview.pull_color", "WV_PULL_COLOR", "color"),
    ("webview.back_button", "WV_BACK_BUTTON", "bool"),
    ("webview.back_exit_msg", "WV_BACK_EXIT_MSG", "str"),
    ("webview.back_double", "WV_BACK_DOUBLE", "bool"),
    ("webview.external_links", "WV_EXTERNAL_LINKS", "str"),
    ("webview.mail_links", "WV_MAIL_LINKS", "str"),
    ("webview.tel_links", "WV_TEL_LINKS", "str"),
    ("webview.whatsapp_links", "WV_WHATSAPP_LINKS", "str"),
    ("webview.telegram_links", "WV_TELEGRAM_LINKS", "str"),
    ("webview.instagram_links", "WV_INSTAGRAM_LINKS", "str"),
    ("webview.timeout", "WV_TIMEOUT", "int"),
    ("webview.retry_count", "WV_RETRY_COUNT", "int"),
    ("webview.cache_enabled", "WV_CACHE", "bool"),
    ("webview.cache_mode", "WV_CACHE_MODE", "str"),
    ("webview.safe_browsing", "WV_SAFE_BROWSING", "bool"),
    ("webview.block_ads", "WV_BLOCK_ADS", "bool"),
    ("webview.show_splash_on_webview", "WV_SHOW_SPLASH_ON_WEBVIEW", "bool"),

    # ERRORS — کلی
    ("errors.enabled", "ERR_ENABLED", "bool"),
    ("errors.show_retry", "ERR_SHOW_RETRY", "bool"),
    ("errors.show_home", "ERR_SHOW_HOME", "bool"),
    ("errors.auto_retry", "ERR_AUTO_RETRY", "bool"),
    ("errors.auto_retry_sec", "ERR_AUTO_RETRY_SEC", "int"),
    ("errors.retry_text", "ERR_RETRY_TEXT", "str"),
    ("errors.retry_bg", "ERR_RETRY_BG", "color"),
    ("errors.retry_color", "ERR_RETRY_COLOR", "color"),
    ("errors.home_text", "ERR_HOME_TEXT", "str"),
    ("errors.home_bg", "ERR_HOME_BG", "str"),
    ("errors.home_color", "ERR_HOME_COLOR", "color"),
    ("errors.home_border", "ERR_HOME_BORDER", "color"),

    # EXIT
    ("exit.enabled", "EXIT_ENABLED", "bool"),
    ("exit.double_back", "EXIT_DOUBLE_BACK", "bool"),
    ("exit.double_back_msg", "EXIT_DOUBLE_BACK_MSG", "str"),
    ("exit.show_icon", "EXIT_SHOW_ICON", "bool"),
    ("exit.dialog_type", "EXIT_DIALOG_TYPE", "str"),
    ("exit.radius", "EXIT_RADIUS", "int"),
    ("exit.border_width", "EXIT_BORDER_WIDTH", "int"),
    ("exit.icon", "EXIT_ICON", "str"),
    ("exit.title", "EXIT_TITLE", "str"),
    ("exit.title_color", "EXIT_TITLE_COLOR", "color"),
    ("exit.text", "EXIT_TEXT", "str"),
    ("exit.text_color", "EXIT_TEXT_COLOR", "color"),
    ("exit.bg_color", "EXIT_BG_COLOR", "color"),
    ("exit.border_color", "EXIT_BORDER_COLOR", "color"),
    ("exit.overlay_color", "EXIT_OVERLAY_COLOR", "str"),
    ("exit.btn_confirm_text", "EXIT_BTN_CONFIRM_TEXT", "str"),
    ("exit.btn_confirm_bg", "EXIT_BTN_CONFIRM_BG", "color"),
    ("exit.btn_confirm_color", "EXIT_BTN_CONFIRM_COLOR", "color"),
    ("exit.btn_cancel_text", "EXIT_BTN_CANCEL_TEXT", "str"),
    ("exit.btn_cancel_bg", "EXIT_BTN_CANCEL_BG", "color"),
    ("exit.btn_cancel_color", "EXIT_BTN_CANCEL_COLOR", "color"),
    ("exit.btn_layout", "EXIT_BTN_LAYOUT", "str"),

    # ADVANCED
    ("advanced.output_name", "ADV_OUTPUT_NAME", "str"),
    ("advanced.output_format", "ADV_OUTPUT_FORMAT", "str"),
    ("advanced.min_sdk", "ADV_MIN_SDK", "int"),
    ("advanced.target_sdk", "ADV_TARGET_SDK", "int"),
    ("advanced.architecture", "ADV_ARCHITECTURE", "str"),
    ("advanced.developer_name", "ADV_DEVELOPER_NAME", "str"),
    ("advanced.developer_email", "ADV_DEVELOPER_EMAIL", "str"),
    ("advanced.website", "ADV_WEBSITE", "str"),
    ("advanced.description", "ADV_DESCRIPTION", "str"),
]


# ===== فیلدهای دینامیک (تودرتو) =====
# splash s1, s2, s3
for n in (1, 2, 3):
    FIELDS += [
        (f"onboarding.s{n}_enabled", f"ONB_S{n}_ENABLED", "bool"),
        (f"onboarding.s{n}_bg_type", f"ONB_S{n}_BG_TYPE", "str"),
        (f"onboarding.s{n}_bg_solid", f"ONB_S{n}_BG_SOLID", "color"),
        (f"onboarding.s{n}_bg_1", f"ONB_S{n}_BG_1", "color"),
        (f"onboarding.s{n}_bg_2", f"ONB_S{n}_BG_2", "color"),
        (f"onboarding.s{n}_title", f"ONB_S{n}_TITLE", "str"),
        (f"onboarding.s{n}_title_color", f"ONB_S{n}_TITLE_COLOR", "color"),
        (f"onboarding.s{n}_text", f"ONB_S{n}_TEXT", "str"),
        (f"onboarding.s{n}_text_color", f"ONB_S{n}_TEXT_COLOR", "color"),
    ]

# vpn state_on / state_off / state_unknown
for st, prefix in [("state_on", "ON"), ("state_off", "OFF"), ("state_unknown", "UNK")]:
    for k in ["icon", "title", "text", "color", "bg", "btn"]:
        FIELDS.append((f"vpn.{st}.{k}", f"VPN_{prefix}_{k.upper()}", "color" if k in ("color", "bg") else "str"))

# vpn recheck
for k in ["text", "bg", "border", "color"]:
    FIELDS.append((f"vpn.recheck.{k}", f"VPN_RECHECK_{k.upper()}", "color" if k in ("border", "color") else "str"))

# errors 9 types
for t in ["offline", "server", "nf", "fb", "to", "dns", "ssl", "conn", "unk"]:
    T = t.upper()
    for k in ["icon", "title", "text", "color", "bg"]:
        FIELDS.append((f"errors.{t}_{k}", f"ERR_{T}_{k.upper()}", "color" if k in ("color", "bg") else "str"))


# =========================================================
# توابع کمکی
# =========================================================
def get(d, path):
    keys = path.split(".")
    cur = d
    for k in keys:
        if not isinstance(cur, dict) or k not in cur:
            return None
        cur = cur[k]
    return cur


def hex_norm(h):
    """#fb7185 → #FFFB7185 برای مقایسه"""
    if h is None:
        return None
    h = str(h).strip().lstrip("#")
    if len(h) == 6:
        return f"#FF{h.upper()}"
    if len(h) == 8:
        return f"#{h.upper()}"
    return h


def java_to_py_expected(value, conv):
    if value is None:
        return None
    if conv == "str":
        return str(value)
    if conv == "int":
        try:
            return str(int(value))
        except Exception:
            return None
    if conv == "bool":
        return "true" if value in (1, True, "1", "true", "True") else "false"
    if conv == "float":
        try:
            return str(float(value))
        except Exception:
            return None
    if conv == "color":
        return hex_norm(value)
    return str(value)


def parse_config_java(text):
    """مقدار هر ثابت رو استخراج می‌کنه."""
    result = {}
    # public static final String NAME = "...";
    # public static final int NAME = 123;
    # public static final boolean NAME = true;
    # public static final float NAME = 0.5f;
    pattern = re.compile(
        r'public\s+static\s+final\s+\w+\s+(\w+)\s*=\s*(.+?);',
        re.DOTALL
    )
    for m in pattern.finditer(text):
        name = m.group(1)
        raw = m.group(2).strip()

        # حذف 'f' آخر برای float
        if raw.endswith("f"):
            raw = raw[:-1]

        # حذف کوتیشن‌ها
        if raw.startswith('"') and raw.endswith('"'):
            raw = raw[1:-1]
            # unescape
            raw = raw.replace('\\"', '"').replace('\\n', '\n').replace('\\\\', '\\')

        result[name] = raw
    return result


# =========================================================
# main
# =========================================================
def main():
    print("=" * 70)
    print("🧪 تست inject.py — مقایسه app01.json با Config.java")
    print("=" * 70)
    print()

    if not CONFIG_FILE.exists():
        print(f"❌ پیدا نشد: {CONFIG_FILE}")
        sys.exit(1)

    with open(CONFIG_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)

    # پیدا کردن Config.java
    pkg_name = get(data, "branding.package_name")
    config_java_path = JAVA_SRC_DIR / pkg_name.replace(".", "/") / "Config.java"

    if not config_java_path.exists():
        print(f"❌ Config.java پیدا نشد: {config_java_path}")
        print("   → یعنی inject.py نساخته یا خطا داده")
        sys.exit(1)

    java_text = config_java_path.read_text(encoding="utf-8")
    java_values = parse_config_java(java_text)

    print(f"📁 app01.json: {CONFIG_FILE}")
    print(f"📁 Config.java: {config_java_path}")
    print(f"📊 تعداد ثابت‌های Config.java: {len(java_values)}")
    print(f"📊 تعداد فیلدهای تست: {len(FIELDS)}")
    print()

    ok = 0
    missing_in_java = []
    empty_in_java = []
    mismatch = []

    for json_path, java_name, conv in FIELDS:
        json_val = get(data, json_path)
        expected = java_to_py_expected(json_val, conv)
        actual = java_values.get(java_name)

        if actual is None:
            missing_in_java.append((json_path, java_name, expected))
            continue

        if actual == "" and expected not in (None, ""):
            empty_in_java.append((json_path, java_name, expected, actual))
            continue

        if expected != actual:
            mismatch.append((json_path, java_name, expected, actual))
            continue

        ok += 1

    # ===== گزارش =====
    print(f"✅ درست: {ok} / {len(FIELDS)}")
    print()

    if missing_in_java:
        print(f"❌ {len(missing_in_java)} فیلد اصلاً توی Config.java نیستن:")
        for jp, jn, exp in missing_in_java:
            print(f"   - {jp} (کد جاواش: {jn}) = انتظار: {exp!r}")
        print()

    if empty_in_java:
        print(f"⚠️ {len(empty_in_java)} فیلد توی Config.java خالی هستن:")
        for jp, jn, exp, act in empty_in_java:
            print(f"   - {jp} (کد جاواش: {jn}) = انتظار: {exp!r} | واقعی: {act!r}")
        print()

    if mismatch:
        print(f"🔴 {len(mismatch)} فیلد مقدارشون غلط:")
        for jp, jn, exp, act in mismatch:
            print(f"   - {jp} (کد جاواش: {jn})")
            print(f"       انتظار: {exp!r}")
            print(f"       واقعی:  {act!r}")
        print()

    total_problem = len(missing_in_java) + len(empty_in_java) + len(mismatch)

    print("=" * 70)
    if total_problem == 0:
        print(f"🎉 همه‌ی {len(FIELDS)} فیلد درست منتقل شدن")
    else:
        print(f"❌ {total_problem} فیلد مشکل دارن")
    print("=" * 70)


if __name__ == "__main__":
    main()
