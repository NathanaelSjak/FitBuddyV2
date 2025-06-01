package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitbuddy.R
import com.example.fitbuddy.databinding.ActivityBottomNavBarBinding

class BottomNavBarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBottomNavBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavClickListeners()
    }
    
    private fun setupNavClickListeners() {
        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        
        binding.navPlay.setOnClickListener {
            Toast.makeText(this, "Play feature coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.navStats.setOnClickListener {
            Toast.makeText(this, "Stats feature coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}