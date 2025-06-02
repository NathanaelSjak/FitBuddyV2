package com.example.fitbuddy.data

import com.example.fitbuddy.R

data class ExerciseEntity(
    val id: Long = 0,
    val name: String,
    val repsOrTime: String,
    val videoUrl: String? = null,
    val imageResId: Int,
    val categoryId: Long
) {
    companion object {
        fun getDefaultExercises(categoryIds: Map<Pair<String, String>, Long>): List<ExerciseEntity> {
            return listOf(
                // Abs Exercises
                createExercise("Crunches", "x10", categoryIds, "Abs", DifficultyLevel.BEGINNER, R.drawable.ic_abs),
                createExercise("Plank", "00:20", categoryIds, "Abs", DifficultyLevel.BEGINNER, R.drawable.ic_abs),
                createExercise("Bicycle Crunches", "x15", categoryIds, "Abs", DifficultyLevel.INTERMEDIATE, R.drawable.ic_abs),
                createExercise("Leg Raises", "x12", categoryIds, "Abs", DifficultyLevel.INTERMEDIATE, R.drawable.ic_abs),
                createExercise("V-Ups", "x20", categoryIds, "Abs", DifficultyLevel.ADVANCED, R.drawable.ic_abs),
                createExercise("Hanging Leg Raise", "x15", categoryIds, "Abs", DifficultyLevel.ADVANCED, R.drawable.ic_abs),

                // Chest Exercises
                createExercise("Knee Push-Ups", "x8", categoryIds, "Chest", DifficultyLevel.BEGINNER, R.drawable.ic_chest),
                createExercise("Incline Push-Ups", "x10", categoryIds, "Chest", DifficultyLevel.BEGINNER, R.drawable.ic_chest),
                createExercise("Push-Ups", "x15", categoryIds, "Chest", DifficultyLevel.INTERMEDIATE, R.drawable.ic_chest),
                createExercise("Decline Push-Ups", "x12", categoryIds, "Chest", DifficultyLevel.INTERMEDIATE, R.drawable.ic_chest),
                createExercise("Diamond Push-Ups", "x20", categoryIds, "Chest", DifficultyLevel.ADVANCED, R.drawable.ic_chest),
                createExercise("Archer Push-Ups", "x10", categoryIds, "Chest", DifficultyLevel.ADVANCED, R.drawable.ic_chest),

                // Arms Exercises
                createExercise("Tricep Dips", "x10", categoryIds, "Arms", DifficultyLevel.BEGINNER, R.drawable.ic_arms),
                createExercise("Wall Push-Ups", "x12", categoryIds, "Arms", DifficultyLevel.BEGINNER, R.drawable.ic_arms),
                createExercise("Diamond Push-Ups", "x12", categoryIds, "Arms", DifficultyLevel.INTERMEDIATE, R.drawable.ic_arms),
                createExercise("Close Grip Push-Ups", "x15", categoryIds, "Arms", DifficultyLevel.INTERMEDIATE, R.drawable.ic_arms),
                createExercise("One Arm Push-Ups", "x8", categoryIds, "Arms", DifficultyLevel.ADVANCED, R.drawable.ic_arms),
                createExercise("Bench Dips", "x20", categoryIds, "Arms", DifficultyLevel.ADVANCED, R.drawable.ic_arms),

                // Legs Exercises
                createExercise("Squats", "x15", categoryIds, "Legs", DifficultyLevel.BEGINNER, R.drawable.ic_legs),
                createExercise("Lunges", "x10", categoryIds, "Legs", DifficultyLevel.BEGINNER, R.drawable.ic_legs),
                createExercise("Jump Squats", "x12", categoryIds, "Legs", DifficultyLevel.INTERMEDIATE, R.drawable.ic_legs),
                createExercise("Bulgarian Split Squat", "x10", categoryIds, "Legs", DifficultyLevel.INTERMEDIATE, R.drawable.ic_legs),
                createExercise("Pistol Squats", "x8", categoryIds, "Legs", DifficultyLevel.ADVANCED, R.drawable.ic_legs),
                createExercise("Box Jumps", "x15", categoryIds, "Legs", DifficultyLevel.ADVANCED, R.drawable.ic_legs),

                // Back Exercises
                createExercise("Superman", "x12", categoryIds, "Back", DifficultyLevel.BEGINNER, R.drawable.ic_back),
                createExercise("Reverse Snow Angels", "x10", categoryIds, "Back", DifficultyLevel.BEGINNER, R.drawable.ic_back),
                createExercise("Pull-Ups", "x8", categoryIds, "Back", DifficultyLevel.INTERMEDIATE, R.drawable.ic_back),
                createExercise("Inverted Rows", "x10", categoryIds, "Back", DifficultyLevel.INTERMEDIATE, R.drawable.ic_back),
                createExercise("Archer Pull-Ups", "x6", categoryIds, "Back", DifficultyLevel.ADVANCED, R.drawable.ic_back),
                createExercise("One Arm Rows", "x10", categoryIds, "Back", DifficultyLevel.ADVANCED, R.drawable.ic_back)
            )
        }

        private fun createExercise(
            name: String,
            repsOrTime: String,
            categoryIds: Map<Pair<String, String>, Long>,
            bodyPart: String,
            level: DifficultyLevel,
            imageResId: Int
        ): ExerciseEntity {
            val categoryId = categoryIds[Pair(bodyPart, level.name)] ?: throw IllegalStateException("Category not found for $bodyPart - ${level.name}")
            return ExerciseEntity(
                name = name,
                repsOrTime = repsOrTime,
                videoUrl = null, // TODO: Add video URLs when available
                imageResId = imageResId,
                categoryId = categoryId
            )
        }
    }
}

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
) {
    companion object {
        private val bodyParts = listOf("Abs", "Chest", "Arms", "Legs", "Back")
        private val levels = listOf(
            DifficultyLevel.BEGINNER,
            DifficultyLevel.INTERMEDIATE,
            DifficultyLevel.ADVANCED
        )

        fun getDefaultCategories(): List<WorkoutCategoryEntity> {
            return bodyParts.flatMap { bodyPart ->
                levels.map { level ->
                    WorkoutCategoryEntity(
                        bodyPart = bodyPart,
                        level = level,
                        isUnlocked = level == DifficultyLevel.BEGINNER
                    )
                }
            }
        }
    }
}

data class DifficultyLevel(
    val name: String,
    val requiredPoints: Int,
    val isUnlocked: Boolean
) {
    companion object {
        val BEGINNER = DifficultyLevel("Beginner", 0, true)
        val INTERMEDIATE = DifficultyLevel("Intermediate", 1000, false)
        val ADVANCED = DifficultyLevel("Advanced", 2500, false)

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
