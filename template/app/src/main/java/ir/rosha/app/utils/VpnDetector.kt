/* =========================================================
   VpnDetector.kt — تشخیص فیلترشکن
   مسیر: template/app/src/main/java/ir/rosha/app/utils/VpnDetector.kt
   ========================================================= */

package ir.rosha.app.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import ir.rosha.app.VpnState

object VpnDetector {

    private val VPN_PACKAGES = listOf(
        "com.vpn", "free.vpn", "vpn.free", "turbo.vpn", "snap.vpn",
        "com.vpn.free.proxy", "com.freevpn", "com.supervpn", "com.fastvpn",
        "com.vpnfast", "com.quickvpn", "com.speedvpn",
        "org.torproject.torbrowser", "org.torproject.android", "com.torproject",
        "com.wireguard.android",
        "com.cloudflare.onedotonedotonedotone", "com.cloudflare.client.equivalent",
        "com.protonvpn.android", "ch.protonvpn.android",
        "com.expressvpn.vpn", "com.nordvpn.android", "com.surfshark.vpnclient.android",
        "com.hola.vpn", "org.hola", "com.hola.launcher",
        "com.psiphon3", "com.psiphon3.subscription",
        "com.tunnelbear.android", "com.hotspotshield.android.vpn",
        "com.anchorfree.hydravpn", "com.anchorfree.hotspotshield",
        "com.betternet", "com.pango.android",
        "com.free.vpn.super.hotspot.proxy", "com.vpn.proxy.unblock",
        "com.vpn.proxy.master",
        "com.lantern", "io.lantern", "com.getlantern.lantern",
        "com.vpnhub", "com.vpnhub.android", "com.vpnhood.client.android",
        "com.v2ray.ang", "io.nekohasekai.sagernet",
        "com.github.shadowsocks", "com.github.shadowsocks.tv",
        "in.zhaoj.shadowsocksr", "com.heddxxtv",
        "com.sockshttp.pro", "com.trojan", "com.clash",
        "com.brook", "com.txthinking.brook",
        "com.independer.vpnmaster", "com.vpnpro", "com.vpn.pro",
        "com.deltavpn", "com.superfastvpn", "com.oneclickvpn",
        "com.vpngate", "com.tigervpn", "com.usvpn", "com.greenvpn",
        "com.freevpnplanet", "com.vpn1click", "com.vpngatepro"
    )

    fun detect(context: Context): VpnState {

        // ===== ۱. VPN نیتیو =====
        if (isNativeVpnOn(context)) {
            return VpnState.ON
        }

        // ===== ۲. اپ VPN نصب =====
        if (hasVpnAppInstalled(context)) {
            return VpnState.UNKNOWN
        }

        // ===== ۳. خاموش =====
        return VpnState.OFF
    }

    private fun isNativeVpnOn(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } catch (e: Exception) {
            false
        }
    }

    private fun hasVpnAppInstalled(context: Context): Boolean {
        val pm = context.packageManager
        for (pkg in VPN_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // ادامه
            }
        }
        return false
    }
}
