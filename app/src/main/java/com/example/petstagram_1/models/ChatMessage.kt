package com.example.petstagram_1.models

/**
 * Data class representing a single message in the AI chat.
 *
 * @property message The text content of the message.
 * @property isUser A boolean to distinguish between a user's message and the AI's response.
 */
data class ChatMessage(
    val message: String,
    val isUser: Boolean
)
