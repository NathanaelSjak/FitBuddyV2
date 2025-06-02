package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.ExerciseDao
import com.example.fitbuddy.data.ExerciseEntity
import com.example.fitbuddy.data.WorkoutCategoryEntity
import com.example.fitbuddy.databinding.ActivityOnboardingBinding
import kotlinx.coroutines.*

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var dbHelper: FitBuddyDbHelper
    private lateinit var exerciseDao: ExerciseDao
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
        if (!prefs.getBoolean("database_initialized", false)) {
            initializeDatabase()
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun initializeDatabase() {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("DB", "Initializing database...")
                    dbHelper = FitBuddyDbHelper(this@OnboardingActivity)
                    exerciseDao = ExerciseDao(dbHelper)

                    val db = dbHelper.writableDatabase
                    Log.d("DB", "Got writable database at ${db.path}")
                    
                    val cursor = db.rawQuery("SELECT COUNT(*) FROM exercises", null)
                    var exerciseCount = 0
                    if (cursor.moveToFirst()) {
                        exerciseCount = cursor.getInt(0)
                    }
                    cursor.close()
                    
                    Log.d("DB", "Current exercise count in database: $exerciseCount")

                    if (exerciseCount == 0) {
                        populateDefaultExercises()
                    }

                    getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("database_initialized", true)
                        .apply()
                }

            } catch (e: Exception) {
                Log.e("DB", "Error during database initialization: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun populateDefaultExercises() {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            Log.d("DB", "Starting database seeding...")
            
            db.delete("workout_categories", null, null)
            db.delete("exercises", null, null)
            Log.d("DB", "Cleared existing data")
            
            val categoryIds = mutableMapOf<Pair<String, String>, Long>()
            WorkoutCategoryEntity.getDefaultCategories().forEach { category ->
                val values = android.content.ContentValues().apply {
                    put("body_part", category.bodyPart)
                    put("level", category.level.name)
                    put("points_required", category.level.requiredPoints)
                    put("is_unlocked", category.isUnlocked)
                }
                val id = db.insert("workout_categories", null, values)
                categoryIds[Pair(category.bodyPart, category.level.name)] = id
                Log.d("DB", "Inserted category: ${category.bodyPart} - ${category.level.name} " +
                          "(Points: ${category.level.requiredPoints}, Unlocked: ${category.isUnlocked}) with ID: $id")
            }
            
            ExerciseEntity.getDefaultExercises(categoryIds).forEach { exercise ->
                val values = android.content.ContentValues().apply {
                    put("name", exercise.name)
                    put("reps_or_time", exercise.repsOrTime)
                    put("video_url", exercise.videoUrl)
                    put("image_res_id", exercise.imageResId)
                    put("category_id", exercise.categoryId)
                }
                db.insert("exercises", null, values)
                Log.d("DB", "Inserted exercise: ${exercise.name} for category ID: ${exercise.categoryId}")
            }
            
            db.setTransactionSuccessful()
            Log.d("DB", "Successfully seeded database with ${categoryIds.size} categories and exercises")
            
        } catch (e: Exception) {
            Log.e("DB", "Error seeding database: ${e.message}")
            e.printStackTrace()
            throw e
        } finally {
            db.endTransaction()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}