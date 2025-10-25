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
import com.sh.my.diskdir.data.model.VirtualCatalog
import com.sh.my.diskdir.data.model.VirtualFileItem
import com.sh.my.diskdir.ui.components.PathSelectionDialog
import com.sh.my.diskdir.ui.components.VirtualCatalogCard
import com.sh.my.diskdir.viewmodel.VirtualCatalogViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualCatalogScreen(
    viewModel: VirtualCatalogViewModel,
    onNavigateBack: () -> Unit
) {
    val catalogs by viewModel.catalogs.collectAsStateWithLifecycle()
    val currentCatalog by viewModel.currentCatalog.collectAsStateWithLifecycle()
    val currentDirectoryItems by viewModel.currentDirectoryItems.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showPathDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (currentCatalog != null) "Виртуальный каталог" else "Виртуальные каталоги"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (currentCatalog == null) {
                        IconButton(onClick = { showPathDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Создать каталог")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentCatalog == null) {
                FloatingActionButton(
                    onClick = { showPathDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Создать каталог")
                }
            }
        }
    ) { paddingValues ->
        if (currentCatalog == null) {
            // Список виртуальных каталогов
            VirtualCatalogList(
                catalogs = catalogs,
                isLoading = isLoading,
                onCatalogClick = { catalog ->
                    viewModel.openCatalog(catalog.id)
                },
                onDeleteCatalog = { catalog ->
                    viewModel.deleteCatalog(catalog.id)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            // Просмотр содержимого каталога
            VirtualCatalogExplorer(
                catalog = currentCatalog!!,
                items = currentDirectoryItems,
                isLoading = isLoading,
                onItemClick = { item ->
                    viewModel.navigateToDirectory(item.id)
                },
                onNavigateBack = {
                    if (!viewModel.navigateBack()) {
                        // Если не можем вернуться, закрываем каталог
                        viewModel.openCatalog("")
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
    
    // Диалог выбора пути
    if (showPathDialog) {
        PathSelectionDialog(
            onDismiss = { showPathDialog = false },
            onPathSelected = { path, name ->
                viewModel.createVirtualCatalog(path, name)
                showPathDialog = false
            }
        )
    }
    
    // Прогресс сканирования
    scanProgress?.let { (currentPath, current, total) ->
        LaunchedEffect(Unit) {
            // Показываем прогресс сканирования
        }
    }
}

@Composable
fun VirtualCatalogList(
    catalogs: List<VirtualCatalog>,
    isLoading: Boolean,
    onCatalogClick: (VirtualCatalog) -> Unit,
    onDeleteCatalog: (VirtualCatalog) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (catalogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Нет виртуальных каталогов",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Создайте виртуальный каталог для просмотра содержимого без подключения устройства",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(catalogs) { catalog ->
                    VirtualCatalogCard(
                        catalog = catalog,
                        onClick = { onCatalogClick(catalog) },
                        onDelete = { onDeleteCatalog(catalog) }
                    )
                }
            }
        }
    }
}


@Composable
fun VirtualCatalogExplorer(
    catalog: VirtualCatalog,
    items: List<VirtualFileItem>,
    isLoading: Boolean,
    onItemClick: (VirtualFileItem) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Заголовок с информацией о каталоге
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = catalog.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Файлов: ${catalog.totalFiles}, Папок: ${catalog.totalDirectories}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Кнопка ".." для возврата в родительскую папку
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateBack() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "..",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Назад",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                items(items) { item ->
                    VirtualFileItemCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun VirtualFileItemCard(
    item: VirtualFileItem,
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
            Icon(
                imageVector = if (item.isDirectory) Icons.Default.Folder else getFileIcon(item.extension),
                contentDescription = null,
                tint = if (item.isDirectory) Color(0xFF4CAF50) else Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.isDirectory) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (item.isDirectory) {
                        Text(
                            text = "dir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = item.extension.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Text(
                        text = formatFileSize(item.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(item.lastModified),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (item.isDirectory) {
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