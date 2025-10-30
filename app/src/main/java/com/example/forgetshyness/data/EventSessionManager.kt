package com.example.forgetshyness.data

import java.util.Date

/**
 * Mantiene en memoria los datos del evento que se está creando o editando.
 * Esto evita perderlos cuando se cambia de pantalla.
 */
object EventSessionManager {
    var eventName: String = ""
    var eventDescription: String = ""
    var eventDate: Date? = null
    var eventLocation: String = ""
    var latitude: Double? = null
    var longitude: Double? = null
    var shoppingList: String = ""
    var invitedUsers: MutableList<String> = mutableListOf()
    var invitedUserNames = mutableListOf<String>() // 🆕 nombres de usuarios
    var currentUserId: String? = null   // 👈 Agrega esto para saber quién es el usuario actual

    fun clear() {
        eventName = ""
        eventDescription = ""
        eventDate = null
        eventLocation = ""
        latitude = null
        longitude = null
        shoppingList = ""
        invitedUsers.clear()
    }
}


