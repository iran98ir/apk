#!/usr/bin/env python3
# =========================================================
# inject.py — JSON به کد
# مسیر: scripts/inject.py
# =========================================================
# این اسکریپت قبل از build اجرا می‌شه:
# 1. config/app01.json رو می‌خونه
# 2. اگه فیلدی خالی/ناقص باشه → خطا می‌ده و build متوقف می‌شه
# 3. اگه همه‌چی درست باشه → مقادیر رو توی فایل‌های XML/Java می‌ذاره
# =========================================================

import json
import os
import re
import shutil
import sys
from pathlib import Path


# =========================================================
# مسیرها
# =========================================================
ROOT = Path(__file__).resolve().parent.parent
CONFIG_FILE = ROOT / "config" / "app01.json"
ASSETS_DIR = ROOT / "assets"
RES_DIR = ROOT / "app" / "src" / "main" / "res"
GRADLE_APP = ROOT / "app" / "build.gradle"


# =========================================================
# رنگ‌های اجباری
# =========================================================
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
    "colors.color_accent",
    "splash.enabled",
    "splash.title",
    "splash.subtitle",
    "webview.url",
    "advanced.min_sdk",
    "advanced.target_sdk",
]


def log(msg):
    print(f"[inject] {msg}")


def fail(msg):
    print(f"\n❌ خطا: {msg}\n")
    sys.exit(1)


def get(data, path):
    """مسیر نقطه‌ای رو از JSON می‌خونه: get(data, 'branding.app_name')"""
    keys = path.split(".")
    cur = data
    for k in keys:
        if not isinstance(cur, dict) or k not in cur:
            return None
        cur = cur[k]
    return cur


def require(data):
    """چک می‌کنه همه‌ی فیلدهای اجباری موجود باشن."""
    missing = []
    for field in REQUIRED_FIELDS:
        val = get(data, field)
        if val is None or (isinstance(val, str) and val.strip() == ""):
            missing.append(field)
    if missing:
        fail(
            "این فیلدهای اجباری توی app01.json نیستن یا خالی‌ان:\n  - "
            + "\n  - ".join(missing)
        )


def hex_to_android(hex_color):
    """#fb7185 رو به #FFFB7185 تبدیل می‌کنه (با آلفا)."""
    h = hex_color.strip().lstrip("#")
    if len(h) == 6:
        return f"#FF{h.upper()}"
    if len(h) == 8:
        return f"#{h.upper()}"
    fail(f"رنگ نامعتبر: {hex_color}")


# =========================================================
# نوشتن colors.xml
# =========================================================
def write_colors(data):
    path = RES_DIR / "values" / "colors.xml"
    path.parent.mkdir(parents=True, exist_ok=True)

    c = data["colors"]
    splash = data.get("splash", {})
    welcome = data.get("welcome", {})
    errors = data.get("errors", {})
    exit_cfg = data.get("exit", {})
    onboarding = data.get("onboarding", {})

    colors = {
        "color_primary": hex_to_android(c["color_primary"]),
        "color_background": hex_to_android(c["color_background"]),
        "color_text": hex_to_android(c["color_text"]),
        "color_text_secondary": hex_to_android(c.get("color_text_secondary", c["color_text"])),
        "color_button": hex_to_android(c["color_button"]),
        "color_button_text": hex_to_android(c["color_button_text"]),
        "color_accent": hex_to_android(c["color_accent"]),
        "bg_main": hex_to_android(c["color_background"]),
        "bg_surface": hex_to_android(c["color_background"]),
        "text_primary": hex_to_android(c["color_text"]),
        "text_secondary": hex_to_android(c.get("color_text_secondary", c["color_text"])),
        "text_inverse": hex_to_android(c["color_button_text"]),
        "transparent": "#00000000",
    }

    # رنگ‌های splash
    if splash.get("bg_color_1"):
        colors["splash_bg_1"] = hex_to_android(splash["bg_color_1"])
    if splash.get("bg_color_2"):
        colors["splash_bg_2"] = hex_to_android(splash["bg_color_2"])
    if splash.get("bg_color_solid"):
        colors["splash_bg_solid"] = hex_to_android(splash["bg_color_solid"])
    if splash.get("title_color"):
        colors["splash_title_color"] = hex_to_android(splash["title_color"])
    if splash.get("subtitle_color"):
        colors["splash_subtitle_color"] = hex_to_android(splash["subtitle_color"])
    if splash.get("loader_color"):
        colors["splash_loader_color"] = hex_to_android(splash["loader_color"])

    # رنگ‌های welcome
    if welcome.get("bg_color"):
        colors["welcome_bg"] = hex_to_android(welcome["bg_color"])
    if welcome.get("title_color"):
        colors["welcome_title_color"] = hex_to_android(welcome["title_color"])
    if welcome.get("subtitle_color"):
        colors["welcome_subtitle_color"] = hex_to_android(welcome["subtitle_color"])
    if welcome.get("button_bg"):
        colors["welcome_btn_bg"] = hex_to_android(welcome["button_bg"])
    if welcome.get("button_text_color"):
        colors["welcome_btn_text"] = hex_to_android(welcome["button_text_color"])

    # رنگ‌های خطا
    if errors.get("retry_bg"):
        colors["error_retry_bg"] = hex_to_android(errors["retry_bg"])
    if errors.get("retry_color"):
        colors["error_retry_text"] = hex_to_android(errors["retry_color"])
    if errors.get("offline_bg"):
        colors["error_bg"] = hex_to_android(errors["offline_bg"])
    if errors.get("offline_color"):
        colors["error_title_color"] = hex_to_android(errors["offline_color"])

    # رنگ‌های exit
    if exit_cfg.get("bg_color"):
        colors["exit_bg"] = hex_to_android(exit_cfg["bg_color"])
    if exit_cfg.get("title_color"):
        colors["exit_title_color"] = hex_to_android(exit_cfg["title_color"])
    if exit_cfg.get("text_color"):
        colors["exit_text_color"] = hex_to_android(exit_cfg["text_color"])
    if exit_cfg.get("btn_confirm_bg"):
        colors["exit_confirm_bg"] = hex_to_android(exit_cfg["btn_confirm_bg"])
    if exit_cfg.get("btn_confirm_color"):
        colors["exit_confirm_text"] = hex_to_android(exit_cfg["btn_confirm_color"])
    if exit_cfg.get("btn_cancel_bg"):
        colors["exit_cancel_bg"] = hex_to_android(exit_cfg["btn_cancel_bg"])
    if exit_cfg.get("btn_cancel_color"):
        colors["exit_cancel_text"] = hex_to_android(exit_cfg["btn_cancel_color"])

    # رنگ‌های onboarding
    if onboarding.get("btn_bg"):
        colors["onb_btn_bg"] = hex_to_android(onboarding["btn_bg"])
    if onboarding.get("btn_text_color"):
        colors["onb_btn_text"] = hex_to_android(onboarding["btn_text_color"])
    if onboarding.get("dot_active"):
        colors["onb_dot_active"] = hex_to_android(onboarding["dot_active"])
    if onboarding.get("dot_inactive"):
        colors["onb_dot_inactive"] = hex_to_android(onboarding["dot_inactive"])

    # نوشتن
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>", ""]
    for name, val in colors.items():
        lines.append(f'    <color name="{name}">{val}</color>')
    lines.append("")
    lines.append("</resources>")
    path.write_text("\n".join(lines), encoding="utf-8")
    log(f"✅ colors.xml نوشته شد ({len(colors)} رنگ)")


