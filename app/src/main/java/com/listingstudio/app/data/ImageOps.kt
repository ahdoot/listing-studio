package com.listingstudio.app.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import androidx.core.graphics.scale
import com.listingstudio.app.model.Background
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

object ImageOps {

    /** Load a picked image into a mutable ARGB bitmap. */
    fun load(context: Context, uri: Uri): Bitmap {
        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val src = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(src) { d, _, _ -> d.isMutableRequired = true }
        } else {
            @Suppress("DEPRECATION")
            Media.getBitmap(context.contentResolver, uri)
        }
        return if (bmp.config == Bitmap.Config.ARGB_8888) bmp
        else bmp.copy(Bitmap.Config.ARGB_8888, true)
    }

    /**
     * Render the cut-out subject centered on a square canvas with the chosen background,
     * optionally with a soft drop shadow. Used for both the on-screen preview and export.
     */
    fun render(cutout: Bitmap, background: Background, shadow: Boolean, size: Int): Bitmap {
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        drawBackground(canvas, background, size)

        // Fit the subject into ~82% of the canvas, centered.
        val scale = (size * 0.82f) / max(cutout.width, cutout.height)
        val w = (cutout.width * scale).roundToInt().coerceAtLeast(1)
        val h = (cutout.height * scale).roundToInt().coerceAtLeast(1)
        val scaled = cutout.scale(w, h)
        val left = (size - w) / 2f
        val top = (size - h) / 2f

        if (shadow) {
            val blur = size * 0.012f
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
            }
            val offset = IntArray(2)
            val alpha = scaled.extractAlpha(shadowPaint, offset)
            val dy = size * 0.02f
            val tint = Paint().apply { color = Color.argb(110, 0, 0, 0) }
            canvas.drawBitmap(alpha, left + offset[0], top + offset[1] + dy, tint)
        }

        canvas.drawBitmap(scaled, left, top, null)
        return out
    }

    private fun drawBackground(canvas: Canvas, background: Background, size: Int) {
        when (background) {
            Background.WHITE -> canvas.drawColor(Color.WHITE)
            Background.STUDIO -> {
                val paint = Paint().apply {
                    shader = LinearGradient(
                        0f, 0f, 0f, size.toFloat(),
                        Color.rgb(245, 246, 248), Color.rgb(214, 218, 224),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
            }
        }
    }

    /** Save a bitmap to the gallery under Pictures/ListingStudio. */
    suspend fun saveToGallery(context: Context, bitmap: Bitmap, name: String): Uri? =
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(Media.DISPLAY_NAME, "$name.jpg")
                put(Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(Media.RELATIVE_PATH, "Pictures/ListingStudio")
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext null
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            uri
        }
}
