package com.example.swipy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swipy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    deletedCount: Int = 7,
    keptCount: Int = 3,
    onBack: () -> Unit = {}
) {
    val aiComments = listOf(
        "Foto blur ini sudah aman masuk sampah 🗑️",
        "Duplikat screenshot — pilihan tepat untuk dihapus!",
        "Foto selfie terbaik sudah tersimpan ❤️",
        "WhatsApp forwarded image — tidak perlu disimpan",
        "Momen indah ini layak jadi favorit 🌟",
        "Foto makanan dari 2 tahun lalu, siap dihapus 😄",
        "Kenangan liburan tersimpan dengan aman!"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ringkasan Sesi", fontWeight = FontWeight.Medium) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                // Stats card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DustyBlue.copy(alpha = 0.10f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip(emoji = "🗑", count = deletedCount, label = "Dihapus", color = SoftPink)
                        Divider(modifier = Modifier.height(48.dp).width(1.dp), color = Color(0xFFE0E0E0))
                        StatChip(emoji = "❤️", count = keptCount, label = "Disimpan", color = SageGreen)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Komentar AI",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF2C2C2C)
                )
            }

            itemsIndexed(aiComments) { idx, comment ->
                CommentCard(comment = comment, accent = when (idx % 3) {
                    0 -> DustyBlue
                    1 -> SoftPink
                    else -> SageGreen
                })
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DustyBlue)
                ) {
                    Text("Kembali ke Beranda")
                }
            }
        }
    }
}

@Composable
private fun StatChip(emoji: String, count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = color)
        Text(label, fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
private fun CommentCard(comment: String, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = 6.dp)
                    .background(accent, shape = androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Text(comment, fontSize = 14.sp, color = Color(0xFF3C3C3C), lineHeight = 20.sp)
        }
    }
}
