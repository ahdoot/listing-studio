package com.listingstudio.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.listingstudio.app.model.Background
import com.listingstudio.app.model.Marketplace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(vm: EditorViewModel = viewModel()) {
    val state by vm.state.collectAsState()
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
            // Preview
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
                        Modifier.matchParentSize().background(Color(0x88000000)),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Color.White) }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                state.status ?: "100% on-device — no account, no internet needed",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))

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
            Text("Background", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Background.values().forEach { bg ->
                    FilterChip(
                        selected = state.background == bg && state.hasCutout,
                        onClick = { vm.setBackground(bg) },
                        enabled = state.original != null && !state.busy,
                        label = { Text(bg.label) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = state.shadow,
                    onClick = { vm.toggleShadow() },
                    enabled = state.original != null && !state.busy,
                    leadingIcon = {
                        if (state.shadow) Icon(Icons.Default.Check, contentDescription = null)
                    },
                    label = { Text("Add shadow") }
                )
            }
        }
    }

    if (showExport) {
        ExportDialog(
            onPick = { vm.exportFor(it); showExport = false },
            onDismiss = { showExport = false }
        )
    }
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
                    "Saves a square JPG sized for the marketplace, into your gallery.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
