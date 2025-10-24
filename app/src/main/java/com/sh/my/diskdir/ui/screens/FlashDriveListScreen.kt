package com.sh.my.diskdir.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sh.my.diskdir.data.model.FlashDrive
import com.sh.my.diskdir.data.model.FlashDriveGroup
import com.sh.my.diskdir.viewmodel.FlashDriveViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashDriveListScreen(
    viewModel: FlashDriveViewModel,
    onNavigateToFileExplorer: (String) -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToGroupManagement: () -> Unit,
    onNavigateToVirtualCatalog: () -> Unit
) {
    val flashDrives by viewModel.flashDrives.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Флешки и диски") },
                actions = {
                    IconButton(onClick = onNavigateToVirtualCatalog) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Виртуальные каталоги")
                    }
                    IconButton(onClick = onNavigateToBackup) {
                        Icon(Icons.Default.Backup, contentDescription = "Бекап")
                    }
                    IconButton(onClick = onNavigateToGroupManagement) {
                        Icon(Icons.Default.Group, contentDescription = "Группы")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Показываем флешки по группам
                groups.forEach { group ->
                    item {
                        GroupHeader(group = group)
                    }
                    
                    items(group.flashDrives) { flashDrive ->
                        FlashDriveCard(
                            flashDrive = flashDrive,
                            onClick = { onNavigateToFileExplorer(flashDrive.id) }
                        )
                    }
                }
                
                // Показываем флешки без группы
                val ungroupedDrives = flashDrives.filter { it.groupId == null }
                if (ungroupedDrives.isNotEmpty()) {
                    item {
                        Text(
                            text = "Без группы",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(ungroupedDrives) { flashDrive ->
                        FlashDriveCard(
                            flashDrive = flashDrive,
                            onClick = { onNavigateToFileExplorer(flashDrive.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupHeader(group: FlashDriveGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(group.color).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        Color(group.color),
                        RoundedCornerShape(6.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${group.flashDrives.size} устройств",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FlashDriveCard(
    flashDrive: FlashDrive,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Usb,
                        contentDescription = null,
                        tint = if (flashDrive.isConnected) Color.Green else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = flashDrive.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (flashDrive.isConnected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Подключено",
                        tint = Color.Green
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = flashDrive.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Информация о размере
            val usedSpace = flashDrive.totalSize - flashDrive.freeSpace
            val usedPercent = if (flashDrive.totalSize > 0) {
                (usedSpace.toFloat() / flashDrive.totalSize.toFloat() * 100).toInt()
            } else 0
            
            Text(
                text = "Использовано: ${formatBytes(usedSpace)} / ${formatBytes(flashDrive.totalSize)} ($usedPercent%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Прогресс-бар использования
            LinearProgressIndicator(
                progress = usedPercent / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = if (usedPercent > 90) Color.Red else Color.Blue
            )
            
            // Последнее сканирование
            flashDrive.lastScanned?.let { lastScanned ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Последнее сканирование: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(lastScanned)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return String.format("%.1f %s", size, units[unitIndex])
}
