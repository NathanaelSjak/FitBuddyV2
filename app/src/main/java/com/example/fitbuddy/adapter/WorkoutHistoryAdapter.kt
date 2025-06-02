package com.example.fitbuddy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.model.WorkoutHistory

class WorkoutHistoryAdapter(private var workouts: List<WorkoutHistory>) :
    RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bodyPartText: TextView = view.findViewById(R.id.tvBodyPart)
        val levelText: TextView = view.findViewById(R.id.tvLevel)
        val completedText: TextView = view.findViewById(R.id.tvCompleted)
        val timeText: TextView = view.findViewById(R.id.tvTime)
        val pointsText: TextView = view.findViewById(R.id.tvPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]
        holder.bodyPartText.text = workout.bodyPart
        holder.levelText.text = workout.level
        holder.completedText.text = if (workout.completed) "Completed" else "Not Completed"
        holder.timeText.text = workout.completedAt
        holder.pointsText.text = "${workout.pointsEarned} points"
    }

    override fun getItemCount() = workouts.size

    fun updateData(newWorkouts: List<WorkoutHistory>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }
} 