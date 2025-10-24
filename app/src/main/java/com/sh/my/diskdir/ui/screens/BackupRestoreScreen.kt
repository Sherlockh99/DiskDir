package com.sh.my.diskdir.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit
) {
    var selectedBackup by remember { mutableStateOf<String?>(null) }
    var isCreatingBackup by remember { mutableStateOf(false) }
    var isRestoringBackup by remember { mutableStateOf(false) }

    // Тестовые данные для бекапов
    val backupList = remember {
        listOf(
            BackupItem(
                id = "1",
                name = "Backup USB Drive 1",
                date = Date(),
                size = 1024000000L,
                sourcePath = "/storage/usb1",
                isComplete = true
            ),
            BackupItem(
                id = "2", 
                name = "Backup USB Drive 2",
                date = Date(System.currentTimeMillis() - 86400000), // Вчера
                size = 2048000000L,
                sourcePath = "/storage/usb2",
                isComplete = true
            ),
            BackupItem(
                id = "3",
                name = "Backup in progress...",
                date = Date(),
                size = 0L,
                sourcePath = "/storage/usb3",
                isComplete = false
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Бекап и восстановление") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Создание нового бекапа
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Создать бекап",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Выберите флешку для создания бекапа её содержимого",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { isCreatingBackup = true },
                            enabled = !isCreatingBackup,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isCreatingBackup) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Начать создание бекапа")
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Существующие бекапы",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(backupList) { backup ->
                BackupItemCard(
                    backup = backup,
                    isSelected = selectedBackup == backup.id,
                    onSelect = { selectedBackup = if (selectedBackup == backup.id) null else backup.id },
                    onRestore = { 
                        isRestoringBackup = true
                        // Логика восстановления
                    }
                )
            }
            
            if (selectedBackup != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Действия с выбранным бекапом",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        isRestoringBackup = true
                                        // Логика восстановления
                                    },
                                    enabled = !isRestoringBackup,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isRestoringBackup) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Восстановить")
                                }
                                
                                OutlinedButton(
                                    onClick = { 
                                        // Логика удаления бекапа
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Удалить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackupItemCard(
    backup: BackupItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
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
                        Icons.Default.Archive,
                        contentDescription = null,
                        tint = if (backup.isComplete) Color.Green else Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = backup.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (backup.isComplete) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Завершен",
                        tint = Color.Green
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Источник: ${backup.sourcePath}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Размер: ${formatFileSize(backup.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Дата: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(backup.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class BackupItem(
    val id: String,
    val name: String,
    val date: Date,
    val size: Long,
    val sourcePath: String,
    val isComplete: Boolean
)

private fun formatFileSize(bytes: Long): String {
    if (bytes == 0L) return "0 B"
    
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return String.format("%.1f %s", size, units[unitIndex])
}
