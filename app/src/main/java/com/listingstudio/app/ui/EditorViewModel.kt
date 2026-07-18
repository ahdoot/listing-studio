package com.listingstudio.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.listingstudio.app.data.ImageOps
import com.listingstudio.app.data.Segmenter
import com.listingstudio.app.model.Background
import com.listingstudio.app.model.Marketplace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

data class UiState(
    val original: Bitmap? = null,
    val current: Bitmap? = null,
    val hasCutout: Boolean = false,
    val background: Background = Background.WHITE,
    val shadow: Boolean = false,
    val busy: Boolean = false,
    val status: String? = null
)

class EditorViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    // The subject cut out from its background (with transparency). Computed once per photo.
    private var cutout: Bitmap? = null

    fun loadImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val bmp = ImageOps.load(getApplication(), uri)
                cutout = null
                _state.value = UiState(original = bmp, current = bmp, status = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(status = "Couldn't open image: ${e.message}")
            }
        }
    }

    fun revert() {
        cutout = null
        _state.value = _state.value.copy(
            current = _state.value.original,
            hasCutout = false, shadow = false, background = Background.WHITE,
            status = "Back to original"
        )
    }

    fun setBackground(bg: Background) = update { it.copy(background = bg) }

    fun toggleShadow() = update { it.copy(shadow = !it.shadow) }

    /** Ensures the subject is segmented, then re-renders the preview with the given state change. */
    private fun update(transform: (UiState) -> UiState) {
        val original = _state.value.original ?: return
        viewModelScope.launch {
            val next = transform(_state.value)
            _state.value = next.copy(busy = true, status = "Removing background…")
            try {
                val cut = cutout ?: Segmenter.cutout(original).also { cutout = it }
                val previewSize = min(1400, max(cut.width, cut.height)).coerceAtLeast(800)
                val rendered = ImageOps.render(cut, next.background, next.shadow, previewSize)
                _state.value = next.copy(
                    current = rendered, hasCutout = true, busy = false,
                    status = "Ready — background removed on-device"
                )
            } catch (e: Exception) {
                _state.value = next.copy(busy = false, status = e.message ?: "Segmentation failed")
            }
        }
    }

    fun exportFor(market: Marketplace) {
        val original = _state.value.original ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, status = "Exporting for ${market.label}…")
            try {
                val cut = cutout ?: Segmenter.cutout(original).also { cutout = it }
                val s = _state.value
                val squared = ImageOps.render(cut, s.background, s.shadow, market.size)
                val name = "listing_${market.name.lowercase()}_${System.currentTimeMillis()}"
                val uri = ImageOps.saveToGallery(getApplication(), squared, name)
                _state.value = _state.value.copy(
                    busy = false, hasCutout = true, current = _state.value.current,
                    status = if (uri != null) "Saved to Gallery ▸ Pictures/ListingStudio"
                    else "Save failed"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(busy = false, status = e.message ?: "Export failed")
            }
        }
    }
}
