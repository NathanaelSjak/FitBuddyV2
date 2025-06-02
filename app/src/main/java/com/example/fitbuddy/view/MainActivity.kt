package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitbuddy.R
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.ExerciseDao
import com.example.fitbuddy.data.ExerciseEntity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var dbHelper: FitBuddyDbHelper
    private lateinit var exerciseDao: ExerciseDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
        if (!prefs.getBoolean("database_initialized", false)) {
            initializeDatabase()
        } else {
            proceedToOnboarding()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeDatabase() {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("DB", "Initializing database...")
                    dbHelper = FitBuddyDbHelper(this@MainActivity)
                    exerciseDao = ExerciseDao(dbHelper)

                    val db = dbHelper.writableDatabase
                    Log.d("DB", "Got writable database at ${db.path}")
                    
                    // Check if database is empty
                    val cursor = db.rawQuery("SELECT COUNT(*) FROM exercises", null)
                    var exerciseCount = 0
                    if (cursor.moveToFirst()) {
                        exerciseCount = cursor.getInt(0)
                    }
                    cursor.close()
                    
                    Log.d("DB", "Current exercise count in database: $exerciseCount")

                    if (exerciseCount == 0) {
//                        populateDefaultExercises()
                    }

                    getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("database_initialized", true)
                        .apply()
                }

                proceedToOnboarding()

            } catch (e: Exception) {
                Log.e("DB", "Error during database initialization: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error initializing app data", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

//    private fun populateDefaultExercises() {
//        val defaultExercises = listOf(
//            ExerciseEntity(0, "Crunches", "x10", null, R.drawable.ic_abs, "Abs", "Beginner"),
//            ExerciseEntity(0, "Plank", "00:20", null, R.drawable.ic_abs, "Abs", "Beginner"),
//            ExerciseEntity(0, "Bicycle Crunches", "x15", null, R.drawable.ic_abs, "Abs", "Intermediate"),
//            ExerciseEntity(0, "Leg Raises", "x12", null, R.drawable.ic_abs, "Abs", "Intermediate"),
//            ExerciseEntity(0, "V-Ups", "x20", null, R.drawable.ic_abs, "Abs", "Advanced"),
//            ExerciseEntity(0, "Hanging Leg Raise", "x15", null, R.drawable.ic_abs, "Abs", "Advanced"),
//
//            ExerciseEntity(0, "Knee Push-Ups", "x8", null, R.drawable.ic_chest, "Chest", "Beginner"),
//            ExerciseEntity(0, "Incline Push-Ups", "x10", null, R.drawable.ic_chest, "Chest", "Beginner"),
//            ExerciseEntity(0, "Push-Ups", "x15", null, R.drawable.ic_chest, "Chest", "Intermediate"),
//            ExerciseEntity(0, "Decline Push-Ups", "x12", null, R.drawable.ic_chest, "Chest", "Intermediate"),
//            ExerciseEntity(0, "Diamond Push-Ups", "x20", null, R.drawable.ic_chest, "Chest", "Advanced"),
//            ExerciseEntity(0, "Archer Push-Ups", "x10", null, R.drawable.ic_chest, "Chest", "Advanced"),
//
//            ExerciseEntity(0, "Tricep Dips", "x10", null, R.drawable.ic_arms, "Arms", "Beginner"),
//            ExerciseEntity(0, "Wall Push-Ups", "x12", null, R.drawable.ic_arms, "Arms", "Beginner"),
//            ExerciseEntity(0, "Diamond Push-Ups", "x12", null, R.drawable.ic_arms, "Arms", "Intermediate"),
//            ExerciseEntity(0, "Close Grip Push-Ups", "x15", null, R.drawable.ic_arms, "Arms", "Intermediate"),
//            ExerciseEntity(0, "One Arm Push-Ups", "x8", null, R.drawable.ic_arms, "Arms", "Advanced"),
//            ExerciseEntity(0, "Bench Dips", "x20", null, R.drawable.ic_arms, "Arms", "Advanced"),
//
//            ExerciseEntity(0, "Squats", "x15", null, R.drawable.ic_legs, "Legs", "Beginner"),
//            ExerciseEntity(0, "Lunges", "x10", null, R.drawable.ic_legs, "Legs", "Beginner"),
//            ExerciseEntity(0, "Jump Squats", "x12", null, R.drawable.ic_legs, "Legs", "Intermediate"),
//            ExerciseEntity(0, "Bulgarian Split Squat", "x10", null, R.drawable.ic_legs, "Legs", "Intermediate"),
//            ExerciseEntity(0, "Pistol Squats", "x8", null, R.drawable.ic_legs, "Legs", "Advanced"),
//            ExerciseEntity(0, "Box Jumps", "x15", null, R.drawable.ic_legs, "Legs", "Advanced"),
//
//            ExerciseEntity(0, "Superman", "x12", null, R.drawable.ic_back, "Back", "Beginner"),
//            ExerciseEntity(0, "Reverse Snow Angels", "x10", null, R.drawable.ic_back, "Back", "Beginner"),
//            ExerciseEntity(0, "Pull-Ups", "x8", null, R.drawable.ic_back, "Back", "Intermediate"),
//            ExerciseEntity(0, "Inverted Rows", "x10", null, R.drawable.ic_back, "Back", "Intermediate"),
//            ExerciseEntity(0, "Archer Pull-Ups", "x6", null, R.drawable.ic_back, "Back", "Advanced"),
//            ExerciseEntity(0, "One Arm Rows", "x10", null, R.drawable.ic_back, "Back", "Advanced")
//        )
//
//        val db = dbHelper.writableDatabase
//        db.beginTransaction()
//        try {
//            Log.d("DB", "Starting exercise insertion...")
//
//            // Clear existing data first
//            db.delete("exercises", null, null)
//            Log.d("DB", "Cleared existing exercises")
//
//            defaultExercises.forEach { exercise ->
//                val values = android.content.ContentValues().apply {
//                    put("name", exercise.name)
//                    put("reps_or_time", exercise.repsOrTime)
//                    put("video_url", exercise.videoUrl)
//                    put("image_res_id", exercise.imageResId)
//                    put("body_part", exercise.bodyPart)
//                    put("level", exercise.level)
//                }
//                db.insert("exercises", null, values)
//            }
//
//            db.setTransactionSuccessful()
//            Log.d("DB", "Successfully inserted ${defaultExercises.size} exercises")
//
//        } catch (e: Exception) {
//            Log.e("DB", "Error inserting exercises: ${e.message}")
//            e.printStackTrace()
//            throw e
//        } finally {
//            db.endTransaction()
//        }
//    }

    private fun proceedToOnboarding() {
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
