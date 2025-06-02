package com.example.fitbuddy.data

import android.content.ContentValues
import android.database.Cursor
import com.example.fitbuddy.model.WorkoutCategory

class WorkoutCategoryDao(private val dbHelper: FitBuddyDbHelper) {
    
    fun getAllWorkoutCategories(): List<WorkoutCategory> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "workout_categories",
            null,
            null,
            null,
            null,
            null,
            "level ASC, body_part ASC"
        )
        
        val categories = mutableListOf<WorkoutCategory>()
        while (cursor.moveToNext()) {
            categories.add(
                WorkoutCategory(
                    bodyPart = cursor.getString(cursor.getColumnIndexOrThrow("body_part")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("body_part")),
                    level = cursor.getString(cursor.getColumnIndexOrThrow("level")),
                    isUnlocked = cursor.getInt(cursor.getColumnIndexOrThrow("is_unlocked")) == 1,
                    pointsRequired = cursor.getInt(cursor.getColumnIndexOrThrow("points_required"))
                )
            )
        }
        cursor.close()
        return categories
    }

    fun getWorkoutCategoriesByLevel(level: String): List<WorkoutCategory> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "workout_categories",
            null,
            "level = ?",
            arrayOf(level),
            null,
            null,
            "body_part ASC"
        )
        
        val categories = mutableListOf<WorkoutCategory>()
        while (cursor.moveToNext()) {
            categories.add(
                WorkoutCategory(
                    bodyPart = cursor.getString(cursor.getColumnIndexOrThrow("body_part")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("body_part")),
                    level = cursor.getString(cursor.getColumnIndexOrThrow("level")),
                    isUnlocked = cursor.getInt(cursor.getColumnIndexOrThrow("is_unlocked")) == 1,
                    pointsRequired = cursor.getInt(cursor.getColumnIndexOrThrow("points_required"))
                )
            )
        }
        cursor.close()
        return categories
    }

    fun unlockWorkoutCategory(bodyPart: String, level: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("is_unlocked", 1)
            put("unlocked_at", System.currentTimeMillis())
        }
        
        return db.update(
            "workout_categories",
            values,
            "body_part = ? AND level = ?",
            arrayOf(bodyPart, level)
        ) > 0
    }

    fun isWorkoutUnlocked(bodyPart: String, level: String): Boolean {
        if (level == "Beginner") return true

        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "workout_categories",
            arrayOf("is_unlocked"),
            "body_part = ? AND level = ?",
            arrayOf(bodyPart, level),
            null,
            null,
            null
        )

        var isUnlocked = false
        if (cursor.moveToFirst()) {
            isUnlocked = cursor.getInt(0) == 1
        }
        cursor.close()
        return isUnlocked
    }

    fun getRequiredPoints(bodyPart: String, level: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "workout_categories",
            arrayOf("points_required"),
            "body_part = ? AND level = ?",
            arrayOf(bodyPart, level),
            null,
            null,
            null
        )

        var points = 0
        if (cursor.moveToFirst()) {
            points = cursor.getInt(0)
        }
        cursor.close()
        return points
    }

    fun getCategoryId(bodyPart: String, level: String): Long {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "workout_categories",
            arrayOf("id"),
            "body_part = ? AND level = ?",
            arrayOf(bodyPart, level),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else {
            -1
        }.also {
            cursor.close()
        }
    }

    fun unlockWorkoutCategory(categoryId: Long): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("is_unlocked", 1)
            put("unlocked_at", System.currentTimeMillis().toString())
        }
        
        return db.update(
            "workout_categories",
            values,
            "id = ?",
            arrayOf(categoryId.toString())
        ) > 0
    }
} 