#!/usr/bin/env python3
# =========================================================
# inject.py — JSON به Config.java + resources
# مسیر: scripts/inject.py
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


REQUIRED_FIELDS = [
    "branding.app_name",
    "branding.package_name",
    "branding.version_code",
    "branding.version_name",
    "colors.color_primary",
    "colors.color_background",
    "colors.color_text",
    "colors.color_button",
    "colors.color_button_text",
    "webview.url",
    "advanced.min_sdk",
    "advanced.target_sdk",
]


def log(m):
    print(f"[inject] {m}")


def fail(m):
    print(f"\n[ERROR] {m}\n")
    sys.exit(1)


def get(d, path, default=None):
    keys = path.split(".")
    cur = d
    for k in keys:
        if not isinstance(cur, dict) or k not in cur:
            return default
        cur = cur[k]
    return cur


def require(d):
    miss = []
    for f in REQUIRED_FIELDS:
        v = get(d, f)
        if v is None or (isinstance(v, str) and v.strip() == ""):
            miss.append(f)
    if miss:
        fail("فیلدهای اجباری خالی:\n  - " + "\n  - ".join(miss))


def hex_to_android(h):
    h = h.strip().lstrip("#")
    if len(h) == 6:
        return f"#FF{h.upper()}"
    if len(h) == 8:
        return f"#{h.upper()}"
    fail(f"رنگ نامعتبر: {h}")


