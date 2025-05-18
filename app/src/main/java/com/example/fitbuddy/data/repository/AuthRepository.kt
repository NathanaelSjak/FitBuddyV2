package com.example.fitbuddy.data.repository

import com.example.fitbuddy.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    suspend fun registerUser(email: String, password: String, user: User): Result<User> = suspendCoroutine { continuation ->
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result.user
                    if (firebaseUser != null) {
                        val userWithId = user.copy(id = firebaseUser.uid)
                        usersRef.child(firebaseUser.uid).setValue(userWithId)
                            .addOnSuccessListener {
                                continuation.resume(Result.success(userWithId))
                            }
                            .addOnFailureListener { e ->
                                continuation.resumeWithException(e)
                            }
                    } else {
                        continuation.resumeWithException(Exception("Failed to create user"))
                    }
                } else {
                    continuation.resumeWithException(task.exception ?: Exception("Registration failed"))
                }
            }
    }

    suspend fun loginUser(email: String, password: String): Result<User> = suspendCoroutine { continuation ->
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result.user
                    if (firebaseUser != null) {
                        usersRef.child(firebaseUser.uid).get()
                            .addOnSuccessListener { snapshot ->
                                val user = snapshot.getValue(User::class.java)
                                if (user != null) {
                                    continuation.resume(Result.success(user))
                                } else {
                                    continuation.resumeWithException(Exception("User data not found"))
                                }
                            }
                            .addOnFailureListener { e ->
                                continuation.resumeWithException(e)
                            }
                    } else {
                        continuation.resumeWithException(Exception("Login failed"))
                    }
                } else {
                    continuation.resumeWithException(task.exception ?: Exception("Login failed"))
                }
            }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            User(id = firebaseUser.uid, email = firebaseUser.email ?: "")
        } else {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun deleteUser(): Task<Void> {
        val user = auth.currentUser
        return if (user != null) {
            val userRef = usersRef.child(user.uid)
            userRef.removeValue().continueWithTask { task ->
                if (task.isSuccessful) {
                    user.delete()
                } else {
                    task.exception?.let { throw it }
                    user.delete()
                }
            }
        } else {
            throw Exception("No authenticated user found")
        }
    }

    fun checkCurrentPassword(password: String): Task<Void> {
        val user = FirebaseAuth.getInstance().currentUser
        return if (user != null) {
            val email = user.email
            if (email != null) {
                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result
                        } else {
                            throw Exception("Current password is incorrect")
                        }
                    }
            } else {
                throw Exception("User email not found")
            }
        } else {
            throw Exception("No authenticated user found")
        }
    }

    fun updatePassword(newPassword: String): Task<Void> {
        val user = FirebaseAuth.getInstance().currentUser
        return if (user != null) {
            user.updatePassword(newPassword)
        } else {
            throw Exception("No authenticated user found")
        }
    }
}
