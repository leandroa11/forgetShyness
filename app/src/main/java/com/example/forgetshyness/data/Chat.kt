package com.example.forgetshyness.data

data class Chat(
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var lastMessage: String = "",
    var lastSender: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", 0L)
}
