package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.UserProgressDao
import com.example.fitbuddy.databinding.ActivityCalendarBinding
import com.example.fitbuddy.adapter.WorkoutHistoryAdapter
import com.example.fitbuddy.model.WorkoutHistory
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var dbHelper: FitBuddyDbHelper
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var selectedDate: String
    private lateinit var adapter: WorkoutHistoryAdapter
    private lateinit var calendarView: RecyclerView
    private lateinit var exerciseHistoryRecyclerView: RecyclerView
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = FitBuddyDbHelper(this)
        userProgressDao = UserProgressDao(dbHelper)

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        selectedDate = currentDate
        
        setupRecyclerView()
        
        setupCalendar()
        
        loadWorkoutHistory(selectedDate)
        
        setupBottomNavigation()
    }

    private fun setupCalendar() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            binding.tvSelectedDate.text = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(calendar.time)
            
            loadWorkoutHistory(selectedDate)
        }
        
        binding.tvSelectedDate.text = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date())
    }

    private fun setupRecyclerView() {
        adapter = WorkoutHistoryAdapter(emptyList())
        binding.rvWorkoutHistory.layoutManager = LinearLayoutManager(this)
        binding.rvWorkoutHistory.adapter = adapter
    }

    private fun loadWorkoutHistory(date: String) {
        try {
            val progressList = userProgressDao.getProgressForDate(date)
            if (progressList.isEmpty()) {
                binding.tvNoWorkouts.visibility = android.view.View.VISIBLE
                binding.rvWorkoutHistory.visibility = android.view.View.GONE
            } else {
                binding.tvNoWorkouts.visibility = android.view.View.GONE
                binding.rvWorkoutHistory.visibility = android.view.View.VISIBLE
                
                val workoutHistory = progressList.map { progress ->
                    WorkoutHistory(
                        progress.bodyPart,
                        progress.level,
                        progress.completed,
                        progress.completedAt ?: "",
                        progress.pointsEarned
                    )
                }
                adapter.updateData(workoutHistory)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading workout history", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<android.view.View>(com.example.fitbuddy.R.id.bottomNavBar)
        if (bottomNav != null) {
            val navHome =
                bottomNav.findViewById<android.widget.ImageView>(com.example.fitbuddy.R.id.navHome)
            val navCalender =
                bottomNav.findViewById<android.widget.ImageView>(com.example.fitbuddy.R.id.navCalender)
            val navStats =
                bottomNav.findViewById<android.widget.ImageView>(com.example.fitbuddy.R.id.navStats)
            val navProfile =
                bottomNav.findViewById<android.widget.ImageView>(com.example.fitbuddy.R.id.navProfile)

            navHome?.setOnClickListener {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }

            navCalender?.setOnClickListener {
                startActivity(Intent(this, CalendarActivity::class.java))
                finish()
            }

            navStats?.setOnClickListener {
                startActivity(Intent(this, StatsActivity::class.java))
                finish()
            }
            navProfile?.setOnClickListener {
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            }
        }
    }
}
