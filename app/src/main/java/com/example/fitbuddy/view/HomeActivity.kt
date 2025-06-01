package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbuddy.R

class HomeActivity : AppCompatActivity() {
    private var selectedLevel: String = "Beginner"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        setupBottomNavigation()

        val tvBeginner = findViewById<TextView>(R.id.tvBeginnerLevel)
        val tvIntermediate = findViewById<TextView>(R.id.tvIntermediateLevel)
        val tvAdvanced = findViewById<TextView>(R.id.tvAdvancedLevel)

        tvBeginner.setOnClickListener {
            selectedLevel = "Beginner"
            highlightLevel(tvBeginner, tvIntermediate, tvAdvanced)
        }
        tvIntermediate.setOnClickListener {
            selectedLevel = "Intermediate"
            highlightLevel(tvIntermediate, tvBeginner, tvAdvanced)
        }
        tvAdvanced.setOnClickListener {
            selectedLevel = "Advanced"
            highlightLevel(tvAdvanced, tvBeginner, tvIntermediate)
        }

        setBodyPartClickListener(R.id.absWorkoutCard, "Abs")
        setBodyPartClickListener(R.id.chestWorkoutCard, "Chest")
        setBodyPartClickListener(R.id.arms2WorkoutCard, "Arms")
        setBodyPartClickListener(R.id.backWorkoutCard, "Back")
        setBodyPartClickListener(R.id.legsWorkoutCard, "Legs")
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<android.view.View>(R.id.bottomNavBar)
        if (bottomNav != null) {
            val navHome = bottomNav.findViewById<android.widget.ImageView>(R.id.navHome)
            val navPlay = bottomNav.findViewById<android.widget.ImageView>(R.id.navPlay)
            val navStats = bottomNav.findViewById<android.widget.ImageView>(R.id.navStats)
            val navProfile = bottomNav.findViewById<android.widget.ImageView>(R.id.navProfile)

            navPlay?.setOnClickListener {
                android.widget.Toast.makeText(this, "Play feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
            }
            
            navStats?.setOnClickListener {
                android.widget.Toast.makeText(this, "Stats feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
            }
            
            navProfile?.setOnClickListener {
                val intent = android.content.Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun highlightLevel(selected: TextView, vararg others: TextView) {
        selected.setBackgroundResource(R.drawable.level_button_bg)
        selected.setBackgroundTintList(android.content.res.ColorStateList.valueOf(resources.getColor(R.color.white)))
        selected.setTextColor(resources.getColor(R.color.navy_blue))
        for (tv in others) {
            tv.setBackgroundResource(0)
            tv.setTextColor(resources.getColor(R.color.white))
        }
    }

    private fun setBodyPartClickListener(cardId: Int, bodyPart: String) {
        val card = findViewById<androidx.cardview.widget.CardView>(cardId)
        card.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            intent.putExtra("level", selectedLevel)
            intent.putExtra("bodyPart", bodyPart)
            startActivity(intent)
        }
    }
}