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
        if (!workout.isUnlocked && totalPoints >= workout.pointsRequired) {
            showUnlockConfirmationDialog(workout, totalPoints)
        } else if (!workout.isUnlocked) {
            Toast.makeText(
                this,
                "You need ${workout.pointsRequired} points to unlock ${workout.bodyPart} ${workout.level}",
                Toast.LENGTH_SHORT
            ).show()
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
            val categoryId = workoutCategoryDao.getCategoryId(workout.bodyPart, workout.level)
            if (categoryId != -1L && workoutCategoryDao.unlockWorkoutCategory(categoryId)) {
                userProgressDao.updatePoints(totalPoints - workout.pointsRequired)
                loadUnlockableWorkouts()
                Toast.makeText(
                    this,
                    "${workout.bodyPart} ${workout.level} unlocked successfully!",
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