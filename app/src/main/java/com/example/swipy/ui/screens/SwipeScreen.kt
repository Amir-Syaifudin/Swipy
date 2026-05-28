package com.example.swipy.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.swipy.ui.theme.*
import kotlin.math.roundToInt
import kotlin.math.abs

data class PhotoItem(val uri: String, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    onBack: () -> Unit = {},
    onDone: () -> Unit = {}
) {
    // Placeholder photos
    val photos = remember {
        (1..10).map { PhotoItem("https://picsum.photos/seed/$it/400/600", "Photo $it") }.toMutableStateList()
    }
    var currentIndex by remember { mutableStateOf(0) }
    var deletedCount by remember { mutableStateOf(0) }
    var keptCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "${currentIndex + 1} / ${photos.size}",
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmWhite)
            )
        },
        containerColor = WarmWhite
    ) { padding ->
        if (currentIndex >= photos.size) {
            // Done
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Selesai!", style = MaterialTheme.typography.headlineMedium, color = DustyBlue)
                    Text("Dihapus: $deletedCount | Disimpan: $keptCount", color = Color.Gray)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onDone,
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

                // Swipe stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🗑 $deletedCount dihapus", color = SoftPink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("❤️ $keptCount disimpan", color = SageGreen, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(16.dp))

                // Swipeable card
                SwipeablePhotoCard(
                    photo = photos[currentIndex],
                    modifier = Modifier.weight(1f),
                    onSwipeLeft = {
                        deletedCount++
                        currentIndex++
                    },
                    onSwipeRight = {
                        keptCount++
                        currentIndex++
                    }
                )

                Spacer(Modifier.height(20.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete button
                    FloatingActionButton(
                        onClick = { deletedCount++; currentIndex++ },
                        containerColor = SoftPink.copy(alpha = 0.15f),
                        contentColor = SoftPink,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(28.dp))
                    }
                    // Keep button
                    FloatingActionButton(
                        onClick = { keptCount++; currentIndex++ },
                        containerColor = SageGreen.copy(alpha = 0.15f),
                        contentColor = SageGreen,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Simpan", modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SwipeablePhotoCard(
    photo: PhotoItem,
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val swipeThreshold = 300f

    val rotation = (offsetX / 30f).coerceIn(-15f, 15f)
    val deleteAlpha = ((-offsetX - 80f) / 200f).coerceIn(0f, 1f)
    val keepAlpha = ((offsetX - 80f) / 200f).coerceIn(0f, 1f)

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
                                offsetX < -swipeThreshold -> onSwipeLeft()
                                offsetX > swipeThreshold  -> onSwipeRight()
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
                // Swipe overlays
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
                // Photo name tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(photo.name, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}
