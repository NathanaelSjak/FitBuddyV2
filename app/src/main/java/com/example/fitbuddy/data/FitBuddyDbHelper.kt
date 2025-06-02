package com.example.fitbuddy.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.fitbuddy.R

class FitBuddyDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        const val DATABASE_NAME = "FitBuddy.db"
        const val DATABASE_VERSION = 15
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DB", "Creating new database tables...")
        
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS workout_categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    body_part TEXT NOT NULL,
                    level TEXT NOT NULL,
                    points_required INTEGER NOT NULL,
                    is_unlocked INTEGER DEFAULT 0,
                    unlocked_at TEXT,
                    UNIQUE(body_part, level)
                )
            """.trimIndent())
            Log.d("DB", "Created workout_categories table")

            db.execSQL("""
                INSERT INTO workout_categories (body_part, level, points_required, is_unlocked)
                VALUES 
                    ('Abs', 'Beginner', 0, 1),
                    ('Abs', 'Intermediate', 100, 0),
                    ('Abs', 'Advanced', 250, 0),
                    ('Chest', 'Beginner', 0, 1),
                    ('Chest', 'Intermediate', 100, 0),
                    ('Chest', 'Advanced', 250, 0),
                    ('Arms', 'Beginner', 0, 1),
                    ('Arms', 'Intermediate', 100, 0),
                    ('Arms', 'Advanced', 250, 0),
                    ('Back', 'Beginner', 0, 1),
                    ('Back', 'Intermediate', 100, 0),
                    ('Back', 'Advanced', 250, 0),
                    ('Legs', 'Beginner', 0, 1),
                    ('Legs', 'Intermediate', 100, 0),
                    ('Legs', 'Advanced', 250, 0)
            """.trimIndent())
            Log.d("DB", "Inserted workout categories")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS exercises (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    reps_or_time TEXT NOT NULL,
                    video_url TEXT,
                    image_res_id INTEGER NOT NULL,
                    category_id INTEGER NOT NULL,
                    FOREIGN KEY(category_id) REFERENCES workout_categories(id)
                )
            """.trimIndent())
            Log.d("DB", "Created exercises table")

            db.execSQL("""
                WITH RECURSIVE
                category_ids AS (
                    SELECT id, body_part, level
                    FROM workout_categories
                )
                INSERT INTO exercises (name, reps_or_time, video_url, image_res_id, category_id)
                SELECT name, reps_or_time, video_url, image_res_id, category_id
                FROM (
                    -- Abs Exercises
                    SELECT 'Crunches' as name, 'x10' as reps_or_time, NULL as video_url, ${R.drawable.ic_abs} as image_res_id,
                           (SELECT id FROM category_ids WHERE body_part = 'Abs' AND level = 'Beginner') as category_id
                    UNION ALL
                    SELECT 'Plank', '00:20', NULL, ${R.drawable.ic_abs},
                           (SELECT id FROM category_ids WHERE body_part = 'Abs' AND level = 'Beginner')
                    UNION ALL
                    SELECT 'Bicycle Crunches', 'x15', NULL, ${R.drawable.ic_abs},
                           (SELECT id FROM category_ids WHERE body_part = 'Abs' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'Leg Raises', 'x12', NULL, ${R.drawable.ic_abs},
                           (SELECT id FROM category_ids WHERE body_part = 'Abs' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'V-Ups', 'x20', NULL, ${R.drawable.ic_abs},
                           (SELECT id FROM category_ids WHERE body_part = 'Abs' AND level = 'Advanced')
                    UNION ALL
                    SELECT 'Hanging Leg Raise', 'x15', NULL, ${R.drawable.ic_abs},
                           (SELECT id FROM category_ids WHERE body_part = 'Abs' AND level = 'Advanced')

                    -- Chest Exercises
                    UNION ALL
                    SELECT 'Knee Push-Ups', 'x8', NULL, ${R.drawable.ic_chest},
                           (SELECT id FROM category_ids WHERE body_part = 'Chest' AND level = 'Beginner')
                    UNION ALL
                    SELECT 'Incline Push-Ups', 'x10', NULL, ${R.drawable.ic_chest},
                           (SELECT id FROM category_ids WHERE body_part = 'Chest' AND level = 'Beginner')
                    UNION ALL
                    SELECT 'Push-Ups', 'x15', NULL, ${R.drawable.ic_chest},
                           (SELECT id FROM category_ids WHERE body_part = 'Chest' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'Decline Push-Ups', 'x12', NULL, ${R.drawable.ic_chest},
                           (SELECT id FROM category_ids WHERE body_part = 'Chest' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'Diamond Push-Ups', 'x20', NULL, ${R.drawable.ic_chest},
                           (SELECT id FROM category_ids WHERE body_part = 'Chest' AND level = 'Advanced')
                    UNION ALL
                    SELECT 'Archer Push-Ups', 'x10', NULL, ${R.drawable.ic_chest},
                           (SELECT id FROM category_ids WHERE body_part = 'Chest' AND level = 'Advanced')

                    -- Arms Exercises
                    UNION ALL
                    SELECT 'Tricep Dips', 'x10', NULL, ${R.drawable.ic_arms},
                           (SELECT id FROM category_ids WHERE body_part = 'Arms' AND level = 'Beginner')
                    UNION ALL
                    SELECT 'Wall Push-Ups', 'x12', NULL, ${R.drawable.ic_arms},
                           (SELECT id FROM category_ids WHERE body_part = 'Arms' AND level = 'Beginner')
                    UNION ALL
                    SELECT 'Diamond Push-Ups', 'x12', NULL, ${R.drawable.ic_arms},
                           (SELECT id FROM category_ids WHERE body_part = 'Arms' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'Close Grip Push-Ups', 'x15', NULL, ${R.drawable.ic_arms},
                           (SELECT id FROM category_ids WHERE body_part = 'Arms' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'One Arm Push-Ups', 'x8', NULL, ${R.drawable.ic_arms},
                           (SELECT id FROM category_ids WHERE body_part = 'Arms' AND level = 'Advanced')
                    UNION ALL
                    SELECT 'Bench Dips', 'x20', NULL, ${R.drawable.ic_arms},
                           (SELECT id FROM category_ids WHERE body_part = 'Arms' AND level = 'Advanced')

                    -- Legs Exercises
                    UNION ALL
                    SELECT 'Squats', 'x15', NULL, ${R.drawable.ic_legs},
                           (SELECT id FROM category_ids WHERE body_part = 'Legs' AND level = 'Beginner')
                    UNION ALL
                    SELECT 'Lunges', 'x10', NULL, ${R.drawable.ic_legs},
                           (SELECT id FROM category_ids WHERE body_part = 'Legs' AND level = 'Beginner')
                    UNION ALL
                    SELECT 'Jump Squats', 'x12', NULL, ${R.drawable.ic_legs},
                           (SELECT id FROM category_ids WHERE body_part = 'Legs' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'Bulgarian Split Squat', 'x10', NULL, ${R.drawable.ic_legs},
                           (SELECT id FROM category_ids WHERE body_part = 'Legs' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'Pistol Squats', 'x8', NULL, ${R.drawable.ic_legs},
                           (SELECT id FROM category_ids WHERE body_part = 'Legs' AND level = 'Advanced')
                    UNION ALL
                    SELECT 'Box Jumps', 'x15', NULL, ${R.drawable.ic_legs},
                           (SELECT id FROM category_ids WHERE body_part = 'Legs' AND level = 'Advanced')

                    -- Back Exercises
                    UNION ALL
                    SELECT 'Superman', 'x12', NULL, ${R.drawable.ic_back},
                           (SELECT id FROM category_ids WHERE body_part = 'Back' AND level = 'Beginner')
                    UNION ALL
                    SELECT 'Reverse Snow Angels', 'x10', NULL, ${R.drawable.ic_back},
                           (SELECT id FROM category_ids WHERE body_part = 'Back' AND level = 'Beginner')
                    UNION ALL
                    SELECT 'Pull-Ups', 'x8', NULL, ${R.drawable.ic_back},
                           (SELECT id FROM category_ids WHERE body_part = 'Back' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'Inverted Rows', 'x10', NULL, ${R.drawable.ic_back},
                           (SELECT id FROM category_ids WHERE body_part = 'Back' AND level = 'Intermediate')
                    UNION ALL
                    SELECT 'Archer Pull-Ups', 'x6', NULL, ${R.drawable.ic_back},
                           (SELECT id FROM category_ids WHERE body_part = 'Back' AND level = 'Advanced')
                    UNION ALL
                    SELECT 'One Arm Rows', 'x10', NULL, ${R.drawable.ic_back},
                           (SELECT id FROM category_ids WHERE body_part = 'Back' AND level = 'Advanced')
                ) exercises
            """.trimIndent())
            Log.d("DB", "Inserted exercises")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS user_progress (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category_id INTEGER,
                    body_part TEXT NOT NULL,
                    level TEXT NOT NULL,
                    date TEXT NOT NULL,
                    completed INTEGER DEFAULT 0,
                    points INTEGER DEFAULT 0,
                    completed_at TEXT,
                    points_earned INTEGER DEFAULT 0,
                    FOREIGN KEY(category_id) REFERENCES workout_categories(id)
                )
            """.trimIndent())
            Log.d("DB", "Created user_progress table")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS user_stats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL,
                    height REAL NOT NULL DEFAULT 0,
                    weight REAL NOT NULL DEFAULT 0,
                    points INTEGER DEFAULT 0
                )
            """.trimIndent())
            Log.d("DB", "Created user_stats table")

            db.execSQL("""
                INSERT OR IGNORE INTO user_stats (id, date, points) 
                VALUES (1, date('now'), 0)
            """)
            Log.d("DB", "Initialized user_stats")
            
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS full_body_challenge (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    start_date TEXT NOT NULL,
                    end_date TEXT NOT NULL,
                    body_part TEXT NOT NULL,
                    scheduled_date TEXT NOT NULL,
                    completed INTEGER DEFAULT 0,
                    completed_at TEXT,
                    points_earned INTEGER DEFAULT 0
                )
            """.trimIndent())
            Log.d("DB", "Created full_body_challenge table")
            
            // Verify data
            val categoriesCount = db.rawQuery("SELECT COUNT(*) FROM workout_categories", null).use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
            val exercisesCount = db.rawQuery("SELECT COUNT(*) FROM exercises", null).use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
            Log.d("DB", "Verification - Categories: $categoriesCount, Exercises: $exercisesCount")
            
        } catch (e: Exception) {
            Log.e("DB", "Error creating database tables: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("DB", "Upgrading database from version $oldVersion to $newVersion")
        
        try {
            db.execSQL("DROP TABLE IF EXISTS exercises")
            db.execSQL("DROP TABLE IF EXISTS user_progress")
            db.execSQL("DROP TABLE IF EXISTS workout_categories")
            db.execSQL("DROP TABLE IF EXISTS user_stats")
            db.execSQL("DROP TABLE IF EXISTS full_body_challenge")
            Log.d("DB", "Successfully dropped all tables")
            
            onCreate(db)
            Log.d("DB", "Successfully recreated all tables")
        } catch (e: Exception) {
            Log.e("DB", "Error during database upgrade: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}
