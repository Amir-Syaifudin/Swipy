package com.example.swipy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swipy.ui.theme.*
import com.example.swipy.ui.viewmodels.MusicViewModel

@Composable
fun HomeScreen(
    musicViewModel: MusicViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val isPlaying by musicViewModel.isPlaying.collectAsState()
    val currentTrack by musicViewModel.currentTrack.collectAsState()
    val currentArtist by musicViewModel.currentArtist.collectAsState()
    val musicSource by musicViewModel.musicSource.collectAsState()
    val isPermissionGranted by musicViewModel.isPermissionGranted.collectAsState()
    var showMusicMenu by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Izin Kontrol Musik") },
            text = { 
                Column {
                    Text("Swipy membutuhkan izin 'Akses Notifikasi' agar tombol Next/Prev berfungsi.")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Jika muncul 'Setelan Terbatas':\n1. Buka Pengaturan HP > Aplikasi > Swipy\n2. Klik titik tiga (kanan atas)\n3. Pilih 'Izinkan setelan terbatas'",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(onClick = { 
                    musicViewModel.requestNotificationPermission()
                    showPermissionDialog = false 
                }) { Text("Buka Pengaturan") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Nanti Saja") }
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                musicViewModel.refreshSessions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Settings Button - Clear and prominent
                Surface(
                    onClick = { onNavigate("settings") },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            PhosphorIcons.Regular.Gear, 
                            contentDescription = null, 
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Pengaturan Aplikasi", 
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Header
            Text(
                text = "Swipy",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp),
                fontWeight = FontWeight.Bold,
                color = DustyBlue
            )
            Text(
                text = "Geser. Hapus. Lega.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(32.dp))
            
            // Bersihkan Hari Ini Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("swipe/all?bucketName=Hari_Ini") },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SageGreen.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(SageGreen.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(PhosphorIcons.Regular.Broom, contentDescription = null, tint = SageGreen, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Bersihkan Hari Ini", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color(0xFF2C2C2C))
                        Spacer(Modifier.height(4.dp))
                        Text("Mulai bersih-bersih foto & video hari ini", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeModeCard(
                    title = "Swipe Foto",
                    subtitle = "Bersihkan galeri",
                    icon = PhosphorIcons.Regular.Image,
                    backgroundColor = DustyBlue.copy(alpha = 0.12f),
                    iconTint = DustyBlue,
                    onClick = { onNavigate("folderPicker/photo") },
                    modifier = Modifier.weight(1f)
                )
                HomeModeCard(
                    title = "Swipe Video",
                    subtitle = "Bersihkan video",
                    icon = PhosphorIcons.Regular.Play,
                    backgroundColor = Color(0xFF673AB7).copy(alpha = 0.12f),
                    iconTint = Color(0xFF673AB7),
                    onClick = { onNavigate("folderPicker/video") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeModeCard(
                    title = "Sampah",
                    subtitle = "Antrian hapus",
                    icon = PhosphorIcons.Regular.Trash,
                    backgroundColor = SoftPink.copy(alpha = 0.12f),
                    iconTint = SoftPink,
                    onClick = { onNavigate("trash") },
                    modifier = Modifier.weight(1f)
                )
                HomeModeCard(
                    title = "Favorit",
                    subtitle = "Moment terbaik",
                    icon = PhosphorIcons.Regular.Heart,
                    backgroundColor = SageGreen.copy(alpha = 0.12f),
                    iconTint = SageGreen,
                    onClick = { onNavigate("favorites") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Music Player Card (Modern Light Theme)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Album Art Placeholder
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = if (musicSource == "Spotify") Color(0xFF1DB954).copy(alpha = 0.15f) else DustyBlue.copy(alpha = 0.15f), 
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (musicSource == "Spotify") PhosphorIcons.Regular.MusicNotes else PhosphorIcons.Regular.MusicNote, 
                                contentDescription = null, 
                                tint = if (musicSource == "Spotify") Color(0xFF1DB954) else DustyBlue,
                                modifier = Modifier.size(32.dp)
                            )
                            // Mini visualizer effect (static representation)
                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .size(16.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(PhosphorIcons.Regular.Play, null, tint = if (musicSource == "Spotify") Color(0xFF1DB954) else DustyBlue, modifier = Modifier.size(10.dp))
                                }
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTrack, 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 17.sp, 
                                color = Color(0xFF1E1E1E),
                                maxLines = 1
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = currentArtist, 
                                fontSize = 13.sp, 
                                color = Color(0xFF757575),
                                maxLines = 1
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            // Source Badge
                            Box {
                                Surface(
                                    color = if (musicSource == "Spotify") Color(0xFF1DB954).copy(alpha = 0.1f) else DustyBlue.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.clickable { showMusicMenu = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            if (musicSource == "None") "Hubungkan Musik" else "Ganti Sumber", 
                                            color = if (musicSource == "Spotify") Color(0xFF1DB954) else DustyBlue, 
                                            fontSize = 11.sp, 
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            PhosphorIcons.Regular.CaretDown, 
                                            null, 
                                            tint = if (musicSource == "Spotify") Color(0xFF1DB954) else DustyBlue,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = showMusicMenu,
                                    onDismissRequest = { showMusicMenu = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Spotify", fontWeight = FontWeight.Medium) },
                                        leadingIcon = { Icon(PhosphorIcons.Regular.MusicNotes, null, tint = Color(0xFF1DB954)) },
                                        onClick = { 
                                            musicViewModel.openSpotify()
                                            showMusicMenu = false 
                                        }
                                    )
                                    if (isPermissionGranted) {
                                        DropdownMenuItem(
                                            text = { Text("Musik Lokal", fontWeight = FontWeight.Medium) },
                                            leadingIcon = { Icon(PhosphorIcons.Regular.Folder, null, tint = DustyBlue) },
                                            onClick = { 
                                                musicViewModel.openLocalMusic()
                                                showMusicMenu = false 
                                            }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text("Beri Izin Kontrol Musik", fontWeight = FontWeight.Medium) },
                                            leadingIcon = { Icon(PhosphorIcons.Regular.ShieldCheck, null, tint = DustyBlue) },
                                            onClick = { 
                                                showPermissionDialog = true
                                                showMusicMenu = false 
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    
                    // Separator
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
                    
                    Spacer(Modifier.height(12.dp))

                    // Player Controls (Centered and large)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { musicViewModel.previousTrack() }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.SkipBack, 
                                contentDescription = "Previous", 
                                tint = Color(0xFF424242),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        IconButton(
                            onClick = { musicViewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (musicSource == "Spotify") Color(0xFF1DB954) else DustyBlue, 
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) PhosphorIcons.Regular.Pause else PhosphorIcons.Regular.Play, 
                                contentDescription = "Play/Pause", 
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        IconButton(onClick = { musicViewModel.nextTrack() }, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.SkipForward, 
                                contentDescription = "Next", 
                                tint = Color(0xFF424242),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF2C2C2C))
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
