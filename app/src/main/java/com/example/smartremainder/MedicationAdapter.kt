package com.example.smartremainder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicationAdapter(
    private var medications: List<Medication> = emptyList(),
    private val onDeleteClick: ((Medication) -> Unit)? = null
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medications[position]

        holder.medicationName.text = medication.name
        holder.medicationDosage.text = medication.dosage
        holder.medicationTime.text = medication.time
        holder.medicationStatus.text = medication.status ?: "Active"

        // ✅ Default color if medication.color is null or invalid
        try {
            holder.colorBar.setBackgroundColor(Color.parseColor(medication.color ?: "#4CAF50"))
        } catch (e: IllegalArgumentException) {
            holder.colorBar.setBackgroundColor(Color.GRAY)
        }

        holder.itemView.setOnLongClickListener {
            onDeleteClick?.invoke(medication)
            true
        }
    }

    override fun getItemCount() = medications.size

    fun updateData(newMedications: List<Medication>) {
        medications = newMedications
        notifyDataSetChanged()
    }

    fun updateMedications(newMedications: List<Medication>) {
        updateData(newMedications)
    }

    class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicationName: TextView = itemView.findViewById(R.id.medicationName)
        val medicationDosage: TextView = itemView.findViewById(R.id.medicationDosage)
        val medicationTime: TextView = itemView.findViewById(R.id.medicationTime)
        val medicationStatus: TextView = itemView.findViewById(R.id.medicationStatus)
        val colorBar: View = itemView.findViewById(R.id.colorBar)
    }
}
