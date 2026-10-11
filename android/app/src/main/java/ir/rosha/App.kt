package ir.rosha

import android.app.Application
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 🔥 نصب Custom Exception Handler
        setupCrashHandler()

        try {
            Config.init(this)
            Log.d("ROSHA", "✓ Config initialized")
        } catch (e: Throwable) {
            Log.e("ROSHA", "Config init failed", e)
            writeCrashLog(e, "CONFIG_INIT")
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            writeCrashLog(throwable, "UNCAUGHT")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrashLog(e: Throwable, tag: String) {
        try {
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())

            val content = """
========================================
CRASH REPORT
Tag: $tag
Time: $timestamp
Device: ${Build.MANUFACTURER} ${Build.MODEL}
Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
========================================

Exception: ${e.javaClass.name}
Message: ${e.message}

Stack Trace:
$stackTrace
""".trimIndent()

            // مسیرهای مختلف برای ذخیره
            val paths = listOf(
                File(getExternalFilesDir(null), "crash_$timestamp.txt"),
                File(Environment.getExternalStorageDirectory(), "RoshaCrash_$timestamp.txt"),
                File(filesDir, "crash_$timestamp.txt")
            )

            paths.forEach { file ->
                try {
                    file.parentFile?.mkdirs()
                    file.writeText(content)
                    Log.e("ROSHA", "Crash saved: ${file.absolutePath}")
                } catch (_: Exception) {}
            }

            Log.e("ROSHA", content)

        } catch (ignored: Throwable) {}
    }
}
