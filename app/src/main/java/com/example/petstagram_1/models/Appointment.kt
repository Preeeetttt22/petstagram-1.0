package com.example.petstagram_1.models

import com.google.firebase.Timestamp

data class Appointment(
    val appointmentId: String = "",
    val userId: String = "",
    val vetId: String = "",
    val petId: String = "",
    val clinicName: String = "",
    val appointmentDate: Timestamp = Timestamp.now(),
    val reason: String = "",
    val status: String = "Scheduled"
)