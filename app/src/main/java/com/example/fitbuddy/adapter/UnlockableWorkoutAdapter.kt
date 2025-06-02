package com.example.fitbuddy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.databinding.ItemUnlockableWorkoutBinding
import com.example.fitbuddy.model.WorkoutCategory

class UnlockableWorkoutAdapter(
    private var workouts: List<WorkoutCategory>,
    private val onWorkoutClick: (WorkoutCategory) -> Unit
) : RecyclerView.Adapter<UnlockableWorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(private val binding: ItemUnlockableWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: WorkoutCategory) {
            binding.apply {
                tvWorkoutName.text = workout.bodyPart
                tvWorkoutLevel.text = workout.level
                tvPointsRequired.text = "${workout.pointsRequired} points"

                root.setOnClickListener {
                    onWorkoutClick(workout)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemUnlockableWorkoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount() = workouts.size

    fun updateWorkouts(newWorkouts: List<WorkoutCategory>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }
} 