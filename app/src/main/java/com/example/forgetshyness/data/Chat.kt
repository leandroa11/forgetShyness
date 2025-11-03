package com.example.forgetshyness.data

data class Chat(
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var lastMessage: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    // 🔹 Constructor vacío requerido por Firestore
    constructor() : this("", "", "", "", 0L)
}