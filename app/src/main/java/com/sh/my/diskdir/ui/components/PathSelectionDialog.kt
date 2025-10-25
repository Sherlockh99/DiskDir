package com.sh.my.diskdir.ui.components

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
import java.io.File

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
        println("=== LOADING FOLDER CONTENTS ===")
        println("Current path: $currentPath")
        isLoading = true
        try {
            folderItems = loadFolderContents(currentPath)
            println("Loaded ${folderItems.size} items")
            folderItems.forEach { item ->
                println("Item: ${item.name}, isDir: ${item.isDirectory}")
            }
        } catch (e: Exception) {
            println("Error loading folder contents: ${e.message}")
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
                                        println("=== CLICKED ROOT FOLDER ===")
                                        showInitialPaths = false
                                        currentPath = "/"
                                        println("Current path set to: $currentPath")
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
                // Добавляем информацию о содержимом папки
                val itemCount = getItemCount(item.path)
                if (itemCount > 0) {
                    Text(
                        text = "Элементов: $itemCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
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

private fun getItemCount(path: String): Int {
    return try {
        val dir = File(path)
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.size ?: 0
        } else {
            0
        }
    } catch (e: Exception) {
        0
    }
}

data class FolderItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean
)

private fun loadFolderContents(path: String): List<FolderItem> {
    return try {
        println("=== LOADING FOLDER CONTENTS ===")
        println("Path: $path")
        
        val directory = File(path)
        println("Directory exists: ${directory.exists()}")
        println("Is directory: ${directory.isDirectory}")
        println("Can read: ${directory.canRead()}")
        println("Can write: ${directory.canWrite()}")
        println("Is hidden: ${directory.isHidden()}")
        
        if (directory.exists() && directory.isDirectory) {
            if (!directory.canRead()) {
                println("WARNING: Directory exists but cannot be read - this may require root access")
                // Для /mnt/media_rw попробуем альтернативные способы
                if (path == "/mnt/media_rw") {
                    return tryAlternativeUsbPaths()
                }
                return emptyList()
            }
            
            val files = directory.listFiles()
            println("Files array: $files")
            println("Files count: ${files?.size ?: 0}")
            
            if (files != null) {
                files.forEach { file ->
                    println("File: ${file.name}, isDir: ${file.isDirectory}, canRead: ${file.canRead()}")
                }
            }
            
            files?.map { file ->
                FolderItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory
                )
            }?.sortedWith(compareBy<FolderItem> { !it.isDirectory }.thenBy { it.name }) ?: emptyList()
        } else {
            println("Directory not accessible or doesn't exist")
            emptyList()
        }
    } catch (e: Exception) {
        println("Exception loading folder contents: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}

private fun tryAlternativeUsbPaths(): List<FolderItem> {
    println("Trying alternative USB paths...")
    val alternativePaths = listOf(
        "/storage" to "Внешние накопители",
        "/mnt" to "Монтированные устройства",
        "/mnt/usb" to "USB устройства",
        "/mnt/usbdisk" to "USB диски"
    )
    
    val items = mutableListOf<FolderItem>()
    alternativePaths.forEach { (path, name) ->
        val dir = File(path)
        if (dir.exists() && dir.canRead()) {
            items.add(FolderItem(name = name, path = path, isDirectory = true))
            println("Found accessible alternative: $path")
        }
    }
    
    return items
}

private fun getParentPath(path: String): String {
    val parent = File(path).parent
    return parent ?: "/"
}

private fun getInitialPaths(): List<FolderItem> {
    val paths = mutableListOf<FolderItem>()
    val addedPaths = mutableSetOf<String>()
    
    // Функция для добавления пути с проверкой на уникальность
    fun addPathIfUnique(absolutePath: String, displayName: String) {
        println("Checking path: '$absolutePath' with name: '$displayName'")
        
        val canonicalPath = try {
            File(absolutePath).canonicalPath
        } catch (e: Exception) {
            println("Error getting canonical path for '$absolutePath': ${e.message}")
            absolutePath
        }
        
        println("Canonical path: '$canonicalPath'")
        
        if (!addedPaths.contains(canonicalPath)) {
            val file = File(absolutePath)
            println("File exists: ${file.exists()}, canRead: ${file.canRead()}")
            
            if (file.exists() && file.canRead()) {
                val folderItem = FolderItem(
                    name = displayName,
                    path = absolutePath,
                    isDirectory = true
                )
                paths.add(folderItem)
                addedPaths.add(canonicalPath)
                println("Added unique path: '$absolutePath' -> '$canonicalPath' with name: '$displayName'")
            } else {
                println("Skipped path '$absolutePath' - not accessible")
            }
        } else {
            println("Skipped duplicate path: '$absolutePath' -> '$canonicalPath'")
        }
    }
    
    // Проверяем основные пути для внешних устройств
    val commonPaths = listOf(
        "/storage/usbotg" to "USB OTG",
        "/storage/usbdisk" to "USB диск", 
        "/storage/sdcard1" to "SD карта",
        "/storage/extSdCard" to "Внешняя SD карта",
        "/mnt/usb" to "USB устройство",
        "/mnt/sdcard" to "SD карта"
    )
    
    commonPaths.forEach { (path, name) ->
        addPathIfUnique(path, name)
    }
    
    // Ищем реальные устройства в /storage
    try {
        val storageDir = File("/storage")
        println("Scanning /storage directory...")
        if (storageDir.exists() && storageDir.canRead()) {
            val storageItems = storageDir.listFiles()
            println("Found ${storageItems?.size ?: 0} items in /storage")
            storageItems?.forEach { item ->
                println("Storage item: ${item.name}, isDir: ${item.isDirectory}")
                if (item.isDirectory && item.name != "emulated" && item.name != "self") {
                    val deviceName = when {
                        item.name.contains("usb", ignoreCase = true) -> "USB устройство (${item.name})"
                        item.name.contains("sdcard", ignoreCase = true) -> "SD карта (${item.name})"
                        item.name.contains("ext", ignoreCase = true) -> "Внешнее хранилище (${item.name})"
                        else -> "Устройство (${item.name})"
                    }
                    addPathIfUnique(item.absolutePath, deviceName)
                }
            }
        } else {
            println("/storage directory not accessible")
        }
    } catch (e: Exception) {
        println("Error scanning /storage: ${e.message}")
    }
    
    // Поиск USB устройств в /mnt/media_rw (основной путь для USB в Android)
    try {
        val mediaRwDir = File("/mnt/media_rw")
        println("Scanning /mnt/media_rw directory...")
        println("MediaRW exists: ${mediaRwDir.exists()}")
        println("MediaRW canRead: ${mediaRwDir.canRead()}")
        println("MediaRW canWrite: ${mediaRwDir.canWrite()}")
        println("MediaRW isDirectory: ${mediaRwDir.isDirectory}")
        
        if (mediaRwDir.exists() && mediaRwDir.canRead()) {
            val mediaRwItems = mediaRwDir.listFiles()
            println("Found ${mediaRwItems?.size ?: 0} items in /mnt/media_rw")
            mediaRwItems?.forEach { item ->
                println("MediaRW item: ${item.name}, isDir: ${item.isDirectory}, canRead: ${item.canRead()}")
                if (item.isDirectory) {
                    val deviceName = when {
                        item.name.contains("usb", ignoreCase = true) -> "USB устройство (${item.name})"
                        item.name.contains("sdcard", ignoreCase = true) -> "SD карта (${item.name})"
                        item.name.contains("ext", ignoreCase = true) -> "Внешнее хранилище (${item.name})"
                        item.name.matches(Regex("[A-F0-9]+")) -> "USB устройство (${item.name})" // UUID-подобные имена
                        else -> "Внешнее устройство (${item.name})"
                    }
                    addPathIfUnique(item.absolutePath, deviceName)
                }
            }
        } else {
            println("/mnt/media_rw directory not accessible")
            // Попробуем альтернативные пути для USB устройств
            val alternativePaths = listOf(
                "/storage" to "Внешние накопители",
                "/mnt" to "Монтированные устройства",
                "/mnt/usb" to "USB устройства",
                "/mnt/usbdisk" to "USB диски"
            )
            alternativePaths.forEach { (path, name) ->
                addPathIfUnique(path, name)
            }
            
            // Попробуем найти USB устройства через другие пути
            try {
                val usbPaths = listOf("/mnt/usb", "/mnt/usbdisk", "/storage/usbotg")
                usbPaths.forEach { usbPath ->
                    val usbDir = File(usbPath)
                    if (usbDir.exists() && usbDir.canRead()) {
                        println("Found accessible USB path: $usbPath")
                        addPathIfUnique(usbPath, "USB устройство")
                    }
                }
            } catch (e: Exception) {
                println("Error checking alternative USB paths: ${e.message}")
            }
        }
    } catch (e: Exception) {
        println("Error scanning /mnt/media_rw: ${e.message}")
        e.printStackTrace()
    }
    
    // Дополнительный поиск в /mnt
    try {
        val mntDir = File("/mnt")
        println("Scanning /mnt directory...")
        if (mntDir.exists() && mntDir.canRead()) {
            val mntItems = mntDir.listFiles()
            println("Found ${mntItems?.size ?: 0} items in /mnt")
            mntItems?.forEach { item ->
                println("Mnt item: ${item.name}, isDir: ${item.isDirectory}")
                if (item.isDirectory && item.name != "media_rw") { // Исключаем media_rw, так как уже проверили
                    val deviceName = when {
                        item.name.contains("usb", ignoreCase = true) -> "USB устройство (${item.name})"
                        item.name.contains("sdcard", ignoreCase = true) -> "SD карта (${item.name})"
                        item.name.contains("ext", ignoreCase = true) -> "Внешнее хранилище (${item.name})"
                        else -> "Устройство (${item.name})"
                    }
                    addPathIfUnique(item.absolutePath, deviceName)
                }
            }
        } else {
            println("/mnt directory not accessible")
        }
    } catch (e: Exception) {
        println("Error scanning /mnt: ${e.message}")
    }
    
    // Добавляем внутреннее хранилище (только один вариант)
    addPathIfUnique("/sdcard", "Внутренняя память")
    
    // Добавляем корневую папку
    addPathIfUnique("/", "Корневая папку (/)")

    // Если /mnt/media_rw недоступна, добавляем информационное сообщение
    val mediaRwDir = File("/mnt/media_rw")
    if (mediaRwDir.exists() && !mediaRwDir.canRead()) {
        println("WARNING: /mnt/media_rw exists but is not readable - this is normal on Android")
        // Добавляем путь для информации, даже если недоступен
        addPathIfUnique("/mnt/media_rw", "USB устройства (требует root)")
    }

    println("=== FINAL PATHS ===")
    println("Total unique paths found: ${paths.size}")
    paths.forEachIndexed { index, path ->
        println("Path $index: '${path.name}' -> '${path.path}'")
    }

    return paths
}
