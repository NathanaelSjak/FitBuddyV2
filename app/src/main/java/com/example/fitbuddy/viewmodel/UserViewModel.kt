package com.example.fitbuddy.viewmodel
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.fitbuddy.data.model.User
//import com.example.fitbuddy.data.repository.AuthRepository
//import kotlinx.coroutines.launch
//
//class UserViewModel : ViewModel() {
//    private val authRepository = AuthRepository()
//
//    private val _user = MutableLiveData<User?>()
//    val user: LiveData<User?> = _user
//
//    private val _error = MutableLiveData<String?>()
//    val error: LiveData<String?> = _error
//
//    private val _loading = MutableLiveData<Boolean>()
//    val loading: LiveData<Boolean> = _loading
//
//    fun fetchCurrentUser() {
//        _loading.value = true
//        viewModelScope.launch {
//            try {
//                val currentUser = authRepository.getCurrentUser()
//                _user.value = currentUser
//            } catch (e: Exception) {
//                _error.value = e.message
//            } finally {
//                _loading.value = false
//            }
//        }
//    }
//
//    fun signOut() {
//        authRepository.signOut()
//        _user.value = null
//    }
//
//    fun updateUserProfile(updatedUser: User) {
//        _loading.value = true
//        viewModelScope.launch {
//            try {
//                // You may want to implement update logic in AuthRepository
//                // For now, just update the local LiveData
//                _user.value = updatedUser
//                _error.value = null
//            } catch (e: Exception) {
//                _error.value = e.message
//            } finally {
//                _loading.value = false
//            }
//        }
//    }
//}
