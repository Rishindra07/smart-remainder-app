package com.example.smartremainder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var medicationsRecyclerView: RecyclerView
    private lateinit var tvMonthYear: TextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var medicationAdapter: MedicationAdapter

    private val calendar = Calendar.getInstance()
    private var selectedDate = Calendar.getInstance()

    private lateinit var dbHelper: DatabaseHelper
    private var allMedications: List<Medication> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        dbHelper = DatabaseHelper(this)

        initViews()
        loadAllMedications() // Load all medications from DB
        setupCalendar()
        setupMedicationList()
        updateMonthYear()
        updateSelectedDate() // Set initial selected date text
        updateMedicationList() // Show medications for the current date initially
    }

    private fun initViews() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        medicationsRecyclerView = findViewById(R.id.medicationsRecyclerView)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)

        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthYear()
            updateCalendar()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthYear()
            updateCalendar()
        }
    }

    private fun loadAllMedications() {
        allMedications = dbHelper.getAllMedications()
    }

    private fun setupCalendar() {
        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)

        calendarAdapter = CalendarAdapter { day ->
            if (day.isNotEmpty()) {
                val dayNum = day.toIntOrNull()
                if (dayNum != null) {
                    selectedDate.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                    selectedDate.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayNum)
                    updateSelectedDate()
                    updateMedicationList()
                }
            }
        }

        calendarRecyclerView.adapter = calendarAdapter
        updateCalendar()
    }

    private fun setupMedicationList() {
        medicationsRecyclerView.layoutManager = LinearLayoutManager(this)
        medicationAdapter = MedicationAdapter()
        medicationsRecyclerView.adapter = medicationAdapter
    }

    private fun updateCalendar() {
        val days = mutableListOf<String>()
        val tempCalendar = calendar.clone() as Calendar

        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1 // Sunday is 1, etc.

        // Add empty slots for days before the month starts
        repeat(firstDayOfMonth) {
            days.add("")
        }

        // Add days of the month
        val maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..maxDay) {
            days.add(day.toString())
        }

        calendarAdapter.updateDays(days, selectedDate.get(Calendar.DAY_OF_MONTH))
    }

    private fun updateMonthYear() {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = format.format(calendar.time)
    }

    private fun updateSelectedDate() {
        val format = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
        tvSelectedDate.text = format.format(selectedDate.time)
    }

    private fun updateMedicationList() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateString = dateFormat.format(selectedDate.time)

        val medicationsForDate = allMedications.filter {
            // This logic assumes `startDate` is in "yyyy-MM-dd" format.
            it.startDate == selectedDateString
        }

        medicationAdapter.updateMedications(medicationsForDate)
    }
}