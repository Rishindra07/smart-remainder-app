package com.example.smartremainder

data class MedicationHistory(
    val id: Long,
    val name: String,
    val dose: String,
    val time: String,
    val date: String,
    val status: String,
    val color: String
)