def esc_xml(s):
    if s is None:
        return ""
    return (str(s)
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;"))


def esc_java(s):
    if s is None:
        return ""
    return (str(s)
            .replace("\\", "\\\\")
            .replace('"', '\\"')
            .replace("\n", "\\n")
            .replace("\r", ""))


def javastr(s):
    return '"' + esc_java(s) + '"'


def javabool(v):
    return "true" if v in (1, True, "1", "true", "True") else "false"


def javaint(v, default=0):
    try:
        return str(int(v))
    except Exception:
        return str(default)


def javafloat(v, default=0.5):
    try:
        return str(float(v))
    except Exception:
        return str(default)


def color_java(h):
    return hex_to_android(h)


# =========================================================
# Config.java
# =========================================================
def write_config_java(data, pkg):
    path = JAVA_SRC_DIR / pkg.replace(".", "/") / "Config.java"
    path.parent.mkdir(parents=True, exist_ok=True)

    b = data.get("branding", {})
    c = data.get("colors", {})
    f = data.get("fonts", {})
    s = data.get("splash", {})
    o = data.get("onboarding", {})
    w = data.get("welcome", {})
    v = data.get("vpn", {})
    wv = data.get("webview", {})
    e = data.get("errors", {})
    ex = data.get("exit", {})
    adv = data.get("advanced", {})

    L = []
    L.append(f"package {pkg};")
    L.append("")
    L.append("// =====================================================")
    L.append("// Config.java — همه‌ی تنظیمات از app01.json")
    L.append("// این فایل خودکار توسط inject.py ساخته میشه")
    L.append("// =====================================================")
    L.append("public final class Config {")
    L.append("    private Config() {}")
    L.append("")
    L.append("    // ===== BRANDING =====")
    L.append(f"    public static final String APP_NAME = {javastr(b.get('app_name',''))};")
    L.append(f"    public static final String APP_NAME_EN = {javastr(b.get('app_name_en',''))};")
    L.append(f"    public static final String PACKAGE_NAME = {javastr(b.get('package_name',''))};")
    L.append(f"    public static final int VERSION_CODE = {javaint(b.get('version_code',1),1)};")
    L.append(f"    public static final String VERSION_NAME = {javastr(b.get('version_name','1.0.0'))};")
    L.append(f"    public static final boolean IS_NEW_APP = {javabool(b.get('is_new_app',0))};")
    L.append("")
    L.append("    // ===== COLORS =====")
    for k in ["color_primary","color_background","color_text","color_text_secondary",
              "color_button","color_button_text","color_accent"]:
        L.append(f"    public static final String {k.upper()} = {javastr(color_java(c.get(k,'#000000')))};")
    L.append(f"    public static final String BG_TYPE = {javastr(c.get('bg_type','solid'))};")
    L.append(f"    public static final String BG_COLOR_SOLID = {javastr(color_java(c.get('bg_color_solid', c.get('bg_color_1','#000000'))))};")
    L.append(f"    public static final String BG_COLOR_1 = {javastr(color_java(c.get('bg_color_1','#000000')))};")
    L.append(f"    public static final String BG_COLOR_2 = {javastr(color_java(c.get('bg_color_2','#000000')))};")
    L.append(f"    public static final int BG_GRADIENT_ANGLE = {javaint(c.get('bg_gradient_angle',180),180)};")
    L.append(f"    public static final String BG_EFFECT = {javastr(c.get('bg_effect','none'))};")
    L.append(f"    public static final float BG_EFFECT_OPACITY = {javafloat(c.get('bg_effect_opacity',0.5),0.5)}f;")
    L.append("")
    L.append("    // ===== FONTS =====")
    L.append(f"    public static final String FONT_FAMILY = {javastr(f.get('font_family','sans-serif'))};")
    L.append(f"    public static final int FONT_WEIGHT = {javaint(f.get('font_weight',400),400)};")
    L.append(f"    public static final int FONT_SIZE_BASE = {javaint(f.get('font_size_base',16),16)};")
    L.append(f"    public static final int TITLE_SIZE = {javaint(f.get('title_size',24),24)};")
    L.append(f"    public static final int TITLE_WEIGHT = {javaint(f.get('title_weight',800),800)};")
    L.append(f"    public static final int SUBTITLE_SIZE = {javaint(f.get('subtitle_size',18),18)};")
    L.append(f"    public static final int SUBTITLE_WEIGHT = {javaint(f.get('subtitle_weight',600),600)};")
    L.append(f"    public static final int BODY_SIZE = {javaint(f.get('body_size',14),14)};")
    L.append(f"    public static final int BODY_WEIGHT = {javaint(f.get('body_weight',400),400)};")
    L.append(f"    public static final int BUTTON_SIZE = {javaint(f.get('button_size',16),16)};")
    L.append(f"    public static final int BUTTON_WEIGHT = {javaint(f.get('button_weight',800),800)};")
    L.append(f"    public static final String ANIM_TITLE = {javastr(f.get('anim_title','fadeUp'))};")
    L.append(f"    public static final String ANIM_TEXT = {javastr(f.get('anim_text','fadeUp'))};")
    L.append(f"    public static final String ANIM_BTN = {javastr(f.get('anim_btn','scale'))};")
    L.append(f"    public static final int ANIM_DURATION = {javaint(f.get('anim_duration',500),500)};")
    L.append(f"    public static final int ANIM_DELAY = {javaint(f.get('anim_delay',100),100)};")
    L.append("")
    L.append("    // ===== SPLASH =====")
    L.append(f"    public static final boolean SPLASH_ENABLED = {javabool(s.get('enabled',1))};")
    L.append(f"    public static final int SPLASH_DURATION = {javaint(s.get('duration',2000),2000)};")
    L.append(f"    public static final String SPLASH_BG_TYPE = {javastr(s.get('bg_type','gradient'))};")
    L.append(f"    public static final String SPLASH_BG_COLOR_SOLID = {javastr(color_java(s.get('bg_color_solid', s.get('bg_color_1','#000000'))))};")
    L.append(f"    public static final String SPLASH_BG_COLOR_1 = {javastr(color_java(s.get('bg_color_1','#000000')))};")
    L.append(f"    public static final String SPLASH_BG_COLOR_2 = {javastr(color_java(s.get('bg_color_2','#000000')))};")
    L.append(f"    public static final int SPLASH_LOGO_SIZE = {javaint(s.get('logo_size',180),180)};")
    L.append(f"    public static final String SPLASH_LOGO_ANIM = {javastr(s.get('logo_anim','pulse'))};")
    L.append(f"    public static final String SPLASH_TITLE = {javastr(s.get('title',''))};")
    L.append(f"    public static final int SPLASH_TITLE_SIZE = {javaint(s.get('title_size',28),28)};")
    L.append(f"    public static final String SPLASH_TITLE_COLOR = {javastr(color_java(s.get('title_color','#FFFFFF')))};")
    L.append(f"    public static final String SPLASH_TITLE_ANIM = {javastr(s.get('title_anim','fadeUp'))};")
    L.append(f"    public static final String SPLASH_SUBTITLE = {javastr(s.get('subtitle',''))};")
    L.append(f"    public static final int SPLASH_SUBTITLE_SIZE = {javaint(s.get('subtitle_size',16),16)};")
    L.append(f"    public static final String SPLASH_SUBTITLE_COLOR = {javastr(color_java(s.get('subtitle_color','#FFFFFF')))};")
    L.append(f"    public static final String SPLASH_SUBTITLE_ANIM = {javastr(s.get('subtitle_anim','fadeUp'))};")
    L.append(f"    public static final String SPLASH_LOADER_TYPE = {javastr(s.get('loader_type','dots'))};")
    L.append(f"    public static final String SPLASH_LOADER_COLOR = {javastr(color_java(s.get('loader_color','#FFFFFF')))};")
    L.append(f"    public static final boolean SPLASH_SHOW_LOADER = {javabool(s.get('show_loader',1))};")
    L.append("")
    L.append("    // ===== ONBOARDING =====")
    L.append(f"    public static final boolean ONB_ENABLED = {javabool(o.get('enabled',0))};")
    L.append(f"    public static final String ONB_SKIP_TEXT = {javastr(o.get('skip_text','رد کردن'))};")
    L.append(f"    public static final String ONB_NEXT_TEXT = {javastr(o.get('next_text','بعدی'))};")
    L.append(f"    public static final String ONB_PREV_TEXT = {javastr(o.get('prev_text','قبلی'))};")
    L.append(f"    public static final String ONB_START_TEXT = {javastr(o.get('start_text','شروع کن'))};")
    L.append(f"    public static final String ONB_BTN_BG = {javastr(color_java(o.get('btn_bg','#000000')))};")
    L.append(f"    public static final String ONB_BTN_TEXT_COLOR = {javastr(color_java(o.get('btn_text_color','#FFFFFF')))};")
    L.append(f"    public static final String ONB_DOT_ACTIVE = {javastr(color_java(o.get('dot_active','#FFFFFF')))};")
    L.append(f"    public static final String ONB_DOT_INACTIVE = {javastr(color_java(o.get('dot_inactive','#888888')))};")
    for n in (1, 2, 3):
        p = f"s{n}_"
        L.append(f"    public static final boolean ONB_S{n}_ENABLED = {javabool(o.get(p+'enabled',1))};")
        L.append(f"    public static final String ONB_S{n}_BG_TYPE = {javastr(o.get(p+'bg_type','solid'))};")
        L.append(f"    public static final String ONB_S{n}_BG_SOLID = {javastr(color_java(o.get(p+'bg_solid', o.get(p+'bg_1','#000000'))))};")
        L.append(f"    public static final String ONB_S{n}_BG_1 = {javastr(color_java(o.get(p+'bg_1','#000000')))};")
        L.append(f"    public static final String ONB_S{n}_BG_2 = {javastr(color_java(o.get(p+'bg_2','#000000')))};")
        L.append(f"    public static final String ONB_S{n}_TITLE = {javastr(o.get(p+'title',''))};")
        L.append(f"    public static final String ONB_S{n}_TITLE_COLOR = {javastr(color_java(o.get(p+'title_color','#FFFFFF')))};")
        L.append(f"    public static final String ONB_S{n}_TEXT = {javastr(o.get(p+'text',''))};")
        L.append(f"    public static final String ONB_S{n}_TEXT_COLOR = {javastr(color_java(o.get(p+'text_color','#FFFFFF')))};")
    L.append("")
    L.append("    // ===== WELCOME =====")
    L.append(f"    public static final boolean WELCOME_ENABLED = {javabool(w.get('enabled',0))};")
    L.append(f"    public static final String WELCOME_TITLE = {javastr(w.get('title',''))};")
    L.append(f"    public static final String WELCOME_SUBTITLE = {javastr(w.get('subtitle',''))};")
    L.append(f"    public static final String WELCOME_ICON = {javastr(w.get('icon',''))};")
    L.append(f"    public static final String WELCOME_BUTTON_TEXT = {javastr(w.get('button_text','ورود'))};")
    L.append(f"    public static final String WELCOME_BUTTON_BG = {javastr(color_java(w.get('button_bg','#000000')))};")
    L.append(f"    public static final String WELCOME_BUTTON_TEXT_COLOR = {javastr(color_java(w.get('button_text_color','#FFFFFF')))};")
    L.append(f"    public static final String WELCOME_BG_COLOR = {javastr(color_java(w.get('bg_color','#000000')))};")
    L.append(f"    public static final String WELCOME_TITLE_COLOR = {javastr(color_java(w.get('title_color','#FFFFFF')))};")
    L.append(f"    public static final String WELCOME_SUBTITLE_COLOR = {javastr(color_java(w.get('subtitle_color','#FFFFFF')))};")
    L.append("")
    L.append("    // ===== VPN =====")
    L.append(f"    public static final boolean VPN_ENABLED = {javabool(v.get('enabled',0))};")
    L.append(f"    public static final String VPN_TOP_TITLE = {javastr(v.get('top_title',''))};")
    L.append(f"    public static final String VPN_TOP_SUBTITLE = {javastr(v.get('top_subtitle',''))};")
    L.append(f"    public static final String VPN_FOOTNOTE = {javastr(v.get('footnote',''))};")
    L.append(f"    public static final boolean VPN_SHOW_RECHECK = {javabool(v.get('show_recheck',0))};")
    on = v.get("state_on", {})
    L.append(f"    public static final String VPN_ON_ICON = {javastr(on.get('icon',''))};")
    L.append(f"    public static final String VPN_ON_TITLE = {javastr(on.get('title',''))};")
    L.append(f"    public static final String VPN_ON_TEXT = {javastr(on.get('text',''))};")
    L.append(f"    public static final String VPN_ON_COLOR = {javastr(color_java(on.get('color','#FFFFFF')))};")
    L.append(f"    public static final String VPN_ON_BG = {javastr(color_java(on.get('bg','#000000')))};")
    L.append(f"    public static final String VPN_ON_BTN = {javastr(on.get('btn','ورود'))};")
    off = v.get("state_off", {})
    L.append(f"    public static final String VPN_OFF_ICON = {javastr(off.get('icon',''))};")
    L.append(f"    public static final String VPN_OFF_TITLE = {javastr(off.get('title',''))};")
    L.append(f"    public static final String VPN_OFF_TEXT = {javastr(off.get('text',''))};")
    L.append(f"    public static final String VPN_OFF_COLOR = {javastr(color_java(off.get('color','#FFFFFF')))};")
    L.append(f"    public static final String VPN_OFF_BG = {javastr(color_java(off.get('bg','#000000')))};")
    L.append(f"    public static final String VPN_OFF_BTN = {javastr(off.get('btn','ورود'))};")
    unk = v.get("state_unknown", {})
    L.append(f"    public static final String VPN_UNK_ICON = {javastr(unk.get('icon',''))};")
    L.append(f"    public static final String VPN_UNK_TITLE = {javastr(unk.get('title',''))};")
    L.append(f"    public static final String VPN_UNK_TEXT = {javastr(unk.get('text',''))};")
    L.append(f"    public static final String VPN_UNK_COLOR = {javastr(color_java(unk.get('color','#FFFFFF')))};")
    L.append(f"    public static final String VPN_UNK_BG = {javastr(color_java(unk.get('bg','#000000')))};")
    L.append(f"    public static final String VPN_UNK_BTN = {javastr(unk.get('btn','ورود'))};")
    rc = v.get("recheck", {})
    L.append(f"    public static final String VPN_RECHECK_TEXT = {javastr(rc.get('text',''))};")
    L.append(f"    public static final String VPN_RECHECK_BG = {javastr(color_java(rc.get('bg','#000000')))};")
    L.append(f"    public static final String VPN_RECHECK_BORDER = {javastr(color_java(rc.get('border','#FFFFFF')))};")
    L.append(f"    public static final String VPN_RECHECK_COLOR = {javastr(color_java(rc.get('color','#FFFFFF')))};")
    L.append("")
    L.append("    // ===== WEBVIEW =====")
    L.append(f"    public static final String WV_URL = {javastr(wv.get('url',''))};")
    L.append(f"    public static final String WV_URL_TYPE = {javastr(wv.get('url_type','single'))};")
    L.append(f"    public static final String WV_URL_DEEP_LINK = {javastr(wv.get('url_deep_link',''))};")
    L.append(f"    public static final String WV_URL_HOME = {javastr(wv.get('url_home',''))};")
    L.append(f"    public static final String WV_USER_AGENT = {javastr(wv.get('user_agent','auto'))};")
    L.append(f"    public static final String WV_USER_AGENT_CUSTOM = {javastr(wv.get('user_agent_custom',''))};")
    L.append(f"    public static final boolean WV_PROGRESS_BAR = {javabool(wv.get('progress_bar',1))};")
    L.append(f"    public static final String WV_PROGRESS_COLOR = {javastr(color_java(wv.get('progress_color','#FFFFFF')))};")
    L.append(f"    public static final int WV_PROGRESS_HEIGHT = {javaint(wv.get('progress_height',3),3)};")
    L.append(f"    public static final boolean WV_ZOOM = {javabool(wv.get('zoom_enabled',0))};")
    L.append(f"    public static final boolean WV_JS = {javabool(wv.get('js_enabled',1))};")
    L.append(f"    public static final boolean WV_DOM = {javabool(wv.get('dom_storage',1))};")
    L.append(f"    public static final boolean WV_DATABASE = {javabool(wv.get('database',1))};")
    L.append(f"    public static final boolean WV_GEOLOCATION = {javabool(wv.get('geolocation',0))};")
    L.append(f"    public static final boolean WV_FILE_UPLOAD = {javabool(wv.get('file_upload',0))};")
    L.append(f"    public static final boolean WV_CAMERA = {javabool(wv.get('camera',0))};")
    L.append(f"    public static final boolean WV_MIC = {javabool(wv.get('microphone',0))};")
    L.append(f"    public static final boolean WV_PULL_REFRESH = {javabool(wv.get('pull_to_refresh',1))};")
    L.append(f"    public static final String WV_PULL_TEXT = {javastr(wv.get('pull_text',''))};")
    L.append(f"    public static final String WV_PULL_RELEASE = {javastr(wv.get('pull_release',''))};")
    L.append(f"    public static final String WV_PULL_LOADING = {javastr(wv.get('pull_loading',''))};")
    L.append(f"    public static final String WV_PULL_COLOR = {javastr(color_java(wv.get('pull_color','#FFFFFF')))};")
    L.append(f"    public static final boolean WV_BACK_BUTTON = {javabool(wv.get('back_button',1))};")
    L.append(f"    public static final String WV_BACK_EXIT_MSG = {javastr(wv.get('back_exit_msg',''))};")
    L.append(f"    public static final boolean WV_BACK_DOUBLE = {javabool(wv.get('back_double',1))};")
    L.append(f"    public static final String WV_EXTERNAL_LINKS = {javastr(wv.get('external_links','inapp'))};")
    L.append(f"    public static final String WV_MAIL_LINKS = {javastr(wv.get('mail_links','inapp'))};")
    L.append(f"    public static final String WV_TEL_LINKS = {javastr(wv.get('tel_links','inapp'))};")
    L.append(f"    public static final String WV_WHATSAPP_LINKS = {javastr(wv.get('whatsapp_links','inapp'))};")
    L.append(f"    public static final String WV_TELEGRAM_LINKS = {javastr(wv.get('telegram_links','inapp'))};")
    L.append(f"    public static final String WV_INSTAGRAM_LINKS = {javastr(wv.get('instagram_links','inapp'))};")
    L.append(f"    public static final int WV_TIMEOUT = {javaint(wv.get('timeout',30000),30000)};")
    L.append(f"    public static final int WV_RETRY_COUNT = {javaint(wv.get('retry_count',2),2)};")
    L.append(f"    public static final boolean WV_CACHE = {javabool(wv.get('cache_enabled',1))};")
    L.append(f"    public static final String WV_CACHE_MODE = {javastr(wv.get('cache_mode','default'))};")
    L.append(f"    public static final boolean WV_SAFE_BROWSING = {javabool(wv.get('safe_browsing',0))};")
    L.append(f"    public static final boolean WV_BLOCK_ADS = {javabool(wv.get('block_ads',0))};")
    L.append(f"    public static final boolean WV_SHOW_SPLASH_ON_WEBVIEW = {javabool(wv.get('show_splash_on_webview',0))};")
    L.append("")
    L.append("    // ===== ERRORS =====")
    L.append(f"    public static final boolean ERR_ENABLED = {javabool(e.get('enabled',1))};")
    L.append(f"    public static final boolean ERR_SHOW_RETRY = {javabool(e.get('show_retry',1))};")
    L.append(f"    public static final boolean ERR_SHOW_HOME = {javabool(e.get('show_home',0))};")
    L.append(f"    public static final boolean ERR_AUTO_RETRY = {javabool(e.get('auto_retry',1))};")
    L.append(f"    public static final int ERR_AUTO_RETRY_SEC = {javaint(e.get('auto_retry_sec',5),5)};")
    L.append(f"    public static final String ERR_RETRY_TEXT = {javastr(e.get('retry_text','🔄 تلاش مجدد'))};")
    L.append(f"    public static final String ERR_RETRY_BG = {javastr(color_java(e.get('retry_bg','#000000')))};")
    L.append(f"    public static final String ERR_RETRY_COLOR = {javastr(color_java(e.get('retry_color','#FFFFFF')))};")
    L.append(f"    public static final String ERR_HOME_TEXT = {javastr(e.get('home_text','🏠 بازگشت به خانه'))};")
    L.append(f"    public static final String ERR_HOME_BG = {javastr(e.get('home_bg','transparent'))};")
    L.append(f"    public static final String ERR_HOME_COLOR = {javastr(color_java(e.get('home_color','#FFFFFF')))};")
    L.append(f"    public static final String ERR_HOME_BORDER = {javastr(color_java(e.get('home_border','#FFFFFF')))};")
    for t in ["offline","server","nf","fb","to","dns","ssl","conn","unk"]:
        T = t.upper()
        L.append(f"    public static final String ERR_{T}_ICON = {javastr(e.get(t+'_icon',''))};")
        L.append(f"    public static final String ERR_{T}_TITLE = {javastr(e.get(t+'_title',''))};")
        L.append(f"    public static final String ERR_{T}_TEXT = {javastr(e.get(t+'_text',''))};")
        L.append(f"    public static final String ERR_{T}_COLOR = {javastr(color_java(e.get(t+'_color','#FFFFFF')))};")
        L.append(f"    public static final String ERR_{T}_BG = {javastr(color_java(e.get(t+'_bg','#000000')))};")
    L.append("")
    L.append("    // ===== EXIT =====")
    L.append(f"    public static final boolean EXIT_ENABLED = {javabool(ex.get('enabled',1))};")
    L.append(f"    public static final boolean EXIT_DOUBLE_BACK = {javabool(ex.get('double_back',1))};")
    L.append(f"    public static final String EXIT_DOUBLE_BACK_MSG = {javastr(ex.get('double_back_msg',''))};")
    L.append(f"    public static final boolean EXIT_SHOW_ICON = {javabool(ex.get('show_icon',1))};")
    L.append(f"    public static final String EXIT_DIALOG_TYPE = {javastr(ex.get('dialog_type','classic'))};")
    L.append(f"    public static final int EXIT_RADIUS = {javaint(ex.get('radius',16),16)};")
    L.append(f"    public static final int EXIT_BORDER_WIDTH = {javaint(ex.get('border_width',0),0)};")
    L.append(f"    public static final String EXIT_ICON = {javastr(ex.get('icon',''))};")
    L.append(f"    public static final String EXIT_TITLE = {javastr(ex.get('title',''))};")
    L.append(f"    public static final String EXIT_TITLE_COLOR = {javastr(color_java(ex.get('title_color','#FFFFFF')))};")
    L.append(f"    public static final String EXIT_TEXT = {javastr(ex.get('text',''))};")
    L.append(f"    public static final String EXIT_TEXT_COLOR = {javastr(color_java(ex.get('text_color','#FFFFFF')))};")
    L.append(f"    public static final String EXIT_BG_COLOR = {javastr(color_java(ex.get('bg_color','#000000')))};")
    L.append(f"    public static final String EXIT_BORDER_COLOR = {javastr(color_java(ex.get('border_color','#FFFFFF')))};")
    L.append(f"    public static final String EXIT_OVERLAY_COLOR = {javastr(ex.get('overlay_color','rgba(0,0,0,0.6)'))};")
    L.append(f"    public static final String EXIT_BTN_CONFIRM_TEXT = {javastr(ex.get('btn_confirm_text','بله، خروج'))};")
    L.append(f"    public static final String EXIT_BTN_CONFIRM_BG = {javastr(color_java(ex.get('btn_confirm_bg','#000000')))};")
    L.append(f"    public static final String EXIT_BTN_CONFIRM_COLOR = {javastr(color_java(ex.get('btn_confirm_color','#FFFFFF')))};")
    L.append(f"    public static final String EXIT_BTN_CANCEL_TEXT = {javastr(ex.get('btn_cancel_text','نه، بمون'))};")
    L.append(f"    public static final String EXIT_BTN_CANCEL_BG = {javastr(color_java(ex.get('btn_cancel_bg','#000000')))};")
    L.append(f"    public static final String EXIT_BTN_CANCEL_COLOR = {javastr(color_java(ex.get('btn_cancel_color','#FFFFFF')))};")
    L.append(f"    public static final String EXIT_BTN_LAYOUT = {javastr(ex.get('btn_layout','horizontal'))};")
    L.append("")
    L.append("    // ===== ADVANCED =====")
    L.append(f"    public static final String ADV_OUTPUT_NAME = {javastr(adv.get('output_name',''))};")
    L.append(f"    public static final String ADV_OUTPUT_FORMAT = {javastr(adv.get('output_format','apk'))};")
    L.append(f"    public static final int ADV_MIN_SDK = {javaint(adv.get('min_sdk',24),24)};")
    L.append(f"    public static final int ADV_TARGET_SDK = {javaint(adv.get('target_sdk',34),34)};")
    L.append(f"    public static final String ADV_ARCHITECTURE = {javastr(adv.get('architecture','universal'))};")
    L.append(f"    public static final String ADV_DEVELOPER_NAME = {javastr(adv.get('developer_name',''))};")
    L.append(f"    public static final String ADV_DEVELOPER_EMAIL = {javastr(adv.get('developer_email',''))};")
    L.append(f"    public static final String ADV_WEBSITE = {javastr(adv.get('website',''))};")
    L.append(f"    public static final String ADV_DESCRIPTION = {javastr(adv.get('description',''))};")
    L.append("}")

    path.write_text("\n".join(L), encoding="utf-8")
    log(f"✅ Config.java نوشته شد ({len(L)} خط)")


# =========================================================
# strings.xml + colors.xml + dimens.xml + styles.xml
# =========================================================
def write_strings(data):
    path = RES_DIR / "values" / "strings.xml"
    path.parent.mkdir(parents=True, exist_ok=True)
    b = data.get("branding", {})
    items = [
        ("app_name", b.get("app_name", "App")),
        ("base_url", data.get("webview", {}).get("url", "")),
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
    c = data.get("colors", {})
    items = {
        "color_primary": c.get("color_primary", "#000000"),
        "color_background": c.get("color_background", "#FFFFFF"),
        "color_text": c.get("color_text", "#000000"),
        "color_text_secondary": c.get("color_text_secondary", "#666666"),
        "color_button": c.get("color_button", "#000000"),
        "color_button_text": c.get("color_button_text", "#FFFFFF"),
        "color_accent": c.get("color_accent", "#000000"),
    }
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>", ""]
    for k, v in items.items():
        lines.append(f'    <color name="{k}">{hex_to_android(v)}</color>')
    lines += ["", "</resources>"]
    path.write_text("\n".join(lines), encoding="utf-8")
    log("✅ colors.xml نوشته شد")


def write_dimens(data):
    path = RES_DIR / "values" / "dimens.xml"
    path.parent.mkdir(parents=True, exist_ok=True)
    f = data.get("fonts", {})
    s = data.get("splash", {})
    items = {
        "font_size_base": f.get("font_size_base", 16),
        "title_size": f.get("title_size", 24),
        "subtitle_size": f.get("subtitle_size", 18),
        "body_size": f.get("body_size", 14),
        "button_size": f.get("button_size", 16),
        "splash_logo_size": s.get("logo_size", 180),
        "splash_title_size": s.get("title_size", 28),
        "splash_subtitle_size": s.get("subtitle_size", 16),
    }
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>", ""]
    for k, v in items.items():
        try:
            val = int(v)
        except Exception:
            val = 16
        lines.append(f'    <dimen name="{k}">{val}sp</dimen>')
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
    b = data.get("branding", {})
    adv = data.get("advanced", {})
    content = f"""plugins {{
    id 'com.android.application'
}}

android {{
    namespace '{b.get('package_name','app.vista')}'
    compileSdk 35

    defaultConfig {{
        applicationId "{b.get('package_name','app.vista')}"
        minSdk {adv.get('min_sdk', 24)}
        targetSdk {adv.get('target_sdk', 34)}
        versionCode {b.get('version_code', 1)}
        versionName "{b.get('version_name', '1.0.0')}"
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
            outputFileName = "{b.get('package_name','app')}-{b.get('version_name','1.0.0')}-" + variant.buildType.name + ".apk"
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

    require(data)
    log("✅ فیلدهای اجباری موجودن")

    pkg = data["branding"]["package_name"]

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
