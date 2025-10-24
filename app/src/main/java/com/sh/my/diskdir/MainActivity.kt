package com.sh.my.diskdir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sh.my.diskdir.navigation.Screen
import com.sh.my.diskdir.ui.screens.*
import com.sh.my.diskdir.ui.theme.DiskDirTheme
import com.sh.my.diskdir.viewmodel.FlashDriveViewModel
import com.sh.my.diskdir.viewmodel.FileExplorerViewModel
import com.sh.my.diskdir.viewmodel.VirtualCatalogViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiskDirTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiskDirApp()
                }
            }
        }
    }
}

@Composable
fun DiskDirApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.FlashDriveList) }
    var flashDriveId by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val flashDriveViewModel: FlashDriveViewModel = viewModel { FlashDriveViewModel(context) }
    val fileExplorerViewModel: FileExplorerViewModel = viewModel()
    val virtualCatalogViewModel: VirtualCatalogViewModel = viewModel { VirtualCatalogViewModel(context) }
    
    when (currentScreen) {
        is Screen.FlashDriveList -> {
            FlashDriveListScreen(
                viewModel = flashDriveViewModel,
                onNavigateToFileExplorer = { id ->
                    flashDriveId = id
                    currentScreen = Screen.FileExplorer
                    fileExplorerViewModel.loadDirectory("/storage/usb$id") // Примерный путь
                },
                onNavigateToBackup = {
                    currentScreen = Screen.BackupRestore
                },
                onNavigateToGroupManagement = {
                    currentScreen = Screen.GroupManagement
                },
                onNavigateToVirtualCatalog = {
                    currentScreen = Screen.VirtualCatalog
                }
            )
        }
        
        is Screen.FileExplorer -> {
            FileExplorerScreen(
                viewModel = fileExplorerViewModel,
                onNavigateBack = {
                    currentScreen = Screen.FlashDriveList
                }
            )
        }
        
        is Screen.BackupRestore -> {
            BackupRestoreScreen(
                onNavigateBack = {
                    currentScreen = Screen.FlashDriveList
                }
            )
        }
        
        is Screen.GroupManagement -> {
            GroupManagementScreen(
                viewModel = flashDriveViewModel,
                onNavigateBack = {
                    currentScreen = Screen.FlashDriveList
                }
            )
        }
        
        is Screen.VirtualCatalog -> {
            VirtualCatalogScreen(
                viewModel = virtualCatalogViewModel,
                onNavigateBack = {
                    currentScreen = Screen.FlashDriveList
                }
            )
        }
    }
}