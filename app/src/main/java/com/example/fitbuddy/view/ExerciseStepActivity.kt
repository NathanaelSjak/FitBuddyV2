package com.example.fitbuddy.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_step)

        tvTitle = findViewById(R.id.tvExerciseName)
        videoView = findViewById(R.id.videoExercise)
        tvRepsOrTime = findViewById(R.id.tvRepsOrTime)
        btnNext = findViewById(R.id.btnNextStep)
        btnSkip = findViewById(R.id.btnSkip)

        val dbHelper = FitBuddyDbHelper(this)
        userProgressDao = UserProgressDao(dbHelper)

        exercises = intent.getSerializableExtra("exercises") as? ArrayList<Exercise> ?: arrayListOf()
        bodyPart = intent.getStringExtra("bodyPart") ?: ""
        level = intent.getStringExtra("level") ?: ""
        
        showStep(currentStep)

        btnNext.setOnClickListener {
            if (currentStep < exercises.size - 1) {
                currentStep++
                showStep(currentStep)
            } else {
                saveWorkoutProgress()
                finish()
            }
        }
        btnSkip.setOnClickListener {
            finish()
        }
    }

    private fun showStep(step: Int) {
        val exercise = exercises[step]
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
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val completedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val progress = UserProgressEntity(
            id = 0,
            date = currentDate,
            bodyPart = bodyPart,
            level = level,
            completed = true,
            points = 10,
            completedAt = completedAt,
            pointsEarned = 10
        )

        userProgressDao.insertProgress(progress)
        Toast.makeText(this, "Workout progress saved!", Toast.LENGTH_SHORT).show()
    }

}
