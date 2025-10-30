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

    LaunchedEffect(eventToEdit?.id) {
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
            EventSessionManager.shoppingList = shoppingList

            EventSessionManager.invitedUsers.clear()
            EventSessionManager.invitedUserNames.clear()
            EventSessionManager.invitedUsers.addAll(ev.invitedUsers.map { it.userId })
            EventSessionManager.invitedUserNames.addAll(ev.invitedUsers.map { it.name })
        }
    }

    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            eventLocation = it
            EventSessionManager.eventLocation = it
            onLocationConsumed?.invoke()
        }
    }

    val saveEvent: () -> Unit = {
        scope.launch {
            isSaving = true
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
                invitedUsers = EventSessionManager.invitedUsers.mapIndexed { index, uId ->
                    val name = EventSessionManager.invitedUserNames.getOrNull(index) ?: ""
                    com.example.forgetshyness.data.InvitedUser(userId = uId, name = name)
                }
            )
            Log.d("CreateEventScreen", "Guardando evento: $eventToSave")

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

            // --- Formulario ---
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
                    FormTextField(value = eventLocation, onValueChange = { eventLocation = it }, placeholder = stringResource(R.string.event_location_label), modifier = Modifier.weight(1f))
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
                        // --- SOLUCIÓN: Guardar estado actual en la sesión antes de navegar ---
                        EventSessionManager.eventName = eventName
                        EventSessionManager.eventDescription = eventDescription
                        EventSessionManager.eventDate = eventDate
                        EventSessionManager.eventLocation = eventLocation
                        EventSessionManager.shoppingList = shoppingList

                        // Pasamos un evento temporal, su contenido no es crítico para la navegación
                        onInvitePlayersClick(Event(id = eventToEdit?.id ?: ""))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C)),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.button_add_players), color = Color.Black) }

                if (EventSessionManager.invitedUserNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.event_invited_players_header), color = Color.White, fontWeight = FontWeight.SemiBold)
                    // Usamos un Set para evitar visualmente los duplicados mientras el estado se estabiliza
                    EventSessionManager.invitedUserNames.toSet().forEach {
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

        // Botón de volver flotante
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.flecha_izquierda),
                contentDescription = stringResource(R.string.content_desc_back),
                tint = Color(0xFFFFCB3C)
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
