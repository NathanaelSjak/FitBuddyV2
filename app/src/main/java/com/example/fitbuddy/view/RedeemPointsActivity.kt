package com.example.fitbuddy.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fitbuddy.adapter.UnlockableWorkoutAdapter
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.UnlockableWorkoutDao
import com.example.fitbuddy.data.UserProgressDao
import com.example.fitbuddy.databinding.ActivityRedeemPointsBinding
import com.example.fitbuddy.model.UnlockableWorkout

class RedeemPointsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRedeemPointsBinding
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var unlockableWorkoutDao: UnlockableWorkoutDao
    private val unlockableWorkouts = mutableListOf<UnlockableWorkout>()
    private lateinit var adapter: UnlockableWorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedeemPointsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = FitBuddyDbHelper(this)
        userProgressDao = UserProgressDao(dbHelper)
        unlockableWorkoutDao = UnlockableWorkoutDao(dbHelper)

        setupViews()
        loadUnlockableWorkouts()
    }

    private fun setupViews() {
        binding.apply {
            rvUnlockableWorkouts.layoutManager = GridLayoutManager(this@RedeemPointsActivity, 2)
            
            btnBack.setOnClickListener {
                finish()
            }

            adapter = UnlockableWorkoutAdapter(unlockableWorkouts) { workout ->
                handleWorkoutUnlock(workout)
            }
            rvUnlockableWorkouts.adapter = adapter
        }
    }

    private fun handleWorkoutUnlock(workout: UnlockableWorkout) {
        val totalPoints = userProgressDao.getTotalPoints()
        if (!workout.isUnlocked && totalPoints >= workout.pointsRequired) {
            unlockWorkout(workout)
            binding.tvPoints.text = "Available Points: ${totalPoints - workout.pointsRequired}"
        } else if (!workout.isUnlocked) {
            Toast.makeText(
                this,
                "You need ${workout.pointsRequired} points to unlock ${workout.bodyPart} ${workout.level}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadUnlockableWorkouts() {
        val totalPoints = userProgressDao.getTotalPoints()
        unlockableWorkouts.clear()
        
        unlockableWorkouts.addAll(unlockableWorkoutDao.getAllUnlockableWorkouts())
        
        adapter.notifyDataSetChanged()
        
        binding.tvPoints.text = "Available Points: $totalPoints"
    }

    private fun unlockWorkout(workout: UnlockableWorkout) {
        unlockableWorkoutDao.unlockWorkout(workout.bodyPart, workout.level)
        
        // Update the UI
        workout.isUnlocked = true
        adapter.notifyDataSetChanged()
        
        Toast.makeText(
            this,
            "${workout.bodyPart} ${workout.level} unlocked successfully!",
            Toast.LENGTH_SHORT
        ).show()
    }
} 