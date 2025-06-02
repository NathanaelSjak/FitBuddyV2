package com.example.fitbuddy.data

data class ExerciseEntity(
    val id: Long = 0,
    val name: String,
    val repsOrTime: String,
    val videoUrl: String?,
    val imageResId: Int,
    val bodyPart: String,
    val level: String
)

data class UserProgressEntity(
    val id: Long = 0,
    val date: String,
    val bodyPart: String,
    val level: String,
    val completed: Boolean,
    val points: Int,
    val completedAt: String? = null,
    val pointsEarned: Int = 0
)

data class UserStatsEntity(
    val id: Long = 0,
    val date: String,
    val height: Float,
    val weight: Float
)

data class DifficultyLevel(
    val name: String,
    val requiredPoints: Int,
    val isUnlocked: Boolean
) {
    companion object {
        val BEGINNER = DifficultyLevel("Beginner", 0, true)
        val INTERMEDIATE = DifficultyLevel("Intermediate", 1000, false)
        val ADVANCED = DifficultyLevel("Advanced", 2500, false)
    }
}
