package com.example.forgetshyness.data

data class MessageModel(
    var sender: String = "",
    var text: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", 0L)
}
