package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.fitbuddy.R
import com.example.fitbuddy.databinding.ActivityRegister1Binding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegister1Binding
    private var selectedGender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        setupGenderSelection()
        setupContinueButton()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupGenderSelection() {
        binding.maleOption.setOnClickListener {
            updateGenderSelection("male", binding.maleOption, binding.femaleOption)
        }

        binding.femaleOption.setOnClickListener {
            updateGenderSelection("female", binding.femaleOption, binding.maleOption)
        }
    }

    private fun updateGenderSelection(
        gender: String,
        selectedOption: LinearLayout,
        unselectedOption: LinearLayout
    ) {
        selectedGender = gender
        (selectedOption.getChildAt(0) as CardView).setCardBackgroundColor(getColor(R.color.gold_yellow))
        (unselectedOption.getChildAt(0) as CardView).setCardBackgroundColor(getColor(R.color.transparent_white))
    }

    private fun setupContinueButton() {
        binding.btnContinue.setOnClickListener {
            if (validateInputs()) {
                val intent = Intent(this, RegisterActivity2::class.java).apply {
                    putExtra("gender", selectedGender)
                    putExtra("age", binding.etAge.text.toString().toInt())
                    putExtra("weight", binding.etWeight.text.toString().toFloat())
                    putExtra("height", binding.etHeight.text.toString().toFloat())
                }
                startActivity(intent)
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (selectedGender == null) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
            return false
        }

        val age = binding.etAge.text.toString().toIntOrNull()
        if (age == null || age < 13 || age > 100) {
            Toast.makeText(this, "Please enter a valid age (13-100)", Toast.LENGTH_SHORT).show()
            return false
        }

        val weight = binding.etWeight.text.toString().toFloatOrNull()
        if (weight == null || weight < 20 || weight > 300) {
            Toast.makeText(this, "Please enter a valid weight (20-300 kg)", Toast.LENGTH_SHORT).show()
            return false
        }

        val height = binding.etHeight.text.toString().toFloatOrNull()
        if (height == null || height < 100 || height > 250) {
            Toast.makeText(this, "Please enter a valid height (100-250 cm)", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
