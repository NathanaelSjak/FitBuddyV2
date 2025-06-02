package com.example.fitbuddy.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.fitbuddy.R

class ExerciseDao(private val dbHelper: FitBuddyDbHelper) {
    fun insertExercise(exercise: ExerciseEntity) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", exercise.name)
            put("reps_or_time", exercise.repsOrTime)
            put("video_resource_name", exercise.videoResourceName)
            put("image_resource_name", exercise.imageResourceName)
            put("category_id", exercise.categoryId)
        }
        db.insert("exercises", null, values)
    }

    fun getExercisesByCategory(categoryId: Long): List<ExerciseEntity> {
        val db = dbHelper.readableDatabase
        Log.d("DB", "Querying exercises for categoryId: $categoryId")
        
        val cursor = db.query(
            "exercises",
            null,
            "category_id = ?",
            arrayOf(categoryId.toString()),
            null,
            null,
            null
        )
        
        Log.d("DB", "Query returned ${cursor.count} results")

        val exercises = mutableListOf<ExerciseEntity>()
        while (cursor.moveToNext()) {
            val exercise = ExerciseEntity(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                repsOrTime = cursor.getString(cursor.getColumnIndexOrThrow("reps_or_time")),
                videoResourceName = cursor.getString(cursor.getColumnIndexOrThrow("video_resource_name")),
                imageResourceName = cursor.getString(cursor.getColumnIndexOrThrow("image_resource_name")),
                categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"))
            )
            exercises.add(exercise)
            Log.d("DB", "Found exercise: ${exercise.name}")
        }
        cursor.close()
        return exercises
    }

    fun getAllExercises(): List<ExerciseEntity> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "exercises",
            null,
            null,
            null,
            null,
            null,
            null
        )

        val exercises = mutableListOf<ExerciseEntity>()
        while (cursor.moveToNext()) {
            exercises.add(
                ExerciseEntity(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    repsOrTime = cursor.getString(cursor.getColumnIndexOrThrow("reps_or_time")),
                    videoResourceName = cursor.getString(cursor.getColumnIndexOrThrow("video_resource_name")),
                    imageResourceName = cursor.getString(cursor.getColumnIndexOrThrow("image_resource_name")),
                    categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"))
                )
            )
        }
        cursor.close()
        return exercises
    }

    private fun getCategoryId(bodyPart: String, level: String): Long {
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
            -1L
        }.also {
            cursor.close()
        }
    }

    fun insertDefaultExercises(context: Context) {
        val defaultExercises = listOf(
            ExerciseEntity(0, "Crunches", "x10", "crunches_video", "crunches_image", getCategoryId("Abs", "Beginner")),
            ExerciseEntity(0, "Plank", "00:20", "plank_video", "plank_image", getCategoryId("Abs", "Beginner")),
            ExerciseEntity(0, "Push-Ups", "x15", null, "chest_exercise", getCategoryId("Chest", "Intermediate")),
            ExerciseEntity(0, "Squats", "x15", null, "legs_exercise", getCategoryId("Legs", "Beginner"))
        )
        for (ex in defaultExercises) {
            insertExercise(ex)
        }
    }
}
