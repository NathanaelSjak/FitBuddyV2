package com.example.fitbuddy.view

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fitbuddy.adapter.UnlockableWorkoutAdapter
import com.example.fitbuddy.data.FitBuddyDbHelper
import com.example.fitbuddy.data.WorkoutCategoryDao
import com.example.fitbuddy.data.UserProgressDao
import com.example.fitbuddy.databinding.ActivityRedeemPointsBinding
import com.example.fitbuddy.model.WorkoutCategory
import com.google.android.material.button.MaterialButton
import android.widget.TextView

class RedeemPointsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRedeemPointsBinding
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var workoutCategoryDao: WorkoutCategoryDao
    private lateinit var adapter: UnlockableWorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedeemPointsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = FitBuddyDbHelper(this)
        userProgressDao = UserProgressDao(dbHelper)
        workoutCategoryDao = WorkoutCategoryDao(dbHelper)

        setupViews()
        loadUnlockableWorkouts()
    }

    private fun setupViews() {
        binding.apply {
            rvUnlockableWorkouts.layoutManager = GridLayoutManager(this@RedeemPointsActivity, 2)
            
            btnBack.setOnClickListener {
                finish()
            }

            adapter = UnlockableWorkoutAdapter(emptyList()) { workout ->
                handleWorkoutUnlock(workout)
            }
            rvUnlockableWorkouts.adapter = adapter
        }
    }

    private fun handleWorkoutUnlock(workout: WorkoutCategory) {
        val totalPoints = userProgressDao.getTotalPoints()
        if (!workout.isUnlocked) {
            if (totalPoints >= workout.pointsRequired) {
                showUnlockConfirmationDialog(workout, totalPoints)
            } else {
                Toast.makeText(
                    this,
                    "You need ${workout.pointsRequired} points to unlock ${workout.bodyPart} ${workout.level}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showUnlockConfirmationDialog(workout: WorkoutCategory, totalPoints: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(com.example.fitbuddy.R.layout.dialog_unlock_confirmation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvConfirmationMessage = dialog.findViewById<TextView>(com.example.fitbuddy.R.id.tvConfirmationMessage)
        val tvPointsCost = dialog.findViewById<TextView>(com.example.fitbuddy.R.id.tvPointsCost)
        val btnCancel = dialog.findViewById<MaterialButton>(com.example.fitbuddy.R.id.btnCancel)
        val btnConfirm = dialog.findViewById<MaterialButton>(com.example.fitbuddy.R.id.btnConfirm)

        tvConfirmationMessage.text = "Are you sure you want to unlock ${workout.bodyPart} ${workout.level}?"
        tvPointsCost.text = "Cost: ${workout.pointsRequired} points"

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            try {
                val categoryId = workoutCategoryDao.getCategoryId(workout.bodyPart, workout.level)
                if (categoryId != -1L) {
                    // Double check points before unlocking
                    val currentPoints = userProgressDao.getTotalPoints()
                    if (currentPoints >= workout.pointsRequired) {
                        // First deduct points
                        val newPoints = currentPoints - workout.pointsRequired
                        val pointsUpdated = userProgressDao.updatePoints(newPoints)
                        
                        if (pointsUpdated) {
                            // Then try to unlock the workout
                            if (workoutCategoryDao.unlockWorkoutCategory(categoryId)) {
                                // Success - refresh UI
                                loadUnlockableWorkouts()
                                Toast.makeText(
                                    this,
                                    "${workout.bodyPart} ${workout.level} unlocked successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // Failed to unlock - refund points
                                userProgressDao.updatePoints(currentPoints)
                                Toast.makeText(
                                    this,
                                    "Failed to unlock workout. Points have been refunded.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Failed to deduct points. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Insufficient points to unlock this workout.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "An error occurred. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadUnlockableWorkouts() {
        val totalPoints = userProgressDao.getTotalPoints()
        val workouts = workoutCategoryDao.getAllWorkoutCategories()
            .filter { !it.isUnlocked }
        
        adapter.updateWorkouts(workouts)
        binding.tvPoints.text = "Available Points: $totalPoints"
    }
} 