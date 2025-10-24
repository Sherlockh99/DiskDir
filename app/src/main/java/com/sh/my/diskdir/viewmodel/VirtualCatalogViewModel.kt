package com.sh.my.diskdir.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.my.diskdir.data.model.VirtualCatalog
import com.sh.my.diskdir.data.model.VirtualFileItem
import com.sh.my.diskdir.repository.VirtualCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VirtualCatalogViewModel(private val context: Context) : ViewModel() {
    
    private val repository = VirtualCatalogRepository(context)
    
    private val _catalogs = MutableStateFlow<List<VirtualCatalog>>(emptyList())
    val catalogs: StateFlow<List<VirtualCatalog>> = _catalogs.asStateFlow()
    
    private val _currentCatalog = MutableStateFlow<VirtualCatalog?>(null)
    val currentCatalog: StateFlow<VirtualCatalog?> = _currentCatalog.asStateFlow()
    
    private val _currentDirectoryItems = MutableStateFlow<List<VirtualFileItem>>(emptyList())
    val currentDirectoryItems: StateFlow<List<VirtualFileItem>> = _currentDirectoryItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _scanProgress = MutableStateFlow<Triple<String, Int, Int>?>(null)
    val scanProgress: StateFlow<Triple<String, Int, Int>?> = _scanProgress.asStateFlow()
    
    private val _navigationStack = MutableStateFlow<List<String>>(emptyList())
    val navigationStack: StateFlow<List<String>> = _navigationStack.asStateFlow()
    
    init {
        loadCatalogs()
    }
    
    private fun loadCatalogs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val catalogsList = repository.getAllCatalogs()
                _catalogs.value = catalogsList
            } catch (e: Exception) {
                _catalogs.value = emptyList()
            }
            _isLoading.value = false
        }
    }
    
    fun createVirtualCatalog(
        rootPath: String,
        catalogName: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val catalog = repository.createVirtualCatalog(
                    rootPath = rootPath,
                    catalogName = catalogName,
                    onProgress = { currentPath, current, total ->
                        _scanProgress.value = Triple(currentPath, current, total)
                    }
                )
                _currentCatalog.value = catalog
                loadCatalogs()
            } catch (e: Exception) {
                // Обработка ошибок
            }
            _isLoading.value = false
            _scanProgress.value = null
        }
    }
    
    fun openCatalog(catalogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val catalog = repository.getCatalogById(catalogId)
                if (catalog != null) {
                    _currentCatalog.value = catalog
                    loadDirectoryContents(catalogId, null)
                    _navigationStack.value = listOf("root")
                }
            } catch (e: Exception) {
                // Обработка ошибок
            }
            _isLoading.value = false
        }
    }
    
    fun loadDirectoryContents(catalogId: String, parentId: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                println("=== VIEWMODEL: Loading directory contents ===")
                println("CatalogId: $catalogId, ParentId: $parentId")
                val items = repository.getDirectoryContents(catalogId, parentId)
                println("ViewModel received ${items.size} items")
                items.forEach { item ->
                    println("ViewModel item: ${item.name}, isDir: ${item.isDirectory}, parentId: ${item.parentId}")
                }
                _currentDirectoryItems.value = items
            } catch (e: Exception) {
                println("ViewModel error: ${e.message}")
                _currentDirectoryItems.value = emptyList()
            }
            _isLoading.value = false
        }
    }
    
    fun navigateToDirectory(itemId: String) {
        val catalog = _currentCatalog.value ?: return
        val item = _currentDirectoryItems.value.find { it.id == itemId } ?: return
        
        println("=== VIEWMODEL: Navigating to directory ===")
        println("Item: ${item.name}, isDir: ${item.isDirectory}, itemId: $itemId")
        
        if (item.isDirectory) {
            loadDirectoryContents(catalog.id, itemId)
            
            // Добавляем в стек навигации
            val newStack = _navigationStack.value.toMutableList()
            newStack.add(itemId)
            _navigationStack.value = newStack
        }
    }
    
    fun navigateBack(): Boolean {
        val stack = _navigationStack.value.toMutableList()
        return if (stack.size > 1) {
            stack.removeLastOrNull()
            _navigationStack.value = stack
            
            val catalog = _currentCatalog.value ?: return false
            val parentId = if (stack.size == 1) null else stack.lastOrNull()
            loadDirectoryContents(catalog.id, parentId)
            true
        } else {
            false
        }
    }
    
    fun deleteCatalog(catalogId: String) {
        viewModelScope.launch {
            try {
                val success = repository.deleteCatalog(catalogId)
                if (success) {
                    loadCatalogs()
                    if (_currentCatalog.value?.id == catalogId) {
                        _currentCatalog.value = null
                        _currentDirectoryItems.value = emptyList()
                        _navigationStack.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
    
    fun searchInCurrentCatalog(query: String) {
        val catalog = _currentCatalog.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = repository.searchInCatalog(catalog.id, query)
                _currentDirectoryItems.value = results
            } catch (e: Exception) {
                _currentDirectoryItems.value = emptyList()
            }
            _isLoading.value = false
        }
    }
}
