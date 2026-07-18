package com.listingstudio.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.listingstudio.app.data.GeminiClient
import com.listingstudio.app.data.ImageOps
import com.listingstudio.app.data.Settings
import com.listingstudio.app.model.AiTool
import com.listingstudio.app.model.Marketplace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class UiState(
    val original: Bitmap? = null,
    val current: Bitmap? = null,
    val busy: Boolean = false,
    val status: String? = null,
    val apiKey: String = ""
)

class EditorViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = Settings(app)
    private val gemini = GeminiClient()

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        viewModelScope.launch {
            settings.apiKey.collect { key ->
                _state.value = _state.value.copy(apiKey = key)
            }
        }
    }

    fun saveApiKey(value: String) = viewModelScope.launch { settings.setApiKey(value) }

    fun loadImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val bmp = ImageOps.load(getApplication(), uri)
                _state.value = _state.value.copy(
                    original = bmp, current = bmp, status = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(status = "Couldn't open image: ${e.message}")
            }
        }
    }

    fun revert() {
        _state.value = _state.value.copy(current = _state.value.original, status = "Reverted to original")
    }

    fun applyTool(tool: AiTool) {
        val src = _state.value.current ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, status = "${tool.label}…")
            val key = settings.apiKey.first()
            when (val r = gemini.edit(key, src, tool.prompt)) {
                is GeminiClient.Result.Success ->
                    _state.value = _state.value.copy(
                        current = r.bitmap, busy = false, status = "${tool.label} done"
                    )
                is GeminiClient.Result.Error ->
                    _state.value = _state.value.copy(busy = false, status = r.message)
            }
        }
    }

    fun exportFor(market: Marketplace) {
        val src = _state.value.current ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, status = "Exporting for ${market.label}…")
            val squared = ImageOps.toSquareCanvas(src, market.size)
            val name = "listing_${market.name.lowercase()}_${System.currentTimeMillis()}"
            val uri = ImageOps.saveToGallery(getApplication(), squared, name)
            _state.value = _state.value.copy(
                busy = false,
                status = if (uri != null) "Saved to Gallery ▸ Pictures/ListingStudio"
                else "Save failed"
            )
        }
    }
}
