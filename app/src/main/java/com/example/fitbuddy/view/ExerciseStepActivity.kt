package com.example.fitbuddy.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.UserProgressDao
import com.example.fitbuddy.data.UserProgressEntity
import com.example.fitbuddy.model.Exercise
import java.text.SimpleDateFormat
import java.util.*

class ExerciseStepActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var videoView: VideoView
    private lateinit var tvRepsOrTime: TextView
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private var currentStep = 0
    private var exercises: ArrayList<Exercise> = arrayListOf()
    private lateinit var bodyPart: String
    private lateinit var level: String
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var dbHelper: FitBuddyDbHelper

    companion object {
        private const val TAG = "ExerciseStepActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_step)

        tvTitle = findViewById(R.id.tvExerciseName)
        videoView = findViewById(R.id.videoExercise)
        tvRepsOrTime = findViewById(R.id.tvRepsOrTime)
        btnNext = findViewById(R.id.btnNextStep)
        btnSkip = findViewById(R.id.btnSkip)

        dbHelper = FitBuddyDbHelper(this)
        userProgressDao = UserProgressDao(dbHelper)

        exercises = intent.getSerializableExtra("exercises") as? ArrayList<Exercise> ?: arrayListOf()
        bodyPart = intent.getStringExtra("bodyPart") ?: ""
        level = intent.getStringExtra("level") ?: ""

        Log.d(TAG, "Received data - Exercises: ${exercises.size}, Body Part: $bodyPart, Level: $level")
        
        if (exercises.isEmpty() || bodyPart.isEmpty() || level.isEmpty()) {
            Log.e(TAG, "Missing required data from intent")
            Toast.makeText(this, "Error: Missing workout data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        showStep(currentStep)

        btnNext.setOnClickListener {
            if (currentStep < exercises.size - 1) {
                currentStep++
                showStep(currentStep)
            } else {
                saveWorkoutProgress()
            }
        }
        
        btnSkip.setOnClickListener {
            finish()
        }
    }

    private fun showStep(step: Int) {
        if (step >= exercises.size) {
            Log.e(TAG, "Invalid step index: $step, exercises size: ${exercises.size}")
            return
        }

        val exercise = exercises[step]
        Log.d(TAG, "Showing exercise: ${exercise.name}, Step: $step/${exercises.size}")
        
        tvTitle.text = exercise.name
        tvRepsOrTime.text = exercise.repsOrTime
        if (exercise.videoUrl.isNotEmpty()) {
            val uri = Uri.parse(exercise.videoUrl)
            videoView.setVideoURI(uri)
            videoView.start()
        } else {
            videoView.stopPlayback()
        }
        btnNext.text = if (step == exercises.size - 1) "Finish" else "Next"
    }

    private fun saveWorkoutProgress() {
        Log.d(TAG, "Starting saveWorkoutProgress")
        
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val completedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val basePoints = when (level) {
            "Beginner" -> 10
            "Intermediate" -> 20
            "Advanced" -> 30
            else -> 10
        }
        
        val totalPoints = basePoints * exercises.size
        Log.d(TAG, "Calculated points - Base: $basePoints, Total: $totalPoints")

        val progress = UserProgressEntity(
            date = currentDate,
            bodyPart = bodyPart,
            level = level,
            completed = true,
            points = totalPoints,
            completedAt = completedAt,
            pointsEarned = totalPoints
        )
        Log.d(TAG, "Created progress entity: $progress")

        try {
            if (!::dbHelper.isInitialized || !::userProgressDao.isInitialized) {
                throw IllegalStateException("Database helper or DAO not initialized")
            }

            val beforePoints = userProgressDao.getTotalPoints()
            Log.d(TAG, "Points before update: $beforePoints")
            
            userProgressDao.insertProgress(progress)
            
            val afterPoints = userProgressDao.getTotalPoints()
            Log.d(TAG, "Points after update: $afterPoints")

            if (afterPoints <= beforePoints) {
                Log.w(TAG, "Points did not increase as expected")
            }

            Toast.makeText(
                this, 
                "Workout completed! Earned $totalPoints points!", 
                Toast.LENGTH_SHORT
            ).show()
            
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving progress", e)
            Toast.makeText(
                this,
                "Error saving progress: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}
