package com.example.fitbuddy.model

data class UnlockableWorkout(
    val bodyPart: String,
    val level: String,
    val pointsRequired: Int,
    var isUnlocked: Boolean = false,
    val description: String = getDefaultDescription(bodyPart, level),
    val imageResId: Int = getDefaultImageResId(bodyPart)
) {
    companion object {
        private fun getDefaultDescription(bodyPart: String, level: String): String {
            return "Unlock $level $bodyPart workouts to access advanced exercises and routines"
        }

        private fun getDefaultImageResId(bodyPart: String): Int {
            return when (bodyPart.lowercase()) {
                "abs" -> com.example.fitbuddy.R.drawable.ic_abs
                "chest" -> com.example.fitbuddy.R.drawable.ic_chest
                "arms" -> com.example.fitbuddy.R.drawable.ic_arms
                "back" -> com.example.fitbuddy.R.drawable.ic_back
                "legs" -> com.example.fitbuddy.R.drawable.ic_legs
                else -> com.example.fitbuddy.R.drawable.ic_abs
            }
        }
    }
} 