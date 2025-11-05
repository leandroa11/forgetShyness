package com.example.forgetshyness.data

data class InvitedUser(
    val userId: String = "",
    val name: String = "",
    val status: String = "pending"  // "pending", "accepted", "declined"
)
