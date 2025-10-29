package com.example.forgetshyness.data

data class GameSession(
    var id: String = "",
    var hostId: String = "",
    var gameType: String = "",
    var createdAt: Long = 0L,
    var active: Boolean = true,
    var participants: List<Map<String, Any>> = emptyList()
)

