package com.example.swipy.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.ArrowClockwise
import com.example.swipy.data.model.DeletedPhoto
import com.example.swipy.ui.theme.*
import com.example.swipy.ui.viewmodels.TrashViewModel
import com.example.swipy.ui.components.MediaPreviewDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit = {},
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val photos by viewModel.trashedPhotos.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(value = false) }
    var previewPhoto by remember { mutableStateOf<DeletedPhoto?>(null) }

    if (previewPhoto != null) {
        MediaPreviewDialog(
            uri = Uri.parse(previewPhoto!!.uri),
            isVideo = previewPhoto!!.isVideo,
            name = previewPhoto!!.name,
            onDismiss = { previewPhoto = null }
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Semua foto di sampah akan dihapus permanen. Lanjutkan?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAll(); showConfirmDialog = false }) {
                    Text("Hapus Semua", color = SoftPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onDeleteConfirmed()
        } else {
            viewModel.onDeleteCancelled()
        }
    }

    val pendingSender by viewModel.pendingActionSender.collectAsState()
    LaunchedEffect(pendingSender) {
        pendingSender?.let { sender ->
            deleteLauncher.launch(IntentSenderRequest.Builder(sender).build())
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sampah (${photos.size})", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back") }
                },
                actions = {
                    if (photos.isNotEmpty()) {
                        TextButton(onClick = { showConfirmDialog = true }) {
                            Text("Hapus Semua", color = SoftPink)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmWhite)
            )
        },
        containerColor = WarmWhite
    ) { padding ->
        if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Trash,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Sampah kosong", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(photos, key = { it.uri }) { photo ->
                    TrashPhotoItem(
                        photo = photo,
                        onDelete = { viewModel.permanentDelete(photo) },
                        onRestore = { viewModel.restore(photo) },
                        onPreview = { previewPhoto = photo }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrashPhotoItem(photo: DeletedPhoto, onDelete: () -> Unit, onRestore: () -> Unit, onPreview: () -> Unit) {
    Card(
        modifier = Modifier.clickable { onPreview() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photo.uri)
                    .videoFrameMillis(1000)
                    .crossfade(true)
                    .build(),
                contentDescription = photo.name,
                modifier = Modifier.aspectRatio(1f).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            // Action overlay at bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onRestore, modifier = Modifier.size(28.dp)) {
                    Icon(PhosphorIcons.Regular.ArrowClockwise, contentDescription = "Restore", tint = SageGreen, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(PhosphorIcons.Regular.Trash, contentDescription = "Delete", tint = SoftPink, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
