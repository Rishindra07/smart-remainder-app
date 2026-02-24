package com.example.smartremainder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(private val onDayClick: (String) -> Unit) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val days = mutableListOf<String>()
    private var selectedDay = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.dayText.text = day

        if (day.isNotEmpty() && day.toInt() == selectedDay) {
            holder.dayText.setBackgroundColor(Color.CYAN)
        } else {
            holder.dayText.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            onDayClick(day)
        }
    }

    override fun getItemCount() = days.size

    fun updateDays(newDays: List<String>, selected: Int) {
        days.clear()
        days.addAll(newDays)
        selectedDay = selected
        notifyDataSetChanged()
    }

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
    }
}