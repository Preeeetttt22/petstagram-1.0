package com.example.petstagram_1.models

import com.google.firebase.Timestamp

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val authorId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
