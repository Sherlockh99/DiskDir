package com.sh.my.diskdir.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class FlashDrive(
    val id: String,
    val name: String,
    val path: String,
    val groupId: String? = null,
    val totalSize: Long = 0,
    val freeSpace: Long = 0,
    val lastScanned: Date? = null,
    val isConnected: Boolean = false
) : Parcelable

@Parcelize
data class FlashDriveGroup(
    val id: String,
    val name: String,
    val color: Int,
    val flashDrives: List<FlashDrive> = emptyList()
) : Parcelable
