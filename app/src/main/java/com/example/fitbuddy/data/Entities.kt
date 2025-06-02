package com.example.fitbuddy.data

data class ExerciseEntity(
    val id: Long = 0,
    val name: String,
    val repsOrTime: String,
    val videoResourceName: String?,
    val imageResourceName: String,
    val categoryId: Long
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

data class WorkoutCategoryEntity(
    val id: Long = 0,
    val bodyPart: String,
    val level: DifficultyLevel,
    val isUnlocked: Boolean = false
)

data class DifficultyLevel(
    val name: String,
    val requiredPoints: Int,
    val isUnlocked: Boolean
) {
    companion object {
        val BEGINNER = DifficultyLevel("Beginner", 0, true)
        val INTERMEDIATE = DifficultyLevel("Intermediate", 100, false)
        val ADVANCED = DifficultyLevel("Advanced", 250, false)

        fun fromName(name: String): DifficultyLevel {
            return when (name) {
                BEGINNER.name -> BEGINNER
                INTERMEDIATE.name -> INTERMEDIATE
                ADVANCED.name -> ADVANCED
                else -> throw IllegalArgumentException("Invalid difficulty level: $name")
            }
        }
    }
}
