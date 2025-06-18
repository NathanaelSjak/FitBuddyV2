package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.model.Exercise
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.ExerciseDao
import com.example.fitbuddy.data.UserProgressDao
import com.example.fitbuddy.data.WorkoutCategoryDao
import android.widget.Toast
import android.widget.TextView
import com.example.fitbuddy.adapter.ExerciseAdapter
import android.util.Log

class ExercisesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnStart: Button
    private var exercises: List<Exercise> = emptyList()
    private var level: String = "Beginner"
    private var bodyPart: String = "Abs"
    private var categoryId: Long = -1
    private lateinit var dbHelper: FitBuddyDbHelper
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var workoutCategoryDao: WorkoutCategoryDao

    companion object {
        private const val TAG = "ExercisesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        dbHelper = FitBuddyDbHelper(this)
        exerciseDao = ExerciseDao(dbHelper)
        userProgressDao = UserProgressDao(dbHelper)
        workoutCategoryDao = WorkoutCategoryDao(dbHelper)

        level = intent.getStringExtra("level") ?: "Beginner"
        bodyPart = intent.getStringExtra("bodyPart") ?: "Abs"
        categoryId = intent.getLongExtra("categoryId", -1L)

        if (!workoutCategoryDao.isWorkoutUnlocked(bodyPart, level)) {
            Toast.makeText(this, "This workout is locked. Unlock it from the shop!", Toast.LENGTH_SHORT).show()
            finish()
            return
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
            exercises = getExercisesFromDatabase()
            if (exercises.isEmpty()) {
                Toast.makeText(this, "No exercises found for this selection.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = ExerciseAdapter(exercises)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ", e)
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

    private fun getExercisesFromDatabase(): List<Exercise> {
        if (categoryId == -1L) {
            categoryId = workoutCategoryDao.getCategoryId(bodyPart, level)
            if (categoryId == -1L) {
                throw IllegalStateException("Category not found for $bodyPart - $level")
            }
        }

        val exerciseEntities = exerciseDao.getExercisesByCategory(categoryId)
        return exerciseEntities.map { entity ->
            Exercise(
                id = entity.id,
                name = entity.name,
                repsOrTime = entity.repsOrTime,
                videoResourceName = entity.videoResourceName?.let { getFullPath(it) },
                imageResourceName = getFullPath(entity.imageResourceName),
                level = level
            )
        }
    }

    private fun getFullPath(relativePath: String): String {
        return relativePath.substringAfterLast("/")
    }
}
