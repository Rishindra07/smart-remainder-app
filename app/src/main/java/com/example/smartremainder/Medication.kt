package com.example.smartremainder



data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val startDate: String,
    val time: String,
    val reminderEnabled: Boolean,
    val refillEnabled: Boolean,
    val notes: String,
    val currentSupply: Int,
    val maxSupply: Int,
    // ✅ Optional UI extras
    val status: String? = null,
    val color: String? = null
)
{
    fun getSupplyStatus(): String {
        val percentage = (currentSupply.toFloat() / maxSupply * 100).toInt()
        return when {
            percentage >= 50 -> "Good"
            percentage >= 20 -> "Low"
            else -> "Critical"
        }
    }

    fun getSupplyPercentage(): Int {
        return ((currentSupply.toFloat() / maxSupply) * 100).toInt()
    }
}