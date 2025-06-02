package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.databinding.ActivityOnboardingBinding
import kotlinx.coroutines.*

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var dbHelper: FitBuddyDbHelper
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
        if (!prefs.getBoolean("database_initialized", false)) {
            initializeDatabase()
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun initializeDatabase() {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("DB", "Initializing database...")
                    dbHelper = FitBuddyDbHelper(this@OnboardingActivity)
                    
                    val db = dbHelper.writableDatabase
                    Log.d("DB", "Database initialized at ${db.path}")

                    getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("database_initialized", true)
                        .apply()
                }
            } catch (e: Exception) {
                Log.e("DB", "Error during database initialization: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }
}