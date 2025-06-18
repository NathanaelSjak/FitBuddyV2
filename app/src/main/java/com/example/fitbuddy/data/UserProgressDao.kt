package com.example.fitbuddy.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class UserProgressDao(private val dbHelper: FitBuddyDbHelper) {
    private var currentUserId: String = "default"
    
    fun setCurrentUserId(userId: String) {
        this.currentUserId = userId
        Log.d("UserProgressDao", "Current user ID set to: $userId")
    }
    
    fun getCurrentUserId(): String {
        return currentUserId
    }

    fun insertProgress(progress: UserProgressEntity) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            Log.d("UserProgressDao", "Inserting progress: $progress")
            
            val values = ContentValues().apply {
                put("user_id", currentUserId)
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
            "user_id = ? AND date = ?",
            arrayOf(currentUserId, date),
            null, null, "completed_at DESC"
        )
        return cursorToList(cursor)
    }    fun getTotalPoints(): Int {
        val db = dbHelper.readableDatabase
        
        var cursor = db.rawQuery(
            "SELECT points FROM user_stats WHERE user_id = ?",
            arrayOf(currentUserId)
        )
        
        var totalPoints = if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getInt(0)
        } else {
            cursor.close()
            cursor = db.rawQuery(
                "SELECT SUM(points_earned) FROM user_progress WHERE user_id = ? AND completed = 1",
                arrayOf(currentUserId)
            )
            
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                val calculatedPoints = cursor.getInt(0)
                updatePoints(calculatedPoints)
                calculatedPoints
            } else {
                0
            }
        }
        cursor.close()
        
        Log.d("UserProgressDao", "Total points for user $currentUserId: $totalPoints")
        return totalPoints
    }

    fun getTotalCompletedWorkouts(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM user_progress WHERE user_id = ? AND completed = 1",
            arrayOf(currentUserId)
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
            "user_id = ? AND date BETWEEN ? AND ?",
            arrayOf(currentUserId, startDate, endDate),
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
    }    fun updatePoints(points: Int): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            Log.d("UserProgressDao", "Updating points for user $currentUserId to: $points")
            
            val cursor = db.query(
                "user_stats",
                arrayOf("id"),
                "user_id = ?",
                arrayOf(currentUserId),
                null,
                null,
                null
            )
            
            val values = ContentValues().apply {
                put("points", points)
                put("user_id", currentUserId)
            }

            val success = if (cursor.moveToFirst()) {
                val userId = cursor.getLong(0)
                val rows = db.update("user_stats", values, "id = ?", arrayOf(userId.toString()))
                Log.d("UserProgressDao", "Updated user_stats for ID: $userId, rows affected: $rows")
                
                val verifyQuery = db.query(
                    "user_stats", 
                    arrayOf("points"),
                    "id = ?", 
                    arrayOf(userId.toString()), 
                    null, null, null
                )
                if (verifyQuery.moveToFirst()) {
                    val verifiedPoints = verifyQuery.getInt(0)
                    Log.d("UserProgressDao", "Verified points after update: $verifiedPoints")
                    verifyQuery.close()
                }
                
                rows > 0
            } else {
                values.apply {
                    put("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                    put("height", 0.0)
                    put("weight", 0.0)
                }
                val id = db.insert("user_stats", null, values)
                Log.d("UserProgressDao", "Created new user_stats record with ID: $id")
                
                if (id != -1L) {
                    val verifyQuery = db.query(
                        "user_stats", 
                        arrayOf("points"),
                        "id = ?", 
                        arrayOf(id.toString()), 
                        null, null, null
                    )
                    if (verifyQuery.moveToFirst()) {
                        val verifiedPoints = verifyQuery.getInt(0)
                        Log.d("UserProgressDao", "Verified points after insert: $verifiedPoints")
                        verifyQuery.close()
                    }
                }
                
                id != -1L
            }
            cursor.close()
            
            try {
                db.execSQL(
                    "UPDATE user_stats SET points = ? WHERE user_id = ?",
                    arrayOf(points, currentUserId)
                )
                Log.d("UserProgressDao", "Direct SQL update executed")
            } catch (e: Exception) {
                Log.e("UserProgressDao", "Direct SQL update failed", e)
            }
            
            db.setTransactionSuccessful()
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
                        userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id")),
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
