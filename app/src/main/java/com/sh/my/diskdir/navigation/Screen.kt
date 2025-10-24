package com.sh.my.diskdir.navigation

sealed class Screen(val route: String) {
    object FlashDriveList : Screen("flash_drive_list")
    object FileExplorer : Screen("file_explorer/{flashDriveId}") {
        fun createRoute(flashDriveId: String) = "file_explorer/$flashDriveId"
    }
    object BackupRestore : Screen("backup_restore")
    object GroupManagement : Screen("group_management")
    object VirtualCatalog : Screen("virtual_catalog")
}
