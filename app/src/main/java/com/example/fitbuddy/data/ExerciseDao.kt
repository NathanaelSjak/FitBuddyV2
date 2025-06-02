package com.example.fitbuddy.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.fitbuddy.R

class ExerciseDao(private val dbHelper: FitBuddyDbHelper) {
    fun insertExercise(ex: ExerciseEntity) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", ex.name)
            put("repsOrTime", ex.repsOrTime)
            put("videoUrl", ex.videoUrl)
            put("imageResId", ex.imageResId)
            put("bodyPart", ex.bodyPart)
            put("level", ex.level)
        }
        db.insert("exercises", null, values)
    }

    fun getExercises(bodyPart: String, level: String): List<ExerciseEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "exercises",
            null,
            "bodyPart=? AND level=?",
            arrayOf(bodyPart, level),
            null, null, null
        )
        return cursorToList(cursor)
    }

    private fun cursorToList(cursor: Cursor): List<ExerciseEntity> {
        val list = mutableListOf<ExerciseEntity>()
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    ExerciseEntity(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        repsOrTime = cursor.getString(cursor.getColumnIndexOrThrow("repsOrTime")),
                        videoUrl = cursor.getString(cursor.getColumnIndexOrThrow("videoUrl")),
                        imageResId = cursor.getInt(cursor.getColumnIndexOrThrow("imageResId")),
                        bodyPart = cursor.getString(cursor.getColumnIndexOrThrow("bodyPart")),
                        level = cursor.getString(cursor.getColumnIndexOrThrow("level"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun insertDefaultExercises(context: Context) {
        // Only call this once, e.g. on first run
        val defaultExercises = listOf(
            ExerciseEntity(0, "Crunches", "x10", null, R.drawable.ic_abs, "Abs", "Beginner"),
            ExerciseEntity(0, "Plank", "00:20", null, R.drawable.ic_abs, "Abs", "Beginner"),
            ExerciseEntity(0, "Push-Ups", "x15", null, R.drawable.ic_chest, "Chest", "Intermediate"),
            ExerciseEntity(0, "Squats", "x15", null, R.drawable.ic_legs, "Legs", "Beginner"),
            // Add more as needed
        )
        for (ex in defaultExercises) {
            insertExercise(ex)
        }
    }
}
