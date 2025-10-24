package com.sh.my.diskdir.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.my.diskdir.data.model.FlashDrive
import com.sh.my.diskdir.data.model.FlashDriveGroup
import com.sh.my.diskdir.repository.FlashDriveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlashDriveViewModel(private val context: Context) : ViewModel() {
    private val repository = FlashDriveRepository(context)
    
    private val _flashDrives = MutableStateFlow<List<FlashDrive>>(emptyList())
    val flashDrives: StateFlow<List<FlashDrive>> = _flashDrives.asStateFlow()

    private val _groups = MutableStateFlow<List<FlashDriveGroup>>(emptyList())
    val groups: StateFlow<List<FlashDriveGroup>> = _groups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFlashDrives()
        loadGroups()
    }

    private fun loadFlashDrives() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val drives = repository.getFlashDrives()
                _flashDrives.value = drives
            } catch (e: Exception) {
                // Обработка ошибок
                _flashDrives.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            try {
                val groupsList = repository.getGroups()
                _groups.value = groupsList
            } catch (e: Exception) {
                // Обработка ошибок
                _groups.value = emptyList()
            }
        }
    }

    fun scanFlashDrive(flashDriveId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repository.scanFlashDrive(flashDriveId)
                if (success) {
                    // Обновляем информацию о флешке
                    loadFlashDrives()
                }
            } catch (e: Exception) {
                // Обработка ошибок
            }
            _isLoading.value = false
        }
    }

    fun moveFlashDriveToGroup(flashDriveId: String, groupId: String?) {
        viewModelScope.launch {
            try {
                repository.moveFlashDriveToGroup(flashDriveId, groupId)
                loadFlashDrives()
                loadGroups()
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
    
    fun createGroup(name: String, color: Int) {
        viewModelScope.launch {
            try {
                repository.createGroup(name, color)
                loadGroups()
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
    
    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            try {
                repository.deleteGroup(groupId)
                loadGroups()
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
}
