package com.example.forgetshyness.data

import java.util.Date

data class Event(
    var id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val name: String = "",
    val description: String = "",
    val date: Date? = null,
    val location: EventLocation = EventLocation(),
    val invitedUsers: List<InvitedUser> = emptyList(),
    val activities: List<String> = emptyList(),
    val shoppingList: List<String> = emptyList()
)




