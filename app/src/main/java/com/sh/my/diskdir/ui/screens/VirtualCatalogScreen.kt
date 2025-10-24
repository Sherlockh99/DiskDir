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
fun VirtualCatalogCard(
    catalog: VirtualCatalog,
    onClick: () -> Unit,
    onDelete: () -> Unit
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
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = catalog.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Путь: ${catalog.originalPath}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Файлов: ${catalog.totalFiles}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Папок: ${catalog.totalDirectories}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Размер: ${formatFileSize(catalog.totalSize)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Сканировано: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(catalog.scanDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

@Composable
fun PathSelectionDialog(
    onDismiss: () -> Unit,
    onPathSelected: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedPath by remember { mutableStateOf<String?>(null) }
    var showFolderPicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать виртуальный каталог") },
        text = {
            Column {
                // Кнопка выбора папки
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFolderPicker = true },
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
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedPath != null) "Выбранная папка" else "Выберите папку",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = selectedPath ?: "Нажмите для выбора папки",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selectedPath != null) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название каталога") },
                    placeholder = { Text("Моя флешка") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (selectedPath != null && name.isNotBlank()) {
                        onPathSelected(selectedPath!!, name)
                    }
                },
                enabled = selectedPath != null && name.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
    
    // Диалог выбора папки
    if (showFolderPicker) {
        FolderPickerDialog(
            onDismiss = { showFolderPicker = false },
            onPathSelected = { path ->
                selectedPath = path
                showFolderPicker = false
            }
        )
    }
}

@Composable
fun FolderPickerDialog(
    onDismiss: () -> Unit,
    onPathSelected: (String) -> Unit
) {
    var currentPath by remember { mutableStateOf("/") }
    var folderItems by remember { mutableStateOf<List<FolderItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInitialPaths by remember { mutableStateOf(true) }
    
    // Загружаем содержимое текущей папки
    LaunchedEffect(currentPath) {
        isLoading = true
        try {
            folderItems = loadFolderContents(currentPath)
        } catch (e: Exception) {
            folderItems = emptyList()
        }
        isLoading = false
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Выберите папку",
                maxLines = 1
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Текущий путь
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = currentPath,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (showInitialPaths) {
                    // Показываем начальные пути для внешних устройств
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            Text(
                                text = "Быстрый доступ:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(getInitialPaths()) { pathItem ->
                            FolderItemCard(
                                item = pathItem,
                                onClick = { 
                                    currentPath = pathItem.path
                                    showInitialPaths = false
                                }
                            )
                        }
                        
                        item {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        
                        item {
                            Text(
                                text = "Навигация по файловой системе:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        showInitialPaths = false
                                        currentPath = "/"
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Корневая папка (/)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                } else if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Кнопка "Назад"
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        if (currentPath != "/") {
                                            currentPath = getParentPath(currentPath)
                                        } else {
                                            showInitialPaths = true
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (currentPath != "/") "Назад" else "К быстрому доступу",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        // Список папок
                        items(folderItems.filter { it.isDirectory }) { item ->
                            FolderItemCard(
                                item = item,
                                onClick = { 
                                    currentPath = item.path
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPathSelected(currentPath) }
            ) {
                Text("Выбрать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun FolderItemCard(
    item: FolderItem,
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
                Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
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

data class FolderItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean
)

private fun loadFolderContents(path: String): List<FolderItem> {
    return try {
        val directory = java.io.File(path)
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.map { file ->
                FolderItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory
                )
            }?.sortedWith(compareBy<FolderItem> { !it.isDirectory }.thenBy { it.name }) ?: emptyList()
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun getParentPath(path: String): String {
    val parent = java.io.File(path).parent
    return parent ?: "/"
}

private fun getInitialPaths(): List<FolderItem> {
    val paths = mutableListOf<FolderItem>()
    
    // Стандартные пути для внешних устройств
    val commonPaths = listOf(
        "/storage" to "Внешние накопители",
        "/storage/usbotg" to "USB OTG",
        "/storage/usbdisk" to "USB диск",
        "/storage/sdcard1" to "SD карта",
        "/storage/extSdCard" to "Внешняя SD карта",
        "/mnt/usb" to "USB устройство",
        "/mnt/sdcard" to "SD карта",
        "/sdcard" to "Внутренняя память"
    )
    
    commonPaths.forEach { (path, name) ->
        val file = java.io.File(path)
        if (file.exists() && file.canRead()) {
            paths.add(
                FolderItem(
                    name = name,
                    path = path,
                    isDirectory = true
                )
            )
        }
    }
    
    // Добавляем корневую папку если нет других путей
    if (paths.isEmpty()) {
        paths.add(
            FolderItem(
                name = "Корневая папка",
                path = "/",
                isDirectory = true
            )
        )
    }
    
    return paths
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