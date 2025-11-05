package com.example.forgetshyness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.forgetshyness.data.Event
import com.example.forgetshyness.data.EventSessionManager
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.events.*
import com.example.forgetshyness.utils.Constants

class EventsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = FirestoreRepository()
        val userName = intent.getStringExtra(Constants.KEY_USER_NAME) ?: "Usuario"
        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""

        EventSessionManager.currentUserId = userId

        setContent {
            var currentScreen by remember { mutableStateOf("list") }
            var selectedEvent by remember { mutableStateOf<Event?>(null) }
            var selectedLocation by remember { mutableStateOf<String?>(null) }

            when (currentScreen) {
                "list" -> EventListScreen(
                    userId = userId,
                    userName = userName,
                    repository = repository,
                    onCreateEventClick = {
                        selectedEvent = null
                        EventSessionManager.clear()
                        currentScreen = "create"
                    },
                    onEventClick = {
                        selectedEvent = it
                        EventSessionManager.clear()
                        currentScreen = "detail"
                    },
                    onBackClick = { onBackPressedDispatcher.onBackPressed() }
                )

                "create" -> CreateEventScreen(
                    repository = repository,
                    userId = userId,
                    userName = userName,
                    eventToEdit = selectedEvent,
                    selectedLocation = selectedLocation,
                    onLocationConsumed = { selectedLocation = null },
                    onEventCreated = {
                        selectedEvent = null
                        EventSessionManager.clear()
                        currentScreen = "list"
                    },
                    onBackClick = {
                        selectedEvent = null
                        EventSessionManager.clear()
                        currentScreen = "list"
                    },
                    onOpenMapClick = { currentScreen = "map" },
                    // El evento que se pasa aquí es solo para mantener el estado, no debe modificar el selectedEvent principal
                    onInvitePlayersClick = { _ ->
                        currentScreen = "invite"
                    }
                )

                "map" -> SelectLocationScreen(
                    previousAddress = selectedLocation,
                    onLocationSelected = { address ->
                        selectedLocation = address
                        currentScreen = "create"
                    },
                    onBackClick = {
                        currentScreen = "create"
                    }
                )

                "invite" -> InvitePlayersScreen(
                    // Pasamos el ID del evento solo si estamos editando (selectedEvent no es nulo)
                    eventId = selectedEvent?.id ?: "",
                    userId = userId,
                    repository = repository,
                    onBackClick = { currentScreen = "create" },
                    onInvitationsSent = {
                        currentScreen = "create"
                    }
                )

                "detail" -> selectedEvent?.let { event ->
                    EventDetailScreen(
                        event = event,
                        repository = repository,
                        onBackClick = { currentScreen = "list" },
                        onEditClick = {
                            selectedEvent = it
                            EventSessionManager.clear()
                            currentScreen = "create"
                        },
                        onEventDeleted = { currentScreen = "list" },
                        onInviteClick = { eventToInvite ->
                            selectedEvent = eventToInvite
                            currentScreen = "invite"
                        }
                    )
                }

            }
        }
    }
}