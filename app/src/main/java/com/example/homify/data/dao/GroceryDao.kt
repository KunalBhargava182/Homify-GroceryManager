package com.example.homify.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.homify.data.model.GroceryItem

@Dao
interface GroceryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GroceryItem)

    @Update
    suspend fun updateItem(item: GroceryItem)

    @Delete
    suspend fun deleteItem(item: GroceryItem)

    @Query("SELECT * FROM grocery_items ORDER BY expiryDate ASC")
    fun getAllItems(): LiveData<List<GroceryItem>>


    @Query("SELECT * FROM grocery_items ORDER BY expiryDate ASC")
    suspend fun getAllItemsList(): List<GroceryItem>


}
