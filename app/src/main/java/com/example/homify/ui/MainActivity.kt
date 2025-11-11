package com.example.homify.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homify.databinding.ActivityMainBinding
import com.example.homify.ui.adapter.GroceryAdapter
import com.example.homify.viewmodel.GroceryViewModel
import com.example.homify.work.ReminderWorker
import com.example.homify.utils.NotificationUtils
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.core.content.edit
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val groceryViewModel: GroceryViewModel by viewModels()
    private lateinit var groceryAdapter: GroceryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ask for notification permission first (only once)
        askNotificationPermission()

        setupRecyclerView()
        observeViewModel()
        setupFab()
        scheduleDailyReminders()

        // 🧪 TEMP: Force run the ReminderWorker immediately (for testing)
        val testWork = androidx.work.OneTimeWorkRequestBuilder<com.example.homify.work.ReminderWorker>()
            .build()
        androidx.work.WorkManager.getInstance(this).enqueue(testWork)

    }

    private fun setupRecyclerView() {
        groceryAdapter = GroceryAdapter(
            onEditClicked = { item -> openEditScreen(item) },
            onDeleteClicked = { item -> confirmDelete(item) }
        )

        binding.recyclerViewItems.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = groceryAdapter
        }
    }

    private fun observeViewModel() {
        groceryViewModel.allItems.observe(this) { items ->
            if (items.isNullOrEmpty()) {
                binding.recyclerViewItems.visibility = android.view.View.GONE
                binding.emptyStateLayout.visibility = android.view.View.VISIBLE
            } else {
                binding.recyclerViewItems.visibility = android.view.View.VISIBLE
                binding.emptyStateLayout.visibility = android.view.View.GONE
                groceryAdapter.updateList(items)
            }
        }
    }

    private fun setupFab() {
        binding.fabAddItem.setOnClickListener {
            val intent = Intent(this, com.example.homify.ui.add.AddItemActivity::class.java)
            startActivity(intent)
        }
    }

    private fun scheduleDailyReminders() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "daily_reminder_work",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

        // Create notification channel (safe to call even if permission not yet granted)
        NotificationUtils.createNotificationChannel(this)
    }

    // ---------------------------------------------------------------------------------------------
    // 🔔 Notification Permission Handling
    // ---------------------------------------------------------------------------------------------

    private fun askNotificationPermission() {
        // For Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val prefs = getSharedPreferences("homify_prefs", Context.MODE_PRIVATE)
            val askedBefore = prefs.getBoolean("asked_notification_permission", false)

            val permissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            // If already granted, just create the channel and return
            if (permissionGranted) {
                NotificationUtils.createNotificationChannel(this)
                return
            }

            // If never asked before, show polite explanation first
            if (!askedBefore) {
                AlertDialog.Builder(this)
                    .setTitle("Allow Notifications?")
                    .setMessage("Homify can remind you before groceries expire. Would you like to allow notifications?")
                    .setPositiveButton("Allow") { _, _ ->
                        prefs.edit { putBoolean("asked_notification_permission", true) }
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton("No, thanks") { _, _ ->
                        prefs.edit { putBoolean("asked_notification_permission", true) }
                        Toast.makeText(this, "You can enable notifications later in settings.", Toast.LENGTH_SHORT).show()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                // Already asked before but not granted
                Toast.makeText(this, "Notifications are disabled. Enable them from system settings.", Toast.LENGTH_LONG).show()
            }
        } else {
            // For Android 12 and below — automatically allow notifications
            NotificationUtils.createNotificationChannel(this)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                NotificationUtils.createNotificationChannel(this)
                Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications are disabled.", Toast.LENGTH_SHORT).show()
            }
        }

    // ---------------------------------------------------------------------------------------------
    // 🗑️ Edit/Delete handlers
    // ---------------------------------------------------------------------------------------------

    private fun openEditScreen(item: com.example.homify.data.model.GroceryItem) {
        val intent = Intent(this, com.example.homify.ui.add.AddItemActivity::class.java).apply {
            putExtra("edit_item_id", item.id)
            putExtra("edit_item_name", item.name)
            putExtra("edit_item_quantity", item.quantity)
            putExtra("edit_item_expiry", item.expiryDate)
            putExtra("edit_item_reminder", item.reminderEnabled)
        }
        startActivity(intent)
    }

    private fun confirmDelete(item: com.example.homify.data.model.GroceryItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete '${item.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                groceryViewModel.deleteItem(item)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
