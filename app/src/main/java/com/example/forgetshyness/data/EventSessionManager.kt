package com.example.forgetshyness.data

import java.util.Date

object EventSessionManager {
    var eventName: String = ""
    var eventDescription: String = ""
    var eventDate: Date? = null
    var eventLocation: String = ""
    var latitude: Double? = null
    var longitude: Double? = null
    var shoppingList: String = ""
    var invitedUsers: MutableList<String> = mutableListOf()
    var invitedUserNames = mutableListOf<String>() // nombres de usuarios
    var currentUserId: String? = null

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
    }
}



