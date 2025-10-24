package com.sh.my.diskdir.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sh.my.diskdir.data.model.FlashDrive
import com.sh.my.diskdir.data.model.FlashDriveGroup
import com.sh.my.diskdir.viewmodel.FlashDriveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManagementScreen(
    viewModel: FlashDriveViewModel,
    onNavigateBack: () -> Unit
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val flashDrives by viewModel.flashDrives.collectAsStateWithLifecycle()
    
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showMoveToGroupDialog by remember { mutableStateOf(false) }
    var selectedFlashDrive by remember { mutableStateOf<FlashDrive?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление группами") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateGroupDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить группу")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateGroupDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить группу")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Существующие группы
            items(groups) { group ->
                GroupCard(
                    group = group,
                    onEdit = { /* Логика редактирования группы */ },
                    onDelete = { /* Логика удаления группы */ }
                )
            }
            
            // Флешки без группы
            val ungroupedDrives = flashDrives.filter { it.groupId == null }
            if (ungroupedDrives.isNotEmpty()) {
                item {
                    Text(
                        text = "Флешки без группы",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(ungroupedDrives) { flashDrive ->
                    FlashDriveInGroupCard(
                        flashDrive = flashDrive,
                        onMoveToGroup = { 
                            selectedFlashDrive = flashDrive
                            showMoveToGroupDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // Диалог создания группы
    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onCreateGroup = { name, color ->
                // Логика создания группы
                showCreateGroupDialog = false
            }
        )
    }
    
    // Диалог перемещения в группу
    if (showMoveToGroupDialog && selectedFlashDrive != null) {
        MoveToGroupDialog(
            flashDrive = selectedFlashDrive!!,
            groups = groups,
            onDismiss = { 
                showMoveToGroupDialog = false
                selectedFlashDrive = null
            },
            onMoveToGroup = { groupId ->
                viewModel.moveFlashDriveToGroup(selectedFlashDrive!!.id, groupId)
                showMoveToGroupDialog = false
                selectedFlashDrive = null
            }
        )
    }
}

@Composable
fun GroupCard(
    group: FlashDriveGroup,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(group.color))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${group.flashDrives.size} устройств",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (group.flashDrives.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                group.flashDrives.forEach { flashDrive ->
                    FlashDriveInGroupCard(
                        flashDrive = flashDrive,
                        onMoveToGroup = { /* Логика перемещения */ }
                    )
                }
            }
        }
    }
}

@Composable
fun FlashDriveInGroupCard(
    flashDrive: FlashDrive,
    onMoveToGroup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMoveToGroup() },
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
                Icons.Default.Usb,
                contentDescription = null,
                tint = if (flashDrive.isConnected) Color.Green else Color.Gray
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = flashDrive.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = flashDrive.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreateGroup: (String, Int) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF0000FF)) }
    
    val colors = listOf(
        Color(0xFFFF0000), Color(0xFF0000FF), Color(0xFF00FF00), Color(0xFFFFFF00),
        Color(0xFFFF00FF), Color(0xFF00FFFF), Color(0xFF808080), Color(0xFF000000)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать группу") },
        text = {
            Column {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Название группы") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Выберите цвет:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (groupName.isNotBlank()) {
                        onCreateGroup(groupName, selectedColor.toArgb())
                    }
                },
                enabled = groupName.isNotBlank()
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
}

@Composable
fun MoveToGroupDialog(
    flashDrive: FlashDrive,
    groups: List<FlashDriveGroup>,
    onDismiss: () -> Unit,
    onMoveToGroup: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Переместить в группу") },
        text = {
            Column {
                Text(
                    text = "Флешка: ${flashDrive.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Кнопка "Без группы"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMoveToGroup(null) }
                ) {
                    Text(
                        text = "Без группы",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Список групп
                groups.forEach { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMoveToGroup(group.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(group.color))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(group.name)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
