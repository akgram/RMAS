package com.example.checkcount.model

import com.google.firebase.firestore.DocumentId

data class Rate (
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val objId: String = "",
    var rate: Int = 0
)