# =========================================================
# نوشتن strings.xml
# =========================================================
def write_strings(data):
    path = RES_DIR / "values" / "strings.xml"
    path.parent.mkdir(parents=True, exist_ok=True)

    b = data["branding"]
    splash = data.get("splash", {})
    welcome = data.get("welcome", {})
    errors = data.get("errors", {})
    exit_cfg = data.get("exit", {})
    webview = data.get("webview", {})
    onboarding = data.get("onboarding", {})
    vpn = data.get("vpn", {})

    def esc(s):
        if s is None:
            return ""
        return (str(s)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "\\'")
                .replace('"', '\\"'))

    strings = {
        "app_name": esc(b["app_name"]),
        "base_url": esc(webview.get("url", "")),
        "splash_title": esc(splash.get("title", "")),
        "splash_subtitle": esc(splash.get("subtitle", "")),
        "splash_loading": esc("در حال آماده‌سازی…"),
        "welcome_title": esc(welcome.get("title", "")),
        "welcome_subtitle": esc(welcome.get("subtitle", "")),
        "welcome_button": esc(welcome.get("button_text", "ورود")),
        "onb_skip": esc(onboarding.get("skip_text", "رد کردن")),
        "onb_next": esc(onboarding.get("next_text", "بعدی")),
        "onb_prev": esc(onboarding.get("prev_text", "قبلی")),
        "onb_start": esc(onboarding.get("start_text", "شروع کن")),
        "onb_s1_title": esc(onboarding.get("s1_title", "")),
        "onb_s1_text": esc(onboarding.get("s1_text", "")),
        "onb_s2_title": esc(onboarding.get("s2_title", "")),
        "onb_s2_text": esc(onboarding.get("s2_text", "")),
        "onb_s3_title": esc(onboarding.get("s3_title", "")),
        "onb_s3_text": esc(onboarding.get("s3_text", "")),
        "vpn_top_title": esc(vpn.get("top_title", "")),
        "vpn_top_subtitle": esc(vpn.get("top_subtitle", "")),
        "vpn_footnote": esc(vpn.get("footnote", "")),
        "vpn_on_title": esc(vpn.get("state_on", {}).get("title", "")),
        "vpn_on_text": esc(vpn.get("state_on", {}).get("text", "")),
        "vpn_on_btn": esc(vpn.get("state_on", {}).get("btn", "")),
        "vpn_off_title": esc(vpn.get("state_off", {}).get("title", "")),
        "vpn_off_text": esc(vpn.get("state_off", {}).get("text", "")),
        "vpn_off_btn": esc(vpn.get("state_off", {}).get("btn", "")),
        "vpn_unk_title": esc(vpn.get("state_unknown", {}).get("title", "")),
        "vpn_unk_text": esc(vpn.get("state_unknown", {}).get("text", "")),
        "vpn_unk_btn": esc(vpn.get("state_unknown", {}).get("btn", "")),
        "error_retry": esc(errors.get("retry_text", "تلاش مجدد")),
        "error_offline_title": esc(errors.get("offline_title", "")),
        "error_offline_text": esc(errors.get("offline_text", "")),
        "error_server_title": esc(errors.get("server_title", "")),
        "error_server_text": esc(errors.get("server_text", "")),
        "error_nf_title": esc(errors.get("nf_title", "")),
        "error_nf_text": esc(errors.get("nf_text", "")),
        "error_fb_title": esc(errors.get("fb_title", "")),
        "error_fb_text": esc(errors.get("fb_text", "")),
        "error_to_title": esc(errors.get("to_title", "")),
        "error_to_text": esc(errors.get("to_text", "")),
        "error_dns_title": esc(errors.get("dns_title", "")),
        "error_dns_text": esc(errors.get("dns_text", "")),
        "error_ssl_title": esc(errors.get("ssl_title", "")),
        "error_ssl_text": esc(errors.get("ssl_text", "")),
        "error_conn_title": esc(errors.get("conn_title", "")),
        "error_conn_text": esc(errors.get("conn_text", "")),
        "error_unk_title": esc(errors.get("unk_title", "")),
        "error_unk_text": esc(errors.get("unk_text", "")),
        "exit_title": esc(exit_cfg.get("title", "")),
        "exit_text": esc(exit_cfg.get("text", "")),
        "exit_confirm": esc(exit_cfg.get("btn_confirm_text", "بله")),
        "exit_cancel": esc(exit_cfg.get("btn_cancel_text", "نه")),
        "back_double_msg": esc(webview.get("back_exit_msg", "")),
        "pull_text": esc(webview.get("pull_text", "")),
        "pull_release": esc(webview.get("pull_release", "")),
        "pull_loading": esc(webview.get("pull_loading", "")),
    }

    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>", ""]
    for name, val in strings.items():
        lines.append(f'    <string name="{name}">{val}</string>')
    lines.append("")
    lines.append("</resources>")
    path.write_text("\n".join(lines), encoding="utf-8")
    log(f"✅ strings.xml نوشته شد ({len(strings)} متن)")


