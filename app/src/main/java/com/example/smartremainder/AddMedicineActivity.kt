package com.example.smartremainder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.smartremainder.databinding.ActivityAddMedicineBinding
import java.util.*

class AddMedicineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicineBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var alarmScheduler: AlarmScheduler

    private var selectedFrequency: String? = null
    private var selectedDuration: String? = null

    private lateinit var frequencyButtons: List<Button>
    private lateinit var durationButtons: List<Button>

    private var userEmail: String? = null // Logged-in user email

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        alarmScheduler = AlarmScheduler(this)

        // ✅ Get logged-in user email
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userEmail = prefs.getString("email", null)

        initializeButtonGroups()
        setupClickListeners()

        // ✅ Request permissions for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkExactAlarmPermission()
        }
    }

    private fun initializeButtonGroups() {
        frequencyButtons = listOf(
            binding.btnOnceDaily, binding.btnTwiceDaily, binding.btnThriceDaily,
            binding.btnEveryHour, binding.btnEvery6Hours, binding.btnEvery12Hours
        )
        durationButtons = listOf(
            binding.btn7days, binding.btn14days, binding.btn30days,
            binding.btn90days, binding.btnOngoing
        )
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }

        binding.tvDate.setOnClickListener { showDatePickerDialog() }
        binding.tvTime.setOnClickListener { showTimePickerDialog() }

        frequencyButtons.forEach { button ->
            button.setOnClickListener {
                handleSelection(button, frequencyButtons)
                selectedFrequency = button.text.toString()
            }
        }

        durationButtons.forEach { button ->
            button.setOnClickListener {
                handleSelection(button, durationButtons)
                selectedDuration = button.text.toString()
            }
        }

        binding.btnAddMedication.setOnClickListener {
            saveMedication()
        }
    }

    private fun handleSelection(selectedButton: Button, group: List<Button>) {
        group.forEach { button ->
            button.isSelected = (button.id == selectedButton.id)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val date = "$year-${month + 1}-$dayOfMonth"
                binding.tvDate.text = date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                binding.tvTime.text = time
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun checkExactAlarmPermission() {
        val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Exact alarm permission needed for reminders.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
    }

    private fun saveMedication() {
        val medicineName = binding.etMedicineName.text.toString().trim()
        val dosage = binding.etDosage.text.toString().trim()
        val startDate = binding.tvDate.text.toString()
        val time = binding.tvTime.text.toString()
        val notes = binding.etNotes.text.toString().trim()
        val reminderEnabled = binding.switchReminder.isChecked
        val refillEnabled = binding.switchRefill.isChecked
        val currentSupply = binding.etCurrentSupply.text.toString().toIntOrNull() ?: 0
        val maxSupply = binding.etMaxSupply.text.toString().toIntOrNull() ?: 0

        if (medicineName.isBlank() || selectedFrequency == null || selectedDuration == null ||
            startDate == "Select Start Date" || time == "Select Medication Time"
        ) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (userEmail == null) {
            Toast.makeText(this, "User not logged in. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        val newMedication = Medication(
            id = 0,
            name = medicineName,
            dosage = dosage,
            frequency = selectedFrequency!!,
            duration = selectedDuration!!,
            startDate = startDate,
            time = time,
            reminderEnabled = reminderEnabled,
            refillEnabled = refillEnabled,
            notes = notes,
            currentSupply = currentSupply,
            maxSupply = maxSupply
        )

        val id = dbHelper.addMedication(newMedication, userEmail!!)

        if (id != -1L) {
            if (reminderEnabled) {
                alarmScheduler.schedule(newMedication.copy(id = id))
            }
            Toast.makeText(this, "Medication added successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to add medication.", Toast.LENGTH_SHORT).show()
        }
    }
}
