package com.example.forgetshyness.events

import android.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.data.Event
import com.example.forgetshyness.data.EventLocation
import com.example.forgetshyness.data.EventSessionManager
import com.example.forgetshyness.data.FirestoreRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    userId: String,
    userName: String,
    repository: FirestoreRepository,
    eventToEdit: Event? = null,
    selectedLocation: String? = null,           // <- viene desde EventsActivity
    onLocationConsumed: (() -> Unit)? = null,
    onEventCreated: () -> Unit,
    onBackClick: () -> Unit,
    onOpenMapClick: () -> Unit,
    onInvitePlayersClick: (Event) -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx: Context = LocalContext.current

    val gradient = Brush.verticalGradient(listOf(Color(0xFF8B0A1A), Color(0xFFD94F4F)))

    // inicializamos desde EventSessionManager para persistencia entre pantallas
    var eventName by remember { mutableStateOf(EventSessionManager.eventName) }
    var eventDate by remember { mutableStateOf(EventSessionManager.eventDate ?: Date()) }
    var eventLocation by remember { mutableStateOf(EventSessionManager.eventLocation) }
    var eventDescription by remember { mutableStateOf(EventSessionManager.eventDescription) }
    var shoppingList by remember { mutableStateOf(EventSessionManager.shoppingList) }
    var isSaving by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate by remember(eventDate) { derivedStateOf { dateFormat.format(eventDate) } }

    val calendar = remember(eventDate) { Calendar.getInstance().apply { time = eventDate } }

    // si abrimos la pantalla para editar, prellenamos y sincronizamos con SessionManager
    LaunchedEffect(eventToEdit) {
        eventToEdit?.let { ev ->
            eventName = ev.name
            eventDescription = ev.description
            eventDate = ev.date ?: Date()
            eventLocation = ev.location.address
            shoppingList = ev.shoppingList.joinToString(", ")

            EventSessionManager.eventName = ev.name
            EventSessionManager.eventDescription = ev.description
            EventSessionManager.eventDate = ev.date
            EventSessionManager.eventLocation = ev.location.address
            EventSessionManager.shoppingList = ev.shoppingList.joinToString(", ")

            EventSessionManager.invitedUsers.addAll(ev.invitedUsers.map { it.userId })


            EventSessionManager.invitedUserNames.addAll(ev.invitedUsers.map { it.name })

        }
    }

    // Cuando llega una ubicación nueva desde SelectLocationScreen actualizamos local y SessionManager
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            eventLocation = it
            EventSessionManager.eventLocation = it
            onLocationConsumed?.invoke()
        }
    }

    // Mantener SessionManager actualizado si cambian valores locales
    LaunchedEffect(eventName) { EventSessionManager.eventName = eventName }
    LaunchedEffect(eventDescription) { EventSessionManager.eventDescription = eventDescription }
    LaunchedEffect(shoppingList) { EventSessionManager.shoppingList = shoppingList }
    LaunchedEffect(eventDate) { EventSessionManager.eventDate = eventDate } // garantiza persistencia al navegar

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (eventToEdit == null) stringResource(com.example.forgetshyness.R.string.create_event_title) else stringResource(
                            com.example.forgetshyness.R.string.edit_event_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack,
                            contentDescription = stringResource(com.example.forgetshyness.R.string.back_content_description),
                            tint = Color.Yellow)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // guardar rápido desde icono
                        if (eventName.isNotBlank()) {
                            scope.launch {
                                isSaving = true
                                val ev = Event(
                                    id = eventToEdit?.id ?: "", // si existe, mantenemos id
                                    ownerId = userId,
                                    ownerName = userName,
                                    name = eventName.trim(),
                                    description = eventDescription,
                                    date = eventDate,
                                    location = EventLocation(address = eventLocation),
                                    shoppingList = shoppingList.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    invitedUsers = EventSessionManager.invitedUsers.mapIndexed { index, uId ->
                                        val name = EventSessionManager.invitedUserNames.getOrNull(index) ?: ""
                                        com.example.forgetshyness.data.InvitedUser(userId = uId, name = name)
                                    }
                                )

                                if (eventToEdit == null || ev.id.isBlank()) {
                                    // crear nuevo
                                    val newId = repository.createEvent(ev)
                                    // Si se creó correctamente y hay invitados guardados en SessionManager,
                                    // invitamos desde backend usando el nuevo id:
                                    if (!newId.isNullOrBlank() && EventSessionManager.invitedUsers.isNotEmpty()) {
                                        repository.invitePlayersToEvent(newId, EventSessionManager.invitedUsers,
                                            EventSessionManager.invitedUsers.mapIndexed { idx, id -> mapOf("id" to id, "name" to (EventSessionManager.invitedUserNames.getOrNull(idx) ?: "")) }
                                        )
                                    }
                                } else {
                                    // actualizar: se asume que FirestoreRepository tiene updateEvent()
                                    repository.updateEvent(ev)
                                }

                                isSaving = false
                                // limpiar sesión (opcional): solo si quieres descartar datos en memoria
                                EventSessionManager.clear()
                                onEventCreated()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(com.example.forgetshyness.R.string.save_event_button), tint = Color(0xFFFFCB3C))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFC44545))
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8E9E9)),
                shape = RoundedCornerShape(20.dp)
            ) {


                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nombre del evento
                    Text(stringResource(com.example.forgetshyness.R.string.event_name_label), fontWeight = FontWeight.SemiBold)
                    TextField(
                        value = eventName,
                        onValueChange = {
                            eventName = it
                        },
                        label = { Text(stringResource(com.example.forgetshyness.R.string.event_name_label)) }
                    )

                    TextField(
                        value = eventDescription,
                        onValueChange = {
                            eventDescription = it
                        },
                        label = { Text(stringResource(com.example.forgetshyness.R.string.event_description_label)) }
                    )

                    // Fecha y hora
                    Text(stringResource(com.example.forgetshyness.R.string.event_date_time_label), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                showDateTimePicker(ctx, calendar) { newDate ->
                                    eventDate = newDate
                                    EventSessionManager.eventDate = newDate
                                }
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(
                                    com.example.forgetshyness.R.string.open_date_content_description), tint = Color.Gray)
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFE8E8E8)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Ubicación
                    Text(stringResource(com.example.forgetshyness.R.string.event_location_label), fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = eventLocation,
                            onValueChange = {
                                eventLocation = it
                                EventSessionManager.eventLocation = it
                            },
                            placeholder = { Text(stringResource(com.example.forgetshyness.R.string.event_location_label)) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFE8E8E8)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = { onOpenMapClick() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C)),
                            shape = RoundedCornerShape(20.dp)
                        ) { Text(stringResource(com.example.forgetshyness.R.string.open_map_button), color = Color.Black) }
                    }

                    // Invitados
                    Text(stringResource(com.example.forgetshyness.R.string.event_guests_label), fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = {
                            // PASAMOS el evento actual. Si no existe en BD todavía, id = ""
                            val tempEvent = eventToEdit ?: Event(
                                id = "", // IMPORTANT: id vacío => InvitePlayersScreen no intentará escribir en Firestore
                                ownerId = userId,
                                ownerName = userName,
                                name = eventName,
                                description = eventDescription,
                                date = eventDate,
                                location = EventLocation(address = eventLocation)
                            )
                            onInvitePlayersClick(tempEvent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C))
                    ) { Text(stringResource(com.example.forgetshyness.R.string.add_players_button), color = Color.Black) }

                    // Mostrar los jugadores invitados (si existen)
                    if (EventSessionManager.invitedUsers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(com.example.forgetshyness.R.string.invited_players_label),
                            color = Color.DarkGray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            EventSessionManager.invitedUserNames.forEach { userName ->
                                Text(
                                    text = userName,
                                    color = Color.DarkGray,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }


                    // Lista de compras
                    Text(stringResource(com.example.forgetshyness.R.string.shopping_list_label), fontWeight = FontWeight.SemiBold)
                    TextField(
                        value = shoppingList,
                        onValueChange = { shoppingList = it },
                        placeholder = { Text(stringResource(com.example.forgetshyness.R.string.shopping_list_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFE8E8E8)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón Guardar (principal)
                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                val newEvent = Event(
                                    ownerId = userId,
                                    ownerName = userName,
                                    name = EventSessionManager.eventName.ifBlank { eventName },
                                    description = EventSessionManager.eventDescription.ifBlank { eventDescription },
                                    date = EventSessionManager.eventDate ?: eventDate,
                                    location = EventLocation(
                                        latitude = EventSessionManager.latitude ?: 0.0,
                                        longitude = EventSessionManager.longitude ?: 0.0,
                                        address = EventSessionManager.eventLocation.ifBlank { eventLocation }
                                    ),
                                    shoppingList = EventSessionManager.shoppingList
                                        .ifBlank { shoppingList }
                                        .split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    invitedUsers = EventSessionManager.invitedUsers.mapIndexed { idx, uId ->
                                        val name = EventSessionManager.invitedUserNames.getOrNull(idx) ?: ""
                                        com.example.forgetshyness.data.InvitedUser(userId = uId, name = name)
                                    }
                                )
                                repository.createEvent(newEvent)
                                EventSessionManager.clear()
                                isSaving = false
                                onEventCreated()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF800020)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) { Text(stringResource(com.example.forgetshyness.R.string.Eventsave), color = Color.White) }

                    if (isSaving) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFFFFCB3C))
                    }
                }
            }
        }
    }
}

/** muestra DatePicker + TimePicker y devuelve Date vía callback */
private fun showDateTimePicker(ctx: Context, calendar: Calendar, onPicked: (Date) -> Unit) {
    DatePickerDialog(
        ctx,
        { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            TimePickerDialog(
                ctx,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    val newDate = calendar.time
                    EventSessionManager.eventDate = newDate // guardamos en Session manager
                    onPicked(newDate)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}









