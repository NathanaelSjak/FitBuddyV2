package com.example.fitbuddy.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fitbuddy.databinding.ActivityBottomNavBarBinding
import com.example.fitbuddy.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private lateinit var navBinding : ActivityBottomNavBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchUserData()

        binding.btnProfileDetails.setOnClickListener {
            startActivity(Intent(this, AccountDetailsActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    private fun fetchUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val weight = snapshot.child("weight").getValue(Float::class.java) ?: 0f
                val age = snapshot.child("age").getValue(Int::class.java) ?: 0
                val height = snapshot.child("height").getValue(Float::class.java) ?: 0f
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""

                binding.tvProfileName.text = name
                binding.tvWeight.text = "${weight.toInt()} Kg"
                binding.tvAge.text = age.toString()
                binding.tvHeight.text = "${height.toInt()} CM"
                if (profileImageUrl.isNotEmpty()) {
                    Glide.with(this@ProfileActivity).load(profileImageUrl).into(binding.ivProfileImage)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
