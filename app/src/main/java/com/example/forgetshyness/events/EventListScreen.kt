package com.example.forgetshyness.events

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.R
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
    onEventClick: (Event) -> Unit,
    onBackClick: () -> Unit 
) {
    val scope = rememberCoroutineScope()
    var allEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        allEvents = repository.getEventsForUser(userId)
        loading = false
    }

    val createdEvents = allEvents.filter { it.ownerId == userId }
    val invitations = allEvents.filter { it.ownerId != userId && it.invitedUsers.any { u -> u.userId == userId && u.status == "pending" } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = Color(0xFFFFCB3C)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.event_list_create_event), tint = Color.Black)
            }
        },
        containerColor = Color.Transparent // Hacemos el fondo del Scaffold transparente
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 1. Fondo de pantalla
            Image(
                painter = painterResource(id = R.drawable.fondo_burbujas_4),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 2. Contenido principal
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 80.dp) // Espacio para el botón de volver y FAB
                ) {

                    // Saludo y título
                    item {
                        Text(
                            text = stringResource(R.string.event_list_greeting, userName),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.event_list_title),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )
                    }

                    // Mensaje de "Sin eventos"
                    if (createdEvents.isEmpty() && invitations.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(vertical = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    stringResource(R.string.event_list_no_events_title),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.event_list_no_events_subtitle),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // Sección de invitaciones
                    if (invitations.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.event_list_invitations_header),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(invitations) {
                            InvitationCard(it, userId, repository) {
                                scope.launch { allEvents = repository.getEventsForUser(userId) }
                            }
                        }
                    }

                    // Sección de tus eventos
                    if (createdEvents.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.event_list_my_events_header),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
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

            // 3. Botón de volver flotante
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.flecha_izquierda),
                    contentDescription = stringResource(id = R.string.content_desc_back),
                    tint = Color(0xFFFFCB3C)
                )
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(event.name, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.event_card_host, event.ownerName), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Text(stringResource(R.string.event_card_location, event.location.address), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Text(stringResource(R.string.event_card_guests, event.invitedUsers.size), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}


@Composable
fun InvitationCard(event: Event, userId: String, repository: FirestoreRepository, onRefresh: () -> Unit) {
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.invitation_card_title, event.name), fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.event_card_host, event.ownerName), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateInvitationStatus(event.id, userId, "accepted")
                            onRefresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6FCF97)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_accept), color = Color.Black)
                }
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateInvitationStatus(event.id, userId, "declined")
                            onRefresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_decline), color = Color.Black)
                }
            }
        }
    }
}