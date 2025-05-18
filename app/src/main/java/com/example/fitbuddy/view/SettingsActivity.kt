package com.example.fitbuddy.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // TODO: Implement settings UI and logic
    }
}
