package com.example.smartremainder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private var historyItems: List<HistoryItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE = 0
        private const val TYPE_EVENT = 1
    }

    // Sealed class to represent either a date or a history event
    sealed class HistoryItem {
        data class DateItem(val date: String) : HistoryItem()
        data class EventItem(val event: MedicationHistory) : HistoryItem()
    }

    override fun getItemViewType(position: Int): Int {
        return when (historyItems[position]) {
            is HistoryItem.DateItem -> TYPE_DATE
            is HistoryItem.EventItem -> TYPE_EVENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_DATE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_date, parent, false)
            DateViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_event, parent, false)
            EventViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = historyItems[position]) {
            is HistoryItem.DateItem -> (holder as DateViewHolder).bind(item.date)
            is HistoryItem.EventItem -> (holder as EventViewHolder).bind(item.event)
        }
    }

    override fun getItemCount(): Int = historyItems.size

    fun updateData(newHistory: List<MedicationHistory>) {
        val groupedData = newHistory.groupBy { it.date } // Assuming 'date' is a property in MedicationHistory
        val items = mutableListOf<HistoryItem>()
        for ((date, events) in groupedData) {
            items.add(HistoryItem.DateItem(date))
            events.forEach { event -> items.add(HistoryItem.EventItem(event)) }
        }
        this.historyItems = items
        notifyDataSetChanged() // Consider using DiffUtil for better performance
    }

    // ViewHolder for Date Headers
    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        fun bind(date: String) {
            tvDate.text = date
        }
    }

    // ViewHolder for History Events
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvMedicationName)
        private val tvDose: TextView = itemView.findViewById(R.id.tvMedicationDose)
        private val tvTime: TextView = itemView.findViewById(R.id.tvMedicationTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvMedicationStatus)

        fun bind(history: MedicationHistory) {
            tvName.text = history.name
            tvDose.text = history.dose
            tvTime.text = history.time
            tvStatus.text = history.status
            // You might want to set a color based on the status as well
        }
    }
}
