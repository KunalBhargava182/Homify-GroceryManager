package com.example.homify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItem(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 🔹 Basic Info
    val name: String,
    val quantity: String,

    // 🔹 Expiry tracking
    val expiryDate: String? = null, // format: dd/MM/yyyy

    // 🔹 Consumption tracking
    val expectedDays: Int? = null, // expected duration in days
    val addedDate: Long = System.currentTimeMillis(), // auto set when item is created

    // 🔹 Reminder settings
    val reminderEnabled: Boolean = true
)
