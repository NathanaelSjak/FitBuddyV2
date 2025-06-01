package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.model.Exercise

class ExercisesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnStart: Button
    private var exercises: List<Exercise> = emptyList()
    private var level: String = "Beginner"
    private var bodyPart: String = "Abs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        recyclerView = findViewById(R.id.recyclerExercises)
        btnStart = findViewById(R.id.btnStartWorkout)

        val tvTitle = findViewById<android.widget.TextView>(R.id.tvTitle)

        level = intent.getStringExtra("level") ?: "Beginner"
        bodyPart = intent.getStringExtra("bodyPart") ?: "Abs"
        android.util.Log.d("ExercisesActivity", "level: $level, bodyPart: $bodyPart")
        android.widget.Toast.makeText(this, "Level: $level, BodyPart: $bodyPart", android.widget.Toast.LENGTH_SHORT).show()
        try {
            exercises = getDummyExercises(level, bodyPart)
            if (exercises.isEmpty()) {
                android.widget.Toast.makeText(this, "No exercises found for this selection.", android.widget.Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = ExerciseAdapter(exercises)
        } catch (e: Exception) {
            android.util.Log.e("ExercisesActivity", "Error: ", e)
            android.widget.Toast.makeText(this, "Error: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }

        tvTitle.text = "$bodyPart - $level"

        btnStart.setOnClickListener {
            val intent = Intent(this, ExerciseStepActivity::class.java)
            intent.putExtra("exercises", ArrayList(exercises))
            startActivity(intent)
        }
    }

    private fun getDummyExercises(level: String, bodyPart: String): List<Exercise> {
        return when (bodyPart) {
            "Abs" -> when (level) {
                "Beginner" -> listOf(
                    Exercise("Crunches", "x10", "android.resource://com.example.fitbuddy/raw/sample", R.drawable.ic_abs),
                    Exercise("Plank", "00:20", "", R.drawable.ic_abs)
                )
                "Intermediate" -> listOf(
                    Exercise("Bicycle Crunches", "x15", "", R.drawable.ic_abs),
                    Exercise("Leg Raises", "x12", "", R.drawable.ic_abs)
                )
                else -> listOf(
                    Exercise("V-Ups", "x20", "", R.drawable.ic_abs),
                    Exercise("Hanging Leg Raise", "x15", "", R.drawable.ic_abs)
                )
            }
            "Chest" -> when (level) {
                "Beginner" -> listOf(
                    Exercise("Knee Push-Ups", "x8", "", R.drawable.ic_chest),
                    Exercise("Incline Push-Ups", "x10", "", R.drawable.ic_chest)
                )
                "Intermediate" -> listOf(
                    Exercise("Push-Ups", "x15", "", R.drawable.ic_chest),
                    Exercise("Decline Push-Ups", "x12", "", R.drawable.ic_chest)
                )
                else -> listOf(
                    Exercise("Diamond Push-Ups", "x20", "", R.drawable.ic_chest),
                    Exercise("Archer Push-Ups", "x10", "", R.drawable.ic_chest)
                )
            }
            "Arms" -> when (level) {
                "Beginner" -> listOf(
                    Exercise("Tricep Dips", "x10", "", R.drawable.ic_arms),
                    Exercise("Wall Push-Ups", "x12", "", R.drawable.ic_arms)
                )
                "Intermediate" -> listOf(
                    Exercise("Diamond Push-Ups", "x12", "", R.drawable.ic_arms),
                    Exercise("Close Grip Push-Ups", "x15", "", R.drawable.ic_arms)
                )
                else -> listOf(
                    Exercise("One Arm Push-Ups", "x8", "", R.drawable.ic_arms),
                    Exercise("Bench Dips", "x20", "", R.drawable.ic_arms)
                )
            }
            "Legs" -> when (level) {
                "Beginner" -> listOf(
                    Exercise("Squats", "x15", "", R.drawable.ic_legs),
                    Exercise("Lunges", "x10", "", R.drawable.ic_legs)
                )
                "Intermediate" -> listOf(
                    Exercise("Jump Squats", "x12", "", R.drawable.ic_legs),
                    Exercise("Bulgarian Split Squat", "x10", "", R.drawable.ic_legs)
                )
                else -> listOf(
                    Exercise("Pistol Squats", "x8", "", R.drawable.ic_legs),
                    Exercise("Box Jumps", "x15", "", R.drawable.ic_legs)
                )
            }
            "Back" -> when (level) {
                "Beginner" -> listOf(
                    Exercise("Superman", "x12", "", R.drawable.ic_back),
                    Exercise("Reverse Snow Angels", "x10", "", R.drawable.ic_back)
                )
                "Intermediate" -> listOf(
                    Exercise("Pull-Ups", "x8", "", R.drawable.ic_back),
                    Exercise("Inverted Rows", "x10", "", R.drawable.ic_back)
                )
                else -> listOf(
                    Exercise("Archer Pull-Ups", "x6", "", R.drawable.ic_back),
                    Exercise("One Arm Rows", "x10", "", R.drawable.ic_back)
                )
            }
            else -> emptyList()
        }
    }
}
