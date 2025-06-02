package com.example.fitbuddy.model

data class WorkoutCategory(
    val bodyPart: String,
    val name: String,
    val level: String,
    var isUnlocked: Boolean,
    val pointsRequired: Int = 0
) 