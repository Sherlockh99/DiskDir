package com.sh.my.diskdir.repository

import android.content.Context
import androidx.room.Room
import com.sh.my.diskdir.data.database.VirtualCatalogDatabase
import com.sh.my.diskdir.data.database.VirtualCatalogEntity
import com.sh.my.diskdir.data.database.VirtualFileItemEntity
import com.sh.my.diskdir.data.model.VirtualCatalog
import com.sh.my.diskdir.data.model.VirtualFileItem
import com.sh.my.diskdir.service.FileSystemScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class VirtualCatalogRepository(private val context: Context) {
    
    private val database = Room.databaseBuilder(
        context,
        VirtualCatalogDatabase::class.java,
        "virtual_catalog_database"
    ).build()
    
    private val catalogDao = database.virtualCatalogDao()
    private val fileItemDao = database.virtualFileItemDao()
    private val scanner = FileSystemScanner(context)
    
    suspend fun getAllCatalogs(): List<VirtualCatalog> = withContext(Dispatchers.IO) {
        catalogDao.getAllCatalogs().map { it.toVirtualCatalog() }
    }
    
    suspend fun getCatalogById(id: String): VirtualCatalog? = withContext(Dispatchers.IO) {
        catalogDao.getCatalogById(id)?.toVirtualCatalog()
    }
    
    suspend fun createVirtualCatalog(
        rootPath: String,
        catalogName: String,
        onProgress: (String, Int, Int) -> Unit
    ): VirtualCatalog = withContext(Dispatchers.IO) {
        // Сканируем файловую систему
        val catalog = scanner.scanDirectory(rootPath, catalogName, onProgress)
        
        // Сохраняем каталог в базу данных
        val catalogEntity = VirtualCatalogEntity(
            id = catalog.id,
            name = catalog.name,
            originalPath = catalog.originalPath,
            scanDate = catalog.scanDate.time,
            totalFiles = catalog.totalFiles,
            totalDirectories = catalog.totalDirectories,
            totalSize = catalog.totalSize
        )
        catalogDao.insertCatalog(catalogEntity)
        
        // Сохраняем все файлы и папки
        saveFileItems(catalog.rootItems, catalog.id)
        
        catalog
    }
    
    private suspend fun saveFileItems(items: List<VirtualFileItem>, catalogId: String) {
        // Собираем все элементы рекурсивно
        val allItems = mutableListOf<VirtualFileItem>()
        collectAllItemsRecursively(items, allItems)
        
        // Отладочная информация
        println("=== SAVING FILE ITEMS ===")
        println("Total items to save: ${allItems.size}")
        println("Root items: ${items.size}")
        allItems.forEach { item ->
            println("Item: ${item.name}, isDir: ${item.isDirectory}, parentId: ${item.parentId}, children: ${item.children.size}")
        }
        
        val entities = allItems.map { item ->
            VirtualFileItemEntity(
                id = item.id,
                catalogId = catalogId,
                name = item.name,
                relativePath = item.relativePath,
                isDirectory = item.isDirectory,
                size = item.size,
                lastModified = item.lastModified.time,
                extension = item.extension,
                parentId = item.parentId
            )
        }
        fileItemDao.insertItems(entities)
        println("=== SAVED ${entities.size} ITEMS ===")
    }
    
    private fun collectAllItemsRecursively(items: List<VirtualFileItem>, allItems: MutableList<VirtualFileItem>) {
        for (item in items) {
            allItems.add(item)
            if (item.children.isNotEmpty()) {
                collectAllItemsRecursively(item.children, allItems)
            }
        }
    }
    
    suspend fun getDirectoryContents(catalogId: String, parentId: String?): List<VirtualFileItem> = withContext(Dispatchers.IO) {
        val items = if (parentId == null) {
            fileItemDao.getRootItems(catalogId).map { it.toVirtualFileItem() }
        } else {
            fileItemDao.getChildren(parentId, catalogId).map { it.toVirtualFileItem() }
        }
        
        // Отладочная информация
        println("=== GETTING DIRECTORY CONTENTS ===")
        println("CatalogId: $catalogId, ParentId: $parentId")
        println("Found ${items.size} items")
        items.forEach { item ->
            println("Item: ${item.name}, isDir: ${item.isDirectory}, parentId: ${item.parentId}")
        }
        
        items
    }
    
    suspend fun searchInCatalog(catalogId: String, query: String): List<VirtualFileItem> = withContext(Dispatchers.IO) {
        scanner.searchInCatalog(catalogId, query, fileItemDao)
    }
    
    suspend fun deleteCatalog(catalogId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Удаляем все файлы каталога
            fileItemDao.deleteItemsByCatalogId(catalogId)
            // Удаляем сам каталог
            catalogDao.deleteCatalogById(catalogId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateCatalogStats(catalogId: String): VirtualCatalog? = withContext(Dispatchers.IO) {
        try {
            val catalog = catalogDao.getCatalogById(catalogId) ?: return@withContext null
            
            val fileCount = fileItemDao.getFileCount(catalogId)
            val directoryCount = fileItemDao.getDirectoryCount(catalogId)
            val totalSize = fileItemDao.getTotalSize(catalogId) ?: 0L
            
            val updatedCatalog = catalog.toVirtualCatalog().copy(
                totalFiles = fileCount,
                totalDirectories = directoryCount,
                totalSize = totalSize
            )
            
            val catalogEntity = VirtualCatalogEntity(
                id = updatedCatalog.id,
                name = updatedCatalog.name,
                originalPath = updatedCatalog.originalPath,
                scanDate = updatedCatalog.scanDate.time,
                totalFiles = updatedCatalog.totalFiles,
                totalDirectories = updatedCatalog.totalDirectories,
                totalSize = updatedCatalog.totalSize
            )
            
            catalogDao.insertCatalog(catalogEntity)
            updatedCatalog
        } catch (e: Exception) {
            null
        }
    }
}
