package com.example.petstagram_1.models

import com.google.firebase.Timestamp

/**
 * Data class representing a single medical record for a pet.
 *
 * @property recordId A unique ID for the document.
 * @property petId The ID of the pet this record belongs to.
 * @property title The title of the record (e.g., "Rabies Vaccination").
 * @property date The date the record was issued.
 * @property documentUrl The URL of the uploaded file in Firebase Storage.
 */
data class MedicalRecord(
    val recordId: String = "",
    val petId: String = "",
    val title: String = "",
    val date: Timestamp = Timestamp.now(),
    val documentUrl: String = ""
)
