package com.example.fitbuddy.model

import java.io.Serializable

data class Exercise(
    val id: Long = 0,
    val name: String,
    val repsOrTime: String,
    val videoResourceName: String?,
    val imageResourceName: String,
    val level: String
) : Serializable
