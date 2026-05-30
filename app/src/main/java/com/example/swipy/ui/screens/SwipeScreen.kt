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
import androidx.compose.foundation.shape.CircleShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.FloppyDisk
import com.adamglin.phosphoricons.regular.Star
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.Pause
import com.adamglin.phosphoricons.regular.SkipBack
import com.adamglin.phosphoricons.regular.SkipForward
import com.adamglin.phosphoricons.regular.Confetti
import com.adamglin.phosphoricons.regular.MusicNote
import com.adamglin.phosphoricons.regular.MusicNotes
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
import com.example.swipy.ui.viewmodels.MusicViewModel
import com.example.swipy.ui.components.MediaPreviewDialog
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
    viewModel: SwipeViewModel = hiltViewModel(),
    musicViewModel: MusicViewModel = hiltViewModel()
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

    var showMusicDialog by remember { mutableStateOf(false) }

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
        if (showMusicDialog) {
            MusicPlayerDialog(
                musicViewModel = musicViewModel,
                onDismiss = { showMusicDialog = false }
            )
        }

        if (photos.isEmpty()) {
            // ── Layar Selesai (semua foto habis di-swipe) ──────────────
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(PhosphorIcons.Regular.Confetti, contentDescription = null, tint = DustyBlue, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Semua foto sudah ditinjau!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DustyBlue
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { onSelesai() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DustyBlue),
                        modifier = Modifier.fillMaxWidth(0.65f).height(52.dp)
                    ) {
                        Text("Lihat Ringkasan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PhosphorIcons.Regular.FloppyDisk, contentDescription = null, tint = SageGreen, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$keptCount simpan", color = SageGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (favoriteCount > 0) {
                            Icon(PhosphorIcons.Regular.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("$favoriteCount", color = Color(0xFFFFB300), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(12.dp))
                        }
                        
                        IconButton(
                            onClick = { showMusicDialog = true },
                            modifier = Modifier.size(30.dp).background(DustyBlue.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(PhosphorIcons.Regular.MusicNote, null, tint = DustyBlue, modifier = Modifier.size(14.dp))
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$deletedCount hapus", color = SoftPink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(4.dp))
                        Icon(PhosphorIcons.Regular.Trash, contentDescription = null, tint = SoftPink, modifier = Modifier.size(14.dp))
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
        MediaPreviewDialog(
            uri = photo.uri,
            isVideo = photo.isVideo,
            name = photo.name,
            onDismiss = { showPreview = false }
        )
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

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MusicPlayerDialog(
    musicViewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val currentTrack by musicViewModel.currentTrack.collectAsState()
    val currentArtist by musicViewModel.currentArtist.collectAsState()
    val isPlaying by musicViewModel.isPlaying.collectAsState()
    val musicSource by musicViewModel.musicSource.collectAsState()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Sedang Diputar", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(20.dp))
                
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            color = if (musicSource == "Spotify") Color(0xFF1DB954).copy(alpha = 0.15f) else DustyBlue.copy(alpha = 0.15f), 
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (musicSource == "Spotify") PhosphorIcons.Regular.MusicNotes else PhosphorIcons.Regular.MusicNote, 
                        contentDescription = null, 
                        tint = if (musicSource == "Spotify") Color(0xFF1DB954) else DustyBlue,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = currentTrack, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 19.sp, 
                    color = Color(0xFF1E1E1E),
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = currentArtist, 
                    fontSize = 14.sp, 
                    color = Color(0xFF757575),
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { musicViewModel.previousTrack() }, modifier = Modifier.size(48.dp)) {
                        Icon(PhosphorIcons.Regular.SkipBack, null, tint = Color(0xFF424242), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(20.dp))
                    IconButton(
                        onClick = { musicViewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(64.dp)
                            .background(if (musicSource == "Spotify") Color(0xFF1DB954) else DustyBlue, CircleShape)
                    ) {
                        Icon(if (isPlaying) PhosphorIcons.Regular.Pause else PhosphorIcons.Regular.Play, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(20.dp))
                    IconButton(onClick = { musicViewModel.nextTrack() }, modifier = Modifier.size(48.dp)) {
                        Icon(PhosphorIcons.Regular.SkipForward, null, tint = Color(0xFF424242), modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

// MediaPreviewDialog moved to common components
