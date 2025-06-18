package com.example.fitbuddy.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitbuddy.adapter.StatsHistoryAdapter
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.UserProgressDao
import com.example.fitbuddy.data.UserStatsDao
import com.example.fitbuddy.data.UserStatsEntity
import com.example.fitbuddy.databinding.ActivityStatsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding
    private lateinit var userStatsDao: UserStatsDao
    private lateinit var userProgressDao: UserProgressDao
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = FitBuddyDbHelper(this)
        userStatsDao = UserStatsDao(dbHelper)
        userProgressDao = UserProgressDao(dbHelper)
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
            userRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("name").getValue(String::class.java) ?: "User"
                userStatsDao.setCurrentUserId(username)
                userProgressDao.setCurrentUserId(username)
                Log.d("StatsActivity", "Using username: $username")
                
                loadTodayStats()
                loadStatsHistory()
                updateCharts()
            }.addOnFailureListener {
                userStatsDao.setCurrentUserId("User")
                userProgressDao.setCurrentUserId("User")
                Log.d("StatsActivity", "Failed to get username, using default: User")
                loadTodayStats()
                loadStatsHistory()
                updateCharts()
            }
        } else {
            userStatsDao.setCurrentUserId("User")
            userProgressDao.setCurrentUserId("User")
            Log.d("StatsActivity", "No user logged in, using default: User")
        }

        setupViews()
        setupCharts()
        loadTodayStats()
        loadStatsHistory()
        setupBottomNavigation()
    }

    private fun setupViews() {
        binding.saveButton.setOnClickListener {
            saveStats()
        }

        binding.progressRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupCharts() {
        setupChart(binding.weightChart, "Weight Progress")
        setupChart(binding.heightChart, "Height Progress")
        updateCharts()
    }

    private fun setupChart(chart: com.github.mikephil.charting.charts.LineChart, label: String) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                setDrawGridLines(false)
            }
            
            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                gridColor = Color.argb(50, 255, 255, 255)
            }
            
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
        }
    }

    private fun updateCharts() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val startDate = dateFormat.format(calendar.time)
        val endDate = dateFormat.format(Date())
        
        val statsHistory = userStatsDao.getStatsForDateRange(startDate, endDate)
        
        val weightEntries = statsHistory.mapIndexed { index, stat ->
            Entry(index.toFloat(), stat.weight)
        }
        val weightDataSet = LineDataSet(weightEntries, "Weight (kg)").apply {
            color = Color.YELLOW
            setCircleColor(Color.YELLOW)
            valueTextColor = Color.WHITE
        }
        binding.weightChart.data = LineData(weightDataSet)
        
        val heightEntries = statsHistory.mapIndexed { index, stat ->
            Entry(index.toFloat(), stat.height)
        }
        val heightDataSet = LineDataSet(heightEntries, "Height (cm)").apply {
            color = Color.CYAN
            setCircleColor(Color.CYAN)
            valueTextColor = Color.WHITE
        }
        binding.heightChart.data = LineData(heightDataSet)

        val dates = statsHistory.map { displayDateFormat.format(dateFormat.parse(it.date)!!) }
        binding.weightChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        binding.heightChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)

        binding.weightChart.invalidate()
        binding.heightChart.invalidate()
    }

    private fun saveStats() {
        val height = binding.heightInput.text.toString().toFloatOrNull()
        val weight = binding.weightInput.text.toString().toFloatOrNull()

        if (height != null && weight != null) {
            val stats = UserStatsEntity(
                date = dateFormat.format(Date()),
                height = height,
                weight = weight
            )
            userStatsDao.insertStats(stats)
            updateCharts()
            Toast.makeText(this, "Stats saved successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enter valid height and weight", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTodayStats() {
        val today = dateFormat.format(Date())
        val stats = userStatsDao.getStatsByDate(today)
        
        stats?.let {
            binding.heightInput.setText(it.height.toString())
            binding.weightInput.setText(it.weight.toString())
        }
    }

    private fun loadStatsHistory() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val startDate = dateFormat.format(calendar.time)
        val endDate = dateFormat.format(Date())
        
        val statsHistory = userStatsDao.getStatsForDateRange(startDate, endDate)
        val adapter = StatsHistoryAdapter(statsHistory)
        binding.progressRecyclerView.adapter = adapter
    }

    private fun setupBottomNavigation() {
        binding.bottomNavBar.root.apply {
            findViewById<android.widget.ImageView>(com.example.fitbuddy.R.id.navHome)?.setOnClickListener {
                startActivity(Intent(this@StatsActivity, HomeActivity::class.java))
                finish()
            }
            findViewById<android.widget.ImageView>(com.example.fitbuddy.R.id.navCalender)?.setOnClickListener {
                startActivity(Intent(this@StatsActivity, CalendarActivity::class.java))
                finish()
            }
            findViewById<android.widget.ImageView>(com.example.fitbuddy.R.id.navStats)?.setOnClickListener {
            }
            findViewById<android.widget.ImageView>(com.example.fitbuddy.R.id.navProfile)?.setOnClickListener {
                startActivity(Intent(this@StatsActivity, ProfileActivity::class.java))
                finish()
            }
        }
    }
} 