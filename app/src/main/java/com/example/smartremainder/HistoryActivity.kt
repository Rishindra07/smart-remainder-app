package com.example.smartremainder

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var btnFilterAll: TextView
    private lateinit var btnFilterTaken: TextView
    private lateinit var btnFilterSkipped: TextView
    private lateinit var btnClearAll: TextView

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var dbHelper: DatabaseHelper

    private var userEmail: String? = null // ✅ current logged-in user email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        dbHelper = DatabaseHelper(this)

        // ✅ fetch the logged-in user email from SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userEmail = prefs.getString("email", null)

        initViews()
        setupRecyclerView()
        setupClickListeners()

        loadHistory("All")
    }

    private fun initViews() {
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        btnFilterAll = findViewById(R.id.btnFilterAll)
        btnFilterTaken = findViewById(R.id.btnFilterTaken)
        btnFilterSkipped = findViewById(R.id.btnFilterSkipped)
        btnClearAll = findViewById(R.id.btnClearAll)
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList())
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupClickListeners() {
        btnFilterAll.setOnClickListener {
            loadHistory("All")
            updateFilterButtons("All")
        }
        btnFilterTaken.setOnClickListener {
            loadHistory("Taken")
            updateFilterButtons("Taken")
        }
        btnFilterSkipped.setOnClickListener {
            loadHistory("Skipped")
            updateFilterButtons("Skipped")
        }
        btnClearAll.setOnClickListener {
            dbHelper.clearAllHistory()
            loadHistory("All") // Refresh the list
        }
    }

    private fun loadHistory(filter: String) {
        if (userEmail == null) {
            // fallback if email missing
            val allHistory = dbHelper.getAllHistory()
            historyAdapter.updateData(allHistory)
            return
        }

        val history = when (filter) {
            "Taken" -> dbHelper.getFilteredHistory("Taken", userEmail!!)
            "Skipped" -> dbHelper.getFilteredHistory("Skipped", userEmail!!)
            else -> dbHelper.getAllHistory(userEmail!!)
        }
        historyAdapter.updateData(history)
    }

    private fun updateFilterButtons(selectedFilter: String) {
        btnFilterAll.isSelected = selectedFilter == "All"
        btnFilterTaken.isSelected = selectedFilter == "Taken"
        btnFilterSkipped.isSelected = selectedFilter == "Skipped"

        btnFilterAll.setBackgroundResource(
            if (selectedFilter == "All")
                R.drawable.filter_button_selected else R.drawable.filter_button_unselected
        )
        btnFilterTaken.setBackgroundResource(
            if (selectedFilter == "Taken")
                R.drawable.filter_button_selected else R.drawable.filter_button_unselected
        )
        btnFilterSkipped.setBackgroundResource(
            if (selectedFilter == "Skipped")
                R.drawable.filter_button_selected else R.drawable.filter_button_unselected
        )

        val whiteColor = ContextCompat.getColor(this, R.color.white)
        val darkGrayColor = ContextCompat.getColor(this, R.color.dark_gray)

        btnFilterAll.setTextColor(if (selectedFilter == "All") whiteColor else darkGrayColor)
        btnFilterTaken.setTextColor(if (selectedFilter == "Taken") whiteColor else darkGrayColor)
        btnFilterSkipped.setTextColor(if (selectedFilter == "Skipped") whiteColor else darkGrayColor)
    }
}
