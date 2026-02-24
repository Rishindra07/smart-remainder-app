package com.example.smartremainder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // keep app context for prefs lookup
    private val appContext: Context = context.applicationContext

    companion object {
        private const val DATABASE_VERSION = 6
        private const val DATABASE_NAME = "MedicationDatabase.db"
        private const val TABLE_MEDICATIONS = "medications"
        private const val TABLE_HISTORY = "medication_history"

        // Medication Table Columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_EMAIL = "user_email" // Added
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DOSAGE = "dosage"
        private const val COLUMN_FREQUENCY = "frequency"
        private const val COLUMN_DURATION = "duration"
        private const val COLUMN_START_DATE = "start_date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_REMINDER_ENABLED = "reminder_enabled"
        private const val COLUMN_REFILL_ENABLED = "refill_enabled"
        private const val COLUMN_NOTES = "notes"
        private const val COLUMN_CURRENT_SUPPLY = "current_supply"
        private const val COLUMN_MAX_SUPPLY = "max_supply"

        // History Table Columns
        private const val COLUMN_HISTORY_ID = "history_id"
        private const val COLUMN_HISTORY_MED_ID = "med_id"
        private const val COLUMN_HISTORY_STATUS = "status"
        private const val COLUMN_HISTORY_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createMedicationsTable = ("CREATE TABLE " + TABLE_MEDICATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_EMAIL + " TEXT," // Linked user column
                + COLUMN_NAME + " TEXT,"
                + COLUMN_DOSAGE + " TEXT,"
                + COLUMN_FREQUENCY + " TEXT,"
                + COLUMN_DURATION + " TEXT,"
                + COLUMN_START_DATE + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_REMINDER_ENABLED + " INTEGER,"
                + COLUMN_REFILL_ENABLED + " INTEGER,"
                + COLUMN_NOTES + " TEXT,"
                + COLUMN_CURRENT_SUPPLY + " INTEGER,"
                + COLUMN_MAX_SUPPLY + " INTEGER" + ")")

        val createHistoryTable = ("CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_HISTORY_MED_ID + " INTEGER,"
                + COLUMN_HISTORY_STATUS + " TEXT,"
                + COLUMN_HISTORY_TIMESTAMP + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_HISTORY_MED_ID + ") REFERENCES " +
                TABLE_MEDICATIONS + "(" + COLUMN_ID + ") ON DELETE CASCADE" + ")")

        db?.execSQL(createMedicationsTable)
        db?.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            try {
                db?.execSQL("ALTER TABLE $TABLE_MEDICATIONS ADD COLUMN $COLUMN_USER_EMAIL TEXT DEFAULT ''")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Add medication (user-specific)
    fun addMedication(med: Medication, userEmail: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, userEmail)
            put(COLUMN_NAME, med.name)
            put(COLUMN_DOSAGE, med.dosage)
            put(COLUMN_FREQUENCY, med.frequency)
            put(COLUMN_DURATION, med.duration)
            put(COLUMN_START_DATE, med.startDate)
            put(COLUMN_TIME, med.time)
            put(COLUMN_REMINDER_ENABLED, if (med.reminderEnabled) 1 else 0)
            put(COLUMN_REFILL_ENABLED, if (med.refillEnabled) 1 else 0)
            put(COLUMN_NOTES, med.notes)
            put(COLUMN_CURRENT_SUPPLY, med.currentSupply)
            put(COLUMN_MAX_SUPPLY, med.maxSupply)
        }
        val id = db.insert(TABLE_MEDICATIONS, null, values)
        db.close()
        return id
    }

    // Get all medications for specified user
    fun getAllMedications(userEmail: String): List<Medication> {
        val medList = mutableListOf<Medication>()
        val db = this.readableDatabase
        db.rawQuery("SELECT * FROM $TABLE_MEDICATIONS WHERE $COLUMN_USER_EMAIL = ?", arrayOf(userEmail))
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val med = Medication(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                            dosage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)),
                            frequency = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREQUENCY)),
                            duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                            startDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE)),
                            time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                            reminderEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ENABLED)) == 1,
                            refillEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REFILL_ENABLED)) == 1,
                            notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                            currentSupply = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_SUPPLY)),
                            maxSupply = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_SUPPLY))
                        )
                        medList.add(med)
                    } while (cursor.moveToNext())
                }
            }
        return medList
    }

    // Overload: getAllMedications() uses logged-in user email from SharedPreferences if not provided.
    fun getAllMedications(): List<Medication> {
        val prefs = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("email", null)
        return if (!userEmail.isNullOrEmpty()) {
            getAllMedications(userEmail)
        } else {
            // Fallback: return all meds if no userEmail found (admin/debug)
            val medList = mutableListOf<Medication>()
            val db = this.readableDatabase
            db.rawQuery("SELECT * FROM $TABLE_MEDICATIONS", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val med = Medication(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                            dosage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)),
                            frequency = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREQUENCY)),
                            duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                            startDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE)),
                            time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                            reminderEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ENABLED)) == 1,
                            refillEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REFILL_ENABLED)) == 1,
                            notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                            currentSupply = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_SUPPLY)),
                            maxSupply = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_SUPPLY))
                        )
                        medList.add(med)
                    } while (cursor.moveToNext())
                }
            }
            medList
        }
    }

    fun deleteMedication(medId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_MEDICATIONS, "$COLUMN_ID = ?", arrayOf(medId.toString()))
        db.close()
    }

    // History Tracking
    fun addHistoryEvent(medicationId: Long, status: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_HISTORY_MED_ID, medicationId)
            put(COLUMN_HISTORY_STATUS, status)
            put(COLUMN_HISTORY_TIMESTAMP, System.currentTimeMillis())
        }
        db.insert(TABLE_HISTORY, null, values)
        db.close()
    }

    fun getAllHistory(): List<MedicationHistory> {
        val historyList = mutableListOf<MedicationHistory>()
        val db = this.readableDatabase
        val query =
            "SELECT h.history_id, m.name, m.dosage, h.status, h.timestamp, m.time FROM $TABLE_HISTORY h JOIN $TABLE_MEDICATIONS m ON h.med_id = m.id ORDER BY h.timestamp DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_TIMESTAMP))
                val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
                val color =
                    if (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_STATUS)) == "Taken") "#4CAF50" else "#F44336"

                val history = MedicationHistory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("history_id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    dose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)),
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                    date = date,
                    status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_STATUS)),
                    color = color
                )
                historyList.add(history)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return historyList
    }

    fun clearAllHistory() {
        val db = this.writableDatabase
        db.delete(TABLE_HISTORY, null, null)
        db.close()
    }

    // Refill Tracking
    fun recordRefill(medicineId: Long, refillAmount: Int): Boolean {
        val db = writableDatabase
        var success = false
        db.rawQuery(
            "SELECT $COLUMN_CURRENT_SUPPLY, $COLUMN_MAX_SUPPLY FROM $TABLE_MEDICATIONS WHERE $COLUMN_ID = ?",
            arrayOf(medicineId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val currentSupply = cursor.getInt(0)
                val maxSupply = cursor.getInt(1)
                val newSupply = (currentSupply + refillAmount).coerceAtMost(maxSupply)
                val values = ContentValues().apply {
                    put(COLUMN_CURRENT_SUPPLY, newSupply)
                }
                success =
                    db.update(TABLE_MEDICATIONS, values, "$COLUMN_ID = ?", arrayOf(medicineId.toString())) > 0
            }
        }
        return success
    }

    // Analytics (Weekly + Overall)
    fun getOverallAdherenceStats(userEmail: String): Pair<Float, Float> {
        val db = readableDatabase
        var total = 0
        var taken = 0

        db.rawQuery(
            "SELECT $COLUMN_HISTORY_STATUS FROM $TABLE_HISTORY h " +
                    "JOIN $TABLE_MEDICATIONS m ON h.$COLUMN_HISTORY_MED_ID = m.$COLUMN_ID " +
                    "WHERE m.$COLUMN_USER_EMAIL = ?", arrayOf(userEmail)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    total++
                    if (cursor.getString(0) == "Taken") taken++
                } while (cursor.moveToNext())
            }
        }

        if (total == 0) return Pair(0f, 0f)
        val takenPercent = (taken.toFloat() / total.toFloat()) * 100
        return Pair(takenPercent, 100 - takenPercent)
    }
    fun getResponseTimeStats(userEmail: String): List<Entry> {
        val entries = mutableListOf<Entry>()
        readableDatabase.rawQuery(
            "SELECT $COLUMN_HISTORY_TIMESTAMP FROM $TABLE_HISTORY h " +
                    "JOIN $TABLE_MEDICATIONS m ON h.$COLUMN_HISTORY_MED_ID = m.$COLUMN_ID " +
                    "WHERE h.$COLUMN_HISTORY_STATUS = 'Taken' AND m.$COLUMN_USER_EMAIL = ?",
            arrayOf(userEmail)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val cal = Calendar.getInstance().apply { timeInMillis = cursor.getLong(0) }
                    val hour = cal.get(Calendar.HOUR_OF_DAY).toFloat()
                    val minute = cal.get(Calendar.MINUTE).toFloat()
                    entries.add(Entry(hour, minute))
                } while (cursor.moveToNext())
            }
        }
        return entries
    }


    fun getWeeklyAdherenceStats(userEmail: String): List<BarEntry> {
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dailyAdherence = days.associateWith { Pair(0, 0) }.toMutableMap()
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())

        readableDatabase.rawQuery(
            "SELECT $COLUMN_HISTORY_TIMESTAMP, $COLUMN_HISTORY_STATUS " +
                    "FROM $TABLE_HISTORY h JOIN $TABLE_MEDICATIONS m ON h.$COLUMN_HISTORY_MED_ID = m.$COLUMN_ID " +
                    "WHERE m.$COLUMN_USER_EMAIL = ?", arrayOf(userEmail)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val day = sdf.format(Date(cursor.getLong(0)))
                    if (dailyAdherence.containsKey(day)) {
                        var (taken, total) = dailyAdherence[day]!!
                        total++
                        if (cursor.getString(1) == "Taken") taken++
                        dailyAdherence[day] = Pair(taken, total)
                    }
                } while (cursor.moveToNext())
            }
        }

        return days.mapIndexed { index, day ->
            val (taken, total) = dailyAdherence[day]!!
            val percent = if (total > 0) (taken.toFloat() / total.toFloat()) * 100 else 0f
            BarEntry(index.toFloat(), percent)
        }
    }

    // ✅ Get all history for a specific user
    fun getAllHistory(userEmail: String): List<MedicationHistory> {
        val historyList = mutableListOf<MedicationHistory>()
        val db = this.readableDatabase
        val query = """
        SELECT h.history_id, m.name, m.dosage, h.status, h.timestamp, m.time
        FROM $TABLE_HISTORY h
        JOIN $TABLE_MEDICATIONS m ON h.med_id = m.id
        WHERE m.$COLUMN_USER_EMAIL = ?
        ORDER BY h.timestamp DESC
    """
        db.rawQuery(query, arrayOf(userEmail)).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_TIMESTAMP))
                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
                    val color =
                        if (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_STATUS)) == "Taken")
                            "#4CAF50" else "#F44336"

                    val history = MedicationHistory(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("history_id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        dose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)),
                        time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        date = date,
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_STATUS)),
                        color = color
                    )
                    historyList.add(history)
                } while (cursor.moveToNext())
            }
        }
        return historyList
    }

    // ✅ Get filtered history (Taken / Skipped) for a specific user
    fun getFilteredHistory(status: String, userEmail: String): List<MedicationHistory> {
        val historyList = mutableListOf<MedicationHistory>()
        val db = this.readableDatabase
        val query = """
        SELECT h.history_id, m.name, m.dosage, h.status, h.timestamp, m.time
        FROM $TABLE_HISTORY h
        JOIN $TABLE_MEDICATIONS m ON h.med_id = m.id
        WHERE h.status = ? AND m.$COLUMN_USER_EMAIL = ?
        ORDER BY h.timestamp DESC
    """
        db.rawQuery(query, arrayOf(status, userEmail)).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_TIMESTAMP))
                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
                    val color =
                        if (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_STATUS)) == "Taken")
                            "#4CAF50" else "#F44336"

                    val history = MedicationHistory(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("history_id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        dose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOSAGE)),
                        time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        date = date,
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_STATUS)),
                        color = color
                    )
                    historyList.add(history)
                } while (cursor.moveToNext())
            }
        }
        return historyList
    }
    // ✅ Get total doses for today for a specific user
    fun getTodayTotalDoses(userEmail: String): Int {
        val db = readableDatabase
        var totalDoses = 0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val query = """
        SELECT $COLUMN_FREQUENCY, $COLUMN_START_DATE, $COLUMN_DURATION 
        FROM $TABLE_MEDICATIONS 
        WHERE $COLUMN_USER_EMAIL = ?
    """.trimIndent()

        db.rawQuery(query, arrayOf(userEmail)).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val frequency = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FREQUENCY))
                    val startDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE))
                    val duration = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION))

                    val durationDays = duration.split(" ")[0].toIntOrNull() ?: 0
                    val startDateParsed = dateFormat.parse(startDate)
                    val cal = Calendar.getInstance()
                    cal.time = startDateParsed ?: continue
                    cal.add(Calendar.DAY_OF_YEAR, durationDays)
                    val endDate = cal.time

                    if (Date().before(endDate)) {
                        val dosesPerDay = when {
                            frequency.equals("Once Daily", ignoreCase = true) -> 1
                            frequency.equals("Twice Daily", ignoreCase = true) -> 2
                            frequency.equals("Thrice Daily", ignoreCase = true) -> 3
                            frequency.contains("Every 6", true) -> 4
                            frequency.contains("Every 12", true) -> 2
                            else -> 1
                        }
                        totalDoses += dosesPerDay
                    }
                } while (cursor.moveToNext())
            }
        }
        return totalDoses
    }

    // ✅ Get taken doses for today for a specific user
    fun getTodayTakenDoses(userEmail: String): Int {
        val db = readableDatabase
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val query = """
        SELECT COUNT(*) 
        FROM $TABLE_HISTORY h
        JOIN $TABLE_MEDICATIONS m ON h.$COLUMN_HISTORY_MED_ID = m.$COLUMN_ID
        WHERE h.$COLUMN_HISTORY_STATUS = 'Taken'
        AND m.$COLUMN_USER_EMAIL = ?
        AND h.$COLUMN_HISTORY_TIMESTAMP BETWEEN ? AND ?
    """.trimIndent()

        db.rawQuery(query, arrayOf(userEmail, todayStart.toString(), todayEnd.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }
        return 0
    }


    fun getLowestAdherenceDay(userEmail: String): String {
        val weekly = getWeeklyAdherenceStats(userEmail)
        if (weekly.isEmpty() || weekly.all { it.y == 0f }) return ""
        val minEntry = weekly.minByOrNull { it.y } ?: return ""
        return arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")[minEntry.x.toInt()]
    }

}

