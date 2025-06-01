package com.example.fitbuddy.view

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.model.Exercise

class ExerciseStepActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var videoView: VideoView
    private lateinit var tvRepsOrTime: TextView
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button
    private var currentStep = 0
    private var exercises: ArrayList<Exercise> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_step)

        tvTitle = findViewById(R.id.tvExerciseName)
        videoView = findViewById(R.id.videoExercise)
        tvRepsOrTime = findViewById(R.id.tvRepsOrTime)
        btnNext = findViewById(R.id.btnNextStep)
        btnSkip = findViewById(R.id.btnSkip)

        exercises = intent.getSerializableExtra("exercises") as? ArrayList<Exercise> ?: arrayListOf()
        showStep(currentStep)

        btnNext.setOnClickListener {
            if (currentStep < exercises.size - 1) {
                currentStep++
                showStep(currentStep)
            } else {
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
}
