    package com.example.forgetshyness.data

    data class Turn(
        val participantId: String = "",
        val challengeId: String = "",
        val liked: Boolean? = null,
        val timestamp: Long = System.currentTimeMillis()
    )
