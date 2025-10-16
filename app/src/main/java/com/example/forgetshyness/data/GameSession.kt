package com.example.forgetshyness.data

data class GameSession(
    val id: String = "",
    val hostId: String = "",
    val gameType: String = "",
    val createdAt: Long = 0L,
    val active: Boolean = true
)
