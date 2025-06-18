package com.example.fitbuddy.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import android.widget.Toast
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.UserProgressDao
import com.example.fitbuddy.data.UserProgressEntity
import com.example.fitbuddy.model.Exercise
import java.text.SimpleDateFormat
import java.util.*
import java.io.File
import com.google.firebase.auth.FirebaseAuth
import android.content.ContentValues
import com.google.firebase.database.FirebaseDatabase

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

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

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
        videoView.setZOrderOnTop(true)
        if (!exercise.videoResourceName.isNullOrEmpty()) {
            try {
                val resourceName = exercise.videoResourceName
                val videoResId = resources.getIdentifier(resourceName, "raw", packageName)
                Log.d(TAG, "Trying to load video: $resourceName, resource ID: $videoResId")
                if (videoResId != 0) {
                    val videoUri = Uri.parse("android.resource://$packageName/$videoResId")
                    videoView.setVideoURI(videoUri)
                    videoView.requestFocus()
                    videoView.setOnPreparedListener { mp ->
                        mp.isLooping = true
                        videoView.start()
                    }
                    videoView.setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "Video error - what: $what, extra: $extra")
                        Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show()
                        true
                    }
                } else {
                    Log.e(TAG, "Video resource not found: $resourceName")
                    Toast.makeText(this, "Video not found", Toast.LENGTH_SHORT).show()
                    videoView.stopPlayback()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading video for ${exercise.name}: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this, "Error loading video", Toast.LENGTH_SHORT).show()
                videoView.stopPlayback()
            }
        } else {
            Log.d(TAG, "No video resource for exercise: ${exercise.name}")
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
        
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        var username = "User"
        
        if (uid != null) {
            try {
                val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
                userRef.get().addOnSuccessListener { snapshot ->
                    username = snapshot.child("name").getValue(String::class.java) ?: "User"
                    Log.d(TAG, "Retrieved username from Firebase: $username")
                    
                    userProgressDao.setCurrentUserId(username)
                    
                    saveProgressWithUsername(username, currentDate, completedAt, totalPoints)
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error getting username from Firebase", e)
                    userProgressDao.setCurrentUserId(username)
                    saveProgressWithUsername(username, currentDate, completedAt, totalPoints)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error accessing Firebase", e)
                userProgressDao.setCurrentUserId(username)
                saveProgressWithUsername(username, currentDate, completedAt, totalPoints)
            }
        } else {
            userProgressDao.setCurrentUserId(username)
            saveProgressWithUsername(username, currentDate, completedAt, totalPoints)
        }
    }

    private fun saveProgressWithUsername(username: String, currentDate: String, completedAt: String, totalPoints: Int) {
        Log.d(TAG, "Saving progress with username: $username")
        
        val progress = UserProgressEntity(
            userId = username,
            date = currentDate,
            bodyPart = bodyPart,
            level = level,
            completed = true,
            points = totalPoints,
            completedAt = completedAt,
            pointsEarned = totalPoints
        )

        try {
            if (!::dbHelper.isInitialized || !::userProgressDao.isInitialized) {
                throw IllegalStateException("Database helper or DAO not initialized")
            }

            val beforePoints = userProgressDao.getTotalPoints()
            Log.d(TAG, "Points before update: $beforePoints")
            
            userProgressDao.insertProgress(progress)
            
            val afterPoints = userProgressDao.getTotalPoints()
            Log.d(TAG, "Points after update: $afterPoints, points increased by: ${afterPoints - beforePoints}")

            runOnUiThread {
                Toast.makeText(
                    this, 
                    "Workout completed! Earned $totalPoints points!", 
                    Toast.LENGTH_SHORT
                ).show()
                
                if (intent.getBooleanExtra("isDaily", false)) {
                    val prefs = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
                    prefs.edit().putString("last_daily_task_date", currentDate).apply()
                    setResult(RESULT_OK)
                }
                
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving workout progress", e)
            runOnUiThread {
                Toast.makeText(this, "Error saving progress: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
        if (::videoView.isInitialized) {
            videoView.stopPlayback()
        }
    }
}
