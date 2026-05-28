package com.example.swipy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swipy.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    deletedCount: Int = 0,
    deletedSize: Long = 0L,
    keptCount: Int = 0,
    keptSize: Long = 0L,
    favoriteCount: Int = 0,
    favoriteSize: Long = 0L,
    onBack: () -> Unit = {},
) {
    fun formatSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            kb >= 1 -> String.format(Locale.getDefault(), "%.1f KB", kb)
            else     -> "$size B"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ringkasan", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmWhite)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                color = WarmWhite
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DustyBlue)
                ) {
                    Text(
                        "Selesai",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        },
        containerColor = WarmWhite
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Emoji header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🎉", fontSize = 56.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Semua foto sudah ditinjau!",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            // Dihapus
            item {
                SummaryStatCard(
                    emoji = "🗑",
                    title = "Dihapus",
                    count = deletedCount,
                    size = formatSize(deletedSize),
                    color = SoftPink
                )
            }

            // Disimpan
            item {
                SummaryStatCard(
                    emoji = "💾",
                    title = "Disimpan",
                    count = keptCount,
                    size = formatSize(keptSize),
                    color = SageGreen
                )
            }

            // Favorit (hanya tampil jika ada)
            if (favoriteCount > 0) {
                item {
                    SummaryStatCard(
                        emoji = "⭐",
                        title = "Favorit",
                        count = favoriteCount,
                        size = formatSize(favoriteSize),
                        color = Color(0xFFFFB300)
                    )
                }
            }

            // Total ruang dibebaskan
            if (deletedSize > 0) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DustyBlue.copy(alpha = 0.10f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "💡 Ruang yang dibebaskan",
                                fontSize = 14.sp,
                                color = DustyBlue,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                formatSize(deletedSize),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DustyBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    emoji: String,
    title: String,
    count: Int,
    size: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 28.sp)
                Spacer(Modifier.width(14.dp))
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$count foto",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    size,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
