package com.example.homify.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.homify.R
import com.example.homify.data.model.GroceryItem
import com.example.homify.databinding.ItemGroceryBinding
import java.text.SimpleDateFormat
import java.util.*

class GroceryAdapter(
    private val onEditClicked: (GroceryItem) -> Unit,
    private val onDeleteClicked: (GroceryItem) -> Unit,
    private val onItemClicked: (GroceryItem) -> Unit = {}
) : ListAdapter<GroceryItem, GroceryAdapter.GroceryViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GroceryItem>() {
            override fun areItemsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean =
                oldItem == newItem
        }

        private const val INPUT_DATE_FORMAT = "dd/MM/yyyy"
        private val inputDateFormatter = SimpleDateFormat(INPUT_DATE_FORMAT, Locale.getDefault())
        private val outputDateFormatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    }

    inner class GroceryViewHolder(private val binding: ItemGroceryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GroceryItem) {
            binding.apply {
                // Basic fields
                tvItemName.text = item.name
                tvQuantity.text = item.quantity

                // Reminder icon
                ivReminderStatus.setImageResource(
                    if (item.reminderEnabled) R.drawable.ic_notification_on
                    else R.drawable.ic_notification_off
                )
                ivReminderStatus.contentDescription =
                    if (item.reminderEnabled) "Reminder enabled" else "Reminder disabled"

                // Options popup
                ivOptions.setOnClickListener {
                    val popup = PopupMenu(root.context, ivOptions)
                    popup.menuInflater.inflate(R.menu.menu_item_options, popup.menu)
                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.menu_edit -> onEditClicked(item)
                            R.id.menu_delete -> onDeleteClicked(item)
                        }
                        true
                    }
                    popup.show()
                }

                // Whole item click (optional)
                root.setOnClickListener { onItemClicked(item) }

                // ------------------------------
                // Expiry chip handling
                // ------------------------------
                var expiryShown = false
                if (!item.expiryDate.isNullOrEmpty()) {
                    try {
                        val parsed = inputDateFormatter.parse(item.expiryDate)
                        parsed?.let {
                            val formatted = outputDateFormatter.format(it)
                            chipExpiry.text = "Expires: $formatted"
                            chipExpiry.visibility = View.VISIBLE
                            chipExpiry.contentDescription = "Expiry date $formatted"
                            expiryShown = true
                        }
                    } catch (e: Exception) {
                        // parsing failed -> show raw string as fallback
                        chipExpiry.text = "Expires: ${item.expiryDate}"
                        chipExpiry.visibility = View.VISIBLE
                        chipExpiry.contentDescription = "Expiry date ${item.expiryDate}"
                        expiryShown = true
                    }
                }
                if (!expiryShown) {
                    chipExpiry.visibility = View.GONE
                }

                // ------------------------------
                // Expected consumption handling
                // ------------------------------
                if (item.expectedDays != null) {
                    // compute usage end = addedDate + expectedDays (days)
                    val today = Calendar.getInstance()
                    // normalize both to midnight for day-diff
                    val usageEnd = Calendar.getInstance().apply {
                        timeInMillis = item.addedDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        add(Calendar.DAY_OF_YEAR, item.expectedDays)
                    }
                    val todayMid = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val diffMillis = usageEnd.timeInMillis - todayMid.timeInMillis
                    val daysLeft = (diffMillis / (1000L * 60 * 60 * 24)).toInt()

                    when {
                        daysLeft < 0 -> {
                            chipExpectedFinish.text = "Likely finished"
                            chipExpectedFinish.visibility = View.VISIBLE
                            chipExpectedFinish.contentDescription = "Item likely finished"
                        }
                        daysLeft == 0 -> {
                            chipExpectedFinish.text = "Finishes today"
                            chipExpectedFinish.visibility = View.VISIBLE
                            chipExpectedFinish.contentDescription = "Finishes today"
                        }
                        daysLeft == 1 -> {
                            chipExpectedFinish.text = "Finishes tomorrow"
                            chipExpectedFinish.visibility = View.VISIBLE
                            chipExpectedFinish.contentDescription = "Finishes tomorrow"
                        }
                        else -> {
                            chipExpectedFinish.text = "Finishes in: ${daysLeft}d"
                            chipExpectedFinish.visibility = View.VISIBLE
                            chipExpectedFinish.contentDescription = "Finishes in $daysLeft days"
                        }
                    }
                } else {
                    chipExpectedFinish.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryViewHolder {
        val binding = ItemGroceryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroceryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroceryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** Helper to update list from outside (keeps compatibility) */
    fun updateList(newItems: List<GroceryItem>) {
        submitList(newItems.toList()) // pass a copy to avoid mutation issues
    }
}
