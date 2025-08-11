package com.example.petstagram_1.models // Make sure this matches your package name

/**
 * Data class representing a user in the application.
 *
 * This class defines the structure of a user document in the Firestore database.
 * The empty default values are required for Firestore to be able to deserialize
 * the document back into a User object automatically.
 *
 * @property uid The unique ID of the user from Firebase Authentication.
 * @property email The user's email address.
 * @property username A display name for the user.
 * @property role The role of the user (e.g., "User", "Veterinarian", "Admin").
 * @property profileImageUrl A URL to the user's profile picture stored in Firebase Storage.
 */
data class User(
    val uid: String = "",
    val email: String? = null,
    val username: String = "",
    val role: String = "User", // Default role is "User"
    val profileImageUrl: String = ""
)
