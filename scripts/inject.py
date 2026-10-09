#!/usr/bin/env python3
# =========================================================
# inject.py — JSON به Config.java + resources
# مسیر: scripts/inject.py
# =========================================================
# 📌 هیچ پیش‌فرضی نداره
# 📌 اگه حتی یک فیلد اجباری نباشه → بیلد متوقف می‌شه
# 📌 فیلدهای عکس (icon_48, ..., splash_logo) استثنا هستن
# 📌 اگه یه بخش enabled=0 باشه، فیلدهای اون بخش معاف می‌شن
# =========================================================

import json
import re
import shutil
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parent.parent
CONFIG_FILE = ROOT / "config" / "app01.json"
ASSETS_DIR = ROOT / "assets"
RES_DIR = ROOT / "app" / "src" / "main" / "res"
JAVA_SRC_DIR = ROOT / "app" / "src" / "main" / "java"
GRADLE_APP = ROOT / "app" / "build.gradle"
PKG_OLD = "app.vista"


# =========================================================
# فیلدهایی که همیشه اجباری هستن (مستقل از enabled)
# =========================================================
ALWAYS_REQUIRED = [
    "branding.app_name",
    "branding.package_name",
    "branding.version_code",
    "branding.version_name",
    "colors.color_primary",
    "colors.color_background",
    "colors.color_text",
    "colors.color_text_secondary",
    "colors.color_button",
    "colors.color_button_text",
    "colors.color_accent",
    "colors.bg_type",
    "colors.bg_color_solid",
    "colors.bg_color_1",
    "colors.bg_color_2",
    "colors.bg_gradient_angle",
    "colors.bg_effect",
    "colors.bg_effect_opacity",
    "fonts.font_family",
    "fonts.font_weight",
    "fonts.font_size_base",
    "fonts.title_size",
    "fonts.title_weight",
    "fonts.subtitle_size",
    "fonts.subtitle_weight",
    "fonts.body_size",
    "fonts.body_weight",
    "fonts.button_size",
    "fonts.button_weight",
    "fonts.anim_title",
    "fonts.anim_text",
    "fonts.anim_btn",
    "fonts.anim_duration",
    "fonts.anim_delay",
    "webview.url",
    "webview.url_type",
    "webview.user_agent",
    "webview.progress_bar",
    "webview.progress_color",
    "webview.progress_height",
    "webview.zoom_enabled",
    "webview.js_enabled",
    "webview.dom_storage",
    "webview.database",
    "webview.geolocation",
    "webview.file_upload",
    "webview.camera",
    "webview.microphone",
    "webview.pull_to_refresh",
    "webview.pull_text",
    "webview.pull_release",
    "webview.pull_loading",
    "webview.pull_color",
    "webview.back_button",
    "webview.back_exit_msg",
    "webview.back_double",
    "webview.external_links",
    "webview.mail_links",
    "webview.tel_links",
    "webview.whatsapp_links",
    "webview.telegram_links",
    "webview.instagram_links",
    "webview.timeout",
    "webview.retry_count",
    "webview.cache_enabled",
    "webview.cache_mode",
    "webview.safe_browsing",
    "webview.block_ads",
    "webview.show_splash_on_webview",
    "errors.enabled",
    "errors.show_retry",
    "errors.show_home",
    "errors.auto_retry",
    "errors.auto_retry_sec",
    "errors.retry_text",
    "errors.retry_bg",
    "errors.retry_color",
    "errors.home_text",
    "errors.home_bg",
    "errors.home_color",
    "errors.home_border",
    "exit.enabled",
    "exit.double_back",
    "exit.double_back_msg",
    "exit.show_icon",
    "exit.dialog_type",
    "exit.radius",
    "exit.border_width",
    "exit.icon",
    "exit.title",
    "exit.title_color",
    "exit.text",
    "exit.text_color",
    "exit.bg_color",
    "exit.border_color",
    "exit.overlay_color",
    "exit.btn_confirm_text",
    "exit.btn_confirm_bg",
    "exit.btn_confirm_color",
    "exit.btn_cancel_text",
    "exit.btn_cancel_bg",
    "exit.btn_cancel_color",
    "exit.btn_layout",
    "advanced.output_name",
    "advanced.output_format",
    "advanced.min_sdk",
    "advanced.target_sdk",
    "advanced.architecture",
    "advanced.developer_name",
    "advanced.developer_email",
    "advanced.website",
    "advanced.description",
]


# =========================================================
# فیلدهای وابسته به splash (اگه splash.enabled=1)
# =========================================================
SPLASH_REQUIRED = [
    "splash.enabled",
    "splash.duration",
    "splash.bg_type",
    "splash.bg_color_solid",
    "splash.bg_color_1",
    "splash.bg_color_2",
    "splash.logo_size",
    "splash.logo_anim",
    "splash.title",
    "splash.title_size",
    "splash.title_color",
    "splash.title_anim",
    "splash.subtitle",
    "splash.subtitle_size",
    "splash.subtitle_color",
    "splash.subtitle_anim",
    "splash.loader_type",
    "splash.loader_color",
    "splash.show_loader",
]


# =========================================================
# فیلدهای وابسته به onboarding (اگه onboarding.enabled=1)
# =========================================================
ONB_REQUIRED = [
    "onboarding.enabled",
    "onboarding.skip_text",
    "onboarding.next_text",
    "onboarding.prev_text",
    "onboarding.start_text",
    "onboarding.btn_bg",
    "onboarding.btn_text_color",
    "onboarding.dot_active",
    "onboarding.dot_inactive",
    "onboarding.s1_enabled",
    "onboarding.s1_bg_type",
    "onboarding.s1_bg_solid",
    "onboarding.s1_bg_1",
    "onboarding.s1_bg_2",
    "onboarding.s1_title",
    "onboarding.s1_title_color",
    "onboarding.s1_text",
    "onboarding.s1_text_color",
    "onboarding.s2_enabled",
    "onboarding.s2_bg_type",
    "onboarding.s2_bg_solid",
    "onboarding.s2_bg_1",
    "onboarding.s2_bg_2",
    "onboarding.s2_title",
    "onboarding.s2_title_color",
    "onboarding.s2_text",
    "onboarding.s2_text_color",
    "onboarding.s3_enabled",
    "onboarding.s3_bg_type",
    "onboarding.s3_bg_solid",
    "onboarding.s3_bg_1",
    "onboarding.s3_bg_2",
    "onboarding.s3_title",
    "onboarding.s3_title_color",
    "onboarding.s3_text",
    "onboarding.s3_text_color",
]


