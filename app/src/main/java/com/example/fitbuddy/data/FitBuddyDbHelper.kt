package com.example.fitbuddy.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FitBuddyDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        // Create exercises table
        db.execSQL("""
            CREATE TABLE exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                repsOrTime TEXT NOT NULL,
                videoUrl TEXT,
                imageResId INTEGER NOT NULL,
                bodyPart TEXT NOT NULL,
                level TEXT NOT NULL,
                description TEXT,
                points_required INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Create user_progress table
        db.execSQL("""
            CREATE TABLE user_progress (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                bodyPart TEXT NOT NULL,
                level TEXT NOT NULL,
                completed INTEGER NOT NULL DEFAULT 0,
                points INTEGER NOT NULL DEFAULT 0,
                completed_at TEXT,
                points_earned INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Create user_stats table
        db.execSQL("""
            CREATE TABLE user_stats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                height REAL NOT NULL,
                weight REAL NOT NULL
            )
        """)

        // Create unlocked_workouts table with more detailed information
        db.execSQL("""
            CREATE TABLE unlocked_workouts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                body_part TEXT NOT NULL,
                level TEXT NOT NULL,
                points_required INTEGER NOT NULL,
                is_unlocked INTEGER NOT NULL DEFAULT 0,
                unlocked_at TIMESTAMP,
                UNIQUE(body_part, level)
            )
        """)

        // Insert default unlockable workouts
        insertDefaultUnlockableWorkouts(db)
    }

    private fun insertDefaultUnlockableWorkouts(db: SQLiteDatabase) {
        // Insert intermediate level workouts
        val intermediatePoints = 200
        val advancedPoints = 500
        val bodyParts = listOf("Abs", "Arms", "Chest", "Back", "Legs")

        for (bodyPart in bodyParts) {
            // Insert intermediate workouts
            db.execSQL("""
                INSERT INTO unlocked_workouts (body_part, level, points_required, is_unlocked)
                VALUES (?, 'Intermediate', ?, 0)
            """, arrayOf(bodyPart, intermediatePoints))

            // Insert advanced workouts
            db.execSQL("""
                INSERT INTO unlocked_workouts (body_part, level, points_required, is_unlocked)
                VALUES (?, 'Advanced', ?, 0)
            """, arrayOf(bodyPart, advancedPoints))
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE user_progress ADD COLUMN completed_at TEXT")
            db.execSQL("ALTER TABLE user_progress ADD COLUMN points_earned INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 3) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS user_stats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL,
                    height REAL NOT NULL,
                    weight REAL NOT NULL
                )
            """)
        }
        if (oldVersion < 4) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS unlocked_workouts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    body_part TEXT NOT NULL,
                    level TEXT NOT NULL,
                    points_required INTEGER NOT NULL,
                    is_unlocked INTEGER NOT NULL DEFAULT 0,
                    unlocked_at TIMESTAMP,
                    UNIQUE(body_part, level)
                )
            """)
            insertDefaultUnlockableWorkouts(db)
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE exercises ADD COLUMN description TEXT")
            db.execSQL("ALTER TABLE exercises ADD COLUMN points_required INTEGER NOT NULL DEFAULT 0")
        }
    }

    companion object {
        const val DATABASE_NAME = "fitbuddy.db"
        const val DATABASE_VERSION = 5
    }
}
