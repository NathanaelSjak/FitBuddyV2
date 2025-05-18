package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitbuddy.R
import com.example.fitbuddy.data.model.User
import com.example.fitbuddy.data.repository.AuthRepository
import com.example.fitbuddy.databinding.ActivityRegister5Binding
import kotlinx.coroutines.launch

class RegisterActivity5 : AppCompatActivity() {

    private lateinit var binding: ActivityRegister5Binding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegister5Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        setupPasswordValidation()

        binding.btnCreateAccount.setOnClickListener {
            if (validatePasswords()) {
                registerUser()
            }
        }
    }

    private fun setupPasswordValidation() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updatePasswordStrength(s.toString())
                validatePasswordMatch()
            }
        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validatePasswordMatch()
            }
        })
    }

    private fun updatePasswordStrength(password: String) {
        // Check password requirements
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasNumber = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        // Update requirement indicators
        binding.ivLengthCheck.visibility = if (hasMinLength) View.VISIBLE else View.INVISIBLE
        binding.ivUppercaseCheck.visibility = if (hasUppercase) View.VISIBLE else View.INVISIBLE
        binding.ivNumberCheck.visibility = if (hasNumber) View.VISIBLE else View.INVISIBLE
        binding.ivSpecialCharCheck.visibility = if (hasSpecialChar) View.VISIBLE else View.INVISIBLE

        // Calculate password strength
        var strength = 0
        if (hasMinLength) strength += 25
        if (hasUppercase) strength += 25
        if (hasNumber) strength += 25
        if (hasSpecialChar) strength += 25

        // Update progress bar
        binding.passwordStrengthBar.progress = strength

        // Update strength text
        val strengthText = when {
            strength < 25 -> "Weak"
            strength < 50 -> "Fair"
            strength < 75 -> "Good"
            else -> "Strong"
        }

        binding.tvPasswordStrength.text = strengthText

        // Update strength text color
        val strengthColor = when {
            strength < 25 -> resources.getColor(android.R.color.holo_red_light, theme)
            strength < 50 -> resources.getColor(android.R.color.holo_orange_light, theme)
            strength < 75 -> resources.getColor(android.R.color.holo_green_light, theme)
            else -> resources.getColor(R.color.gold_yellow, theme)
        }

        binding.tvPasswordStrength.setTextColor(strengthColor)
    }

    private fun validatePasswordMatch(): Boolean {
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        val doMatch = password == confirmPassword

        if (!doMatch && confirmPassword.isNotEmpty()) {
            binding.confirmPasswordLayout.error = "Passwords do not match"
        } else {
            binding.confirmPasswordLayout.error = null
        }

        return doMatch
    }

    private fun validatePasswords(): Boolean {
        val password = binding.etPassword.text.toString()

        // Check password requirements
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasNumber = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        val isStrongEnough = hasMinLength && hasUppercase && hasNumber && hasSpecialChar

        if (!isStrongEnough) {
            binding.passwordLayout.error = "Password doesn't meet all requirements"
            return false
        }

        binding.passwordLayout.error = null

        return validatePasswordMatch()
    }

    private fun registerUser() {
        val email = intent.getStringExtra("email") ?: run {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            return
        }
        val password = binding.etPassword.text.toString()

        // Create user object from intent extras
        val user = User(
            name = intent.getStringExtra("name") ?: "",
            email = email,
            gender = intent.getStringExtra("gender") ?: "",
            age = intent.getIntExtra("age", 0),
            fitnessGoal = intent.getStringExtra("fitnessGoal") ?: "",
            experienceLevel = intent.getStringExtra("experienceLevel") ?: "",
            height = intent.getFloatExtra("height", 0f),
            weight = intent.getFloatExtra("weight", 0f),
            birthDate = intent.getStringExtra("birthDate") ?: ""
        )

        lifecycleScope.launch {
            try {
                binding.btnCreateAccount.isEnabled = false
                val result = authRepository.registerUser(email, password, user)
                result.fold(
                    onSuccess = { registeredUser ->
                        // Navigate to home screen
                        Toast.makeText(
                            this@RegisterActivity5,
                            "success", Toast.LENGTH_SHORT
                        )
//                        startActivity(Intent(this@RegisterActivity5, HomeActivity::class.java))
                        finishAffinity()
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            this@RegisterActivity5,
                            "Registration failed: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity5,
                    "Registration failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.btnCreateAccount.isEnabled = true
            }
        }
    }
}
