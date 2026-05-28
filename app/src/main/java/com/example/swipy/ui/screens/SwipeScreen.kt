package com.example.swipy.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.swipy.data.GalleryPhoto
import com.example.swipy.ui.theme.*
import com.example.swipy.ui.viewmodels.SwipeViewModel
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    bucketName: String?,
    onBack: () -> Unit = {},
    onDone: (Int, Long, Int, Long, Int, Long) -> Unit = { _, _, _, _, _, _ -> },
    viewModel: SwipeViewModel = hiltViewModel()
) {
    LaunchedEffect(bucketName) {
        viewModel.loadPhotos(if (bucketName == "Semua Foto") null else bucketName)
    }

    val photos         by viewModel.photoList.collectAsState()
    val deletedCount   by viewModel.deletedCount.collectAsState()
    val deletedSize    by viewModel.deletedSize.collectAsState()
    val keptCount      by viewModel.keptCount.collectAsState()
    val keptSize       by viewModel.keptSize.collectAsState()
    val favoriteCount  by viewModel.favoriteCount.collectAsState()
    val favoriteSize   by viewModel.favoriteSize.collectAsState()
    val pendingSender  by viewModel.pendingDeleteSender.collectAsState()

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

    // Launcher untuk system delete dialog (Android 11+)
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // User mengkonfirmasi → foto terhapus
            viewModel.onDeleteConfirmed()
        } else {
            // User membatalkan
            viewModel.onDeleteCancelled()
        }
        // Navigasi ke summary setelah dialog selesai (apapun hasilnya)
        savedStats?.let { s ->
            onDone(s[0].toInt(), s[1], s[2].toInt(), s[3], s[4].toInt(), s[5])
        }
    }

    // Ketika ViewModel menyiapkan IntentSender, launch dialog
    LaunchedEffect(pendingSender) {
        pendingSender?.let { sender ->
            deleteLauncher.launch(
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
        // Jika tidak ada foto yang dihapus, langsung navigate
        if (deletedCount == 0) {
            onDone(deletedCount, deletedSize, keptCount, keptSize, favoriteCount, favoriteSize)
        }
        // Jika ada deletions → commitDeletions() akan trigger pendingSender → launcher akan navigate
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onUndo() }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = DustyBlue)
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
                    Text(
                        "💾 $keptCount simpan",
                        color = SageGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                    if (favoriteCount > 0) {
                        Text(
                            "⭐ $favoriteCount favorit",
                            color = Color(0xFFFFB300), fontSize = 13.sp, fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        "🗑 $deletedCount hapus",
                        color = SoftPink, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(16.dp))

                val currentPhoto = photos.first()
                key(currentPhoto.uri) {
                    SwipeablePhotoCard(
                        photo        = currentPhoto,
                        modifier     = Modifier.weight(1f),
                        onSwipeLeft  = { viewModel.onSwipeLeft(currentPhoto) },   // ← Simpan
                        onSwipeRight = { viewModel.onSwipeRight(currentPhoto) },  // → Hapus
                        onDoubleTap  = { viewModel.onDoubleTap(currentPhoto) }    // 2× = Favorit
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "← Simpan   |   Hapus →   |   2× ketuk ⭐ Favorit",
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
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onDoubleTap: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var showStar by remember { mutableStateOf(false) }
    val swipeThreshold = 300f

    val rotation    = (offsetX / 30f).coerceIn(-15f, 15f)
    val deleteAlpha = ((offsetX - 80f)  / 200f).coerceIn(0f, 1f)
    val keepAlpha   = ((-offsetX - 80f) / 200f).coerceIn(0f, 1f)

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
                            }
                            offsetX = 0f
                            offsetY = 0f
                        },
                        onDrag = { change, drag ->
                            change.consume()
                            offsetX += drag.x
                            offsetY += drag.y
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
                    model = photo.uri,
                    contentDescription = photo.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay hapus (→ kanan)
                if (deleteAlpha > 0f) {
                    Box(
                        Modifier.fillMaxSize()
                            .background(SoftPink.copy(alpha = deleteAlpha * 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗑  Hapus", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Overlay simpan (← kiri)
                if (keepAlpha > 0f) {
                    Box(
                        Modifier.fillMaxSize()
                            .background(SageGreen.copy(alpha = keepAlpha * 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💾  Simpan", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
                        Text(
                            text = "⭐",
                            fontSize = (72 * starScale.value).sp,
                            color = Color(0xFFFFB300).copy(alpha = starAlpha.value)
                        )
                    }
                }
            }
        }
    }
}