# =========================================================
# فیلدهای welcome (اگه welcome.enabled=1)
# =========================================================
WELCOME_REQUIRED = [
    "welcome.enabled",
    "welcome.title",
    "welcome.subtitle",
    "welcome.icon",
    "welcome.button_text",
    "welcome.button_bg",
    "welcome.button_text_color",
    "welcome.bg_color",
    "welcome.title_color",
    "welcome.subtitle_color",
]


# =========================================================
# فیلدهای vpn (اگه vpn.enabled=1)
# =========================================================
VPN_REQUIRED = [
    "vpn.enabled",
    "vpn.top_title",
    "vpn.top_subtitle",
    "vpn.footnote",
    "vpn.show_recheck",
    "vpn.state_on.icon",
    "vpn.state_on.title",
    "vpn.state_on.text",
    "vpn.state_on.color",
    "vpn.state_on.bg",
    "vpn.state_on.btn",
    "vpn.state_off.icon",
    "vpn.state_off.title",
    "vpn.state_off.text",
    "vpn.state_off.color",
    "vpn.state_off.bg",
    "vpn.state_off.btn",
    "vpn.state_unknown.icon",
    "vpn.state_unknown.title",
    "vpn.state_unknown.text",
    "vpn.state_unknown.color",
    "vpn.state_unknown.bg",
    "vpn.state_unknown.btn",
    "vpn.recheck.text",
    "vpn.recheck.bg",
    "vpn.recheck.border",
    "vpn.recheck.color",
]


# =========================================================
# فیلدهای errors — ۹ نوع خطا (اگه errors.enabled=1)
# =========================================================
ERROR_TYPES = ["offline", "server", "nf", "fb", "to", "dns", "ssl", "conn", "unk"]


def build_errors_required():
    out = []
    for t in ERROR_TYPES:
        out.append(f"errors.{t}_icon")
        out.append(f"errors.{t}_title")
        out.append(f"errors.{t}_text")
        out.append(f"errors.{t}_color")
        out.append(f"errors.{t}_bg")
    return out


ERRORS_REQUIRED = build_errors_required()


def log(m):
    print(f"[inject] {m}")


def fail(m):
    print(f"\n❌ [inject] بیلد متوقف شد\n{m}\n")
    sys.exit(1)


def get(d, path):
    keys = path.split(".")
    cur = d
    for k in keys:
        if not isinstance(cur, dict) or k not in cur:
            return None
        cur = cur[k]
    return cur


def is_present(v):
    if v is None:
        return False
    if isinstance(v, str) and v.strip() == "":
        return False
    return True


def check_all(data):
    """همه‌ی فیلدهای اجباری رو چک می‌کنه."""
    missing = []

    # ===== always required =====
    for f in ALWAYS_REQUIRED:
        if not is_present(get(data, f)):
            missing.append(f)

    # ===== splash =====
    sp_enabled = get(data, "splash.enabled")
    if sp_enabled in (1, True, "1"):
        for f in SPLASH_REQUIRED:
            if not is_present(get(data, f)):
                missing.append(f)

    # ===== onboarding =====
    onb_enabled = get(data, "onboarding.enabled")
    if onb_enabled in (1, True, "1"):
        for f in ONB_REQUIRED:
            if not is_present(get(data, f)):
                missing.append(f)

    # ===== welcome =====
    wel_enabled = get(data, "welcome.enabled")
    if wel_enabled in (1, True, "1"):
        for f in WELCOME_REQUIRED:
            if not is_present(get(data, f)):
                missing.append(f)

    # ===== vpn =====
    vpn_enabled = get(data, "vpn.enabled")
    if vpn_enabled in (1, True, "1"):
        for f in VPN_REQUIRED:
            if not is_present(get(data, f)):
                missing.append(f)

    # ===== errors =====
    err_enabled = get(data, "errors.enabled")
    if err_enabled in (1, True, "1"):
        for f in ERRORS_REQUIRED:
            if not is_present(get(data, f)):
                missing.append(f)

    if missing:
        unique_missing = sorted(set(missing))
        msg = "این فیلدهای اجباری توی app01.json نیستن یا خالی‌ان:\n  - "
        msg += "\n  - ".join(unique_missing)
        msg += f"\n\nتعداد کل: {len(unique_missing)}"
        fail(msg)

    log(f"✅ همه‌ی فیلدهای اجباری موجودن (کل چک: {len(ALWAYS_REQUIRED) + len(SPLASH_REQUIRED) + len(ONB_REQUIRED) + len(WELCOME_REQUIRED) + len(VPN_REQUIRED) + len(ERRORS_REQUIRED)}+ فیلد)")


# =========================================================
# ابزارهای کمکی جاوا
# =========================================================
def hex_to_android(h):
    h = str(h).strip().lstrip("#")
    if len(h) == 6:
        return f"#FF{h.upper()}"
    if len(h) == 8:
        return f"#{h.upper()}"
    fail(f"رنگ نامعتبر: {h}")


