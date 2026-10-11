package ir.rosha

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Picture
import android.graphics.drawable.PictureDrawable
import android.util.Log
import android.widget.ImageView
import com.caverock.androidsvg.SVG

object SvgLoader {

    fun loadInto(iv: ImageView, svgCode: String, color: String, colorOn: Boolean) {
        if (svgCode.isBlank()) {
            iv.setImageDrawable(null)
            return
        }

        try {
            var finalSvg = svgCode.trim()

            // اگه SVG ناقصه، یه wrapper اضافه کن
            if (!finalSvg.startsWith("<svg") && !finalSvg.startsWith("<?xml")) {
                Log.w("SvgLoader", "SVG does not start with <svg")
            }

            if (colorOn && color.isNotBlank()) {
                finalSvg = finalSvg.replace("currentColor", color, ignoreCase = true)
                if (!finalSvg.contains("fill=")) {
                    finalSvg = finalSvg.replaceFirst("<svg", "<svg fill=\"$color\"")
                }
            }

            val svg = SVG.getFromString(finalSvg)
            val drawable = PictureDrawable(svg.renderToPicture())
            iv.setImageDrawable(drawable)
        } catch (e: Throwable) {
            Log.e("SvgLoader", "SVG load failed: ${e.message}", e)
            iv.setImageDrawable(null)
        }
    }

    fun renderBitmap(svgCode: String, color: String, colorOn: Boolean, sizePx: Int): Bitmap? {
        if (svgCode.isBlank()) return null
        return try {
            var finalSvg = svgCode
            if (colorOn && color.isNotBlank()) {
                finalSvg = finalSvg.replace("currentColor", color, ignoreCase = true)
            }
            val svg = SVG.getFromString(finalSvg)
            val picture: Picture = svg.renderToPicture(sizePx, sizePx)
            val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawPicture(picture)
            bmp
        } catch (e: Throwable) {
            Log.e("SvgLoader", "SVG render failed: ${e.message}", e)
            null
        }
    }
}
