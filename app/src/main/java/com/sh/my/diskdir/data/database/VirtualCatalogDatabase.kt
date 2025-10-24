package com.sh.my.diskdir.data.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sh.my.diskdir.data.model.VirtualCatalog
import com.sh.my.diskdir.data.model.VirtualFileItem
import java.util.Date

@Database(
    entities = [VirtualCatalogEntity::class, VirtualFileItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VirtualCatalogDatabase : RoomDatabase() {
    abstract fun virtualCatalogDao(): VirtualCatalogDao
    abstract fun virtualFileItemDao(): VirtualFileItemDao
}

@Dao
interface VirtualCatalogDao {
    @Query("SELECT * FROM virtualcatalog ORDER BY scanDate DESC")
    suspend fun getAllCatalogs(): List<VirtualCatalogEntity>
    
    @Query("SELECT * FROM virtualcatalog WHERE id = :id")
    suspend fun getCatalogById(id: String): VirtualCatalogEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalog(catalog: VirtualCatalogEntity)
    
    @Delete
    suspend fun deleteCatalog(catalog: VirtualCatalogEntity)
    
    @Query("DELETE FROM virtualcatalog WHERE id = :id")
    suspend fun deleteCatalogById(id: String)
}

@Dao
interface VirtualFileItemDao {
    @Query("SELECT * FROM virtualfileitem WHERE catalogId = :catalogId AND parentId IS NULL ORDER BY isDirectory DESC, name ASC")
    suspend fun getRootItems(catalogId: String): List<VirtualFileItemEntity>
    
    @Query("SELECT * FROM virtualfileitem WHERE catalogId = :catalogId AND parentId = :parentId ORDER BY isDirectory DESC, name ASC")
    suspend fun getChildren(parentId: String, catalogId: String): List<VirtualFileItemEntity>
    
    @Query("SELECT * FROM virtualfileitem WHERE id = :id")
    suspend fun getItemById(id: String): VirtualFileItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VirtualFileItemEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<VirtualFileItemEntity>)
    
    @Query("DELETE FROM virtualfileitem WHERE catalogId = :catalogId")
    suspend fun deleteItemsByCatalogId(catalogId: String)
    
    @Query("SELECT COUNT(*) FROM virtualfileitem WHERE catalogId = :catalogId AND isDirectory = 0")
    suspend fun getFileCount(catalogId: String): Int
    
    @Query("SELECT COUNT(*) FROM virtualfileitem WHERE catalogId = :catalogId AND isDirectory = 1")
    suspend fun getDirectoryCount(catalogId: String): Int
    
    @Query("SELECT SUM(size) FROM virtualfileitem WHERE catalogId = :catalogId AND isDirectory = 0")
    suspend fun getTotalSize(catalogId: String): Long?
}

@Entity(tableName = "virtualcatalog")
data class VirtualCatalogEntity(
    @PrimaryKey val id: String,
    val name: String,
    val originalPath: String,
    val scanDate: Long,
    val totalFiles: Int,
    val totalDirectories: Int,
    val totalSize: Long
) {
    fun toVirtualCatalog(): VirtualCatalog {
        return VirtualCatalog(
            id = id,
            name = name,
            originalPath = originalPath,
            scanDate = Date(scanDate),
            totalFiles = totalFiles,
            totalDirectories = totalDirectories,
            totalSize = totalSize
        )
    }
}

@Entity(tableName = "virtualfileitem")
data class VirtualFileItemEntity(
    @PrimaryKey val id: String,
    val catalogId: String,
    val name: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val parentId: String?
) {
    fun toVirtualFileItem(): VirtualFileItem {
        return VirtualFileItem(
            id = id,
            name = name,
            relativePath = relativePath,
            isDirectory = isDirectory,
            size = size,
            lastModified = Date(lastModified),
            extension = extension,
            parentId = parentId
        )
    }
}

