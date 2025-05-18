package com.example.fitbuddy.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitbuddy.R
import com.example.fitbuddy.data.repository.AuthRepository
import com.example.fitbuddy.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.checkCurrentPasswordButton.setOnClickListener {
            val currentPassword = binding.etCurrentPassword.text.toString()

            authRepository.checkCurrentPassword(currentPassword)
                .addOnSuccessListener {
                    Toast.makeText(this, "Current password is correct", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Incorrect password: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.changePasswordButton.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val currentPassword = binding.etCurrentPassword.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authRepository.checkCurrentPassword(currentPassword)
                .addOnSuccessListener {
                    if (newPassword == currentPassword) {
                        Toast.makeText(this, "New password cannot be the same as current password", Toast.LENGTH_SHORT).show()
                    } else {
                        authRepository.updatePassword(newPassword)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to change password: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Incorrect current password: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }


    }
}