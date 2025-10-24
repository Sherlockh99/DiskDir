package com.sh.my.diskdir.repository

import android.content.Context
import com.sh.my.diskdir.data.model.FlashDrive
import com.sh.my.diskdir.data.model.FlashDriveGroup
import com.sh.my.diskdir.utils.FileSystemUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class FlashDriveRepository(private val context: Context) {
    
    private val storageDevices = mutableListOf<FlashDrive>()
    private val groups = mutableListOf<FlashDriveGroup>()
    
    suspend fun getFlashDrives(): List<FlashDrive> = withContext(Dispatchers.IO) {
        // Получаем список внешних устройств
        val devices = FileSystemUtils.getExternalStorageDevices(context)
        
        // Конвертируем в FlashDrive объекты
        devices.map { device ->
            FlashDrive(
                id = device.id,
                name = device.name,
                path = device.path,
                totalSize = device.totalSize,
                freeSpace = device.freeSpace,
                lastScanned = device.lastScanned,
                isConnected = device.isConnected
            )
        }
    }
    
    suspend fun getGroups(): List<FlashDriveGroup> = withContext(Dispatchers.IO) {
        // Загружаем группы из локального хранилища
        // Пока что возвращаем тестовые данные
        listOf(
            FlashDriveGroup(
                id = "group1",
                name = "Рабочие флешки",
                color = android.graphics.Color.BLUE
            ),
            FlashDriveGroup(
                id = "group2", 
                name = "Личные флешки",
                color = android.graphics.Color.GREEN
            )
        )
    }
    
    suspend fun scanFlashDrive(flashDriveId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val flashDrive = storageDevices.find { it.id == flashDriveId }
            flashDrive?.let { drive ->
                // Сканируем содержимое флешки
                val files = FileSystemUtils.scanDirectory(drive.path)
                // Сохраняем результаты сканирования
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun moveFlashDriveToGroup(flashDriveId: String, groupId: String?) {
        withContext(Dispatchers.IO) {
            val index = storageDevices.indexOfFirst { it.id == flashDriveId }
            if (index != -1) {
                storageDevices[index] = storageDevices[index].copy(groupId = groupId)
            }
        }
    }
    
    suspend fun createGroup(name: String, color: Int): FlashDriveGroup = withContext(Dispatchers.IO) {
        val group = FlashDriveGroup(
            id = UUID.randomUUID().toString(),
            name = name,
            color = color
        )
        groups.add(group)
        group
    }
    
    suspend fun deleteGroup(groupId: String): Boolean = withContext(Dispatchers.IO) {
        val index = groups.indexOfFirst { it.id == groupId }
        if (index != -1) {
            groups.removeAt(index)
            // Убираем группу у всех флешек в этой группе
            storageDevices.forEachIndexed { i, drive ->
                if (drive.groupId == groupId) {
                    storageDevices[i] = drive.copy(groupId = null)
                }
            }
            true
        } else {
            false
        }
    }
    
    suspend fun updateGroup(group: FlashDriveGroup) {
        withContext(Dispatchers.IO) {
            val index = groups.indexOfFirst { it.id == group.id }
            if (index != -1) {
                groups[index] = group
            }
        }
    }
}
