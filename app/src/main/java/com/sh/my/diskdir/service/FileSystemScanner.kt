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
        
        // Сканируем рекурсивно и строим иерархию
        val rootItems = scanDirectoryRecursive(
            rootFile,
            catalogId,
            "",
            allItems,
            onProgress = { currentPath, current, total ->
                onProgress(currentPath, current, totalFiles)
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
            rootItems = rootItems
        )
    }
    
    private suspend fun scanDirectoryRecursive(
        directory: File,
        catalogId: String,
        relativePath: String,
        allItems: MutableList<VirtualFileItem>,
        onProgress: (String, Int, Int) -> Unit,
        parentId: String? = null
    ): List<VirtualFileItem> {
        try {
            val files = directory.listFiles() ?: return emptyList()
            val items = mutableListOf<VirtualFileItem>()
            
            println("=== SCANNING DIRECTORY: ${directory.absolutePath} ===")
            println("Files found: ${files.size}, parentId: $parentId")
            
            for (file in files) {
                val itemId = UUID.randomUUID().toString()
                val currentRelativePath = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
                
                val extension = if (file.isFile) {
                    val lastDot = file.name.lastIndexOf('.')
                    if (lastDot > 0) file.name.substring(lastDot + 1) else ""
                } else ""
                
                println("Processing: ${file.name}, isDir: ${file.isDirectory}, parentId: $parentId")
                
                // Если это директория, сначала сканируем её содержимое
                val children = if (file.isDirectory && file.canRead()) {
                    println("Scanning subdirectory: ${file.absolutePath}")
                    val subItems = scanDirectoryRecursive(
                        file,
                        catalogId,
                        currentRelativePath,
                        allItems,
                        onProgress,
                        itemId // Передаем ID текущей папки как parentId для дочерних элементов
                    )
                    println("Subdirectory ${file.name} has ${subItems.size} items")
                    subItems
                } else {
                    emptyList()
                }
                
                val virtualItem = VirtualFileItem(
                    id = itemId,
                    name = file.name,
                    relativePath = currentRelativePath,
                    isDirectory = file.isDirectory,
                    size = if (file.isFile) file.length() else 0,
                    lastModified = Date(file.lastModified()),
                    extension = extension,
                    parentId = parentId,
                    children = children
                )
                
                items.add(virtualItem)
                allItems.add(virtualItem)
                
                println("Added item: ${file.name}, children: ${children.size}, parentId: $parentId")
                
                // Обновляем прогресс
                onProgress(file.absolutePath, allItems.size, 0)
            }
            
            println("=== DIRECTORY ${directory.name} COMPLETE: ${items.size} items ===")
            return items
        } catch (e: SecurityException) {
            println("Security exception for ${directory.absolutePath}: ${e.message}")
            return emptyList()
        } catch (e: Exception) {
            println("Exception for ${directory.absolutePath}: ${e.message}")
            return emptyList()
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
