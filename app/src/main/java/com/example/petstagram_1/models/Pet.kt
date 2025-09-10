package com.example.petstagram_1.models

/**
 * Data class representing a single pet.
 *
 * @property petId A unique ID for the document.
 * @property ownerId The UID of the user who owns this pet.
 * @property name The name of the pet.
 * @property breed The breed of the pet.
 * @property age The age of the pet.
 * @property profileImageUrl The URL of the pet's profile picture.
 * @property medicalRecords A list of IDs linking to its medical records.
 */
data class Pet(
    val petId: String = "",
    val ownerId: String = "",
    val name: String = "",
    val breed: String = "",
    val age: Int = 0, // Using age as a simple number
    val profileImageUrl: String = "",
    val medicalRecords: List<String> = emptyList()
)
