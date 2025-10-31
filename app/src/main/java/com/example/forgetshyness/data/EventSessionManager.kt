package com.example.forgetshyness.data

import androidx.compose.runtime.mutableStateListOf
import java.util.Date

object EventSessionManager {
    var eventName: String = ""
    var eventDescription: String = ""
    var eventDate: Date? = null
    var eventLocation: String = ""
    var latitude: Double? = null
    var longitude: Double? = null
    var shoppingList: String = ""
    // ✅ Listas reactivas para invitados
    var invitedUsers = mutableStateListOf<String>()
    var invitedUserNames = mutableStateListOf<String>()
    var currentUserId: String? = null
    // ✅ Nuevo: Rastrear qué evento se está editando actualmente
    var currentEditingEventId: String? = null

    fun clear() {
        eventName = ""
        eventDescription = ""
        eventDate = null
        eventLocation = ""
        latitude = null
        longitude = null
        shoppingList = ""
        invitedUsers.clear()
        invitedUserNames.clear()
        currentEditingEventId = null
    }
}



