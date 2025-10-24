package com.sh.my.diskdir.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class VirtualCatalog(
    val id: String,
    val name: String,
    val originalPath: String,
    val scanDate: Date,
    val totalFiles: Int,
    val totalDirectories: Int,
    val totalSize: Long,
    val rootItems: List<VirtualFileItem> = emptyList()
) : Parcelable

@Parcelize
data class VirtualFileItem(
    val id: String,
    val name: String,
    val relativePath: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Date,
    val extension: String = "",
    val parentId: String? = null,
    val children: List<VirtualFileItem> = emptyList()
) : Parcelable {
    val displayName: String
        get() = if (isDirectory) "$name (dir)" else "$name.$extension"
    
    val fullPath: String
        get() = if (relativePath.isEmpty()) name else "$relativePath/$name"
}
