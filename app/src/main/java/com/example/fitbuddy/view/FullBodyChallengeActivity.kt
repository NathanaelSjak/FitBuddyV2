package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R
import com.example.fitbuddy.data.ChallengeDay
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.FullBodyChallengeDao
import com.example.fitbuddy.databinding.ActivityFullBodyChallengeBinding
import java.text.SimpleDateFormat
import java.util.*

class FullBodyChallengeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullBodyChallengeBinding
    private lateinit var dbHelper: FitBuddyDbHelper
    private lateinit var challengeDao: FullBodyChallengeDao
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var challengeDays: List<ChallengeDay> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullBodyChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = FitBuddyDbHelper(this)
        challengeDao = FullBodyChallengeDao(dbHelper)

        setupViews()
        checkChallengeStatus()
    }

    private fun setupViews() {
        binding.btnStartChallenge.setOnClickListener {
            showStartChallengeDialog()
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDate = dateFormat.format(calendar.time)
            
            val challengeDay = challengeDays.find { it.scheduledDate == selectedDate }
            if (challengeDay != null) {
                updateWorkoutCard(challengeDay)
            } else {
                binding.cardTodayWorkout.visibility = View.GONE
            }
        }
    }

    private fun showStartChallengeDialog() {
        if (challengeDao.hasActiveChallenge()) {
            AlertDialog.Builder(this)
                .setTitle("Active Challenge Found")
                .setMessage("You already have an active challenge. Would you like to restart?")
                .setPositiveButton("Restart") { _, _ -> startNewChallenge() }
                .setNegativeButton("Continue Current", null)
                .show()
        } else {
            startNewChallenge()
        }
    }

    private fun startNewChallenge() {
        if (challengeDao.startNewChallenge()) {
            Toast.makeText(this, "Challenge started!", Toast.LENGTH_SHORT).show()
            updateChallengeData()
        } else {
            Toast.makeText(this, "Failed to start challenge", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkChallengeStatus() {
        if (challengeDao.hasActiveChallenge()) {
            binding.btnStartChallenge.text = "Restart Challenge"
            updateChallengeData()
        } else {
            binding.btnStartChallenge.text = "Start Challenge"
            binding.cardTodayWorkout.visibility = View.GONE
        }
    }

    private fun updateChallengeData() {
        challengeDays = challengeDao.getAllChallengeDays()
        
        val today = Date()
        binding.calendarView.date = today.time
        
        val todayStr = dateFormat.format(today)
        challengeDays.find { it.scheduledDate == todayStr }?.let { 
            updateWorkoutCard(it)
        }
    }

    private fun updateWorkoutCard(challengeDay: ChallengeDay) {
        binding.cardTodayWorkout.visibility = View.VISIBLE
        binding.tvTodayBodyPart.text = challengeDay.bodyPart
        
        when {
            challengeDay.completed -> {
                binding.tvStatus.text = "✓ COMPLETED"
                binding.tvStatus.setTextColor(getColor(R.color.light_blue))
                binding.tvTodayStatus.text = "Completed on ${challengeDay.completedAt?.substringBefore(" ")}\nPoints earned: +${challengeDay.pointsEarned}"
                binding.tvTodayStatus.setTextColor(getColor(R.color.light_blue))
                binding.cardTodayWorkout.setOnClickListener(null)
            }
            challengeDay.scheduledDate == dateFormat.format(Date()) -> {
                binding.tvStatus.text = "TODAY'S WORKOUT"
                binding.tvStatus.setTextColor(getColor(R.color.gold_yellow))
                binding.tvTodayStatus.text = "Tap to start workout"
                binding.tvTodayStatus.setTextColor(getColor(R.color.gold_yellow))
                binding.cardTodayWorkout.setOnClickListener { startDayChallenge(challengeDay) }
            }
            challengeDay.scheduledDate < dateFormat.format(Date()) -> {
                binding.tvStatus.text = "✗ MISSED"
                binding.tvStatus.setTextColor(getColor(R.color.pink_accent))
                binding.tvTodayStatus.text = "This workout was not completed"
                binding.tvTodayStatus.setTextColor(getColor(R.color.pink_accent))
                binding.cardTodayWorkout.setOnClickListener(null)
            }
            else -> {
                binding.tvStatus.text = "UPCOMING"
                binding.tvStatus.setTextColor(getColor(R.color.white))
                binding.tvTodayStatus.text = "Scheduled for ${challengeDay.scheduledDate}"
                binding.tvTodayStatus.setTextColor(getColor(R.color.white))
                binding.cardTodayWorkout.setOnClickListener(null)
            }
        }
    }

    private fun startDayChallenge(challengeDay: ChallengeDay) {
        val intent = Intent(this, ExercisesActivity::class.java).apply {
            putExtra("bodyPart", challengeDay.bodyPart)
            putExtra("level", "Beginner")
            putExtra("isChallenge", true)
            putExtra("challengeId", challengeDay.id)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (challengeDao.hasActiveChallenge()) {
            updateChallengeData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
} 