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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sh.my.diskdir.data.model.FileItem
import com.sh.my.diskdir.viewmodel.FileExplorerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    viewModel: FileExplorerViewModel,
    onNavigateBack: () -> Unit
) {
    val currentPath by viewModel.currentPath.collectAsStateWithLifecycle()
    val fileItems by viewModel.fileItems.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val navigationStack by viewModel.navigationStack.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = currentPath.ifEmpty { "Корневая папка" },
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        // Обновить содержимое
                        viewModel.loadDirectory(currentPath)
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Показываем родительскую папку если не в корне
                if (navigationStack.size > 1) {
                    item {
                        FileItemCard(
                            fileItem = FileItem(
                                name = "..",
                                path = "",
                                isDirectory = true,
                                lastModified = Date(),
                                parentPath = ""
                            ),
                            onClick = { viewModel.navigateBack() }
                        )
                    }
                }
                
                items(fileItems) { fileItem ->
                    FileItemCard(
                        fileItem = fileItem,
                        onClick = {
                            if (fileItem.isDirectory) {
                                viewModel.navigateToDirectory(fileItem.path)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FileItemCard(
    fileItem: FileItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка файла/папки
            Icon(
                imageVector = if (fileItem.isDirectory) Icons.Default.Folder else getFileIcon(fileItem.extension),
                contentDescription = null,
                tint = if (fileItem.isDirectory) Color(0xFF4CAF50) else Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Информация о файле
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fileItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (fileItem.isDirectory) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (fileItem.isDirectory) {
                        Text(
                            text = "dir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = fileItem.extension.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Text(
                        text = formatFileSize(fileItem.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(fileItem.lastModified),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Стрелка для папок
            if (fileItem.isDirectory) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getFileIcon(extension: String) = when (extension.lowercase()) {
    "txt", "md" -> Icons.Default.Description
    "jpg", "jpeg", "png", "gif", "bmp" -> Icons.Default.Image
    "mp4", "avi", "mkv", "mov" -> Icons.Default.VideoFile
    "mp3", "wav", "flac" -> Icons.Default.AudioFile
    "pdf" -> Icons.Default.PictureAsPdf
    "zip", "rar", "7z" -> Icons.Default.Archive
    "doc", "docx" -> Icons.Default.Description
    "xls", "xlsx" -> Icons.Default.TableChart
    "ppt", "pptx" -> Icons.Default.Slideshow
    else -> Icons.Default.InsertDriveFile
}

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
