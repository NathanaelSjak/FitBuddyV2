package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitbuddy.R
import com.example.fitbuddy.databinding.ActivityBottomNavBarBinding
import com.example.fitbuddy.databinding.ActivityProfileBinding

class BottomNavBarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBottomNavBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}