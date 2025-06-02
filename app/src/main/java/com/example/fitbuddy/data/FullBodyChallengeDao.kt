package com.example.fitbuddy.data

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class ChallengeDay(
    val id: Long = 0,
    val startDate: String,
    val endDate: String,
    val bodyPart: String,
    val scheduledDate: String,
    val completed: Boolean = false,
    val completedAt: String? = null,
    val pointsEarned: Int = 0
)

class FullBodyChallengeDao(private val dbHelper: FitBuddyDbHelper) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun startNewChallenge(): Boolean {
        val db = dbHelper.writableDatabase
        val startDate = dateFormat.format(Date())
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 1)
        val endDate = dateFormat.format(calendar.time)

        db.delete("full_body_challenge", null, null)

        try {
            db.beginTransaction()

            // List of body parts
            val bodyParts = listOf("Abs", "Chest", "Arms", "Back", "Legs")
            calendar.time = dateFormat.parse(startDate) ?: Date()

            // Create schedule for each day
            while (!dateFormat.format(calendar.time).equals(endDate)) {
                val currentDate = dateFormat.format(calendar.time)
                
                // Get body part for this day (cycling through the list)
                val daysSinceStart = ((calendar.timeInMillis - dateFormat.parse(startDate)!!.time) 
                    / (24 * 60 * 60 * 1000)).toInt()
                val bodyPart = bodyParts[daysSinceStart % bodyParts.size]

                val values = ContentValues().apply {
                    put("start_date", startDate)
                    put("end_date", endDate)
                    put("body_part", bodyPart)
                    put("scheduled_date", currentDate)
                    put("completed", 0)
                }
                db.insert("full_body_challenge", null, values)

                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            Log.e("FullBodyChallengeDao", "Error starting challenge", e)
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun getTodayChallenge(): ChallengeDay? {
        val today = dateFormat.format(Date())
        return getChallengeForDate(today)
    }

    fun getChallengeForDate(date: String): ChallengeDay? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "full_body_challenge",
            null,
            "scheduled_date = ?",
            arrayOf(date),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) cursorToChallenge(it) else null
        }
    }

    fun getAllChallengeDays(): List<ChallengeDay> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "full_body_challenge",
            null,
            null,
            null,
            null, null,
            "scheduled_date ASC"
        )

        return cursor.use {
            val challenges = mutableListOf<ChallengeDay>()
            while (it.moveToNext()) {
                challenges.add(cursorToChallenge(it))
            }
            challenges
        }
    }

    fun markChallengeCompleted(id: Long, pointsEarned: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("completed", 1)
            put("completed_at", dateTimeFormat.format(Date()))
            put("points_earned", pointsEarned)
        }
        db.update(
            "full_body_challenge",
            values,
            "id = ?",
            arrayOf(id.toString())
        )
    }

    fun hasActiveChallenge(): Boolean {
        val today = dateFormat.format(Date())
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "full_body_challenge",
            arrayOf("COUNT(*)"),
            "scheduled_date >= ?",
            arrayOf(today),
            null, null, null
        )

        return cursor.use {
            it.moveToFirst() && it.getInt(0) > 0
        }
    }

    private fun cursorToChallenge(cursor: Cursor): ChallengeDay {
        return ChallengeDay(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
            endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
            bodyPart = cursor.getString(cursor.getColumnIndexOrThrow("body_part")),
            scheduledDate = cursor.getString(cursor.getColumnIndexOrThrow("scheduled_date")),
            completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1,
            completedAt = cursor.getString(cursor.getColumnIndexOrThrow("completed_at")),
            pointsEarned = cursor.getInt(cursor.getColumnIndexOrThrow("points_earned"))
        )
    }
} 