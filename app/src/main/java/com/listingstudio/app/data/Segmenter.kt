package com.listingstudio.app.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Free, on-device background removal using Google's ML Kit Subject Segmentation.
 * The model runs entirely on the phone (downloaded once via Google Play services),
 * so there is no API key, no network call, and no cost.
 */
object Segmenter {

    private val client by lazy {
        SubjectSegmentation.getClient(
            SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap()
                .build()
        )
    }

    /** Returns the subject cut out from its background as a bitmap with transparency. */
    suspend fun cutout(src: Bitmap): Bitmap = suspendCancellableCoroutine { cont ->
        val input = InputImage.fromBitmap(src, 0)
        client.process(input)
            .addOnSuccessListener { result ->
                val fg = result.foregroundBitmap
                if (fg != null) cont.resume(fg)
                else cont.resumeWithException(
                    IllegalStateException("Couldn't find a clear subject. Try better lighting or a plainer background.")
                )
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}