# =========================================================
# کپی عکس‌ها
# =========================================================
def copy_icons():
    """عکس‌های assets رو به res منتقل می‌کنه."""
    # ===== آیکون اپ =====
    target_dir = RES_DIR / "mipmap-xxhdpi"
    target_dir.mkdir(parents=True, exist_ok=True)

    icon_144 = ASSETS_DIR / "icon-144.png"
    if not icon_144.exists():
        fail("عکس پیدا نشد: assets/icon-144.png")

    # کپی برای آیکون اصلی
    shutil.copy(icon_144, target_dir / "ic_launcher.png")
    log("✅ icon-144.png → ic_launcher.png کپی شد")

    # کپی برای foreground (Adaptive Icon)
    shutil.copy(icon_144, target_dir / "ic_launcher_foreground.png")
    log("✅ icon-144.png → ic_launcher_foreground.png کپی شد")

    # ===== لوگو splash =====
    splash_src = ASSETS_DIR / "splash-logo.png"
    splash_dst = RES_DIR / "drawable" / "splash_logo.png"
    splash_dst.parent.mkdir(parents=True, exist_ok=True)
    if splash_src.exists():
        shutil.copy(splash_src, splash_dst)
        log("✅ splash-logo.png کپی شد")
    else:
        fail("عکس پیدا نشد: assets/splash-logo.png")


# =========================================================
# نوشتن build.gradle اپ
# =========================================================
def write_gradle(data):
    b = data["branding"]
    adv = data.get("advanced", {})

    content = f'''plugins {{
    id 'com.android.application'
}}

android {{
    namespace '{b["package_name"]}'
    compileSdk 35

    defaultConfig {{
        applicationId "{b["package_name"]}"
        minSdk {adv.get("min_sdk", 24)}
        targetSdk {adv.get("target_sdk", 34)}
        versionCode {b["version_code"]}
        versionName "{b["version_name"]}"
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
            outputFileName = "{b["package_name"]}-{b["version_name"]}-" + variant.buildType.name + ".apk"
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
}}
'''
    GRADLE_APP.write_text(content, encoding="utf-8")
    log("✅ app/build.gradle نوشته شد")


# =========================================================
# main
# =========================================================
def main():
    if not CONFIG_FILE.exists():
        fail(f"فایل پیدا نشد: {CONFIG_FILE}")

    log(f"خوندن {CONFIG_FILE}")
    try:
        data = json.loads(CONFIG_FILE.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        fail(f"app01.json معتبر نیست: {e}")

    require(data)
    log("✅ همه‌ی فیلدهای اجباری موجودن")

    write_colors(data)
    write_strings(data)
    copy_icons()
    write_gradle(data)

    log("🎉 همه‌چیز با موفقیت آماده شد")


if __name__ == "__main__":
    main()
