package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.data.DifficultyLevel
import com.example.fitbuddy.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.UserProgressDao
import com.example.fitbuddy.data.ExerciseDao
import com.example.fitbuddy.data.ExerciseEntity
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.app.Dialog

class HomeActivity : AppCompatActivity() {
    private var selectedLevel: String = "Beginner"
    private lateinit var binding: ActivityHomeBinding
    private lateinit var dbHelper: FitBuddyDbHelper
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var exerciseDao: ExerciseDao
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database
        dbHelper = FitBuddyDbHelper(this)
        userProgressDao = UserProgressDao(dbHelper)
        exerciseDao = ExerciseDao(dbHelper)

        initializeViews()
        setupBottomNavigation()
        fetchUserData()
        loadProgressData()
        updateUI()

        setupLevelSelection()
        setupWorkoutCards()
        setupWeeklyTargetButton()
        setupViews()
    }

    private fun initializeViews() {
        // Initialize views using binding
        binding.apply {
            // Set up click listeners and other view initialization
        }
    }

    private fun setupLevelSelection() {
        binding.apply {
            tvBeginnerLevel.setOnClickListener {
                selectedLevel = "Beginner"
                highlightLevel(tvBeginnerLevel, tvIntermediateLevel, tvAdvancedLevel)
            }
            tvIntermediateLevel.setOnClickListener {
                selectedLevel = "Intermediate"
                highlightLevel(tvIntermediateLevel, tvBeginnerLevel, tvAdvancedLevel)
            }
            tvAdvancedLevel.setOnClickListener {
                selectedLevel = "Advanced"
                highlightLevel(tvAdvancedLevel, tvBeginnerLevel, tvIntermediateLevel)
            }
        }
    }

    private fun setupWorkoutCards() {
        setBodyPartClickListener(R.id.absWorkoutCard, "Abs")
        setBodyPartClickListener(R.id.chestWorkoutCard, "Chest")
        setBodyPartClickListener(R.id.arms2WorkoutCard, "Arms")
        setBodyPartClickListener(R.id.backWorkoutCard, "Back")
        setBodyPartClickListener(R.id.legsWorkoutCard, "Legs")
    }

    private fun setupWeeklyTargetButton() {
        binding.btnSetTarget.setOnClickListener {
            showWeeklyTargetDialog()
        }
    }

    private fun showWeeklyTargetDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_weekly_target)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etWorkoutTarget = dialog.findViewById<TextInputEditText>(R.id.etWorkoutTarget)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)

        val currentTarget = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
            .getInt("weekly_target", 0)
        etWorkoutTarget.setText(if (currentTarget > 0) currentTarget.toString() else "")

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val target = etWorkoutTarget.text.toString().toIntOrNull()
            if (target != null && target > 0) {
                getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
                    .edit()
                    .putInt("weekly_target", target)
                    .apply()
                updateWeeklyProgress()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateWeeklyProgress() {
        val target = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
            .getInt("weekly_target", 0)
        
        if (target > 0) {
            val calendar = Calendar.getInstance()
            var weeklyCompleted = 0
            
            for (i in 0..6) {
                val date = dateFormat.format(calendar.time)
                val dayProgress = userProgressDao.getProgressForDate(date)
                weeklyCompleted += dayProgress.count { it.completed }
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
            
            val weeklyPercentage = (weeklyCompleted * 100) / target
            binding.weeklyProgressIndicator.progress = weeklyPercentage
            binding.tvWeeklyProgressPercent.text = "$weeklyPercentage%"
        }
    }

    private fun loadProgressData() {
        try {
            val currentDate = dateFormat.format(Date())
            
            val todayProgress = userProgressDao.getProgressForDate(currentDate)
            val completedToday = todayProgress.count { it.completed }
            val totalBodyParts = 5 // Abs, Chest, Arms, Legs, Back
            val todayPercentage = if (totalBodyParts > 0) (completedToday * 100) / totalBodyParts else 0
            
            binding.todayProgressIndicator.progress = todayPercentage
            binding.tvTodayProgressPercent.text = "$todayPercentage%"
            
            val calendar = Calendar.getInstance()
            var weeklyCompleted = 0
            var weeklyTotal = 0
            
            for (i in 0..6) {
                val date = dateFormat.format(calendar.time)
                val dayProgress = userProgressDao.getProgressForDate(date)
                weeklyCompleted += dayProgress.count { it.completed }
                weeklyTotal += totalBodyParts
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
            
            val weeklyPercentage = if (weeklyTotal > 0) (weeklyCompleted * 100) / weeklyTotal else 0
            binding.weeklyProgressIndicator.progress = weeklyPercentage
            binding.tvWeeklyProgressPercent.text = "$weeklyPercentage%"
            
            val totalPoints = userProgressDao.getTotalPoints()
            binding.tvChallengePoints.text = "Challenge Points: $totalPoints"
            
            val challengeProgress = if (totalPoints > 0) minOf((totalPoints * 100) / 1000, 100) else 0
            binding.challengeProgressBar.progress = challengeProgress
            
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error loading progress data", e)
            Toast.makeText(this, "Error loading progress data", Toast.LENGTH_SHORT).show()
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

    private fun highlightLevel(selected: TextView, vararg others: TextView) {
        val totalPoints = userProgressDao.getTotalPoints()
        val unlockedLevels = getUnlockedLevels(totalPoints)
        
        if (unlockedLevels.any { it.name == selected.text.toString() }) {
            selected.setBackgroundResource(R.drawable.level_button_bg)
            selected.setBackgroundTintList(android.content.res.ColorStateList.valueOf(resources.getColor(R.color.white)))
            selected.setTextColor(resources.getColor(R.color.navy_blue))
            selectedLevel = selected.text.toString()
        } else {
            selected.setBackgroundResource(R.drawable.level_button_bg)
            selected.setBackgroundTintList(android.content.res.ColorStateList.valueOf(resources.getColor(R.color.white)))
            selected.setTextColor(resources.getColor(R.color.white))
            Toast.makeText(
                this,
                "This level is locked. Keep earning points to unlock it!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        for (tv in others) {
            if (unlockedLevels.any { it.name == tv.text.toString() }) {
                tv.setBackgroundResource(0)
                tv.setTextColor(resources.getColor(R.color.white))
            } else {
                tv.setBackgroundResource(R.drawable.level_button_bg)
                tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(resources.getColor(R.color.white)))
                tv.setTextColor(resources.getColor(R.color.white))
            }
        }
    }

    private fun setBodyPartClickListener(cardId: Int, bodyPart: String) {
        findViewById<androidx.cardview.widget.CardView>(cardId)?.setOnClickListener {
            val totalPoints = userProgressDao.getTotalPoints()
            val unlockedLevels = getUnlockedLevels(totalPoints)
            
            if (unlockedLevels.any { it.name == selectedLevel }) {
                val intent = Intent(this, ExercisesActivity::class.java)
                intent.putExtra("level", selectedLevel)
                intent.putExtra("bodyPart", bodyPart)
                startActivity(intent)
            } else {
                val requiredPoints = when (selectedLevel) {
                    "Intermediate" -> DifficultyLevel.INTERMEDIATE.requiredPoints
                    "Advanced" -> DifficultyLevel.ADVANCED.requiredPoints
                    else -> 0
                }
                Toast.makeText(
                    this,
                    "You need $requiredPoints points to unlock $selectedLevel level",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun fetchUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                binding.tvHello.text = "Hello, $name"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI() {
        val totalPoints = userProgressDao.getTotalPoints()
        val completedWorkouts = userProgressDao.getTotalCompletedWorkouts()
        
        binding.apply {
            tvChallengePoints.text = totalPoints.toString()
            
            val unlockedLevels = getUnlockedLevels(totalPoints)
            val unlockedText = buildUnlockedLevelsText(unlockedLevels)

            val todayProgress = getTodayProgress()
            todayProgressIndicator.max = 100
            todayProgressIndicator.progress = todayProgress
            
            // Update completed workouts text
            tvTodayProgressPercent.text = "Completed workouts: $completedWorkouts"
            
            loadAvailableWorkouts(unlockedLevels)
        }
    }

    private fun getUnlockedLevels(points: Int): List<DifficultyLevel> {
        return listOf(
            DifficultyLevel.BEGINNER,
            DifficultyLevel.INTERMEDIATE,
            DifficultyLevel.ADVANCED
        ).filter { it.requiredPoints <= points }
    }

    private fun buildUnlockedLevelsText(levels: List<DifficultyLevel>): String {
        return "Unlocked levels: ${levels.joinToString(", ") { it.name }}"
    }

    private fun getTodayProgress(): Int {
        val today = dateFormat.format(Date())
        val todayProgress = userProgressDao.getProgressForDate(today)
        val completed = todayProgress.count { it.completed }
        return if (todayProgress.isEmpty()) 0 else (completed * 100) / todayProgress.size
    }

    private fun loadAvailableWorkouts(unlockedLevels: List<DifficultyLevel>) {
        val availableWorkouts = mutableListOf<ExerciseEntity>()
        for (level in unlockedLevels) {
            availableWorkouts.addAll(exerciseDao.getExercises(bodyPart = "All", level = level.name))
        }
        // TODO: Create and set adapter for available workouts
    }

    private fun setupViews() {
        binding.apply {
            btnRedeemPoints.setOnClickListener {
                startActivity(Intent(this@HomeActivity, RedeemPointsActivity::class.java))
            }
        }
    }
}