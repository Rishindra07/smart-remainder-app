package com.example.smartremainder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "UserProfile.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USER = "user_profile"
        
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_USERNAME = "username"
        private const val COL_EMAIL = "email"
        private const val COL_PHONE = "phone"
        private const val COL_PROFILE_IMAGE = "profile_image"
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USER (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_USERNAME TEXT NOT NULL,
                $COL_EMAIL TEXT NOT NULL,
                $COL_PHONE TEXT NOT NULL,
                $COL_PROFILE_IMAGE TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }
    
    fun getUserProfile(): UserProfile? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USER LIMIT 1", null)
        
        var userProfile: UserProfile? = null
        if (cursor.moveToFirst()) {
            userProfile = UserProfile(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
                profileImage = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROFILE_IMAGE))
            )
        }
        cursor.close()
        return userProfile
    }
    
    fun insertOrUpdateUser(userProfile: UserProfile): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, userProfile.name)
            put(COL_USERNAME, userProfile.username)
            put(COL_EMAIL, userProfile.email)
            put(COL_PHONE, userProfile.phone)
            put(COL_PROFILE_IMAGE, userProfile.profileImage)
        }
        
        // Check if user exists
        val cursor = db.rawQuery("SELECT $COL_ID FROM $TABLE_USER LIMIT 1", null)
        val result = if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            db.update(TABLE_USER, values, "$COL_ID = ?", arrayOf(id.toString())).toLong()
        } else {
            db.insert(TABLE_USER, null, values)
        }
        cursor.close()
        return result
    }
    
    fun deleteUser(): Int {
        val db = writableDatabase
        return db.delete(TABLE_USER, null, null)
    }
}