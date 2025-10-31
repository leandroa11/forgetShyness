package com.example.forgetshyness.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.R
import com.example.forgetshyness.data.Event
import com.example.forgetshyness.data.EventLocation
import com.example.forgetshyness.data.EventSessionManager
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.InvitedUser
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    repository: FirestoreRepository,
    userId: String,
    userName: String,
    onBackClick: () -> Unit,
    onEventCreated: () -> Unit,
    onOpenMapClick: () -> Unit,
    selectedLocation: String? = null,
    onLocationConsumed: (() -> Unit)? = null,
    eventToEdit: Event? = null,
    onInvitePlayersClick: (Event) -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx: Context = LocalContext.current

    var eventName by remember { mutableStateOf(EventSessionManager.eventName) }
    var eventDate by remember { mutableStateOf(EventSessionManager.eventDate ?: Date()) }
    var eventLocation by remember { mutableStateOf(EventSessionManager.eventLocation) }
    var eventDescription by remember { mutableStateOf(EventSessionManager.eventDescription) }
    var shoppingList by remember { mutableStateOf(EventSessionManager.shoppingList) }
    var isSaving by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate by remember(eventDate) { derivedStateOf { dateFormat.format(eventDate) } }
    val calendar = remember(eventDate) { Calendar.getInstance().apply { time = eventDate } }

    // ✅ Solo inicializar UNA VEZ cuando se carga un evento para editar
    // Usar el EventSessionManager para persistir el estado de inicialización
    LaunchedEffect(eventToEdit?.id) {
        val eventId = eventToEdit?.id ?: ""
        // Solo inicializar si el evento cambió Y no se ha inicializado este evento específico
        if (eventToEdit != null && EventSessionManager.currentEditingEventId != eventId) {
            Log.d("CreateEventScreen", "Inicializando evento para editar: ${eventToEdit.id}")

            EventSessionManager.currentEditingEventId = eventId

            eventName = eventToEdit.name
            eventDescription = eventToEdit.description
            eventDate = eventToEdit.date ?: Date()
            eventLocation = eventToEdit.location.address
            shoppingList = eventToEdit.shoppingList.joinToString(", ")

            EventSessionManager.eventName = eventToEdit.name
            EventSessionManager.eventDescription = eventToEdit.description
            EventSessionManager.eventDate = eventToEdit.date
            EventSessionManager.eventLocation = eventToEdit.location.address
            EventSessionManager.shoppingList = shoppingList

            // ✅ Solo limpiar e inicializar si realmente es la primera vez
            EventSessionManager.invitedUsers.clear()
            EventSessionManager.invitedUserNames.clear()
            EventSessionManager.invitedUsers.addAll(eventToEdit.invitedUsers.map { it.userId })
            EventSessionManager.invitedUserNames.addAll(eventToEdit.invitedUsers.map { it.name })

            Log.d("CreateEventScreen", "Inicializados ${EventSessionManager.invitedUsers.size} invitados")
        } else if (eventToEdit != null) {
            Log.d("CreateEventScreen", "Evento ${eventToEdit.id} ya inicializado, saltando reinicialización")
        }
    }

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

    // ✅ Observar directamente el SnapshotStateList sin derivar
    val invitedNamesState = EventSessionManager.invitedUserNames

    // ✅ Forzar recomposición cuando cambian los invitados
    val invitedCount = EventSessionManager.invitedUsers.size
    val namesCount = EventSessionManager.invitedUserNames.size

    // ✅ Log para debug - se ejecuta en cada recomposición
    Log.d("CreateEventScreen", "Recomposición - IDs: $invitedCount, Nombres: $namesCount - ${invitedNamesState.toList()}")

    val saveEvent: () -> Unit = {
        scope.launch {
            isSaving = true

            Log.d("CreateEventScreen", "=== GUARDANDO EVENTO ===")
            Log.d("CreateEventScreen", "IDs en SessionManager: ${EventSessionManager.invitedUsers.toList()}")
            Log.d("CreateEventScreen", "Nombres en SessionManager: ${EventSessionManager.invitedUserNames.toList()}")

            // ✅ FIX: Construir correctamente la lista sincronizada de invitados
            val finalInvitedUsers = if (eventToEdit != null) {
                // Crear mapa de usuarios originales para preservar el status
                val originalGuests = eventToEdit.invitedUsers.associateBy { it.userId }

                // Sincronizar IDs con nombres usando el índice
                EventSessionManager.invitedUsers.mapIndexed { index, userId ->
                    val userName = EventSessionManager.invitedUserNames.getOrNull(index) ?: ""

                    // Si el usuario ya existía, preservar su información (especialmente el status)
                    originalGuests[userId]?.copy(name = userName) ?: InvitedUser(
                        userId = userId,
                        name = userName,
                        status = "pending"
                    )
                }
            } else {
                // Para eventos nuevos, simplemente mapear IDs con nombres
                EventSessionManager.invitedUsers.mapIndexed { index, userId ->
                    InvitedUser(
                        userId = userId,
                        name = EventSessionManager.invitedUserNames.getOrNull(index) ?: "",
                        status = "pending"
                    )
                }
            }

            val eventToSave = Event(
                id = eventToEdit?.id ?: "",
                ownerId = userId,
                ownerName = eventToEdit?.ownerName ?: userName,
                name = eventName,
                description = eventDescription,
                date = eventDate,
                location = EventLocation(
                    latitude = EventSessionManager.latitude ?: eventToEdit?.location?.latitude ?: 0.0,
                    longitude = EventSessionManager.longitude ?: eventToEdit?.location?.longitude ?: 0.0,
                    address = eventLocation
                ),
                shoppingList = shoppingList.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                invitedUsers = finalInvitedUsers
            )

            Log.d("CreateEventScreen", "SessionManager IDs: ${EventSessionManager.invitedUsers}")
            Log.d("CreateEventScreen", "SessionManager Nombres: ${EventSessionManager.invitedUserNames}")
            Log.d("CreateEventScreen", "Guardando evento con ${finalInvitedUsers.size} invitados: $eventToSave")

            if (eventToEdit == null) {
                repository.createEvent(eventToSave)
            } else {
                repository.updateEvent(eventToSave)
            }
            EventSessionManager.clear()
            isSaving = false
            onEventCreated()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 16.dp)
        ) {
            Text(
                text = stringResource(if (eventToEdit == null) R.string.create_event_title else R.string.edit_event_title),
                color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            FormSection(label = stringResource(R.string.event_name_label)) {
                FormTextField(value = eventName, onValueChange = { eventName = it }, placeholder = stringResource(R.string.event_name_label))
            }
            FormSection(label = stringResource(R.string.event_description_label)) {
                FormTextField(value = eventDescription, onValueChange = { eventDescription = it }, placeholder = stringResource(R.string.event_description_label))
            }
            FormSection(label = stringResource(R.string.event_date_time_label)) {
                OutlinedTextField(
                    value = formattedDate, onValueChange = {},
                    readOnly = true, modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDateTimePicker(ctx, calendar) { eventDate = it } }) {
                            Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.content_desc_open_datepicker), tint = Color(0xFFFFCB3C))
                        }
                    },
                    colors = formTextFieldColors(), shape = MaterialTheme.shapes.medium
                )
            }
            FormSection(label = stringResource(R.string.event_location_label)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormTextField(value = eventLocation, onValueChange = { eventLocation = it
                        EventSessionManager.eventLocation = it }, placeholder = stringResource(R.string.event_location_label), modifier = Modifier.weight(1f))
                    Button(
                        onClick = onOpenMapClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C)),
                        shape = MaterialTheme.shapes.medium
                    ) { Text(stringResource(R.string.button_open_map), color = Color.Black) }
                }
            }
            FormSection(label = stringResource(R.string.event_guests_label)) {
                Button(
                    onClick = {
                        EventSessionManager.eventName = eventName
                        EventSessionManager.eventDescription = eventDescription
                        EventSessionManager.eventDate = eventDate
                        EventSessionManager.eventLocation = eventLocation
                        EventSessionManager.shoppingList = shoppingList

                        onInvitePlayersClick(Event(id = eventToEdit?.id ?: ""))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C)),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.button_add_players), color = Color.Black) }

                if (invitedNamesState.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.event_invited_players_header), color = Color.White, fontWeight = FontWeight.SemiBold)
                    // ✅ Usar toList() para crear snapshot y forzar recomposición
                    invitedNamesState.toList().toSet().forEach {
                        Text("• $it", color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            FormSection(label = stringResource(R.string.event_shopping_list_label)) {
                FormTextField(value = shoppingList, onValueChange = { shoppingList = it }, placeholder = stringResource(R.string.event_shopping_list_placeholder))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { if (eventName.isNotBlank()) saveEvent() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C))
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.Black)
                } else {
                    Text(stringResource(if (eventToEdit == null) R.string.button_save else R.string.button_update), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.flecha_izquierda),
                contentDescription = stringResource(R.string.content_desc_back),
                tint = Color.Yellow
            )
        }
    }
}

@Composable
fun FormSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.5f)) },
        modifier = modifier.fillMaxWidth(),
        colors = formTextFieldColors(),
        shape = MaterialTheme.shapes.medium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun formTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Black.copy(alpha = 0.3f),
    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFFFFCB3C),
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)

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
                    onPicked(calendar.time)
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