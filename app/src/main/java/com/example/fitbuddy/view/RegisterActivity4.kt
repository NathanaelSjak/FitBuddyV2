package com.example.fitbuddy.view

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.databinding.ActivityRegister4Binding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity4 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister4Binding
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        setupDatePicker()
        setupUpdateProfileButton()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupDatePicker() {
        binding.etBirthDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                binding.etBirthDate.setText(dateFormatter.format(calendar.time))
            },
            year,
            month,
            day
        ).show()
    }

    private fun setupUpdateProfileButton() {
        binding.btnUpdateProfile.setOnClickListener {
            if (validateInput()) {
                val intent = Intent(this, RegisterActivity5::class.java).apply {
                    // Pass previous data
                    putExtra("gender", getIntent().getStringExtra("gender"))
                    putExtra("age", getIntent().getIntExtra("age", 0))
                    putExtra("weight", getIntent().getFloatExtra("weight", 0f))
                    putExtra("height", getIntent().getFloatExtra("height", 0f))
                    putExtra("fitnessGoal", getIntent().getStringExtra("fitnessGoal"))
                    putExtra("experienceLevel", getIntent().getStringExtra("experienceLevel"))
                    // Add new data
                    putExtra("name", binding.etFullName.text.toString())
                    putExtra("birthDate", binding.etBirthDate.text.toString())
                    putExtra("email", binding.etEmail.text.toString())
                }
                startActivity(intent)
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.etFullName.text.toString()
        if (name.isEmpty()) {
            binding.etFullName.error = "Name is required"
            return false
        }

        val birthDate = binding.etBirthDate.text.toString()
        if (birthDate.isEmpty()) {
            binding.etBirthDate.error = "Birth date is required"
            return false
        }

        val email = binding.etEmail.text.toString()
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            return false
        }

        return true
    }
}
