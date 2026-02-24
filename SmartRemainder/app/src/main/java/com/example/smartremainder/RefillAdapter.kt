package com.example.smartremainder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartremainder.databinding.ItemRefillMedicineBinding

class RefillAdapter(
    private var medicines: List<Medication>,
    private val onRefillClick: (Medication) -> Unit
) : RecyclerView.Adapter<RefillAdapter.RefillViewHolder>() {

    inner class RefillViewHolder(val binding: ItemRefillMedicineBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefillViewHolder {
        val binding = ItemRefillMedicineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RefillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RefillViewHolder, position: Int) {
        val medicine = medicines[position]
        with(holder.binding) {
            tvMedicineName.text = medicine.name
            tvDosage.text = medicine.dosage
            tvCurrentSupply.text = "${medicine.currentSupply} units"
            tvSupplyStatus.text = medicine.getSupplyStatus()

            // Set color indicator
            val colorInt = Color.parseColor(medicine.color)
            colorIndicator.setBackgroundColor(colorInt)

            // Set progress bar
            progressBar.progress = medicine.getSupplyPercentage()
            progressBar.max = 100

            // Set status color
            val statusColor = when (medicine.getSupplyStatus()) {
                "Good" -> Color.parseColor("#4CAF50")
                "Low" -> Color.parseColor("#FF9800")
                else -> Color.parseColor("#F44336")
            }
            tvSupplyStatus.setTextColor(statusColor)

            btnRecordRefill.setOnClickListener {
                onRefillClick(medicine)
            }
        }
    }

    override fun getItemCount() = medicines.size

    fun updateData(newMedicines: List<Medication>) {
        medicines = newMedicines
        notifyDataSetChanged()
    }
}