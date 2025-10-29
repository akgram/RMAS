package com.example.checkcount.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId var id: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val profileImage: String = "",
    val points: Int = 0
)