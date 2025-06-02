package com.example.fitbuddy.data

import android.content.ContentValues
import android.database.Cursor

class UserProgressDao(private val dbHelper: FitBuddyDbHelper) {
    fun insertProgress(progress: UserProgressEntity) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("date", progress.date)
            put("bodyPart", progress.bodyPart)
            put("level", progress.level)
            put("completed", if (progress.completed) 1 else 0)
            put("points", progress.points)
            put("completed_at", progress.completedAt)
            put("points_earned", progress.pointsEarned)
        }
        db.insert("user_progress", null, values)
    }

    fun getProgressForDate(date: String): List<UserProgressEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "user_progress",
            null,
            "date = ?",
            arrayOf(date),
            null, null, null
        )
        return cursorToList(cursor)
    }

    fun getTotalPoints(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(points_earned) FROM user_progress", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun getTotalCompletedWorkouts(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM user_progress WHERE completed = 1", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun getProgressForDateRange(startDate: String, endDate: String): List<UserProgressEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "user_progress",
            null,
            "date BETWEEN ? AND ?",
            arrayOf(startDate, endDate),
            null, null, null
        )
        val list = mutableListOf<UserProgressEntity>()
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    UserProgressEntity(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        bodyPart = cursor.getString(cursor.getColumnIndexOrThrow("bodyPart")),
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

    fun unlockWorkout(bodyPart: String, level: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("body_part", bodyPart)
            put("level", level)
            put("is_unlocked", 1)
        }
        db.insert("unlocked_workouts", null, values)
    }

    fun isWorkoutUnlocked(bodyPart: String, level: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "unlocked_workouts",
            null,
            "body_part = ? AND level = ?",
            arrayOf(bodyPart, level),
            null, null, null
        )
        val isUnlocked = cursor.count > 0
        cursor.close()
        return isUnlocked || level == "Beginner" // Beginner workouts are always unlocked
    }

    private fun cursorToList(cursor: Cursor): List<UserProgressEntity> {
        val list = mutableListOf<UserProgressEntity>()
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    UserProgressEntity(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        bodyPart = cursor.getString(cursor.getColumnIndexOrThrow("bodyPart")),
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
