package com.example.forgetshyness.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.data.Event
import com.example.forgetshyness.data.FirestoreRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    userId: String,
    userName: String,
    repository: FirestoreRepository,
    onCreateEventClick: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    val scope = rememberCoroutineScope()
    var allEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        allEvents = repository.getEventsForUser(userId)
        loading = false
    }

    val createdEvents = allEvents.filter { it.ownerId == userId }
    val invitations = allEvents.filter { it.ownerId != userId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Asistente de planificación", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFC44545)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = Color(0xFFFFCB3C)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear evento", tint = Color.Black)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF8B0000), Color(0xFFF5B642))
                    )
                )
                .padding(padding)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Hola, $userName 👋",
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (createdEvents.isEmpty() && invitations.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Aún sin eventos 🎉",
                                    color = Color.White,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "¡Empecemos la diversión!",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    if (invitations.isNotEmpty()) {
                        item {
                            Text(
                                "📩 Invitaciones recibidas",
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        items(invitations) { event ->
                            InvitationCard(event, userId, repository) {
                                scope.launch {
                                    allEvents = repository.getEventsForUser(userId)
                                }
                            }
                        }
                    }

                    if (createdEvents.isNotEmpty()) {
                        item {
                            Text(
                                "🎊 Tus eventos",
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        items(createdEvents) { event ->
                            EventCard(event = event) {
                                onEventClick(event)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2CC)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(event.name, fontSize = 18.sp, color = Color.Black)
            Text("Anfitrión: ${event.ownerName}", color = Color.DarkGray, fontSize = 14.sp)
            Text("Ubicación: ${event.location.address}", color = Color.DarkGray, fontSize = 14.sp)
            Text("Invitados: ${event.invitedUsers.size}", color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}


@Composable
fun InvitationCard(event: Event, userId: String, repository: FirestoreRepository, onRefresh: () -> Unit) {
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2CC)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Invitación: ${event.name}", fontSize = 18.sp, color = Color.Black)
            Text("Anfitrión: ${event.ownerName}", color = Color.DarkGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateInvitationStatus(event.id, userId, "accepted")
                            onRefresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FCF97))
                ) {
                    Text("Aceptar")
                }
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateInvitationStatus(event.id, userId, "declined")
                            onRefresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                ) {
                    Text("Rechazar")
                }
            }
        }
    }
}






