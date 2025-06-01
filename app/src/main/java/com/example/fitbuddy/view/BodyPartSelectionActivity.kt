package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R

class BodyPartSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_part_selection)

        val level = intent.getStringExtra("level") ?: "Beginner"

        findViewById<TextView>(R.id.tvAbs).setOnClickListener {
            openExercises(level, "Abs")
        }
        findViewById<TextView>(R.id.tvChest).setOnClickListener {
            openExercises(level, "Chest")
        }
        findViewById<TextView>(R.id.tvArms).setOnClickListener {
            openExercises(level, "Arms")
        }
        findViewById<TextView>(R.id.tvLegs).setOnClickListener {
            openExercises(level, "Legs")
        }
    }

    private fun openExercises(level: String, bodyPart: String) {
        val intent = Intent(this, ExercisesActivity::class.java)
        intent.putExtra("level", level)
        intent.putExtra("bodyPart", bodyPart)
        startActivity(intent)
    }
}
