package com.example.fitbuddy.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbuddy.R
import com.example.fitbuddy.model.Exercise

class ExerciseAdapter(private val exercises: List<Exercise>) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position])
    }
    
    override fun getItemCount() = exercises.size
    
    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvExerciseName)
        private val repsTextView: TextView = itemView.findViewById(R.id.exerciseReps)
        private val imageView: ImageView = itemView.findViewById(R.id.exerciseImage)

        fun bind(exercise: Exercise) {
            nameTextView.text = exercise.name
            repsTextView.text = exercise.repsOrTime

            try {
                // Load image from drawable resources
                val resId = itemView.context.resources.getIdentifier(
                    exercise.imageResourceName,
                    "drawable",
                    itemView.context.packageName
                )
                if (resId != 0) {
                    imageView.setImageResource(resId)
                } else {
                    imageView.setImageResource(R.drawable.ic_profile_placeholder)
                }
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.ic_profile_placeholder)
            }
        }
    }
}
