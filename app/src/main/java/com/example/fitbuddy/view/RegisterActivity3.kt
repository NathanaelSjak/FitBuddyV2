package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.databinding.ActivityRegister3Binding

class RegisterActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister3Binding
    private var selectedExperience: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnBeginner.setOnClickListener {
            selectedExperience = "Beginner"
            updateButtonStates()
        }

        binding.btnIntermediate.setOnClickListener {
            selectedExperience = "Intermediate"
            updateButtonStates()
        }

        binding.btnAdvanced.setOnClickListener {
            selectedExperience = "Advanced"
            updateButtonStates()
        }

        binding.btnContinue.setOnClickListener {
            if (selectedExperience != null) {
                val intent = Intent(this, RegisterActivity4::class.java).apply {
                    // Pass previous data
                    putExtra("gender", intent.getStringExtra("gender"))
                    putExtra("age", intent.getIntExtra("age", 0))
                    putExtra("weight", intent.getFloatExtra("weight", 0f))
                    putExtra("height", intent.getFloatExtra("height", 0f))
                    putExtra("fitnessGoal", intent.getStringExtra("fitnessGoal"))
                    // Add new data
                    putExtra("experienceLevel", selectedExperience)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select your experience level", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateButtonStates() {
        binding.btnBeginner.setBackgroundColor(
            resources.getColor(
                if (selectedExperience == "Beginner") R.color.gold_yellow else R.color.transparent_white,
                theme
            )
        )
        binding.btnBeginner.setTextColor(
            resources.getColor(
                if (selectedExperience == "Beginner") R.color.navy_blue else R.color.white,
                theme
            )
        )

        binding.btnIntermediate.setBackgroundColor(
            resources.getColor(
                if (selectedExperience == "Intermediate") R.color.gold_yellow else R.color.transparent_white,
                theme
            )
        )
        binding.btnIntermediate.setTextColor(
            resources.getColor(
                if (selectedExperience == "Intermediate") R.color.navy_blue else R.color.white,
                theme
            )
        )

        binding.btnAdvanced.setBackgroundColor(
            resources.getColor(
                if (selectedExperience == "Advanced") R.color.gold_yellow else R.color.transparent_white,
                theme
            )
        )
        binding.btnAdvanced.setTextColor(
            resources.getColor(
                if (selectedExperience == "Advanced") R.color.navy_blue else R.color.white,
                theme
            )
        )
    }
}
