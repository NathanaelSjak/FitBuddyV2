package com.example.fitbuddy.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fitbuddy.databinding.ActivityAccountDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AccountDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchUserDetails()

        binding.btnUpdateProfile.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun fetchUserDetails() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.etFullName.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                binding.etEmail.setText(snapshot.child("email").getValue(String::class.java) ?: "")
                binding.etMobileNumber.setText(snapshot.child("mobileNumber").getValue(String::class.java) ?: "")
                binding.etBirthDate.setText(snapshot.child("birthDate").getValue(String::class.java) ?: "")
                binding.etWeight.setText((snapshot.child("weight").getValue(Number::class.java)?.toString() ?: ""))
                binding.etHeight.setText((snapshot.child("height").getValue(Number::class.java)?.toString() ?: ""))
                val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java) ?: ""
                if (profileImageUrl.isNotEmpty()) {
                    Glide.with(this@AccountDetailsActivity).load(profileImageUrl).into(binding.ivProfileImage)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AccountDetailsActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        val updates = mapOf(
            "name" to binding.etFullName.text.toString(),
            "email" to binding.etEmail.text.toString(),
            "mobileNumber" to binding.etMobileNumber.text.toString(),
            "birthDate" to binding.etBirthDate.text.toString(),
            "weight" to binding.etWeight.text.toString().toFloatOrNull(),
            "height" to binding.etHeight.text.toString().toFloatOrNull()
        )
        userRef.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
        }
    }
}
