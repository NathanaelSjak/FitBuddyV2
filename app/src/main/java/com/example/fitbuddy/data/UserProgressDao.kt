package com.example.fitbuddy.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class UserProgressDao(private val dbHelper: FitBuddyDbHelper) {
    fun insertProgress(progress: UserProgressEntity) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            Log.d("UserProgressDao", "Inserting progress: $progress")
            
            val values = ContentValues().apply {
                put("date", progress.date)
                put("body_part", progress.bodyPart)
                put("level", progress.level)
                put("completed", if (progress.completed) 1 else 0)
                put("points", progress.points)
                put("completed_at", progress.completedAt)
                put("points_earned", progress.pointsEarned)
            }
            val rowId = db.insert("user_progress", null, values)
            Log.d("UserProgressDao", "Inserted progress with row ID: $rowId")

            if (progress.completed) {
                val currentPoints = getTotalPoints()
                val newPoints = currentPoints + progress.pointsEarned
                updatePoints(newPoints)
                Log.d("UserProgressDao", "Updated total points from $currentPoints to $newPoints")
            }

            db.setTransactionSuccessful()
            Log.d("UserProgressDao", "Transaction completed successfully")
        } catch (e: Exception) {
            Log.e("UserProgressDao", "Error inserting progress: ${e.message}")
            e.printStackTrace()
            throw e
        } finally {
            db.endTransaction()
        }
    }

    fun getProgressForDate(date: String): List<UserProgressEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "user_progress",
            null,
            "date = ?",
            arrayOf(date),
            null, null, "completed_at DESC"
        )
        return cursorToList(cursor)
    }

    fun getTotalPoints(): Int {
        val db = dbHelper.readableDatabase
        
        var cursor = db.rawQuery(
            "SELECT SUM(points_earned) FROM user_progress WHERE completed = 1",
            null
        )
        
        var totalPoints = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getInt(0)
        } else {
            0
        }
        cursor.close()
        
        Log.d("UserProgressDao", "Total points from user_progress: $totalPoints")
        return totalPoints
    }

    fun getTotalCompletedWorkouts(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM user_progress WHERE completed = 1",
            null
        )
        return cursor.use { 
            if (it.moveToFirst()) it.getInt(0) else 0 
        }
    }

    fun getProgressForDateRange(startDate: String, endDate: String): List<UserProgressEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "user_progress",
            null,
            "date BETWEEN ? AND ?",
            arrayOf(startDate, endDate),
            null, null, "date DESC, completed_at DESC"
        )
        return cursorToList(cursor)
    }

    fun unlockWorkout(bodyPart: String, level: String, pointsCost: Int): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val currentPoints = getTotalPoints()
            if (currentPoints < pointsCost) {
                return false
            }

            val values = ContentValues().apply {
                put("is_unlocked", 1)
            }
            db.update(
                "workout_categories",
                values,
                "body_part = ? AND level = ?",
                arrayOf(bodyPart, level)
            )

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun isWorkoutUnlocked(bodyPart: String, level: String): Boolean {
        if (level == "Beginner") return true
        
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "workout_categories",
            arrayOf("is_unlocked"),
            "body_part = ? AND level = ?",
            arrayOf(bodyPart, level),
            null, null, null
        )

        return cursor.use { 
            it.moveToFirst() && it.getInt(0) == 1 
        }
    }

    fun updatePoints(points: Int): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.query(
                "user_stats",
                arrayOf("id"),
                "id = 1",
                null,
                null,
                null,
                null
            )
            
            val values = ContentValues().apply {
                put("points", points)
            }

            val success = if (cursor.count > 0) {
                val rows = db.update("user_stats", values, "id = 1", null)
                Log.d("UserProgressDao", "Updated user_stats points, rows affected: $rows")
                rows > 0
            } else {
                values.apply {
                    put("id", 1)
                    put("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                    put("height", 0.0)
                    put("weight", 0.0)
                }
                val id = db.insert("user_stats", null, values)
                Log.d("UserProgressDao", "Created new user_stats record with ID: $id")
                id != -1L
            }
            cursor.close()
            
            db.setTransactionSuccessful()
            Log.d("UserProgressDao", "Successfully updated points to: $points")
            return success
        } catch (e: Exception) {
            Log.e("UserProgressDao", "Error updating points: ${e.message}")
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    private fun cursorToList(cursor: Cursor): List<UserProgressEntity> {
        val list = mutableListOf<UserProgressEntity>()
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    UserProgressEntity(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        bodyPart = cursor.getString(cursor.getColumnIndexOrThrow("body_part")),
                        level = cursor.getString(cursor.getColumnIndexOrThrow("level")),
                        completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1,
                        points = cursor.getInt(cursor.getColumnIndexOrThrow("points")),
                        completedAt = cursor.getString(cursor.getColumnIndexOrThrow("completed_at")),
                        pointsEarned = cursor.getInt(cursor.getColumnIndexOrThrow("points_earned"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
