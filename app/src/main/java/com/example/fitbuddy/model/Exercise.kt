package com.example.fitbuddy.model

import java.io.Serializable

data class Exercise(
    val name: String,
    val repsOrTime: String,
    val videoUrl: String = "",
    val imageResId: Int,
    val level: String
) : Serializable
