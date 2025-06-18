package com.example.fitbuddy.data

import android.content.ContentValues
import android.database.Cursor
import android.util.Log

class UserStatsDao(private val dbHelper: FitBuddyDbHelper) {
    private var currentUserId: String = "default"
    
    fun setCurrentUserId(userId: String) {
        this.currentUserId = userId
        Log.d("UserStatsDao", "Current user ID set to: $userId")
    }
    
    fun getCurrentUserId(): String {
        return currentUserId
    }
    
    fun insertStats(stats: UserStatsEntity) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("user_id", currentUserId)
            put("date", stats.date)
            put("height", stats.height)
            put("weight", stats.weight)
        }
        db.insert("user_stats", null, values)
    }

    fun getStatsByDate(date: String): UserStatsEntity? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "user_stats",
            null,
            "user_id = ? AND date = ?",
            arrayOf(currentUserId, date),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            cursorToStats(cursor)
        } else {
            null
        }
    }    fun getStatsForDateRange(startDate: String, endDate: String): List<UserStatsEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "user_stats",
            null,
            "user_id = ? AND date BETWEEN ? AND ?",
            arrayOf(currentUserId, startDate, endDate),
            null, null, "date ASC"
        )
        return cursorToList(cursor)
    }

    private fun cursorToStats(cursor: Cursor): UserStatsEntity {
        return UserStatsEntity(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
            height = cursor.getFloat(cursor.getColumnIndexOrThrow("height")),
            weight = cursor.getFloat(cursor.getColumnIndexOrThrow("weight"))
        )
    }

    private fun cursorToList(cursor: Cursor): List<UserStatsEntity> {
        val list = mutableListOf<UserStatsEntity>()
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToStats(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}