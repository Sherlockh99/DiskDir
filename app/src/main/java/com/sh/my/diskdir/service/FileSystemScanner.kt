package com.sh.my.diskdir.service

import android.content.Context
import com.sh.my.diskdir.data.model.VirtualCatalog
import com.sh.my.diskdir.data.model.VirtualFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class FileSystemScanner(private val context: Context) {
    
    suspend fun scanDirectory(
        rootPath: String,
        catalogName: String,
        onProgress: (String, Int, Int) -> Unit = { _, _, _ -> }
    ): VirtualCatalog = withContext(Dispatchers.IO) {
        
        val rootFile = File(rootPath)
        if (!rootFile.exists() || !rootFile.isDirectory) {
            throw IllegalArgumentException("Path does not exist or is not a directory: $rootPath")
        }
        
        val catalogId = UUID.randomUUID().toString()
        val scanDate = Date()
        val allItems = mutableListOf<VirtualFileItem>()
        var processedFiles = 0
        var totalFiles = 0
        
        // Сначала подсчитываем общее количество файлов для прогресса
        totalFiles = countFilesRecursively(rootFile)
        
        // Сканируем рекурсивно
        scanDirectoryRecursive(
            rootFile,
            catalogId,
            "",
            allItems,
            onProgress = { currentPath, current, total ->
                onProgress(currentPath, current, total)
                processedFiles = current
            }
        )
        
        val totalDirectories = allItems.count { it.isDirectory }
        val totalFilesCount = allItems.count { !it.isDirectory }
        val totalSize = allItems.filter { !it.isDirectory }.sumOf { it.size }
        
        VirtualCatalog(
            id = catalogId,
            name = catalogName,
            originalPath = rootPath,
            scanDate = scanDate,
            totalFiles = totalFilesCount,
            totalDirectories = totalDirectories,
            totalSize = totalSize,
            rootItems = allItems.filter { it.parentId == null }
        )
    }
    
    private suspend fun scanDirectoryRecursive(
        directory: File,
        catalogId: String,
        relativePath: String,
        allItems: MutableList<VirtualFileItem>,
        onProgress: (String, Int, Int) -> Unit
    ) {
        try {
            val files = directory.listFiles() ?: return
            
            for (file in files) {
                val itemId = UUID.randomUUID().toString()
                val currentRelativePath = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
                val parentId = if (relativePath.isEmpty()) null else {
                    // Находим ID родительской папки
                    allItems.find { it.relativePath == relativePath }?.id
                }
                
                val extension = if (file.isFile) {
                    val lastDot = file.name.lastIndexOf('.')
                    if (lastDot > 0) file.name.substring(lastDot + 1) else ""
                } else ""
                
                val virtualItem = VirtualFileItem(
                    id = itemId,
                    name = file.name,
                    relativePath = currentRelativePath,
                    isDirectory = file.isDirectory,
                    size = if (file.isFile) file.length() else 0,
                    lastModified = Date(file.lastModified()),
                    extension = extension,
                    parentId = parentId
                )
                
                allItems.add(virtualItem)
                
                // Обновляем прогресс
                onProgress(file.absolutePath, allItems.size, 0)
                
                // Если это директория, сканируем её рекурсивно
                if (file.isDirectory && file.canRead()) {
                    scanDirectoryRecursive(
                        file,
                        catalogId,
                        currentRelativePath,
                        allItems,
                        onProgress
                    )
                }
            }
        } catch (e: SecurityException) {
            // Игнорируем файлы, к которым нет доступа
        } catch (e: Exception) {
            // Логируем другие ошибки, но продолжаем сканирование
        }
    }
    
    private fun countFilesRecursively(directory: File): Int {
        var count = 0
        try {
            val files = directory.listFiles() ?: return 0
            
            for (file in files) {
                count++
                if (file.isDirectory && file.canRead()) {
                    count += countFilesRecursively(file)
                }
            }
        } catch (e: SecurityException) {
            // Игнорируем недоступные файлы
        } catch (e: Exception) {
            // Игнорируем другие ошибки
        }
        return count
    }
    
    suspend fun getDirectoryContents(
        catalogId: String,
        parentId: String?,
        fileItemDao: com.sh.my.diskdir.data.database.VirtualFileItemDao
    ): List<VirtualFileItem> = withContext(Dispatchers.IO) {
        if (parentId == null) {
            fileItemDao.getRootItems(catalogId).map { it.toVirtualFileItem() }
        } else {
            fileItemDao.getChildren(parentId, catalogId).map { it.toVirtualFileItem() }
        }
    }
    
    suspend fun searchInCatalog(
        catalogId: String,
        query: String,
        fileItemDao: com.sh.my.diskdir.data.database.VirtualFileItemDao
    ): List<VirtualFileItem> = withContext(Dispatchers.IO) {
        // Здесь можно добавить поиск по базе данных
        // Пока что возвращаем пустой список
        emptyList()
    }
}
