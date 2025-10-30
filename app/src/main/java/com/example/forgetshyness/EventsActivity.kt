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

        // 🟡 Aquí guardamos el usuario actual en memoria
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
                    onCreateEventClick = { currentScreen = "create" },
                    onEventClick = {
                        selectedEvent = it
                        currentScreen = "detail"
                    }
                )

                "create" -> CreateEventScreen(
                    userId = userId,
                    userName = userName,
                    repository = repository,
                    eventToEdit = selectedEvent,
                    selectedLocation = selectedLocation,
                    onLocationConsumed = { selectedLocation = null },
                    onEventCreated = { currentScreen = "list" },
                    onBackClick = { currentScreen = "list" },
                    onOpenMapClick = { currentScreen = "map" },  // ✅ ESTA LÍNEA
                    onInvitePlayersClick = { event ->
                        selectedEvent = event
                        currentScreen = "invite"
                    }
                )

                "map" -> SelectLocationScreen(
                    previousAddress = selectedLocation,
                    onLocationSelected = { address ->
                        EventSessionManager.eventLocation = address
                        selectedLocation = address
                        currentScreen = "create"
                    },
                    onBackClick = {
                        currentScreen = "create"
                    }
                )

                "invite" -> selectedEvent?.let { event ->
                    InvitePlayersScreen(
                        eventId = event.id,
                        repository = repository,
                        userId = userId,              // ✅ nuevo parámetro
                        onBackClick = { currentScreen = "create" }, // ✅ regresar a crear
                        onInvitationsSent = {
                            currentScreen = "create" // ✅ regresar al formulario
                        }
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
                        onInviteClick = { eventToInvite ->
                            selectedEvent = eventToInvite   // ✅ esta es la variable remember de arriba
                            currentScreen = "invite"
                        }


                    )
                }

            }
        }
    }
}







