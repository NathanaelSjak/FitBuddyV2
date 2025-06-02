package com.example.fitbuddy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.model.Exercise

class ExerciseAdapter(private val exercises: List<Exercise>) : 
    RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exerciseImage: ImageView = view.findViewById(R.id.exerciseImage)
        val exerciseName: TextView = view.findViewById(R.id.exerciseName)
        val exerciseReps: TextView = view.findViewById(R.id.exerciseReps)
        val exerciseLevel: TextView = view.findViewById(R.id.exerciseLevel)

        fun bind(exercise: Exercise) {
            exerciseImage.setImageResource(exercise.imageResId)
            exerciseName.text = exercise.name
            exerciseReps.text = exercise.repsOrTime
            exerciseLevel.text = exercise.level
            
            // Set color based on level
            val context = itemView.context
            val levelColor = when (exercise.level) {
                "Beginner" -> context.getColor(R.color.light_blue)
                "Intermediate" -> context.getColor(R.color.gold_yellow)
                "Advanced" -> context.getColor(R.color.pink_accent)
                else -> context.getColor(R.color.white)
            }
            exerciseLevel.setTextColor(levelColor)

            // Add rest time based on level
            if (exercise.level == "Intermediate") {
                exerciseReps.text = "${exercise.repsOrTime} (60s rest)"
            } else if (exercise.level == "Advanced") {
                exerciseReps.text = "${exercise.repsOrTime} (30s rest)"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(exercises[position])
    }

    override fun getItemCount() = exercises.size
} 