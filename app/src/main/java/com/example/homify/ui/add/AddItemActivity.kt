package com.example.homify.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.homify.data.model.GroceryItem
import com.example.homify.databinding.ActivityAddItemBinding
import com.example.homify.viewmodel.GroceryViewModel
import java.util.*

class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemBinding
    private val viewModel: GroceryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatePicker()
        checkIfEditing()   // ✅ must call before save setup
        setupSaveButton()
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        binding.etExpiryDate.setOnClickListener {
            val dpd = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val formattedDate = "%02d/%02d/%04d".format(dayOfMonth, month + 1, year)
                    binding.etExpiryDate.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.etItemName.text.toString().trim()
            val quantity = binding.etQuantity.text.toString().trim()
            val expiry = binding.etExpiryDate.text.toString().trim()
            val reminder = binding.switchReminder.isChecked
            val expectedDaysText = binding.etExpectedDays.text.toString().trim()
            val expectedDays = if (expectedDaysText.isNotEmpty()) expectedDaysText.toInt() else null

            if (name.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = GroceryItem(
                name = name,
                quantity = quantity,
                expiryDate = if (expiry.isEmpty()) null else expiry,
                expectedDays = expectedDays,
                reminderEnabled = reminder
            )

            viewModel.insertItem(item)
            Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkIfEditing() {
        val itemId = intent.getIntExtra("edit_item_id", -1)
        if (itemId != -1) {
            val name = intent.getStringExtra("edit_item_name") ?: ""
            val quantity = intent.getStringExtra("edit_item_quantity") ?: ""
            val expiry = intent.getStringExtra("edit_item_expiry") ?: ""
            val reminder = intent.getBooleanExtra("edit_item_reminder", true)

            // 🟢 Set existing data to fields
            binding.etItemName.setText(name)
            binding.etQuantity.setText(quantity)
            binding.etExpiryDate.setText(expiry)
            binding.switchReminder.isChecked = reminder

            binding.btnSave.text = "Update Item"

            binding.btnSave.setOnClickListener {
                val updatedName = binding.etItemName.text.toString().trim()
                val updatedQuantity = binding.etQuantity.text.toString().trim()
                val updatedExpiry = binding.etExpiryDate.text.toString().trim()
                val updatedReminder = binding.switchReminder.isChecked
                val expectedDaysText = binding.etExpectedDays.text.toString().trim()
                val expectedDays = if (expectedDaysText.isNotEmpty()) expectedDaysText.toInt() else null

                if (updatedName.isEmpty() || updatedQuantity.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedItem = GroceryItem(
                    id = itemId,
                    name = updatedName,
                    quantity = updatedQuantity,
                    expiryDate = if (updatedExpiry.isEmpty()) null else updatedExpiry,
                    expectedDays = expectedDays,
                    reminderEnabled = updatedReminder
                )

                viewModel.updateItem(updatedItem)
                Toast.makeText(this, "Item updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
