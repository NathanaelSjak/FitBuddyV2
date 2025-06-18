package com.example.fitbuddy.view

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RedeemPointsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRedeemPointsBinding
    private lateinit var userProgressDao: UserProgressDao
    private lateinit var workoutCategoryDao: WorkoutCategoryDao
    private lateinit var adapter: UnlockableWorkoutAdapter
    private lateinit var dbHelper: FitBuddyDbHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedeemPointsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = FitBuddyDbHelper(this)
        userProgressDao = UserProgressDao(dbHelper)
        workoutCategoryDao = WorkoutCategoryDao(dbHelper)
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
            userRef.get().addOnSuccessListener { snapshot ->
                val username = snapshot.child("name").getValue(String::class.java) ?: "User"
                userProgressDao.setCurrentUserId(username)
                Log.d("RedeemPointsActivity", "Using username: $username")
                
                loadUnlockableWorkouts()
            }.addOnFailureListener {
                userProgressDao.setCurrentUserId("User")
                Log.d("RedeemPointsActivity", "Failed to get username, using default: User")
                loadUnlockableWorkouts()
            }
        } else {
            userProgressDao.setCurrentUserId("User")
            Log.d("RedeemPointsActivity", "No user logged in, using default: User")
        }

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
                    val currentPoints = userProgressDao.getTotalPoints()
                    if (currentPoints >= workout.pointsRequired) {
                        val newPoints = currentPoints - workout.pointsRequired
                        Log.d("RedeemPointsActivity", "Starting points: $currentPoints, deducting: ${workout.pointsRequired}, result: $newPoints")
                        
                        val db = dbHelper.writableDatabase
                        var updateSuccess = false
                        db.beginTransaction()
                        try {
                            db.execSQL(
                                "UPDATE user_stats SET points = ? WHERE user_id = ?",
                                arrayOf(newPoints, userProgressDao.getCurrentUserId())
                            )
                            db.setTransactionSuccessful()
                            updateSuccess = true
                            Log.d("RedeemPointsActivity", "Direct DB update successful")
                        } catch (e: Exception) {
                            Log.e("RedeemPointsActivity", "Direct DB update failed", e)
                        } finally {
                            db.endTransaction()
                        }
                        
                        val pointsUpdated = userProgressDao.updatePoints(newPoints)
                        Log.d("RedeemPointsActivity", "DAO update result: $pointsUpdated")
                        
                        if (updateSuccess || pointsUpdated) {
                            if (workoutCategoryDao.unlockWorkoutCategory(categoryId)) {
                                val intent = Intent("com.example.fitbuddy.POINTS_UPDATED")
                                intent.putExtra("points", newPoints)
                                intent.putExtra("timestamp", System.currentTimeMillis())
                                sendBroadcast(intent)
                                Log.d("RedeemPointsActivity", "Broadcast sent with points: $newPoints")
                                
                                loadUnlockableWorkouts()
                                binding.tvPoints.text = "Available Points: $newPoints"
                                
                                Toast.makeText(
                                    this,
                                    "${workout.bodyPart} ${workout.level} unlocked successfully!\nNew Points Balance: $newPoints",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                syncPointsWithDatabase(currentPoints)
                                loadUnlockableWorkouts()
                                Toast.makeText(
                                    this,
                                    "Failed to unlock workout. Points have been refunded.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            loadUnlockableWorkouts()
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
                loadUnlockableWorkouts()
                binding.tvPoints.text = "Available Points: ${userProgressDao.getTotalPoints()}"
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
      /**
     * Ensures the user points are synchronized with the database
     * and broadcasts the update to other components
     * @param points The updated point value
     */
    private fun syncPointsWithDatabase(points: Int) {
        try {
            Log.d("RedeemPointsActivity", "Syncing points to DB: $points")
            
            val success = userProgressDao.updatePoints(points)
            
            if (success) {
                val confirmedPoints = userProgressDao.getTotalPoints()
                Log.d("RedeemPointsActivity", "Points confirmed in DB: $confirmedPoints")
                
                val intent = Intent("com.example.fitbuddy.POINTS_UPDATED")
                intent.putExtra("points", confirmedPoints)
                intent.putExtra("updated_at", System.currentTimeMillis())
                sendBroadcast(intent)
                Log.d("RedeemPointsActivity", "Broadcast sent with points: $confirmedPoints")
                
                binding.tvPoints.text = "Available Points: $confirmedPoints"
            } else {
                Log.e("RedeemPointsActivity", "Failed to sync points in database")
                val db = dbHelper.writableDatabase
                try {
                    db.execSQL(
                        "UPDATE user_stats SET points = ? WHERE user_id = ?", 
                        arrayOf(points, userProgressDao.getCurrentUserId())
                    )
                    Log.d("RedeemPointsActivity", "Direct DB update for points: $points")
                    
                    val intent = Intent("com.example.fitbuddy.POINTS_UPDATED")
                    intent.putExtra("points", points)
                    intent.putExtra("updated_at", System.currentTimeMillis())
                    sendBroadcast(intent)
                    
                    binding.tvPoints.text = "Available Points: $points"
                } catch (e: Exception) {
                    Log.e("RedeemPointsActivity", "Direct DB update failed", e)
                }
            }
        } catch (e: Exception) {
            Log.e("RedeemPointsActivity", "Error syncing points with database", e)
        }
    }
}