def esc_xml(s):
    return str(s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def esc_java(s):
    return (str(s)
            .replace("\\", "\\\\")
            .replace('"', '\\"')
            .replace("\n", "\\n")
            .replace("\r", ""))


def J(s):
    return '"' + esc_java(s) + '"'


def B(v):
    return "true" if v in (1, True, "1", "true", "True") else "false"


def I(v):
    return str(int(v))


def F(v):
    return str(float(v))


def C(h):
    return hex_to_android(h)


# =========================================================
# نوشتن Config.java
# =========================================================
def write_config_java(data, pkg):
    path = JAVA_SRC_DIR / pkg.replace(".", "/") / "Config.java"
    path.parent.mkdir(parents=True, exist_ok=True)

    def g(p):
        return get(data, p)

    L = []
    L.append(f"package {pkg};")
    L.append("")
    L.append("public final class Config {")
    L.append("    private Config() {}")

    # BRANDING
    L.append("")
    L.append("    // ===== BRANDING =====")
    L.append(f"    public static final String APP_NAME = {J(g('branding.app_name'))};")
    L.append(f"    public static final String APP_NAME_EN = {J(g('branding.app_name_en'))};")
    L.append(f"    public static final String PACKAGE_NAME = {J(g('branding.package_name'))};")
    L.append(f"    public static final int VERSION_CODE = {I(g('branding.version_code'))};")
    L.append(f"    public static final String VERSION_NAME = {J(g('branding.version_name'))};")
    L.append(f"    public static final boolean IS_NEW_APP = {B(g('branding.is_new_app'))};")

    # COLORS
    L.append("")
    L.append("    // ===== COLORS =====")
    L.append(f"    public static final String COLOR_PRIMARY = {J(C(g('colors.color_primary')))};")
    L.append(f"    public static final String COLOR_BACKGROUND = {J(C(g('colors.color_background')))};")
    L.append(f"    public static final String COLOR_TEXT = {J(C(g('colors.color_text')))};")
    L.append(f"    public static final String COLOR_TEXT_SECONDARY = {J(C(g('colors.color_text_secondary')))};")
    L.append(f"    public static final String COLOR_BUTTON = {J(C(g('colors.color_button')))};")
    L.append(f"    public static final String COLOR_BUTTON_TEXT = {J(C(g('colors.color_button_text')))};")
    L.append(f"    public static final String COLOR_ACCENT = {J(C(g('colors.color_accent')))};")
    L.append(f"    public static final String BG_TYPE = {J(g('colors.bg_type'))};")
    L.append(f"    public static final String BG_COLOR_SOLID = {J(C(g('colors.bg_color_solid')))};")
    L.append(f"    public static final String BG_COLOR_1 = {J(C(g('colors.bg_color_1')))};")
    L.append(f"    public static final String BG_COLOR_2 = {J(C(g('colors.bg_color_2')))};")
    L.append(f"    public static final int BG_GRADIENT_ANGLE = {I(g('colors.bg_gradient_angle'))};")
    L.append(f"    public static final String BG_EFFECT = {J(g('colors.bg_effect'))};")
    L.append(f"    public static final float BG_EFFECT_OPACITY = {F(g('colors.bg_effect_opacity'))}f;")

    # FONTS
    L.append("")
    L.append("    // ===== FONTS =====")
    L.append(f"    public static final String FONT_FAMILY = {J(g('fonts.font_family'))};")
    L.append(f"    public static final int FONT_WEIGHT = {I(g('fonts.font_weight'))};")
    L.append(f"    public static final int FONT_SIZE_BASE = {I(g('fonts.font_size_base'))};")
    L.append(f"    public static final int TITLE_SIZE = {I(g('fonts.title_size'))};")
    L.append(f"    public static final int TITLE_WEIGHT = {I(g('fonts.title_weight'))};")
    L.append(f"    public static final int SUBTITLE_SIZE = {I(g('fonts.subtitle_size'))};")
    L.append(f"    public static final int SUBTITLE_WEIGHT = {I(g('fonts.subtitle_weight'))};")
    L.append(f"    public static final int BODY_SIZE = {I(g('fonts.body_size'))};")
    L.append(f"    public static final int BODY_WEIGHT = {I(g('fonts.body_weight'))};")
    L.append(f"    public static final int BUTTON_SIZE = {I(g('fonts.button_size'))};")
    L.append(f"    public static final int BUTTON_WEIGHT = {I(g('fonts.button_weight'))};")
    L.append(f"    public static final String ANIM_TITLE = {J(g('fonts.anim_title'))};")
    L.append(f"    public static final String ANIM_TEXT = {J(g('fonts.anim_text'))};")
    L.append(f"    public static final String ANIM_BTN = {J(g('fonts.anim_btn'))};")
    L.append(f"    public static final int ANIM_DURATION = {I(g('fonts.anim_duration'))};")
    L.append(f"    public static final int ANIM_DELAY = {I(g('fonts.anim_delay'))};")

    # SPLASH
    L.append("")
    L.append("    // ===== SPLASH =====")
    sp_en = g("splash.enabled")
    L.append(f"    public static final boolean SPLASH_ENABLED = {B(sp_en)};")
    if sp_en in (1, True, "1"):
        L.append(f"    public static final int SPLASH_DURATION = {I(g('splash.duration'))};")
        L.append(f"    public static final String SPLASH_BG_TYPE = {J(g('splash.bg_type'))};")
        L.append(f"    public static final String SPLASH_BG_COLOR_SOLID = {J(C(g('splash.bg_color_solid')))};")
        L.append(f"    public static final String SPLASH_BG_COLOR_1 = {J(C(g('splash.bg_color_1')))};")
        L.append(f"    public static final String SPLASH_BG_COLOR_2 = {J(C(g('splash.bg_color_2')))};")
        L.append(f"    public static final int SPLASH_LOGO_SIZE = {I(g('splash.logo_size'))};")
        L.append(f"    public static final String SPLASH_LOGO_ANIM = {J(g('splash.logo_anim'))};")
        L.append(f"    public static final String SPLASH_TITLE = {J(g('splash.title'))};")
        L.append(f"    public static final int SPLASH_TITLE_SIZE = {I(g('splash.title_size'))};")
        L.append(f"    public static final String SPLASH_TITLE_COLOR = {J(C(g('splash.title_color')))};")
        L.append(f"    public static final String SPLASH_TITLE_ANIM = {J(g('splash.title_anim'))};")
        L.append(f"    public static final String SPLASH_SUBTITLE = {J(g('splash.subtitle'))};")
        L.append(f"    public static final int SPLASH_SUBTITLE_SIZE = {I(g('splash.subtitle_size'))};")
        L.append(f"    public static final String SPLASH_SUBTITLE_COLOR = {J(C(g('splash.subtitle_color')))};")
        L.append(f"    public static final String SPLASH_SUBTITLE_ANIM = {J(g('splash.subtitle_anim'))};")
        L.append(f"    public static final String SPLASH_LOADER_TYPE = {J(g('splash.loader_type'))};")
        L.append(f"    public static final String SPLASH_LOADER_COLOR = {J(C(g('splash.loader_color')))};")
        L.append(f"    public static final boolean SPLASH_SHOW_LOADER = {B(g('splash.show_loader'))};")
    else:
        L.append("    public static final int SPLASH_DURATION = 0;")
        L.append("    public static final String SPLASH_BG_TYPE = \"solid\";")
        L.append("    public static final String SPLASH_BG_COLOR_SOLID = \"#FFFFFFFF\";")
        L.append("    public static final String SPLASH_BG_COLOR_1 = \"#FFFFFFFF\";")
        L.append("    public static final String SPLASH_BG_COLOR_2 = \"#FFFFFFFF\";")
        L.append("    public static final int SPLASH_LOGO_SIZE = 0;")
        L.append("    public static final String SPLASH_LOGO_ANIM = \"none\";")
        L.append("    public static final String SPLASH_TITLE = \"\";")
        L.append("    public static final int SPLASH_TITLE_SIZE = 0;")
        L.append("    public static final String SPLASH_TITLE_COLOR = \"#FFFFFFFF\";")
        L.append("    public static final String SPLASH_TITLE_ANIM = \"none\";")
        L.append("    public static final String SPLASH_SUBTITLE = \"\";")
        L.append("    public static final int SPLASH_SUBTITLE_SIZE = 0;")
        L.append("    public static final String SPLASH_SUBTITLE_COLOR = \"#FFFFFFFF\";")
        L.append("    public static final String SPLASH_SUBTITLE_ANIM = \"none\";")
        L.append("    public static final String SPLASH_LOADER_TYPE = \"none\";")
        L.append("    public static final String SPLASH_LOADER_COLOR = \"#FFFFFFFF\";")
        L.append("    public static final boolean SPLASH_SHOW_LOADER = false;")

    # ONBOARDING
    L.append("")
    L.append("    // ===== ONBOARDING =====")
    onb_en = g("onboarding.enabled")
    L.append(f"    public static final boolean ONB_ENABLED = {B(onb_en)};")
    if onb_en in (1, True, "1"):
        L.append(f"    public static final String ONB_SKIP_TEXT = {J(g('onboarding.skip_text'))};")
        L.append(f"    public static final String ONB_NEXT_TEXT = {J(g('onboarding.next_text'))};")
        L.append(f"    public static final String ONB_PREV_TEXT = {J(g('onboarding.prev_text'))};")
        L.append(f"    public static final String ONB_START_TEXT = {J(g('onboarding.start_text'))};")
        L.append(f"    public static final String ONB_BTN_BG = {J(C(g('onboarding.btn_bg')))};")
        L.append(f"    public static final String ONB_BTN_TEXT_COLOR = {J(C(g('onboarding.btn_text_color')))};")
        L.append(f"    public static final String ONB_DOT_ACTIVE = {J(C(g('onboarding.dot_active')))};")
        L.append(f"    public static final String ONB_DOT_INACTIVE = {J(C(g('onboarding.dot_inactive')))};")
        for n in (1, 2, 3):
            p = f"onboarding.s{n}_"
            L.append(f"    public static final boolean ONB_S{n}_ENABLED = {B(g(p+'enabled'))};")
            L.append(f"    public static final String ONB_S{n}_BG_TYPE = {J(g(p+'bg_type'))};")
            L.append(f"    public static final String ONB_S{n}_BG_SOLID = {J(C(g(p+'bg_solid')))};")
            L.append(f"    public static final String ONB_S{n}_BG_1 = {J(C(g(p+'bg_1')))};")
            L.append(f"    public static final String ONB_S{n}_BG_2 = {J(C(g(p+'bg_2')))};")
            L.append(f"    public static final String ONB_S{n}_TITLE = {J(g(p+'title'))};")
            L.append(f"    public static final String ONB_S{n}_TITLE_COLOR = {J(C(g(p+'title_color')))};")
            L.append(f"    public static final String ONB_S{n}_TEXT = {J(g(p+'text'))};")
            L.append(f"    public static final String ONB_S{n}_TEXT_COLOR = {J(C(g(p+'text_color')))};")
    else:
        L.append("    public static final String ONB_SKIP_TEXT = \"\";")
        L.append("    public static final String ONB_NEXT_TEXT = \"\";")
        L.append("    public static final String ONB_PREV_TEXT = \"\";")
        L.append("    public static final String ONB_START_TEXT = \"\";")
        L.append("    public static final String ONB_BTN_BG = \"#FFFFFFFF\";")
        L.append("    public static final String ONB_BTN_TEXT_COLOR = \"#FFFFFFFF\";")
        L.append("    public static final String ONB_DOT_ACTIVE = \"#FFFFFFFF\";")
        L.append("    public static final String ONB_DOT_INACTIVE = \"#FFFFFFFF\";")
        for n in (1, 2, 3):
            L.append(f"    public static final boolean ONB_S{n}_ENABLED = false;")
            L.append(f"    public static final String ONB_S{n}_BG_TYPE = \"solid\";")
            L.append(f"    public static final String ONB_S{n}_BG_SOLID = \"#FFFFFFFF\";")
            L.append(f"    public static final String ONB_S{n}_BG_1 = \"#FFFFFFFF\";")
            L.append(f"    public static final String ONB_S{n}_BG_2 = \"#FFFFFFFF\";")
            L.append(f"    public static final String ONB_S{n}_TITLE = \"\";")
            L.append(f"    public static final String ONB_S{n}_TITLE_COLOR = \"#FFFFFFFF\";")
            L.append(f"    public static final String ONB_S{n}_TEXT = \"\";")
            L.append(f"    public static final String ONB_S{n}_TEXT_COLOR = \"#FFFFFFFF\";")

    # WELCOME
    L.append("")
    L.append("    // ===== WELCOME =====")
    wel_en = g("welcome.enabled")
    L.append(f"    public static final boolean WELCOME_ENABLED = {B(wel_en)};")
    if wel_en in (1, True, "1"):
        L.append(f"    public static final String WELCOME_TITLE = {J(g('welcome.title'))};")
        L.append(f"    public static final String WELCOME_SUBTITLE = {J(g('welcome.subtitle'))};")
        L.append(f"    public static final String WELCOME_ICON = {J(g('welcome.icon'))};")
        L.append(f"    public static final String WELCOME_BUTTON_TEXT = {J(g('welcome.button_text'))};")
        L.append(f"    public static final String WELCOME_BUTTON_BG = {J(C(g('welcome.button_bg')))};")
        L.append(f"    public static final String WELCOME_BUTTON_TEXT_COLOR = {J(C(g('welcome.button_text_color')))};")
        L.append(f"    public static final String WELCOME_BG_COLOR = {J(C(g('welcome.bg_color')))};")
        L.append(f"    public static final String WELCOME_TITLE_COLOR = {J(C(g('welcome.title_color')))};")
        L.append(f"    public static final String WELCOME_SUBTITLE_COLOR = {J(C(g('welcome.subtitle_color')))};")
    else:
        L.append("    public static final String WELCOME_TITLE = \"\";")
        L.append("    public static final String WELCOME_SUBTITLE = \"\";")
        L.append("    public static final String WELCOME_ICON = \"\";")
        L.append("    public static final String WELCOME_BUTTON_TEXT = \"\";")
        L.append("    public static final String WELCOME_BUTTON_BG = \"#FFFFFFFF\";")
        L.append("    public static final String WELCOME_BUTTON_TEXT_COLOR = \"#FFFFFFFF\";")
        L.append("    public static final String WELCOME_BG_COLOR = \"#FFFFFFFF\";")
        L.append("    public static final String WELCOME_TITLE_COLOR = \"#FFFFFFFF\";")
        L.append("    public static final String WELCOME_SUBTITLE_COLOR = \"#FFFFFFFF\";")

    # VPN
    L.append("")
    L.append("    // ===== VPN =====")
    vpn_en = g("vpn.enabled")
    L.append(f"    public static final boolean VPN_ENABLED = {B(vpn_en)};")
    if vpn_en in (1, True, "1"):
        L.append(f"    public static final String VPN_TOP_TITLE = {J(g('vpn.top_title'))};")
        L.append(f"    public static final String VPN_TOP_SUBTITLE = {J(g('vpn.top_subtitle'))};")
        L.append(f"    public static final String VPN_FOOTNOTE = {J(g('vpn.footnote'))};")
        L.append(f"    public static final boolean VPN_SHOW_RECHECK = {B(g('vpn.show_recheck'))};")
        L.append(f"    public static final String VPN_ON_ICON = {J(g('vpn.state_on.icon'))};")
        L.append(f"    public static final String VPN_ON_TITLE = {J(g('vpn.state_on.title'))};")
        L.append(f"    public static final String VPN_ON_TEXT = {J(g('vpn.state_on.text'))};")
        L.append(f"    public static final String VPN_ON_COLOR = {J(C(g('vpn.state_on.color')))};")
        L.append(f"    public static final String VPN_ON_BG = {J(C(g('vpn.state_on.bg')))};")
        L.append(f"    public static final String VPN_ON_BTN = {J(g('vpn.state_on.btn'))};")
        L.append(f"    public static final String VPN_OFF_ICON = {J(g('vpn.state_off.icon'))};")
        L.append(f"    public static final String VPN_OFF_TITLE = {J(g('vpn.state_off.title'))};")
        L.append(f"    public static final String VPN_OFF_TEXT = {J(g('vpn.state_off.text'))};")
        L.append(f"    public static final String VPN_OFF_COLOR = {J(C(g('vpn.state_off.color')))};")
        L.append(f"    public static final String VPN_OFF_BG = {J(C(g('vpn.state_off.bg')))};")
        L.append(f"    public static final String VPN_OFF_BTN = {J(g('vpn.state_off.btn'))};")
        L.append(f"    public static final String VPN_UNK_ICON = {J(g('vpn.state_unknown.icon'))};")
        L.append(f"    public static final String VPN_UNK_TITLE = {J(g('vpn.state_unknown.title'))};")
        L.append(f"    public static final String VPN_UNK_TEXT = {J(g('vpn.state_unknown.text'))};")
        L.append(f"    public static final String VPN_UNK_COLOR = {J(C(g('vpn.state_unknown.color')))};")
        L.append(f"    public static final String VPN_UNK_BG = {J(C(g('vpn.state_unknown.bg')))};")
        L.append(f"    public static final String VPN_UNK_BTN = {J(g('vpn.state_unknown.btn'))};")
        L.append(f"    public static final String VPN_RECHECK_TEXT = {J(g('vpn.recheck.text'))};")
        L.append(f"    public static final String VPN_RECHECK_BG = {J(g('vpn.recheck.bg'))};")
        L.append(f"    public static final String VPN_RECHECK_BORDER = {J(C(g('vpn.recheck.border')))};")
        L.append(f"    public static final String VPN_RECHECK_COLOR = {J(C(g('vpn.recheck.color')))};")
    else:
        for k in ["TOP_TITLE","TOP_SUBTITLE","FOOTNOTE","ON_ICON","ON_TITLE","ON_TEXT",
                  "ON_COLOR","ON_BG","ON_BTN","OFF_ICON","OFF_TITLE","OFF_TEXT",
                  "OFF_COLOR","OFF_BG","OFF_BTN","UNK_ICON","UNK_TITLE","UNK_TEXT",
                  "UNK_COLOR","UNK_BG","UNK_BTN","RECHECK_TEXT","RECHECK_BG",
                  "RECHECK_BORDER","RECHECK_COLOR"]:
            L.append(f'    public static final String VPN_{k} = "";')
        L.append("    public static final boolean VPN_SHOW_RECHECK = false;")

    # WEBVIEW
    L.append("")
    L.append("    // ===== WEBVIEW =====")
    L.append(f"    public static final String WV_URL = {J(g('webview.url'))};")
    L.append(f"    public static final String WV_URL_TYPE = {J(g('webview.url_type'))};")
    L.append(f"    public static final String WV_URL_HOME = {J(g('webview.url_home'))};")
    L.append(f"    public static final String WV_USER_AGENT = {J(g('webview.user_agent'))};")
    L.append(f"    public static final String WV_USER_AGENT_CUSTOM = {J(g('webview.user_agent_custom'))};")
    L.append(f"    public static final boolean WV_PROGRESS_BAR = {B(g('webview.progress_bar'))};")
    L.append(f"    public static final String WV_PROGRESS_COLOR = {J(C(g('webview.progress_color')))};")
    L.append(f"    public static final int WV_PROGRESS_HEIGHT = {I(g('webview.progress_height'))};")
    L.append(f"    public static final boolean WV_ZOOM = {B(g('webview.zoom_enabled'))};")
    L.append(f"    public static final boolean WV_JS = {B(g('webview.js_enabled'))};")
    L.append(f"    public static final boolean WV_DOM = {B(g('webview.dom_storage'))};")
    L.append(f"    public static final boolean WV_DATABASE = {B(g('webview.database'))};")
    L.append(f"    public static final boolean WV_GEOLOCATION = {B(g('webview.geolocation'))};")
    L.append(f"    public static final boolean WV_FILE_UPLOAD = {B(g('webview.file_upload'))};")
    L.append(f"    public static final boolean WV_CAMERA = {B(g('webview.camera'))};")
    L.append(f"    public static final boolean WV_MIC = {B(g('webview.microphone'))};")
    L.append(f"    public static final boolean WV_PULL_REFRESH = {B(g('webview.pull_to_refresh'))};")
    L.append(f"    public static final String WV_PULL_TEXT = {J(g('webview.pull_text'))};")
    L.append(f"    public static final String WV_PULL_RELEASE = {J(g('webview.pull_release'))};")
    L.append(f"    public static final String WV_PULL_LOADING = {J(g('webview.pull_loading'))};")
    L.append(f"    public static final String WV_PULL_COLOR = {J(C(g('webview.pull_color')))};")
    L.append(f"    public static final boolean WV_BACK_BUTTON = {B(g('webview.back_button'))};")
    L.append(f"    public static final String WV_BACK_EXIT_MSG = {J(g('webview.back_exit_msg'))};")
    L.append(f"    public static final boolean WV_BACK_DOUBLE = {B(g('webview.back_double'))};")
    L.append(f"    public static final String WV_EXTERNAL_LINKS = {J(g('webview.external_links'))};")
    L.append(f"    public static final String WV_MAIL_LINKS = {J(g('webview.mail_links'))};")
    L.append(f"    public static final String WV_TEL_LINKS = {J(g('webview.tel_links'))};")
    L.append(f"    public static final String WV_WHATSAPP_LINKS = {J(g('webview.whatsapp_links'))};")
    L.append(f"    public static final String WV_TELEGRAM_LINKS = {J(g('webview.telegram_links'))};")
    L.append(f"    public static final String WV_INSTAGRAM_LINKS = {J(g('webview.instagram_links'))};")
    L.append(f"    public static final int WV_TIMEOUT = {I(g('webview.timeout'))};")
    L.append(f"    public static final int WV_RETRY_COUNT = {I(g('webview.retry_count'))};")
    L.append(f"    public static final boolean WV_CACHE = {B(g('webview.cache_enabled'))};")
    L.append(f"    public static final String WV_CACHE_MODE = {J(g('webview.cache_mode'))};")
    L.append(f"    public static final boolean WV_SAFE_BROWSING = {B(g('webview.safe_browsing'))};")
    L.append(f"    public static final boolean WV_BLOCK_ADS = {B(g('webview.block_ads'))};")
    L.append(f"    public static final boolean WV_SHOW_SPLASH_ON_WEBVIEW = {B(g('webview.show_splash_on_webview'))};")

    # ERRORS
    L.append("")
    L.append("    // ===== ERRORS =====")
    err_en = g("errors.enabled")
    L.append(f"    public static final boolean ERR_ENABLED = {B(err_en)};")
    L.append(f"    public static final boolean ERR_SHOW_RETRY = {B(g('errors.show_retry'))};")
    L.append(f"    public static final boolean ERR_SHOW_HOME = {B(g('errors.show_home'))};")
    L.append(f"    public static final boolean ERR_AUTO_RETRY = {B(g('errors.auto_retry'))};")
    L.append(f"    public static final int ERR_AUTO_RETRY_SEC = {I(g('errors.auto_retry_sec'))};")
    L.append(f"    public static final String ERR_RETRY_TEXT = {J(g('errors.retry_text'))};")
    L.append(f"    public static final String ERR_RETRY_BG = {J(C(g('errors.retry_bg')))};")
    L.append(f"    public static final String ERR_RETRY_COLOR = {J(C(g('errors.retry_color')))};")
    L.append(f"    public static final String ERR_HOME_TEXT = {J(g('errors.home_text'))};")
    L.append(f"    public static final String ERR_HOME_BG = {J(g('errors.home_bg'))};")
    L.append(f"    public static final String ERR_HOME_COLOR = {J(C(g('errors.home_color')))};")
    L.append(f"    public static final String ERR_HOME_BORDER = {J(C(g('errors.home_border')))};")

    if err_en in (1, True, "1"):
        for t in ERROR_TYPES:
            T = t.upper()
            L.append(f"    public static final String ERR_{T}_ICON = {J(g(f'errors.{t}_icon'))};")
            L.append(f"    public static final String ERR_{T}_TITLE = {J(g(f'errors.{t}_title'))};")
            L.append(f"    public static final String ERR_{T}_TEXT = {J(g(f'errors.{t}_text'))};")
            L.append(f"    public static final String ERR_{T}_COLOR = {J(C(g(f'errors.{t}_color')))};")
            L.append(f"    public static final String ERR_{T}_BG = {J(C(g(f'errors.{t}_bg')))};")
    else:
        for t in ERROR_TYPES:
            T = t.upper()
            L.append(f'    public static final String ERR_{T}_ICON = "";')
            L.append(f'    public static final String ERR_{T}_TITLE = "";')
            L.append(f'    public static final String ERR_{T}_TEXT = "";')
            L.append(f'    public static final String ERR_{T}_COLOR = "#FFFFFFFF";')
            L.append(f'    public static final String ERR_{T}_BG = "#FFFFFFFF";')

    # EXIT
    L.append("")
    L.append("    // ===== EXIT =====")
    L.append(f"    public static final boolean EXIT_ENABLED = {B(g('exit.enabled'))};")
    L.append(f"    public static final boolean EXIT_DOUBLE_BACK = {B(g('exit.double_back'))};")
    L.append(f"    public static final String EXIT_DOUBLE_BACK_MSG = {J(g('exit.double_back_msg'))};")
    L.append(f"    public static final boolean EXIT_SHOW_ICON = {B(g('exit.show_icon'))};")
    L.append(f"    public static final String EXIT_DIALOG_TYPE = {J(g('exit.dialog_type'))};")
    L.append(f"    public static final int EXIT_RADIUS = {I(g('exit.radius'))};")
    L.append(f"    public static final int EXIT_BORDER_WIDTH = {I(g('exit.border_width'))};")
    L.append(f"    public static final String EXIT_ICON = {J(g('exit.icon'))};")
    L.append(f"    public static final String EXIT_TITLE = {J(g('exit.title'))};")
    L.append(f"    public static final String EXIT_TITLE_COLOR = {J(C(g('exit.title_color')))};")
    L.append(f"    public static final String EXIT_TEXT = {J(g('exit.text'))};")
    L.append(f"    public static final String EXIT_TEXT_COLOR = {J(C(g('exit.text_color')))};")
    L.append(f"    public static final String EXIT_BG_COLOR = {J(C(g('exit.bg_color')))};")
    L.append(f"    public static final String EXIT_BORDER_COLOR = {J(C(g('exit.border_color')))};")
    L.append(f"    public static final String EXIT_OVERLAY_COLOR = {J(g('exit.overlay_color'))};")
    L.append(f"    public static final String EXIT_BTN_CONFIRM_TEXT = {J(g('exit.btn_confirm_text'))};")
    L.append(f"    public static final String EXIT_BTN_CONFIRM_BG = {J(C(g('exit.btn_confirm_bg')))};")
    L.append(f"    public static final String EXIT_BTN_CONFIRM_COLOR = {J(C(g('exit.btn_confirm_color')))};")
    L.append(f"    public static final String EXIT_BTN_CANCEL_TEXT = {J(g('exit.btn_cancel_text'))};")
    L.append(f"    public static final String EXIT_BTN_CANCEL_BG = {J(C(g('exit.btn_cancel_bg')))};")
    L.append(f"    public static final String EXIT_BTN_CANCEL_COLOR = {J(C(g('exit.btn_cancel_color')))};")
    L.append(f"    public static final String EXIT_BTN_LAYOUT = {J(g('exit.btn_layout'))};")

    # ADVANCED
    L.append("")
    L.append("    // ===== ADVANCED =====")
    L.append(f"    public static final String ADV_OUTPUT_NAME = {J(g('advanced.output_name'))};")
    L.append(f"    public static final String ADV_OUTPUT_FORMAT = {J(g('advanced.output_format'))};")
    L.append(f"    public static final int ADV_MIN_SDK = {I(g('advanced.min_sdk'))};")
    L.append(f"    public static final int ADV_TARGET_SDK = {I(g('advanced.target_sdk'))};")
    L.append(f"    public static final String ADV_ARCHITECTURE = {J(g('advanced.architecture'))};")
    L.append(f"    public static final String ADV_DEVELOPER_NAME = {J(g('advanced.developer_name'))};")
    L.append(f"    public static final String ADV_DEVELOPER_EMAIL = {J(g('advanced.developer_email'))};")
    L.append(f"    public static final String ADV_WEBSITE = {J(g('advanced.website'))};")
    L.append(f"    public static final String ADV_DESCRIPTION = {J(g('advanced.description'))};")

    L.append("}")

    path.write_text("\n".join(L), encoding="utf-8")
    log(f"✅ Config.java نوشته شد ({len(L)} خط)")


# =========================================================
# strings.xml, colors.xml, dimens.xml, styles.xml
# =========================================================
def write_strings(data):
    path = RES_DIR / "values" / "strings.xml"
    path.parent.mkdir(parents=True, exist_ok=True)
    items = [
        ("app_name", get(data, "branding.app_name")),
        ("base_url", get(data, "webview.url")),
    ]
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>", ""]
    for k, v in items:
        lines.append(f'    <string name="{k}">{esc_xml(v)}</string>')
    lines += ["", "</resources>"]
    path.write_text("\n".join(lines), encoding="utf-8")
    log("✅ strings.xml نوشته شد")


def write_colors(data):
    path = RES_DIR / "values" / "colors.xml"
    path.parent.mkdir(parents=True, exist_ok=True)
    items = {
        "color_primary": C(get(data, "colors.color_primary")),
        "color_background": C(get(data, "colors.color_background")),
        "color_text": C(get(data, "colors.color_text")),
        "color_text_secondary": C(get(data, "colors.color_text_secondary")),
        "color_button": C(get(data, "colors.color_button")),
        "color_button_text": C(get(data, "colors.color_button_text")),
        "color_accent": C(get(data, "colors.color_accent")),
    }
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>", ""]
    for k, v in items.items():
        lines.append(f'    <color name="{k}">{v}</color>')
    lines += ["", "</resources>"]
    path.write_text("\n".join(lines), encoding="utf-8")
    log("✅ colors.xml نوشته شد")


def write_dimens(data):
    path = RES_DIR / "values" / "dimens.xml"
    path.parent.mkdir(parents=True, exist_ok=True)
    items = {
        "font_size_base": I(get(data, "fonts.font_size_base")),
        "title_size": I(get(data, "fonts.title_size")),
        "subtitle_size": I(get(data, "fonts.subtitle_size")),
        "body_size": I(get(data, "fonts.body_size")),
        "button_size": I(get(data, "fonts.button_size")),
    }
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>", ""]
    for k, v in items.items():
        lines.append(f'    <dimen name="{k}">{v}sp</dimen>')
    lines += ["", "</resources>"]
    path.write_text("\n".join(lines), encoding="utf-8")
    log("✅ dimens.xml نوشته شد")


def write_styles(data):
    path = RES_DIR / "values" / "styles.xml"
    path.parent.mkdir(parents=True, exist_ok=True)
    content = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.App" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/color_primary</item>
        <item name="colorAccent">@color/color_accent</item>
        <item name="android:windowBackground">@color/color_background</item>
        <item name="android:statusBarColor">@color/color_background</item>
        <item name="android:windowLightStatusBar">true</item>
        <item name="android:navigationBarColor">@color/color_background</item>
        <item name="android:fontFamily">sans-serif</item>
    </style>

    <style name="Theme.App.Splash" parent="Theme.App">
        <item name="android:windowBackground">@color/color_background</item>
        <item name="android:statusBarColor">@color/color_background</item>
        <item name="android:navigationBarColor">@color/color_background</item>
    </style>
</resources>
"""
    path.write_text(content, encoding="utf-8")
    log("✅ styles.xml نوشته شد")


# =========================================================
# جابجایی فایل‌های جاوا
# =========================================================
def move_java_files(pkg):
    old_dir = JAVA_SRC_DIR / PKG_OLD.replace(".", "/")
    new_dir = JAVA_SRC_DIR / pkg.replace(".", "/")

    if not old_dir.exists():
        log(f"⚠️ پوشه‌ی قدیمی نیست: {old_dir}")
        return

    new_dir.mkdir(parents=True, exist_ok=True)

    for src in list(old_dir.glob("*.java")):
        dst = new_dir / src.name
        content = src.read_text(encoding="utf-8")
        content = re.sub(
            r'^package\s+app\.vista\s*;',
            f'package {pkg};',
            content,
            flags=re.MULTILINE,
        )
        content = re.sub(
            r'import\s+app\.vista\.',
            f'import {pkg}.',
            content,
        )
        dst.write_text(content, encoding="utf-8")
        log(f"✅ {src.name} → {new_dir.relative_to(JAVA_SRC_DIR)}/{src.name}")

    shutil.rmtree(old_dir)
    log("✅ پوشه‌ی قدیمی حذف شد")


# =========================================================
# کپی عکس‌ها
# =========================================================
def copy_assets():
    target = RES_DIR / "mipmap-xxhdpi"
    target.mkdir(parents=True, exist_ok=True)

    icon = ASSETS_DIR / "icon-144.png"
    if not icon.exists():
        fail("عکس پیدا نشد: assets/icon-144.png")

    shutil.copy(icon, target / "ic_launcher.png")
    shutil.copy(icon, target / "ic_launcher_foreground.png")
    log("✅ آیکون اپ کپی شد")

    logo = ASSETS_DIR / "splash-logo.png"
    dst = RES_DIR / "drawable" / "splash_logo.png"
    dst.parent.mkdir(parents=True, exist_ok=True)
    if logo.exists():
        shutil.copy(logo, dst)
        log("✅ لوگو اسپلش کپی شد")
    else:
        fail("عکس پیدا نشد: assets/splash-logo.png")


# =========================================================
# build.gradle
# =========================================================
def write_gradle(data):
    b_pkg = get(data, "branding.package_name")
    b_ver_code = I(get(data, "branding.version_code"))
    b_ver_name = get(data, "branding.version_name")
    adv_min = I(get(data, "advanced.min_sdk"))
    adv_target = I(get(data, "advanced.target_sdk"))

    content = f"""plugins {{
    id 'com.android.application'
}}

android {{
    namespace '{b_pkg}'
    compileSdk 35

    defaultConfig {{
        applicationId "{b_pkg}"
        minSdk {adv_min}
        targetSdk {adv_target}
        versionCode {b_ver_code}
        versionName "{b_ver_name}"
    }}

    signingConfigs {{
        release {{
            if (project.hasProperty('RELEASE_STORE_FILE')) {{
                storeFile file(RELEASE_STORE_FILE)
                storePassword RELEASE_STORE_PASSWORD
                keyAlias RELEASE_KEY_ALIAS
                keyPassword RELEASE_KEY_PASSWORD
            }}
        }}
    }}

    buildTypes {{
        debug {{
            minifyEnabled false
            if (project.hasProperty('RELEASE_STORE_FILE')) {{
                signingConfig signingConfigs.release
            }}
        }}
        release {{
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            if (project.hasProperty('RELEASE_STORE_FILE')) {{
                signingConfig signingConfigs.release
            }}
        }}
    }}

    compileOptions {{
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }}

    buildFeatures {{
        buildConfig true
    }}

    packaging {{
        resources {{
            excludes += [
                'META-INF/DEPENDENCIES',
                'META-INF/LICENSE',
                'META-INF/NOTICE',
                'META-INF/*.kotlin_module'
            ]
        }}
    }}

    applicationVariants.all {{ variant ->
        variant.outputs.all {{
            outputFileName = "{b_pkg}-{b_ver_name}-" + variant.buildType.name + ".apk"
        }}
    }}
}}

dependencies {{
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'androidx.core:core:1.13.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'com.google.android.material:material:1.12.0'
    implementation 'androidx.webkit:webkit:1.11.0'
    implementation 'androidx.viewpager2:viewpager2:1.1.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
}}
"""
    GRADLE_APP.write_text(content, encoding="utf-8")
    log("✅ app/build.gradle نوشته شد")


# =========================================================
# main
# =========================================================
def main():
    if not CONFIG_FILE.exists():
        fail(f"پیدا نشد: {CONFIG_FILE}")

    log(f"خوندن {CONFIG_FILE}")
    try:
        data = json.loads(CONFIG_FILE.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        fail(f"app01.json معتبر نیست: {e}")

    check_all(data)

    pkg = get(data, "branding.package_name")

    write_config_java(data, pkg)
    write_strings(data)
    write_colors(data)
    write_dimens(data)
    write_styles(data)
    move_java_files(pkg)
    copy_assets()
    write_gradle(data)

    log("🎉 همه‌چیز آماده شد")


if __name__ == "__main__":
    main()
