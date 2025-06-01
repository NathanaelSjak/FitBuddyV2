package com.example.fitbuddy.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val gender: String = "",
    val age: Int = 0,
    val fitnessGoal: String = "",
    val experienceLevel: String = "",
    val height: Float = 0f,
    val weight: Float = 0f,
    val birthDate: String = "",
    val profileImageUrl: String = ""
)
