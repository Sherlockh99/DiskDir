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
        val directory = File(path)
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
    val parent = File(path).parent
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
        val file = File(path)
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
