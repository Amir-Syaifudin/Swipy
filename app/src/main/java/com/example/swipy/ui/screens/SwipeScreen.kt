package com.example.swipy.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    bucketName: String?,
    onBack: () -> Unit = {},
    onDone: () -> Unit = {},
    viewModel: SwipeViewModel = hiltViewModel()
) {
    LaunchedEffect(bucketName) {
        viewModel.loadPhotos(if (bucketName == "Semua Foto") null else bucketName)
    }

    val photos by viewModel.photoList.collectAsState()
    val deletedCount by viewModel.deletedCount.collectAsState()
    val keptCount by viewModel.keptCount.collectAsState()

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
        containerColor = WarmWhite
    ) { padding ->
        if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Selesai!", style = MaterialTheme.typography.headlineMedium, color = DustyBlue)
                    Text("Dihapus: $deletedCount | Disimpan: $keptCount", color = Color.Gray)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.commitDeletions()
                            onDone()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DustyBlue)
                    ) {
                        Text("Lihat Ringkasan")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("❤️ $keptCount disimpan", color = SageGreen, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("🗑 $deletedCount dihapus", color = SoftPink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(16.dp))

                val currentPhoto = photos.first()

                SwipeablePhotoCard(
                    photo = currentPhoto,
                    modifier = Modifier.weight(1f),
                    onSwipeRight = { viewModel.onSwipeRight(currentPhoto) }, // Kanan = Hapus
                    onSwipeLeft = { viewModel.onSwipeLeft(currentPhoto) },   // Kiri = Simpan
                    onDoubleTap = { viewModel.onDoubleTap(currentPhoto) }
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Keep button (Kiri)
                    FloatingActionButton(
                        onClick = { viewModel.onSwipeLeft(currentPhoto) },
                        containerColor = SageGreen.copy(alpha = 0.15f),
                        contentColor = SageGreen,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Simpan", modifier = Modifier.size(28.dp))
                    }
                    // Delete button (Kanan)
                    FloatingActionButton(
                        onClick = { viewModel.onSwipeRight(currentPhoto) },
                        containerColor = SoftPink.copy(alpha = 0.15f),
                        contentColor = SoftPink,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

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
    var showHeart by remember { mutableStateOf(false) }
    val swipeThreshold = 300f

    val rotation = (offsetX / 30f).coerceIn(-15f, 15f)
    // Swipe Right = Delete (SoftPink)
    val deleteAlpha = ((offsetX - 80f) / 200f).coerceIn(0f, 1f)
    // Swipe Left = Keep (SageGreen)
    val keepAlpha = ((-offsetX - 80f) / 200f).coerceIn(0f, 1f)

    // Heart animation state
    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }

    LaunchedEffect(showHeart) {
        if (showHeart) {
            heartScale.animateTo(1.5f, tween(300))
            heartAlpha.animateTo(1f, tween(300))
            heartAlpha.animateTo(0f, tween(300, delayMillis = 400))
            heartScale.snapTo(0f)
            showHeart = false
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .rotate(rotation)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThreshold  -> onSwipeRight() // Kanan = Hapus
                                offsetX < -swipeThreshold -> onSwipeLeft()  // Kiri = Simpan
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
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            showHeart = true
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
                if (deleteAlpha > 0f) {
                    Box(
                        Modifier.fillMaxSize().background(SoftPink.copy(alpha = deleteAlpha * 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗑 Hapus", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (keepAlpha > 0f) {
                    Box(
                        Modifier.fillMaxSize().background(SageGreen.copy(alpha = keepAlpha * 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("❤️ Simpan", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(photo.name, color = Color.White, fontSize = 14.sp)
                }

                // Heart Animation Overlay
                if (showHeart || heartAlpha.value > 0f) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "❤️",
                            fontSize = (64 * heartScale.value).sp,
                            color = Color.Red.copy(alpha = heartAlpha.value)
                        )
                    }
                }
            }
        }
    }
}
