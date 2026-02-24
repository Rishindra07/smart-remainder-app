package com.example.smartremainder

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartremainder.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var medicationAdapter: MedicationAdapter
    private lateinit var alarmScheduler: AlarmScheduler
    private var permissionsRequested = false
    private lateinit var firestore: FirebaseFirestore
    private var userEmail: String? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionsRequested = true
        }

    private val medicationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.smartremainder.MEDICATION_UPDATE") {
                Log.d("MainActivity", "Received medication update broadcast. Refreshing progress.")
                updateDailyProgress()
                loadMedications()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        alarmScheduler = AlarmScheduler(this)
        firestore = FirebaseFirestore.getInstance()

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userEmail = prefs.getString("email", null)

        // Quick action buttons
        binding.llEmergencyCall.setOnClickListener { showEmergencyCallDialog() }

        binding.quickActions.llAddMedication.setOnClickListener {
            startActivity(Intent(this, AddMedicineActivity::class.java))
        }

        binding.quickActions.llCalendarView.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        binding.quickActions.llHistoryLog.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }


        binding.quickActions.llUserProfile.setOnClickListener {
            startActivity(Intent(this, data_anaysis::class.java))
        }

        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<RelativeLayout>(R.id.notificationLayout).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Floating Chat Button
        val fabChat = findViewById<FloatingActionButton>(R.id.fabChat)
        fabChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        setupRecyclerView()
        loadUserProfile()

        // Register broadcast receiver for medication updates
        val filter = IntentFilter("com.example.smartremainder.MEDICATION_UPDATE")
        ContextCompat.registerReceiver(this, medicationUpdateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun showEmergencyCallDialog() {
        val numbers = arrayOf("100 (Police)", "108 (Ambulance)")
        AlertDialog.Builder(this)
            .setTitle("Emergency Call")
            .setItems(numbers) { _, which ->
                val number = when (which) {
                    0 -> "100"
                    1 -> "108"
                    else -> ""
                }
                if (number.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume: Refreshing UI and checking permissions.")
        if (!permissionsRequested) checkAndRequestPermissions()
        loadMedications()
        loadUserProfile()
        updateDailyProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(medicationUpdateReceiver)
    }

    private fun updateDailyProgress() {
        Log.d("MainActivity", "--- Starting Daily Progress Update ---")
        try {
            if (userEmail == null) {
                Log.w("MainActivity", "User not logged in — skipping progress update.")
                binding.circularProgressBar.progress = 0
                binding.tvPercentage.text = getString(R.string.percentage, 0)
                binding.tvDoses.text = "Login required"
                return
            }

            // ✅ Fetch per-user stats
            val totalDoses = dbHelper.getTodayTotalDoses(userEmail!!)
            val takenDoses = dbHelper.getTodayTakenDoses(userEmail!!)

            Log.i("MainActivity", "User: $userEmail → Total Doses: $totalDoses, Taken: $takenDoses")

            val percentage = if (totalDoses > 0) (takenDoses * 100) / totalDoses else 0
            val finalPercentage = percentage.coerceIn(0, 100)

            binding.circularProgressBar.progress = finalPercentage
            binding.tvPercentage.text = getString(R.string.percentage, finalPercentage)
            binding.tvDoses.text = getString(R.string.doses_taken, takenDoses, totalDoses)

            Log.i("MainActivity", "UI UPDATED for $userEmail → $finalPercentage% complete")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error updating daily progress UI: ${e.message}", e)
            binding.circularProgressBar.progress = 0
            binding.tvPercentage.text = getString(R.string.percentage, 0)
            binding.tvDoses.text = getString(R.string.error)
        }
        Log.d("MainActivity", "--- Finished Daily Progress Update ---")
    }


    // ✅ Fetch profile info from Firestore
    private fun loadUserProfile() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)

        if (email != null) {
            firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val name = documents.documents[0].getString("name") ?: "User"
                        binding.tvUserName.text = getString(R.string.welcome_user_name, name)
                    } else {
                        binding.tvUserName.text = getString(R.string.welcome_user)
                    }
                }
                .addOnFailureListener {
                    binding.tvUserName.text = getString(R.string.welcome_user)
                    Log.e("MainActivity", "Error fetching user profile: ${it.message}")
                }
        } else {
            binding.tvUserName.text = getString(R.string.welcome_user)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun setupRecyclerView() {
        medicationAdapter = MedicationAdapter(emptyList()) { medication ->
            deleteMedication(medication)
        }
        binding.rvMedications.layoutManager = LinearLayoutManager(this)
        binding.rvMedications.adapter = medicationAdapter
    }

    // ✅ Fetch only logged-in user’s medications
    private fun loadMedications() {
        if (userEmail == null) {
            Log.w("MainActivity", "User not logged in; showing no medications.")
            medicationAdapter.updateData(emptyList())
            return
        }

        val medications = dbHelper.getAllMedications(userEmail!!)
        medicationAdapter.updateData(medications)
        Log.d("MainActivity", "Loaded and displayed ${medications.size} medications for $userEmail")
    }

    private fun deleteMedication(medication: Medication) {
        dbHelper.deleteMedication(medication.id)
        alarmScheduler.cancelAlarm(medication.id)
        loadMedications()
        updateDailyProgress()
    }
}
