package com.example.homify.data.repository

import androidx.lifecycle.LiveData
import com.example.homify.data.dao.GroceryDao
import com.example.homify.data.model.GroceryItem

class GroceryRepository(private val groceryDao: GroceryDao) {

    val allItems: LiveData<List<GroceryItem>> = groceryDao.getAllItems()

    suspend fun insertItem(item: GroceryItem) {
        groceryDao.insertItem(item)
    }

    suspend fun updateItem(item: GroceryItem) {
        groceryDao.updateItem(item)
    }

    suspend fun deleteItem(item: GroceryItem) {
        groceryDao.deleteItem(item)
    }
}
