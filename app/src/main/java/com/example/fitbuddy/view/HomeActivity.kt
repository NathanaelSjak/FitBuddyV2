package com.example.fitbuddy.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitbuddy.adapter.WorkoutCategoryAdapter
import com.example.fitbuddy.model.WorkoutCategory
import com.example.fitbuddy.data.WorkoutCategoryDao
import android.widget.Spinner
import android.widget.ArrayAdapter
import com.example.fitbuddy.model.Exercise
import android.view.View
import android.widget.LinearLayout
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.example.fitbuddy.data.repository.AuthRepository
import com.example.fitbuddy.util.ResourceUtil

class HomeActivity : AppCompatActivity() {
    private var selectedLevel: String = "Beginner"
    private lateinit var binding: ActivityHomeBinding
    private lateinit var dbHelper: FitBuddyDbHelper
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var workoutCategoryDao: WorkoutCategoryDao
    private lateinit var workoutAdapter: WorkoutCategoryAdapter
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val pointsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.example.fitbuddy.POINTS_UPDATED") {
                Log.d("HomeActivity", "Received points update broadcast")
                try {
                    syncPointsData()
                    loadProgressData()
                    updateUI()
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error processing points update", e)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = FitBuddyDbHelper(this)
        userProgressDao = UserProgressDao(dbHelper)
        exerciseDao = ExerciseDao(dbHelper)
        workoutCategoryDao = WorkoutCategoryDao(dbHelper)
        
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.uid?.let {
            userProgressDao.setCurrentUserId(it)
        } ?: userProgressDao.setCurrentUserId("default")

        setupRecyclerView()
        initializeViews()
        setupBottomNavigation()
        fetchUserData()
        loadProgressData()
        updateUI()
        setupLevelSelection()
        setupWeeklyTargetButton()
        setupViews()
        
        if (getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE).getInt("weekly_target", 0) == 0) {
            getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE).edit().putInt("weekly_target", 7).apply()
            updateWeeklyProgress()
        }
    }    private var receiverRegistered = false

    override fun onResume() {
        super.onResume()
        loadProgressData()
        updateUI()
        updateWorkoutList()
        
        if (!receiverRegistered) {
            try {
                registerReceiver(pointsUpdateReceiver, IntentFilter("com.example.fitbuddy.POINTS_UPDATED"))
                receiverRegistered = true
                Log.d("HomeActivity", "Broadcast receiver registered")
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error registering receiver", e)
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (receiverRegistered) {
            try {
                unregisterReceiver(pointsUpdateReceiver)
                receiverRegistered = false
                Log.d("HomeActivity", "Broadcast receiver unregistered")
            } catch (e: IllegalArgumentException) {
                Log.e("HomeActivity", "Error unregistering receiver", e)
            }
        }
    }

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutCategoryAdapter(emptyList()) { workout ->
            handleWorkoutClick(workout)
        }

        updateWorkoutList()
    }

    private fun handleWorkoutClick(workout: WorkoutCategory) {
        if (!workout.isUnlocked) {
            Toast.makeText(
                this,
                "You need ${workout.pointsRequired} points to unlock ${workout.level} ${workout.bodyPart}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val categoryId = workoutCategoryDao.getCategoryId(workout.bodyPart, selectedLevel)
        if (categoryId == -1L) {
            Toast.makeText(this, "Error: Category not found", Toast.LENGTH_SHORT).show()
            return
        }

        val exercises = exerciseDao.getExercisesByCategory(categoryId)
        Log.d("HomeActivity", "Found ${exercises.size} exercises for ${workout.bodyPart} - $selectedLevel")
        
        if (exercises.isNotEmpty()) {
            val intent = Intent(this, ExercisesActivity::class.java)
            intent.putExtra("categoryId", categoryId)
            intent.putExtra("level", selectedLevel)
            intent.putExtra("bodyPart", workout.bodyPart)
            startActivity(intent)
        } else {
            Toast.makeText(
                this,
                "No exercises available for ${workout.bodyPart} - $selectedLevel",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initializeViews() {
        binding.apply {
        }
    }

    private fun setupLevelSelection() {
        binding.apply {
            tvBeginnerLevel.setOnClickListener {
                selectedLevel = "Beginner"
                highlightLevel(tvBeginnerLevel, tvIntermediateLevel, tvAdvancedLevel)
                updateWorkoutList()
            }
            tvIntermediateLevel.setOnClickListener {
                selectedLevel = "Intermediate"
                highlightLevel(tvIntermediateLevel, tvBeginnerLevel, tvAdvancedLevel)
                updateWorkoutList()
            }
            tvAdvancedLevel.setOnClickListener {
                selectedLevel = "Advanced"
                highlightLevel(tvAdvancedLevel, tvBeginnerLevel, tvIntermediateLevel)
                updateWorkoutList()
            }
        }
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

        val spinner = dialog.findViewById<Spinner>(R.id.spinnerWorkoutTarget)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)

        val days = (1..7).map { "$it day${if (it > 1) "s" else ""}" }.toTypedArray()
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val currentTarget = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
            .getInt("weekly_target", 7)
        if (currentTarget in 1..7) {
            spinner.setSelection(currentTarget - 1)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val selectedDays = spinner.selectedItemPosition + 1
            getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
                .edit()
                .putInt("weekly_target", selectedDays)
                .apply()
            updateWeeklyProgress()
            dialog.dismiss()
            Toast.makeText(this, "Weekly target set to $selectedDays days", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }    private fun updateWeeklyProgress() {
        val target = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
            .getInt("weekly_target", 7)
        
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
            
            val categories = workoutCategoryDao.getWorkoutCategoriesByLevel(selectedLevel)
            val totalCategories = categories.size
            
            val todayProgress = userProgressDao.getProgressForDate(currentDate)
            val completedToday = todayProgress.count { it.completed }
            val todayPercentage = if (totalCategories > 0) (completedToday * 100) / totalCategories else 0
            
            binding.todayProgressIndicator.progress = todayPercentage
            binding.tvTodayProgressPercent.text = "$todayPercentage%"
            
            val calendar = Calendar.getInstance()
            var weeklyCompleted = 0
            var weeklyTotal = 0
            
            for (i in 0..6) {
                val date = dateFormat.format(calendar.time)
                val dayProgress = userProgressDao.getProgressForDate(date)
                weeklyCompleted += dayProgress.count { it.completed }
                weeklyTotal += totalCategories
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
            
            val weeklyPercentage = if (weeklyTotal > 0) (weeklyCompleted * 100) / weeklyTotal else 0
            binding.weeklyProgressIndicator.progress = weeklyPercentage
            binding.tvWeeklyProgressPercent.text = "$weeklyPercentage%"
            val totalPoints = userProgressDao.getTotalPoints()
            Log.d("HomeActivity", "Updated points from DB: $totalPoints")
            
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
        selected.setBackgroundResource(R.drawable.level_button_bg)
        selected.setBackgroundTintList(android.content.res.ColorStateList.valueOf(resources.getColor(R.color.white)))
        selected.setTextColor(resources.getColor(R.color.navy_blue))
        selectedLevel = selected.text.toString()
        
        for (tv in others) {
            tv.setBackgroundResource(0)
            tv.setTextColor(resources.getColor(R.color.white))
        }
    }

    private fun updateWorkoutList() {
        val workoutCategories = workoutCategoryDao.getWorkoutCategoriesByLevel(selectedLevel)
        
        binding.workoutCategoriesLayout.removeAllViews()

        workoutCategories.forEach { category ->
            val cardView = createWorkoutCard(category.bodyPart, selectedLevel)
            cardView.setOnClickListener {
                handleWorkoutClick(category)
            }
            binding.workoutCategoriesLayout.addView(cardView)
        }
    }    private fun fetchUserData() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        if (uid == null) {
            binding.tvHello.text = "Hello, Guest"
            userProgressDao.setCurrentUserId("Guest")
            return
        }
        
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.addValueEventListener(object : ValueEventListener {            
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val username = if (!name.isNullOrEmpty()) name else "User"
                    
                    binding.tvHello.text = "Hello, $username"
                    
                    userProgressDao.setCurrentUserId(username)
                    Log.d("HomeActivity", "Set current user ID to username: $username")
                    
                    loadProgressData()
                    updateUI()
                    updateWorkoutList()
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error fetching user data", e)
                    binding.tvHello.text = "Hello, User"
                    userProgressDao.setCurrentUserId("User")
                }
            }            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Database error: ${error.message}")
                binding.tvHello.text = "Hello, User"
                userProgressDao.setCurrentUserId("User")
                Toast.makeText(this@HomeActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }    private fun updateUI() {
        val totalPoints = userProgressDao.getTotalPoints()
        val completedWorkouts = userProgressDao.getTotalCompletedWorkouts()
        
        binding.apply {
            tvChallengePoints.text = totalPoints.toString()
            
            val unlockedLevels = getUnlockedLevels(totalPoints)
            val unlockedText = buildUnlockedLevelsText(unlockedLevels)

            val todayProgress = getTodayProgress()
            todayProgressIndicator.max = 100
            todayProgressIndicator.progress = todayProgress
            
            tvTodayProgressPercent.text = "$completedWorkouts"
            
            loadAvailableWorkouts(unlockedLevels)
            
            val challengeProgress = if (totalPoints > 0) minOf((totalPoints * 100) / 1000, 100) else 0
            challengeProgressBar.progress = challengeProgress
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
            val categoryId = workoutCategoryDao.getCategoryId("All", level.name)
            if (categoryId != -1L) {
                availableWorkouts.addAll(exerciseDao.getExercisesByCategory(categoryId))
            }
        }
    }

    private fun setupViews() {
        binding.apply {
            btnRedeemPoints.setOnClickListener {
                startActivity(Intent(this@HomeActivity, RedeemPointsActivity::class.java))
            }

            btnStartChallenge.setOnClickListener {
                startActivity(Intent(this@HomeActivity, FullBodyChallengeActivity::class.java))
            }

            dailyChallengeCard.setOnClickListener {
                handleDailyChallenge()
            }

            generateDailyTask()
        }
    }

    private fun handleDailyChallenge() {
        val prefs = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
        val lastChallengeDate = prefs.getString("last_challenge_date", "") ?: ""
        val currentDate = dateFormat.format(Date())

        if (lastChallengeDate != currentDate) {
            val availableLevels = getAvailableLevels()
            if (availableLevels.isNotEmpty()) {
                val randomLevel = availableLevels.random()
                val categories = workoutCategoryDao.getWorkoutCategoriesByLevel(randomLevel)
                    .filter { it.isUnlocked }

                if (categories.isNotEmpty()) {
                    val randomCategory = categories.random()
                    val categoryId = workoutCategoryDao.getCategoryId(randomCategory.bodyPart, randomLevel)

                    if (categoryId != -1L) {
                        val intent = Intent(this, ExercisesActivity::class.java)
                        intent.putExtra("categoryId", categoryId)
                        intent.putExtra("level", randomLevel)
                        intent.putExtra("bodyPart", randomCategory.bodyPart)
                        intent.putExtra("isDaily", true)
                        startActivity(intent)

                        prefs.edit().putString("last_challenge_date", currentDate).apply()
                        
                        val currentPoints = userProgressDao.getTotalPoints()
                        userProgressDao.updatePoints(currentPoints + 10)
                        
                        Toast.makeText(this, "Daily challenge completed! +10 points", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "You've already completed today's challenge!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRandomUnlockedExercise(): Exercise? {
        val availableLevels = getAvailableLevels()
        if (availableLevels.isEmpty()) return null

        val allExercises = mutableListOf<Exercise>()
        
        for (level in availableLevels) {
            val categories = workoutCategoryDao.getWorkoutCategoriesByLevel(level)
            for (category in categories.filter { it.isUnlocked }) {
                val categoryId = workoutCategoryDao.getCategoryId(category.bodyPart, level)
                if (categoryId != -1L) {
                    val exercises = exerciseDao.getExercisesByCategory(categoryId)
                    allExercises.addAll(exercises.map { entity ->
                        Exercise(
                            id = entity.id,
                            name = entity.name,
                            repsOrTime = entity.repsOrTime,
                            videoResourceName = entity.videoResourceName,
                            imageResourceName = entity.imageResourceName,
                            level = level
                        )
                    })
                }
            }
        }

        return if (allExercises.isNotEmpty()) allExercises.random() else null
    }

    private fun generateDailyTask() {
        val currentDate = dateFormat.format(Date())
        binding.tvTaskDate.text = SimpleDateFormat("MMMM dd, EEE", Locale.getDefault()).format(Date())
        
        val prefs = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
        val lastCompletedDate = prefs.getString("last_daily_task_date", "")
        
        if (lastCompletedDate == currentDate) {
            val messageView = TextView(this).apply {
                text = "Daily task completed! Come back tomorrow for a new task."
                setTextColor(getColor(R.color.white))
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                setPadding(16, 16, 16, 16)
            }
            binding.todayTaskContainer.removeAllViews()
            binding.todayTaskContainer.addView(messageView)
            return
        }

        val exercise = getRandomUnlockedExercise()
        if (exercise != null) {
            val exerciseCard = layoutInflater.inflate(
                R.layout.item_exercise,
                binding.todayTaskContainer,
                false
            )

            val exerciseImage = exerciseCard.findViewById<ImageView>(R.id.exerciseImage)
            val exerciseName = exerciseCard.findViewById<TextView>(R.id.exerciseName)
            val exerciseLevel = exerciseCard.findViewById<TextView>(R.id.exerciseLevel)
            val exerciseReps = exerciseCard.findViewById<TextView>(R.id.exerciseReps)

            try {
                val resId = resources.getIdentifier(
                    exercise.imageResourceName,
                    "drawable",
                    packageName
                )
                if (resId != 0) {
                    exerciseImage.setImageResource(resId)
                } else {
                    exerciseImage.setImageResource(R.drawable.ic_profile_placeholder)
                }
            } catch (e: Exception) {
                exerciseImage.setImageResource(R.drawable.ic_profile_placeholder)
            }

            exerciseName.text = exercise.name
            exerciseLevel.text = exercise.level
            exerciseReps.text = exercise.repsOrTime

            val levelColor = when (exercise.level) {
                "Beginner" -> getColor(R.color.light_blue)
                "Intermediate" -> getColor(R.color.gold_yellow)
                "Advanced" -> getColor(R.color.pink_accent)
                else -> getColor(R.color.white)
            }
            exerciseLevel.setTextColor(levelColor)

            if (exercise.level == "Intermediate") {
                exerciseReps.text = "${exercise.repsOrTime} (60s rest)"
            } else if (exercise.level == "Advanced") {
                exerciseReps.text = "${exercise.repsOrTime} (30s rest)"
            }

            exerciseCard.setOnClickListener {
                if (lastCompletedDate != currentDate) {
                    val intent = Intent(this, ExerciseStepActivity::class.java).apply {
                        putExtra("exercises", arrayListOf(exercise))
                        putExtra("bodyPart", "Daily Task")
                        putExtra("level", exercise.level)
                        putExtra("isDaily", true)
                    }
                    startActivityForResult(intent, DAILY_TASK_REQUEST)
                } else {
                    Toast.makeText(this, "You've already completed today's task!", Toast.LENGTH_SHORT).show()
                }
            }

            binding.todayTaskContainer.removeAllViews()
            binding.todayTaskContainer.addView(exerciseCard)
        } else {
            val messageView = TextView(this).apply {
                text = "Complete more workouts to unlock exercises!"
                setTextColor(getColor(R.color.white))
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                setPadding(16, 16, 16, 16)
            }
            binding.todayTaskContainer.removeAllViews()
            binding.todayTaskContainer.addView(messageView)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DAILY_TASK_REQUEST && resultCode == RESULT_OK) {
            val currentDate = dateFormat.format(Date())
            val lastCompletedDate = getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
                .getString("last_daily_task_date", "")

            if (lastCompletedDate != currentDate) {
                getSharedPreferences("FitBuddyPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("last_daily_task_date", currentDate)
                    .apply()

                val currentPoints = userProgressDao.getTotalPoints()
                userProgressDao.updatePoints(currentPoints + 10)
                
                Toast.makeText(this, "Daily task completed! +10 points", Toast.LENGTH_SHORT).show()
                
                generateDailyTask()
                updateUI()
            } else {
                Toast.makeText(this, "You've already completed today's task!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createWorkoutCard(category: String, level: String): CardView {
        val cardView = layoutInflater.inflate(
            R.layout.item_workout_category,
            binding.workoutCategoriesLayout,
            false
        ) as CardView

        val titleText = cardView.findViewById<TextView>(R.id.tvCategoryTitle)
        val levelText = cardView.findViewById<TextView>(R.id.tvCategoryLevel)
        val lockIcon = cardView.findViewById<ImageView>(R.id.ivLockIcon)
        val pointsRequired = cardView.findViewById<TextView>(R.id.tvPointsRequired)
        val categoryInfo = cardView.findViewById<LinearLayout>(R.id.categoryInfo)

        titleText.text = category
        levelText.text = level

        val isLocked = !workoutCategoryDao.isWorkoutUnlocked(category, level)
        val requiredPoints = when (level) {
            "Intermediate" -> 100
            "Advanced" -> 250
            else -> 0
        }

        if (isLocked) {
            categoryInfo.alpha = 0.5f
            levelText.alpha = 0.5f
            
            lockIcon.visibility = View.VISIBLE
            pointsRequired.visibility = View.VISIBLE
            pointsRequired.text = "+$requiredPoints points"

            cardView.foreground = null
            cardView.setOnClickListener {
                Toast.makeText(
                    this,
                    "Need $requiredPoints points to unlock $level $category workout",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            categoryInfo.alpha = 1.0f
            levelText.alpha = 0.7f 
            
            lockIcon.visibility = View.GONE
            pointsRequired.visibility = View.GONE

            cardView.foreground = TypedValue().apply {
                theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
            }.let { typedValue ->
                ContextCompat.getDrawable(this, typedValue.resourceId)
            }

            cardView.setOnClickListener {
                val intent = Intent(this, ExercisesActivity::class.java)
                intent.putExtra("level", level)
                intent.putExtra("bodyPart", category)
                startActivity(intent)
            }
        }

        return cardView
    }

    private fun getAvailableLevels(): List<String> {
        val levels = mutableListOf<String>()
        val totalPoints = userProgressDao.getTotalPoints()

        levels.add("Beginner")

        if (totalPoints >= 100) {
            levels.add("Intermediate")
        }

        if (totalPoints >= 250) {
            levels.add("Advanced")
        }

        return levels
    }

    private fun displayDailyTask(bodyPart: String, level: String, exercises: List<ExerciseEntity>) {
        binding.apply {
            tvTaskDate.text = SimpleDateFormat("MMMM dd, EEE", Locale.getDefault()).format(Date())
            tvTodaysTask.text = "Today's $level $bodyPart Workout"
            
            val workoutText = exercises.joinToString("\n") { 
                "• ${it.name}: ${it.repsOrTime}"
            }
            tvTodaysTask.text = workoutText
        }
    }

    companion object {
        private const val DAILY_TASK_REQUEST = 100
    }    override fun onDestroy() {
        super.onDestroy()
    }    private fun syncPointsData() {
        try {
            val db = dbHelper.readableDatabase
            var points = 0
            
            var cursor = db.rawQuery(
                "SELECT points FROM user_stats WHERE user_id = ?",
                arrayOf(userProgressDao.getCurrentUserId())
            )
            
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                points = cursor.getInt(0)
            }
            cursor.close()
            
            Log.d("HomeActivity", "Syncing points data from DB: $points")
            
            binding.tvChallengePoints.text = "Challenge Points: $points"
            
            val challengeProgress = if (points > 0) minOf((points * 100) / 1000, 100) else 0
            binding.challengeProgressBar.progress = challengeProgress
            
            val unlockedLevels = getUnlockedLevels(points)
            loadAvailableWorkouts(unlockedLevels)
            updateWorkoutList()
            
            Log.d("HomeActivity", "Points sync complete, UI updated with: $points points")
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error syncing points data", e)
        }
    }
}