package com.example.fitbuddy.model

data class WorkoutHistory(
    val bodyPart: String,
    val level: String,
    val completed: Boolean,
    val completedAt: String,
    val pointsEarned: Int
) 