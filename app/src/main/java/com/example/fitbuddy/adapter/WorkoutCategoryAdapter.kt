package com.example.fitbuddy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.model.WorkoutCategory

class WorkoutCategoryAdapter(
    private var workouts: List<WorkoutCategory>,
    private val onWorkoutClick: (WorkoutCategory) -> Unit
) : RecyclerView.Adapter<WorkoutCategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvWorkoutTitle)
        val subtitle: TextView = view.findViewById(R.id.tvWorkoutSubtitle)
        val level: TextView = view.findViewById(R.id.tvWorkoutLevel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]
        holder.apply {
            title.text = workout.name
            subtitle.text = "Workout"
            level.text = workout.level
            
            itemView.setOnClickListener {
                onWorkoutClick(workout)
            }

            // Set alpha based on unlock status
            itemView.alpha = if (workout.isUnlocked) 1f else 0.5f
        }
    }

    override fun getItemCount() = workouts.size

    fun updateWorkouts(newWorkouts: List<WorkoutCategory>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }
} 