package com.example.fitbuddy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.model.UnlockableWorkout
import com.google.android.material.button.MaterialButton

class UnlockableWorkoutAdapter(
    private val workouts: List<UnlockableWorkout>,
    private val onUnlockClick: (UnlockableWorkout) -> Unit
) : RecyclerView.Adapter<UnlockableWorkoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardWorkout)
        val ivWorkout: ImageView = view.findViewById(R.id.ivWorkout)
        val tvBodyPart: TextView = view.findViewById(R.id.tvBodyPart)
        val tvLevel: TextView = view.findViewById(R.id.tvLevel)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvPoints: TextView = view.findViewById(R.id.tvPointsRequired)
        val btnUnlock: MaterialButton = view.findViewById(R.id.btnUnlock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unlockable_workout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]
        
        holder.apply {
            ivWorkout.setImageResource(workout.imageResId)
            tvBodyPart.text = workout.bodyPart
            tvLevel.text = workout.level
            tvDescription.text = workout.description
            tvPoints.text = "${workout.pointsRequired} Points Required"
            
            if (workout.isUnlocked) {
                btnUnlock.text = "Unlocked"
                btnUnlock.isEnabled = false
                btnUnlock.alpha = 0.5f
                cardView.alpha = 0.7f
                ivWorkout.alpha = 0.5f
            } else {
                btnUnlock.text = "Unlock"
                btnUnlock.isEnabled = true
                btnUnlock.alpha = 1f
                cardView.alpha = 1f
                ivWorkout.alpha = 0.7f
            }
            
            btnUnlock.setOnClickListener {
                onUnlockClick(workout)
            }
        }
    }

    override fun getItemCount() = workouts.size
} 