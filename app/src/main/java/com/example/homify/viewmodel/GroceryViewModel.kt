package com.example.homify.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.homify.data.database.AppDatabase
import com.example.homify.data.model.GroceryItem
import com.example.homify.data.repository.GroceryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GroceryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GroceryRepository
    val allItems: LiveData<List<GroceryItem>>

    init {
        val groceryDao = AppDatabase.getDatabase(application).groceryDao()
        repository = GroceryRepository(groceryDao)
        allItems = repository.allItems
    }

    fun insertItem(item: GroceryItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertItem(item)
    }

    fun updateItem(item: GroceryItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateItem(item)
    }

    fun deleteItem(item: GroceryItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteItem(item)
    }
}
