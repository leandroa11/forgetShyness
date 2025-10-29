package com.example.forgetshyness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.forgetshyness.data.Event
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.events.*

class EventsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = FirestoreRepository()
        val userId = "12345"
        val userName = "Valentina"

        setContent {
            var currentScreen by remember { mutableStateOf("list") }
            var selectedEvent by remember { mutableStateOf<Event?>(null) }
            var selectedLocation by remember { mutableStateOf<String?>(null) }

            when (currentScreen) {
                "list" -> EventListScreen(
                    userId = userId,
                    userName = userName,
                    repository = repository,
                    onCreateEventClick = { currentScreen = "create" },
                    onEventClick = {
                        selectedEvent = it
                        currentScreen = "detail"
                    }
                )

                "create" -> CreateEventScreen(
                    userId = userId,
                    repository = repository,
                    eventToEdit = selectedEvent,
                    selectedLocation = selectedLocation,               // <- pasar aquí
                    onLocationConsumed = { selectedLocation = null },
                    onEventCreated = { currentScreen = "list" },
                    onBackClick = { currentScreen = "list" },
                    onOpenMapClick = { currentScreen = "map" },
                    onInvitePlayersClick = { currentScreen = "invite" }
                )

                "map" -> SelectLocationScreen(
                    onLocationSelected = { address ->
                        selectedLocation = address
                        currentScreen = "create"
                    },
                    onBackClick = { currentScreen = "create" }
                )

                "invite" -> selectedEvent?.let { event ->
                    InvitePlayersScreen(
                        eventId = event.id,
                        repository = repository,
                        onBackClick = { currentScreen = "detail" },
                        onInvitationsSent = { currentScreen = "list" }
                    )
                }

                "detail" -> selectedEvent?.let { event ->
                    EventDetailScreen(
                        event = event,
                        repository = repository,
                        onBackClick = { currentScreen = "list" },
                        onEditClick = {
                            selectedEvent = it
                            currentScreen = "create"
                        },
                        onEventDeleted = { currentScreen = "list" },
                        onInviteClick = {
                            selectedEvent = event
                            currentScreen = "invite"
                        }
                    )
                }
            }
        }
    }
}







