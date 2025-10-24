package com.sh.my.diskdir.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.my.diskdir.data.model.FileItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class FileExplorerViewModel : ViewModel() {
    private val _currentPath = MutableStateFlow("")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _fileItems = MutableStateFlow<List<FileItem>>(emptyList())
    val fileItems: StateFlow<List<FileItem>> = _fileItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _navigationStack = MutableStateFlow<List<String>>(emptyList())
    val navigationStack: StateFlow<List<String>> = _navigationStack.asStateFlow()

    fun loadDirectory(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentPath.value = path
            
            // Добавляем путь в стек навигации
            val newStack = _navigationStack.value.toMutableList()
            if (!newStack.contains(path)) {
                newStack.add(path)
            }
            _navigationStack.value = newStack

            try {
                val files = scanDirectory(path)
                _fileItems.value = files
            } catch (e: Exception) {
                // Обработка ошибок
                _fileItems.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    private fun scanDirectory(path: String): List<FileItem> {
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

    fun navigateToDirectory(path: String) {
        loadDirectory(path)
    }

    fun navigateBack(): Boolean {
        val stack = _navigationStack.value.toMutableList()
        return if (stack.size > 1) {
            stack.removeLastOrNull()
            _navigationStack.value = stack
            val parentPath = stack.lastOrNull() ?: ""
            loadDirectory(parentPath)
            true
        } else {
            false
        }
    }

    fun getParentDirectory(): String? {
        val current = _currentPath.value
        val parent = File(current).parent
        return if (parent != null && parent != current) parent else null
    }
}
