/* =========================================================
   VpnDetector.kt  —  تشخیص فیلترشکن
   مسیر: template/app/src/main/java/ir/rosha/app/utils/VpnDetector.kt
   =========================================================
   📌 ۳ لایه‌ی تشخیص:
      ۱. VPN نیتیو اندروید (NetworkCapabilities)
      ۲. لیست اپ‌های VPN نصب‌شده
      ۳. بررسی اتصال (اختیاری)
   📌 منطق: حداکثر اخطار، صفر اذیت
   ========================================================= */

package ir.rosha.app.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import ir.rosha.app.VpnState

object VpnDetector {

    /* =========================================================
       لیست پکیج‌های VPN معروف
       ========================================================= */
    private val VPN_PACKAGES = listOf(
        // ===== VPN های عمومی =====
        "com.vpn",
        "free.vpn",
        "vpn.free",
        "turbo.vpn",
        "snap.vpn",
        "com.vpn.free.proxy",
        "com.freevpn",
        "com.supervpn",
        "com.fastvpn",
        "com.vpnfast",
        "com.quickvpn",
        "com.speedvpn",

        // ===== VPN های معروف =====
        "org.torproject.torbrowser",
        "org.torproject.android",
        "com.torproject",

        "com.wireguard.android",
        "com.cloudflare.onedotonedotonedotone",
        "com.cloudflare.client.equivalent",

        "com.protonvpn.android",
        "ch.protonvpn.android",

        "com.expressvpn.vpn",
        "com.nordvpn.android",
        "com.surfshark.vpnclient.android",

        "com.hola.vpn",
        "org.hola",
        "com.hola.launcher",

        "com.psiphon3",
        "com.psiphon3.subscription",

        "com.tunnelbear.android",
        "com.hotspotshield.android.vpn",

        "com.anchorfree.hydravpn",
        "com.anchorfree.hotspotshield",
        "com.betternet",
        "com.pango.android",

        "com.free.vpn.super.hotspot.proxy",
        "com.vpn.proxy.unblock",
        "com.vpn.proxy.master",

        // ===== VPN های ایرانی =====
        "com.lantern",
        "io.lantern",
        "com.getlantern.lantern",

        "com.vpnhub",
        "com.vpnhub.android",
        "com.vpnhood.client.android",

        // ===== V2Ray ها =====
        "com.v2ray.ang",
        "io.nekohasekai.sagernet",
        "com.github.shadowsocks",
        "com.github.shadowsocks.tv",
        "in.zhaoj.shadowsocksr",
        "com.heddxxtv",

        // ===== SocksHttp و ... =====
        "com.sockshttp.pro",
        "com.trojan",
        "com.clash",

        // ===== Brook و... =====
        "com.brook",
        "com.txthinking.brook",

        // ===== متفرقه =====
        "com.independer.vpnmaster",
        "com.vpnpro",
        "com.vpn.pro",
        "com.deltavpn",
        "com.superfastvpn",
        "com.oneclickvpn",
        "com.vpngate",
        "com.tigervpn",
        "com.usvpn",
        "com.greenvpn",
        "com.freevpnplanet",
        "com.vpn1click",
        "com.vpngatepro"
    )

    /* =========================================================
       تابع اصلی تشخیص
       ========================================================= */
    fun detect(context: Context): VpnState {

        // ===== ۱. چک VPN نیتیو اندروید =====
        val nativeVpnOn = isNativeVpnOn(context)

        if (nativeVpnOn) {
            return VpnState.ON  // مطمئنیم روشنه
        }

        // ===== ۲. چک اپ‌های VPN نصب‌شده =====
        val hasVpnApp = hasVpnAppInstalled(context)

        if (hasVpnApp) {
            // اپ نصب داره ولی Native VPN خاموشه
            // → نامشخص (شاید Split Tunnel باشه)
            return VpnState.UNKNOWN
        }

        // ===== ۳. هیچی نبود → خاموش =====
        return VpnState.OFF
    }

    /* =========================================================
       چک VPN نیتیو اندروید
       ========================================================= */
    private fun isNativeVpnOn(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false

            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } catch (e: Exception) {
            false
        }
    }

    /* =========================================================
       چک اپ‌های VPN نصب‌شده
       ========================================================= */
    private fun hasVpnAppInstalled(context: Context): Boolean {
        val pm = context.packageManager

        for (pkg in VPN_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // ادامه بده
            }
        }

        return false
    }
}
