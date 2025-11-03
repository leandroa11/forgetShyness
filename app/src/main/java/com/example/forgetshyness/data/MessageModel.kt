package com.example.forgetshyness.data

data class MessageModel(
    var sender: String = "",
    var text: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    // 🔹 Constructor vacío requerido por Firestore
    constructor() : this("", "", 0L)
}