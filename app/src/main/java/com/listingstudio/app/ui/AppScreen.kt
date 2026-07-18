package com.listingstudio.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.listingstudio.app.model.AiTool
import com.listingstudio.app.model.Marketplace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(vm: EditorViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showExport by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { vm.loadImage(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listing Studio", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { vm.revert() }, enabled = state.original != null) {
                        Icon(Icons.Default.Refresh, contentDescription = "Revert")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (state.apiKey.isBlank()) {
                AssistChip(
                    onClick = { showSettings = true },
                    label = { Text("Tap to add your Gemini API key") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) }
                )
                Spacer(Modifier.height(12.dp))
            }

            // Preview area
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val bmp = state.current
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Product preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.height(8.dp))
                        Text("Pick a product photo to start")
                    }
                }
                if (state.busy) {
                    Box(
                        Modifier.matchParentSize().background(androidx.compose.ui.graphics.Color(0x88000000)),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White) }
                }
            }

            Spacer(Modifier.height(8.dp))
            state.status?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            // Pick / Export row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { picker.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp)); Text("Pick photo")
                }
                FilledTonalButton(
                    onClick = { showExport = true },
                    enabled = state.current != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(6.dp)); Text("Export")
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("AI Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiTool.values().forEach { tool ->
                    ElevatedButton(
                        onClick = { vm.applyTool(tool) },
                        enabled = state.current != null && !state.busy
                    ) { Text(tool.label) }
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            current = state.apiKey,
            onSave = { vm.saveApiKey(it); showSettings = false },
            onDismiss = { showSettings = false }
        )
    }
    if (showExport) {
        ExportDialog(
            onPick = { vm.exportFor(it); showExport = false },
            onDismiss = { showExport = false }
        )
    }
}

@Composable
private fun SettingsDialog(current: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gemini API key") },
        text = {
            Column {
                Text(
                    "Get a free key at aistudio.google.com/apikey, then paste it here. " +
                        "It's stored only on this device.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(text) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ExportDialog(onPick: (Marketplace) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export for marketplace") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Marketplace.values().forEach { m ->
                    OutlinedButton(onClick = { onPick(m) }, modifier = Modifier.fillMaxWidth()) {
                        Text(m.label)
                    }
                }
                Text(
                    "Saves a square, white-background JPG sized for the marketplace.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
