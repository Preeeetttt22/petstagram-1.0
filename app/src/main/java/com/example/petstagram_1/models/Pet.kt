package com.example.petstagram_1.models

import com.google.firebase.Timestamp

data class Pet(
    val petId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val birthDate: Timestamp = Timestamp.now(),
    val profileImageUrl: String = ""
)