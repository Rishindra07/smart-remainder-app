package com.example.smartremainder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartremainder.databinding.ActivityRefillBinding

class RefillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRefillBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: RefillAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRefillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupRecyclerView()
        loadMedicines()
    }

    private fun setupRecyclerView() {
        adapter = RefillAdapter(emptyList()) { medicine ->
            showRefillDialog(medicine)
        }

        binding.refillRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RefillActivity)
            adapter = this@RefillActivity.adapter
        }
    }

    private fun loadMedicines() {
        val medicines = dbHelper.getAllMedications()
        adapter.updateData(medicines)
    }

    private fun showRefillDialog(medicine: Medication) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Record Refill")
            .setMessage("Refill ${medicine.name} to maximum supply (${medicine.maxSupply} units)?")
            .setPositiveButton("Refill") { _, _ ->
                val refillAmount = medicine.maxSupply - medicine.currentSupply
                dbHelper.recordRefill(medicine.id, refillAmount)
                loadMedicines()
                Toast.makeText(this, "Refill recorded successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}