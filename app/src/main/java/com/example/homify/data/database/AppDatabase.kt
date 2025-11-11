package com.example.homify.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.homify.data.dao.GroceryDao
import com.example.homify.data.model.GroceryItem

/**
 * AppDatabase — central Room database for the Homify app.
 *
 * Stores all grocery-related data, including item details, expiry dates,
 * and expected consumption durations.
 */
@Database(
    entities = [GroceryItem::class],
    version = 2, // ✅ Incremented after schema change (added expectedDays column)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns a singleton instance of the Room database.
         * Uses fallbackToDestructiveMigration() for easy dev testing
         * — rebuilds DB automatically on schema change.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "homify_database"
                )
                    // ⚙️ Automatically rebuilds DB if version mismatch occurs
                    .fallbackToDestructiveMigration()

                    // (Optional) — uncomment to log migrations
                    // .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
