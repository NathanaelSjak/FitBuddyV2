package com.example.fitbuddy.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class FitBuddyDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        const val DATABASE_NAME = "FitBuddy.db"
        const val DATABASE_VERSION = 11
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
                INSERT OR IGNORE INTO workout_categories (body_part, level, points_required, is_unlocked)
                VALUES 
                    ('Abs', 'Beginner', 0, 1),
                    ('Abs', 'Intermediate', 1000, 0),
                    ('Abs', 'Advanced', 2500, 0),
                    ('Chest', 'Beginner', 0, 1),
                    ('Chest', 'Intermediate', 1000, 0),
                    ('Chest', 'Advanced', 2500, 0),
                    ('Arms', 'Beginner', 0, 1),
                    ('Arms', 'Intermediate', 1000, 0),
                    ('Arms', 'Advanced', 2500, 0),
                    ('Back', 'Beginner', 0, 1),
                    ('Back', 'Intermediate', 1000, 0),
                    ('Back', 'Advanced', 2500, 0),
                    ('Legs', 'Beginner', 0, 1),
                    ('Legs', 'Intermediate', 1000, 0),
                    ('Legs', 'Advanced', 2500, 0)
            """.trimIndent())
            Log.d("DB", "Initialized workout categories")

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
            Log.d("DB", "Initialized user_stats with default values")
            
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
