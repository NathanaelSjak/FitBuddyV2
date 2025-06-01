package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.databinding.ActivityRegister2Binding

class RegisterActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister2Binding
    private var selectedGoal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Set up goal selection radio buttons
        binding.goalOptions.setOnCheckedChangeListener { _, checkedId ->
            selectedGoal = when (checkedId) {
                R.id.rbLoseWeight -> "Lose Weight"
                R.id.rbGainWeight -> "Gain Weight"
                R.id.rbMuscleMassGain -> "Muscle Mass Gain"
                R.id.rbShapeBody -> "Shape Body"
                R.id.rbOthers -> "Others"
                else -> null
            }
        }

        binding.btnContinue.setOnClickListener {
            if (selectedGoal != null) {
                val intent = Intent(this, RegisterActivity3::class.java).apply {
                    // Pass previous data
                    putExtra("gender", intent.getStringExtra("gender"))
                    putExtra("age", intent.getIntExtra("age", 0))
                    putExtra("weight", intent.getFloatExtra("weight", 0f))
                    putExtra("height", intent.getFloatExtra("height", 0f))
                    // Add new data
                    putExtra("fitnessGoal", selectedGoal)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select a fitness goal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
