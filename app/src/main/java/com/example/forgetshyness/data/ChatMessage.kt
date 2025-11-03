package com.example.forgetshyness.data

data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val sender: String = "", // "user" o "bot"
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)