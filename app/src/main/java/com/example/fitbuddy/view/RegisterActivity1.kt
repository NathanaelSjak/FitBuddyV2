package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.databinding.ActivityRegister1Binding

class RegisterActivity1 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister1Binding
    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.maleOption.setOnClickListener {
            selectedGender = "Male"
            updateGenderSelection()
        }

        binding.femaleOption.setOnClickListener {
            selectedGender = "Female"
            updateGenderSelection()
        }

        binding.btnContinue.setOnClickListener {
            if (validateInput()) {
                val intent = Intent(this, RegisterActivity2::class.java).apply {
                    putExtra("gender", selectedGender)
                    putExtra("age", binding.etAge.text.toString().toIntOrNull() ?: 0)
                    putExtra("weight", binding.etWeight.text.toString().toIntOrNull() ?: 0)
                    putExtra("height", binding.etHeight.text.toString().toIntOrNull() ?: 0)
                }
                startActivity(intent)
            }
        }
    }

    private fun validateInput(): Boolean {
        if (selectedGender == null) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
            return false
        }

        val age = binding.etAge.text.toString().toIntOrNull()
        if (age == null || age <= 0) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
            return false
        }

        val weight = binding.etWeight.text.toString().toIntOrNull()
        if (weight == null || weight <= 0) {
            Toast.makeText(this, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
            return false
        }

        val height = binding.etHeight.text.toString().toIntOrNull()
        if (height == null || height <= 0) {
            Toast.makeText(this, "Please enter a valid height", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updateGenderSelection() {
        binding.maleOption.setBackgroundColor(
            resources.getColor(
                if (selectedGender == "Male") R.color.gold_yellow else R.color.transparent_white,
                theme
            )
        )

        binding.femaleOption.setBackgroundColor(
            resources.getColor(
                if (selectedGender == "Female") R.color.gold_yellow else R.color.transparent_white,
                theme
            )
        )
    }
}
