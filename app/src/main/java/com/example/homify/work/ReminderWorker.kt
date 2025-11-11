package com.example.homify.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.homify.data.database.AppDatabase
import com.example.homify.utils.NotificationUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "ReminderWorker"
        private const val DATE_FORMAT = "dd/MM/yyyy"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getDatabase(appContext).groceryDao()
            val items = dao.getAllItemsList() // suspend function that returns List<GroceryItem>
            val today = Calendar.getInstance()
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

            Log.d(TAG, "Running reminder check for ${items.size} items")

            items.forEach { item ->
                try {
                    if (!item.reminderEnabled) {
                        Log.d(TAG, "Skipping ${item.name} - reminders disabled")
                        return@forEach
                    }

                    // -----------------------------
                    // 1) Expiry date reminder
                    // -----------------------------
                    item.expiryDate?.let { expiryStr ->
                        try {
                            val expiryCal = Calendar.getInstance().apply {
                                time = dateFormat.parse(expiryStr) ?: throw IllegalArgumentException("Invalid date")
                                // normalize time-of-day to midnight for consistent day diff calculation
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val diffMillis = expiryCal.timeInMillis - today.timeInMillis
                            val daysLeft = (diffMillis / (1000L * 60 * 60 * 24)).toInt()

                            Log.d(TAG, "${item.name} expiry in $daysLeft day(s) (expiry=$expiryStr)")

                            // Notify if expiry is today or tomorrow
                            if (daysLeft in 0..1) {
                                val title = "⚠️ ${item.name} expiring ${if (daysLeft == 0) "today" else "tomorrow"}"
                                val message = if (daysLeft == 0) {
                                    "${item.name} expires today. Use or replace it soon!"
                                } else {
                                    "${item.name} will expire tomorrow. Consider restocking."
                                }
                                NotificationUtils.showNotification(appContext, title, message)
                                Log.d(TAG, "Sent expiry notification for ${item.name}")
                            }
                        } catch (pex: Exception) {
                            Log.w(TAG, "Failed to parse expiry for ${item.name}: $expiryStr — ${pex.message}")
                        }
                    }

                    // -----------------------------
                    // 2) Expected consumption reminder
                    // -----------------------------
                    // expectedDays is the number of days from addedDate after which item is expected to be finished
                    item.expectedDays?.let { expectedDays ->
                        try {
                            // usageEnd = addedDate + expectedDays days
                            val usageEnd = Calendar.getInstance().apply {
                                timeInMillis = item.addedDate
                                // normalize time
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                                add(Calendar.DAY_OF_YEAR, expectedDays)
                            }

                            val diffUsageMillis = usageEnd.timeInMillis - today.timeInMillis
                            val daysLeftUsage = (diffUsageMillis / (1000L * 60 * 60 * 24)).toInt()

                            Log.d(TAG, "${item.name} expected to finish in $daysLeftUsage day(s) (expectedDays=$expectedDays)")

                            // Notify when expected finish is today or tomorrow
                            if (daysLeftUsage in 0..1) {
                                val title = "🍶 ${item.name} may finish ${if (daysLeftUsage == 0) "today" else "tomorrow"}"
                                val message = if (daysLeftUsage == 0) {
                                    "You likely finish ${item.name} today. Consider restocking."
                                } else {
                                    "You likely finish ${item.name} tomorrow. Consider buying another."
                                }
                                NotificationUtils.showNotification(appContext, title, message)
                                Log.d(TAG, "Sent consumption notification for ${item.name}")
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to compute expected usage for ${item.name}: ${e.message}")
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error while processing item ${item.name}: ${e.message}", e)
                }
            }

            Log.d(TAG, "ReminderWorker finished successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "ReminderWorker failed: ${e.message}", e)
            Result.failure()
        }
    }
}
