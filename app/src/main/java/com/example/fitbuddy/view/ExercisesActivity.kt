package com.example.fitbuddy.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.model.Exercise
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.ExerciseDao
import com.example.fitbuddy.data.ExerciseEntity
import com.example.fitbuddy.data.UserProgressDao
import android.widget.Toast
import android.widget.TextView

class ExercisesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnStart: Button
    private var exercises: List<Exercise> = emptyList()
    private var level: String = "Beginner"
    private var bodyPart: String = "Abs"
    private lateinit var dbHelper: FitBuddyDbHelper
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var sharedPreferences: SharedPreferences    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        // Initialize database
        dbHelper = FitBuddyDbHelper(this)
        exerciseDao = ExerciseDao(dbHelper)
        userProgressDao = UserProgressDao(dbHelper)
        sharedPreferences = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)

        // Get intent extras
        level = intent.getStringExtra("level") ?: "Beginner"
        bodyPart = intent.getStringExtra("bodyPart") ?: "Abs"

        // Check if workout is unlocked
        if (!userProgressDao.isWorkoutUnlocked(bodyPart, level)) {
            Toast.makeText(this, "This workout is locked. Unlock it from the shop!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize default exercises on first run
        if (!sharedPreferences.getBoolean("exercises_initialized", false)) {
            populateDefaultExercises()
            sharedPreferences.edit().putBoolean("exercises_initialized", true).apply()
        }

        recyclerView = findViewById(R.id.recyclerExercises)
        btnStart = findViewById(R.id.btnStartWorkout)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "$bodyPart Workout - $level"

        loadExercises()
        setupButtons()
    }

    private fun loadExercises() {
        try {
            exercises = getExercisesFromDatabase(level, bodyPart)
            if (exercises.isEmpty()) {
                Toast.makeText(this, "No exercises found for this selection.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = ExerciseAdapter(exercises)
        } catch (e: Exception) {
            android.util.Log.e("ExercisesActivity", "Error: ", e)
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupButtons() {
        btnStart.setOnClickListener {
            val intent = Intent(this, ExerciseStepActivity::class.java)
            intent.putExtra("exercises", ArrayList(exercises))
            intent.putExtra("bodyPart", bodyPart)
            intent.putExtra("level", level)
            startActivity(intent)
        }
    }

    private fun getExercisesFromDatabase(level: String, bodyPart: String): List<Exercise> {
        val exerciseEntities = exerciseDao.getExercises(bodyPart, level)
        return exerciseEntities.map { entity ->
            Exercise(
                name = entity.name,
                repsOrTime = entity.repsOrTime,
                videoUrl = entity.videoUrl ?: "",
                imageResId = entity.imageResId
            )
        }
    }

    private fun populateDefaultExercises() {
        val defaultExercises = listOf(
            ExerciseEntity(0, "Crunches", "x10", "android.resource://com.example.fitbuddy/raw/sample", R.drawable.ic_abs, "Abs", "Beginner"),
            ExerciseEntity(0, "Plank", "00:20", "", R.drawable.ic_abs, "Abs", "Beginner"),
            ExerciseEntity(0, "Bicycle Crunches", "x15", "", R.drawable.ic_abs, "Abs", "Intermediate"),
            ExerciseEntity(0, "Leg Raises", "x12", "", R.drawable.ic_abs, "Abs", "Intermediate"),
            ExerciseEntity(0, "V-Ups", "x20", "", R.drawable.ic_abs, "Abs", "Advanced"),
            ExerciseEntity(0, "Hanging Leg Raise", "x15", "", R.drawable.ic_abs, "Abs", "Advanced"),
            
            ExerciseEntity(0, "Knee Push-Ups", "x8", "", R.drawable.ic_chest, "Chest", "Beginner"),
            ExerciseEntity(0, "Incline Push-Ups", "x10", "", R.drawable.ic_chest, "Chest", "Beginner"),
            ExerciseEntity(0, "Push-Ups", "x15", "", R.drawable.ic_chest, "Chest", "Intermediate"),
            ExerciseEntity(0, "Decline Push-Ups", "x12", "", R.drawable.ic_chest, "Chest", "Intermediate"),
            ExerciseEntity(0, "Diamond Push-Ups", "x20", "", R.drawable.ic_chest, "Chest", "Advanced"),
            ExerciseEntity(0, "Archer Push-Ups", "x10", "", R.drawable.ic_chest, "Chest", "Advanced"),
            
            ExerciseEntity(0, "Tricep Dips", "x10", "", R.drawable.ic_arms, "Arms", "Beginner"),
            ExerciseEntity(0, "Wall Push-Ups", "x12", "", R.drawable.ic_arms, "Arms", "Beginner"),
            ExerciseEntity(0, "Diamond Push-Ups", "x12", "", R.drawable.ic_arms, "Arms", "Intermediate"),
            ExerciseEntity(0, "Close Grip Push-Ups", "x15", "", R.drawable.ic_arms, "Arms", "Intermediate"),
            ExerciseEntity(0, "One Arm Push-Ups", "x8", "", R.drawable.ic_arms, "Arms", "Advanced"),
            ExerciseEntity(0, "Bench Dips", "x20", "", R.drawable.ic_arms, "Arms", "Advanced"),
            
            ExerciseEntity(0, "Squats", "x15", "", R.drawable.ic_legs, "Legs", "Beginner"),
            ExerciseEntity(0, "Lunges", "x10", "", R.drawable.ic_legs, "Legs", "Beginner"),
            ExerciseEntity(0, "Jump Squats", "x12", "", R.drawable.ic_legs, "Legs", "Intermediate"),
            ExerciseEntity(0, "Bulgarian Split Squat", "x10", "", R.drawable.ic_legs, "Legs", "Intermediate"),
            ExerciseEntity(0, "Pistol Squats", "x8", "", R.drawable.ic_legs, "Legs", "Advanced"),
            ExerciseEntity(0, "Box Jumps", "x15", "", R.drawable.ic_legs, "Legs", "Advanced"),
            
            ExerciseEntity(0, "Superman", "x12", "", R.drawable.ic_back, "Back", "Beginner"),
            ExerciseEntity(0, "Reverse Snow Angels", "x10", "", R.drawable.ic_back, "Back", "Beginner"),
            ExerciseEntity(0, "Pull-Ups", "x8", "", R.drawable.ic_back, "Back", "Intermediate"),
            ExerciseEntity(0, "Inverted Rows", "x10", "", R.drawable.ic_back, "Back", "Intermediate"),
            ExerciseEntity(0, "Archer Pull-Ups", "x6", "", R.drawable.ic_back, "Back", "Advanced"),
            ExerciseEntity(0, "One Arm Rows", "x10", "", R.drawable.ic_back, "Back", "Advanced")
        )
        
        for (exercise in defaultExercises) {
            exerciseDao.insertExercise(exercise)
        }
    }
}
