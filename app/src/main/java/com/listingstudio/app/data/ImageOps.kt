package com.listingstudio.app.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageOps {

    /** Load a picked image into a mutable software bitmap. */
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
     * Fit the image, centered, onto a square white canvas of [size]px — the standard
     * format marketplaces expect. Keeps aspect ratio and pads with white.
     */
    fun toSquareCanvas(source: Bitmap, size: Int): Bitmap {
        val canvasBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(canvasBmp)
        canvas.drawColor(Color.WHITE)

        val scale = minOf(size.toFloat() / source.width, size.toFloat() / source.height)
        val w = (source.width * scale).toInt().coerceAtLeast(1)
        val h = (source.height * scale).toInt().coerceAtLeast(1)
        val scaled = source.scale(w, h)
        canvas.drawBitmap(scaled, (size - w) / 2f, (size - h) / 2f, null)
        return canvasBmp
    }

    /** Save a bitmap to the device gallery under Pictures/ListingStudio. */
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
            val uri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            ) ?: return@withContext null
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            uri
        }
}
