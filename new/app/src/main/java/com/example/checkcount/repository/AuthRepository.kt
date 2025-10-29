package com.example.checkcount.repository

import android.net.Uri
import com.example.checkcount.model.User
import com.google.firebase.auth.FirebaseUser

interface AuthRepository{
    val currentUser: FirebaseUser?
    suspend fun login(email: String, password: String): Resource<FirebaseUser>
    suspend fun register(fullName: String, phoneNumber: String, profileImage: Uri, email: String, password: String): Resource<FirebaseUser>
    suspend fun getUserData(): Resource<User>
    suspend fun getAllUserData(): Resource<List<User>>
    fun logout()
}