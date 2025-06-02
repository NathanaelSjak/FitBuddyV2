package com.example.fitbuddy.data

import android.content.ContentValues
import android.database.Cursor
import com.example.fitbuddy.model.UnlockableWorkout
import java.text.SimpleDateFormat
import java.util.*

class UnlockableWorkoutDao(private val dbHelper: FitBuddyDbHelper) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun getAllUnlockableWorkouts(): List<UnlockableWorkout> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "unlocked_workouts",
            null,
            null,
            null,
            null,
            null,
            "level ASC, body_part ASC"
        )
        return cursorToList(cursor)
    }

    fun unlockWorkout(bodyPart: String, level: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("is_unlocked", 1)
            put("unlocked_at", dateFormat.format(Date()))
        }
        db.update(
            "unlocked_workouts",
            values,
            "body_part = ? AND level = ?",
            arrayOf(bodyPart, level)
        )
    }

    fun isWorkoutUnlocked(bodyPart: String, level: String): Boolean {
        if (level == "Beginner") return true

        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "unlocked_workouts",
            arrayOf("is_unlocked"),
            "body_part = ? AND level = ?",
            arrayOf(bodyPart, level),
            null, null, null
        )

        var isUnlocked = false
        if (cursor.moveToFirst()) {
            isUnlocked = cursor.getInt(0) == 1
        }
        cursor.close()
        return isUnlocked
    }

    private fun cursorToList(cursor: Cursor): List<UnlockableWorkout> {
        val list = mutableListOf<UnlockableWorkout>()
        if (cursor.moveToFirst()) {
            do {
                val bodyPart = cursor.getString(cursor.getColumnIndexOrThrow("body_part"))
                val level = cursor.getString(cursor.getColumnIndexOrThrow("level"))
                val pointsRequired = cursor.getInt(cursor.getColumnIndexOrThrow("points_required"))
                val isUnlocked = cursor.getInt(cursor.getColumnIndexOrThrow("is_unlocked")) == 1

                list.add(UnlockableWorkout(
                    bodyPart = bodyPart,
                    level = level,
                    pointsRequired = pointsRequired,
                    isUnlocked = isUnlocked
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
} 