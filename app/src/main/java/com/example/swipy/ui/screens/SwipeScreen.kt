package com.example.swipy.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.FloppyDisk
import com.adamglin.phosphoricons.regular.Star
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.swipy.data.GalleryPhoto
import com.example.swipy.ui.theme.*
import com.example.swipy.ui.viewmodels.SwipeViewModel
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.widget.MediaController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    mediaType: String = "photo",
    swipeMode: String = "bouncy",
    bucketName: String?,
    onBack: () -> Unit = {},
    onDone: (Int, Long, Int, Long, Int, Long) -> Unit = { _, _, _, _, _, _ -> },
    viewModel: SwipeViewModel = hiltViewModel()
) {
    LaunchedEffect(bucketName, mediaType) {
        viewModel.loadPhotos(mediaType, if (bucketName == "Semua Foto" || bucketName == "Semua Video") null else bucketName)
    }

    val photos         by viewModel.photoList.collectAsState()
    val deletedCount   by viewModel.deletedCount.collectAsState()
    val deletedSize    by viewModel.deletedSize.collectAsState()
    val keptCount      by viewModel.keptCount.collectAsState()
    val keptSize       by viewModel.keptSize.collectAsState()
    val favoriteCount  by viewModel.favoriteCount.collectAsState()
    val favoriteSize   by viewModel.favoriteSize.collectAsState()
    val pendingFavoriteSender by viewModel.pendingFavoriteSender.collectAsState()

    // Capture stats at the time Selesai is pressed (sebelum navigate)
    var savedStats by remember { mutableStateOf<Array<Long>?>(null) }

    fun formatSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            kb >= 1 -> String.format(Locale.getDefault(), "%.1f KB", kb)
            else     -> "$size B"
        }
    }

    val favoriteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onFavoriteConfirmed()
        } else {
            viewModel.onFavoriteCancelled()
        }
    }

    LaunchedEffect(pendingFavoriteSender) {
        pendingFavoriteSender?.let { sender ->
            favoriteLauncher.launch(
                IntentSenderRequest.Builder(sender).build()
            )
        }
    }

    fun onSelesai() {
        // Simpan stats sebelum commit (karena state akan berubah)
        savedStats = arrayOf(
            deletedCount.toLong(), deletedSize,
            keptCount.toLong(),    keptSize,
            favoriteCount.toLong(), favoriteSize
        )
        viewModel.commitDeletions()
        onDone(deletedCount, deletedSize, keptCount, keptSize, favoriteCount, favoriteSize)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "${deletedCount + keptCount} Diswipe",
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onUndo() }) {
                        Icon(PhosphorIcons.Regular.ArrowCounterClockwise, contentDescription = "Undo", tint = DustyBlue)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmWhite)
            )
        },
        bottomBar = {
            // Tombol Selesai hanya muncul saat masih ada foto tersisa dan sudah ada aksi
            if (photos.isNotEmpty() && (deletedCount > 0 || keptCount > 0)) {
                Surface(tonalElevation = 4.dp, color = WarmWhite) {
                    Button(
                        onClick = { onSelesai() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DustyBlue)
                    ) {
                        Text("Selesai", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = WarmWhite
    ) { padding ->
        if (photos.isEmpty()) {
            // ── Layar Selesai (semua foto habis di-swipe) ──────────────
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Semua foto sudah ditinjau!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DustyBlue
                    )
                    Spacer(Modifier.height(24.dp))

                    SummaryItem("🗑", "Dihapus",  deletedCount,  formatSize(deletedSize),  SoftPink)
                    SummaryItem("💾", "Disimpan", keptCount,     formatSize(keptSize),     SageGreen)
                    if (favoriteCount > 0) {
                        SummaryItem("⭐", "Favorit", favoriteCount, formatSize(favoriteSize), Color(0xFFFFB300))
                    }

                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { onSelesai() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DustyBlue),
                        modifier = Modifier.fillMaxWidth(0.65f).height(52.dp)
                    ) {
                        Text("Selesai", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // ── Swipe Card ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PhosphorIcons.Regular.FloppyDisk, contentDescription = null, tint = SageGreen, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$keptCount simpan", color = SageGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    if (favoriteCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(PhosphorIcons.Regular.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("$favoriteCount favorit", color = Color(0xFFFFB300), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PhosphorIcons.Regular.Trash, contentDescription = null, tint = SoftPink, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$deletedCount hapus", color = SoftPink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Use the first item safely; if list empty show placeholder
                val currentPhoto = photos.firstOrNull()
                if (currentPhoto != null) {
                    key(currentPhoto.uri) {
                        SwipeablePhotoCard(
                            photo        = currentPhoto,
                            swipeMode    = swipeMode,
                            modifier     = Modifier.weight(1f),
                            onSwipeLeft  = { viewModel.onSwipeLeft(currentPhoto) },
                            onSwipeRight = { viewModel.onSwipeRight(currentPhoto) },
                            onDoubleTap  = { viewModel.onDoubleTap(currentPhoto) }
                        )
                    }
                } else {
                    // No media available – show empty state
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada media", color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "← Simpan   |   Hapus →   |   2× ketuk Favorit",
                    fontSize = 11.sp, color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SummaryItem(icon: String, label: String, count: Int, size: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(0.7f).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 18.sp)
            Spacer(Modifier.width(12.dp))
            Text(label, color = Color.Gray, fontSize = 16.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$count Foto", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(size, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SwipeablePhotoCard(
    photo: GalleryPhoto,
    swipeMode: String,
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onDoubleTap: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var showStar by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    // Reduce swipe distance for seamless mode to make it easier
    val swipeThreshold = if (swipeMode == "seamless") 40f else 300f
    
    val rotation    = if (swipeMode == "seamless") 0f else (offsetX / 30f).coerceIn(-15f, 15f)
    // Alpha calculations adapt to swipe mode for easier interaction
    val deleteAlpha = if (swipeMode == "seamless") ((offsetX - 20f) / 70f).coerceIn(0f, 1f) else ((offsetX - 80f) / 200f).coerceIn(0f, 1f)
    val keepAlpha   = if (swipeMode == "seamless") ((-offsetX - 20f) / 70f).coerceIn(0f, 1f) else ((-offsetX - 80f) / 200f).coerceIn(0f, 1f)

    val starScale = remember { Animatable(0f) }
    val starAlpha = remember { Animatable(0f) }

    LaunchedEffect(showStar) {
        if (showStar) {
            starScale.animateTo(1.6f, tween(300))
            starAlpha.animateTo(1f,   tween(300))
            starAlpha.animateTo(0f,   tween(350, delayMillis = 600))
            starScale.snapTo(0f)
            showStar = false
        }
    }

    if (showPreview) {
        MediaPreviewDialog(photo = photo, onDismiss = { showPreview = false })
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .rotate(rotation)
                .pointerInput(photo.uri) {
                    detectDragGestures(
                        onDragEnd = {
                            when {
                                offsetX >  swipeThreshold -> onSwipeRight()
                                offsetX < -swipeThreshold -> onSwipeLeft()
                                else -> {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                        onDrag = { change, drag ->
                            change.consume()
                            offsetX += drag.x
                            if (swipeMode == "bouncy") {
                                offsetY += drag.y
                            }
                        }
                    )
                }
                .pointerInput(photo.uri) {
                    detectTapGestures(
                        onDoubleTap = {
                            showStar = true
                            onDoubleTap()
                        }
                    )
                },
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.uri)
                        .videoFrameMillis(1000) // fetch frame at 1 second
                        .crossfade(true)
                        .build(),
                    contentDescription = photo.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { showPreview = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (photo.isVideo) PhosphorIcons.Regular.Play else PhosphorIcons.Regular.MagnifyingGlass,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (photo.isVideo) "Preview Video" else "Preview", color = Color.White, fontSize = 12.sp)
                    }
                }

                // Overlay hapus (→ kanan)
                if (deleteAlpha > 0f) {
                    Box(
                        Modifier.fillMaxSize()
                            .background(SoftPink.copy(alpha = deleteAlpha * 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(PhosphorIcons.Regular.Trash, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Hapus", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Overlay simpan (← kiri)
                if (keepAlpha > 0f) {
                    Box(
                        Modifier.fillMaxSize()
                            .background(SageGreen.copy(alpha = keepAlpha * 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(PhosphorIcons.Regular.FloppyDisk, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Simpan", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Label nama foto
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(photo.name, color = Color.White, fontSize = 14.sp)
                }

                // Animasi bintang (double tap)
                if (showStar || starAlpha.value > 0f) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            PhosphorIcons.Regular.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300).copy(alpha = starAlpha.value),
                            modifier = Modifier.size((72 * starScale.value).dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaPreviewDialog(photo: GalleryPhoto, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .background(Color.Black, RoundedCornerShape(16.dp)), 
            contentAlignment = Alignment.Center
        ) {
            if (photo.isVideo) {
                AndroidView(
                    factory = { context ->
                        VideoView(context).apply {
                            setVideoURI(photo.uri)
                            val mediaController = MediaController(context)
                            mediaController.setAnchorView(this)
                            setMediaController(mediaController)
                            start()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                AsyncImage(
                    model = photo.uri,
                    contentDescription = photo.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Text("✕", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
