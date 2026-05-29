package com.example.swipy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Folder
import com.adamglin.phosphoricons.regular.SelectionBackground
import com.adamglin.phosphoricons.regular.ArrowsLeftRight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.swipy.ui.theme.*
import com.example.swipy.ui.viewmodels.FolderPickerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerScreen(
    mediaType: String = "photo",
    onFolderSelected: (String, String) -> Unit, // bucketName, swipeMode
    onBack: () -> Unit = {},
    viewModel: FolderPickerViewModel = hiltViewModel(),
) {
    val buckets by viewModel.buckets.collectAsState()
    var selectedBucket by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(mediaType) {
        viewModel.loadBuckets(mediaType)
    }

    if (selectedBucket != null) {
        ModeSelectionDialog(
            onDismiss = { selectedBucket = null },
            onConfirm = { mode, dontShowAgain ->
                if (dontShowAgain) {
                    viewModel.saveModePreferences(true, mode)
                }
                onFolderSelected(selectedBucket!!, mode)
                selectedBucket = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pilih Folder", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmWhite)
            )
        },
        containerColor = WarmWhite
    ) { padding ->
        if (buckets.isEmpty()) {
                // Loading state
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DustyBlue)
                }
            } else if (buckets.all { it.second == 0 }) {
                // No media found for this type – show a friendly placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (mediaType == "video") "Tidak ada video di galeri" else "Tidak ada foto di galeri",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(buckets) { bucket ->
                        FolderCard(bucketName = bucket.first, count = bucket.second, mediaType = mediaType) {
                            scope.launch {
                                if (viewModel.shouldSkipPicker()) {
                                    val mode = viewModel.getDefaultMode()
                                    onFolderSelected(bucket.first, mode)
                                } else {
                                    selectedBucket = bucket.first
                                }
                            }
                        }
                    }
                }
            }
    }
}

@Composable
fun ModeSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var selectedMode by remember { mutableStateOf("bouncy") }
    var dontShowAgain by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1C1C1E) // Darker background like iOS/modern dark mode
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Pilih Mode Geser",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bisa diatur di Pengaturan",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModeCard(
                        title = "Bouncy",
                        icon = PhosphorIcons.Regular.SelectionBackground,
                        isSelected = selectedMode == "bouncy",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMode = "bouncy" }
                    )
                    ModeCard(
                        title = "Seamless",
                        icon = PhosphorIcons.Regular.ArrowsLeftRight,
                        isSelected = selectedMode == "seamless",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMode = "seamless" }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dontShowAgain = !dontShowAgain }
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(2.dp, if (dontShowAgain) DustyBlue else Color(0xFF444444), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dontShowAgain) {
                            Box(modifier = Modifier.fillMaxSize().background(DustyBlue, CircleShape))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Jangan tampilkan lagi",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { onConfirm(selectedMode, dontShowAgain) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)), // Modern Blue
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Pilih", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF007AFF) else Color(0xFF3A3A3C)
    val backgroundColor = if (isSelected) Color.Transparent else Color(0xFF2C2C2E)

    Box(
        modifier = modifier
            .aspectRatio(0.9f)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(if (isSelected) Color.White else Color(0xFF1C1C1E), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFF007AFF) else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FolderCard(bucketName: String, count: Int, mediaType: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DustyBlue.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(PhosphorIcons.Regular.Folder, contentDescription = null, tint = DustyBlue, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bucketName, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF2C2C2C))
                Text("$count ${if(mediaType == "video") "video" else "foto"}", fontSize = 13.sp, color = Color.Gray)
            }
            Text("→", color = DustyBlue, fontSize = 18.sp)
        }
    }
}
