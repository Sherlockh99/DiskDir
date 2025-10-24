package com.sh.my.diskdir.utils

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.util.*

object FileSystemUtils {
    
    /**
     * Получает список всех доступных внешних накопителей
     */
    fun getExternalStorageDevices(context: Context): List<StorageDevice> {
        val devices = mutableListOf<StorageDevice>()
        
        // Получаем основное внешнее хранилище
        val externalStorage = Environment.getExternalStorageDirectory()
        if (externalStorage != null && externalStorage.exists()) {
            devices.add(createStorageDevice(externalStorage, "Внутренняя память"))
        }
        
        // Ищем дополнительные USB устройства
        val usbDevices = findUSBDevices()
        devices.addAll(usbDevices)
        
        // Ищем SD карты
        val sdCards = findSDCards()
        devices.addAll(sdCards)
        
        return devices
    }
    
    private fun findUSBDevices(): List<StorageDevice> {
        val devices = mutableListOf<StorageDevice>()
        
        // Проверяем стандартные пути для USB устройств
        val usbPaths = listOf(
            "/storage/usbotg",
            "/storage/usbdisk",
            "/mnt/usb",
            "/mnt/usbdisk",
            "/storage/external_storage"
        )
        
        usbPaths.forEach { path ->
            val file = File(path)
            if (file.exists() && file.canRead()) {
                devices.add(createStorageDevice(file, "USB устройство"))
            }
        }
        
        return devices
    }
    
    private fun findSDCards(): List<StorageDevice> {
        val devices = mutableListOf<StorageDevice>()
        
        // Проверяем стандартные пути для SD карт
        val sdPaths = listOf(
            "/storage/sdcard1",
            "/storage/extSdCard",
            "/mnt/sdcard/external_sd",
            "/storage/external_SD"
        )
        
        sdPaths.forEach { path ->
            val file = File(path)
            if (file.exists() && file.canRead()) {
                devices.add(createStorageDevice(file, "SD карта"))
            }
        }
        
        return devices
    }
    
    private fun createStorageDevice(file: File, name: String): StorageDevice {
        val statFs = StatFs(file.absolutePath)
        val totalSize = statFs.blockCountLong * statFs.blockSizeLong
        val freeSpace = statFs.availableBlocksLong * statFs.blockSizeLong
        
        return StorageDevice(
            id = UUID.randomUUID().toString(),
            name = name,
            path = file.absolutePath,
            totalSize = totalSize,
            freeSpace = freeSpace,
            isConnected = true,
            lastScanned = Date()
        )
    }
    
    /**
     * Сканирует содержимое директории
     */
    fun scanDirectory(path: String): List<FileItem> {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }
        
        return directory.listFiles()?.map { file ->
            FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0,
                lastModified = Date(file.lastModified()),
                extension = if (file.isFile) {
                    val lastDot = file.name.lastIndexOf('.')
                    if (lastDot > 0) file.name.substring(lastDot + 1) else ""
                } else "",
                parentPath = file.parent ?: ""
            )
        }?.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name }) ?: emptyList()
    }
    
    /**
     * Получает информацию о размере файла в читаемом формате
     */
    fun formatFileSize(bytes: Long): String {
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
    
    /**
     * Проверяет, доступно ли устройство для чтения
     */
    fun isDeviceAccessible(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.canRead()
    }
}

data class StorageDevice(
    val id: String,
    val name: String,
    val path: String,
    val totalSize: Long,
    val freeSpace: Long,
    val isConnected: Boolean,
    val lastScanned: Date
)

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Date,
    val extension: String,
    val parentPath: String
)
