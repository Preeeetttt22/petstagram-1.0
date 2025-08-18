package com.example.petstagram_1.models

import com.google.firebase.Timestamp

/**
 * Data class representing a single post in the feed.
 *
 * @property postId A unique ID for the post.
 * @property authorId The UID of the user who created the post.
 * @property petId The ID of the pet featured in the post.
 * @property text The text caption of the post.
 * @property imageUrl The URL of the post's image (can be null if it's a text-only post).
 * @property timestamp The time when the post was created.
 * @property likes A list of user UIDs who have liked the post.
 */
data class Post(
    val postId: String = "",
    val authorId: String = "",
    val petId: String = "", // Added from my version to link posts to pets
    val text: String = "",
    val imageUrl: String? = null, // Kept your nullable version, it's more flexible
    val timestamp: Timestamp = Timestamp.now(),
    val likes: List<String> = emptyList()
)
