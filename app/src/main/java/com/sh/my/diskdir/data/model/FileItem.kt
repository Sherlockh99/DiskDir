package com.sh.my.diskdir.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Date,
    val extension: String = "",
    val parentPath: String = "",
    val children: List<FileItem> = emptyList()
) : Parcelable {
    val displayName: String
        get() = if (isDirectory) "$name (dir)" else "$name.$extension"
}
