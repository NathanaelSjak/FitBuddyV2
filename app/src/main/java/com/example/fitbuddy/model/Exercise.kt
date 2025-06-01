package com.example.fitbuddy.model

data class Exercise(
    val name: String,
    val repsOrTime: String,
    val videoUrl: String,
    val imageResId: Int
) : java.io.Serializable
