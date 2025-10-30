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
    selectedLocation: String? = null,           // <- nuevo parámetro
    onLocationConsumed: (() -> Unit)? = null,
    onEventCreated: () -> Unit,
    onBackClick: () -> Unit,
    onOpenMapClick: () -> Unit,
    onInvitePlayersClick: (Event) -> Unit // 👈 este
)
 {
    val scope = rememberCoroutineScope()
    val ctx: Context = LocalContext.current // <-- obtiene context aquí (dentro composable)

    // colores / gradiente
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF8B0A1A), Color(0xFFD94F4F))
    )

    // estados del formulario
    var eventName by remember { mutableStateOf(EventSessionManager.eventName) }
    var eventDate by remember { mutableStateOf(EventSessionManager.eventDate ?: Date()) }
    var eventLocation by remember { mutableStateOf(EventSessionManager.eventLocation) }
    var eventDescription by remember { mutableStateOf(EventSessionManager.eventDescription) }
    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) }
    var shoppingList by remember { mutableStateOf(EventSessionManager.shoppingList) }
    var isSaving by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate by remember(eventDate) {
        derivedStateOf { dateFormat.format(eventDate) }
    }


    val calendar = remember(eventDate) {
        Calendar.getInstance().apply { time = eventDate }
    }

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
             EventSessionManager.invitedUsers = ev.invitedUsers.map { it.userId }.toMutableList()
             EventSessionManager.invitedUserNames = ev.invitedUsers.map { it.name }.toMutableList()
         }
     }


     // <-- aquí: sincronizamos el valor que viene desde EventsActivity
     LaunchedEffect(selectedLocation) {
         selectedLocation?.let {
             eventLocation = it
             // Si quieres "consumir" la ubicación y dejar limpia la variable en EventsActivity:
             onLocationConsumed?.invoke()
         }
     }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (eventToEdit == null) "Crear evento" else "Editar evento",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.Yellow)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // guardar rápido desde icono
                        if (eventName.isNotBlank()) {
                            scope.launch {
                                isSaving = true
                                val ev = Event(
                                    id = eventToEdit?.id ?: "",
                                    ownerId = userId,
                                    ownerName = eventToEdit?.ownerName ?: "Organizador",
                                    name = eventName.trim(),
                                    description = eventToEdit?.description ?: "",
                                    date = eventDate,
                                    location = EventLocation(address = eventLocation),
                                    shoppingList = shoppingList.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                )
                                if (eventToEdit == null) {
                                    repository.createEvent(ev)
                                } else {
                                    repository.updateEvent(ev) // 🔹 usa un método update real
                                }

                                isSaving = false
                                onEventCreated()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = Color(0xFFFFCB3C))
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
                        .verticalScroll(scrollState), // 👈 permite hacer scroll
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nombre del evento
                    Text("Nombre del evento", fontWeight = FontWeight.SemiBold)
                    TextField(
                        value = eventName,
                        onValueChange = {
                            eventName = it
                            EventSessionManager.eventName = it
                        },
                        label = { Text("Nombre del evento") }
                    )

                    TextField(
                        value = eventDescription,
                        onValueChange = {
                            eventDescription = it
                            EventSessionManager.eventDescription = it
                        },
                        label = { Text("Descripción") }
                    )


                    // Fecha y hora (abrir Date & Time pickers)
                    Text("Fecha y hora", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                // abrir DatePicker then TimePicker
                                showDateTimePicker(ctx, calendar) { newDate ->
                                    eventDate = newDate
                                }
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Abrir fecha", tint = Color.Gray)
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFE8E8E8)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Ubicación
                    Text("Ubicación", fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = eventLocation,
                            onValueChange = {
                                eventLocation = it
                                EventSessionManager.eventLocation = it
                            },
                            placeholder = { Text("Ubicación") },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFE8E8E8)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = {
                                // Navega a pantalla de mapa
                                onOpenMapClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Abrir mapa", color = Color.Black)
                        }
                    }

                    // Invitados
                    Text("Invitados", fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = {
                            // Crea un objeto temporal de Event con los datos actuales del formulario
                            val tempEvent = eventToEdit ?: Event(
                                id = java.util.UUID.randomUUID().toString(),
                                ownerId = userId,               // Ajusta según cómo obtienes el id del usuario actual
                                ownerName = userName,           // Ajusta según tu variable real
                                name = eventName,
                                description = eventDescription, // Usa el nombre real de tu campo de descripción
                                date = eventDate,               // Ya es un Date?, no uses .time
                                location = EventLocation(
                                    latitude = selectedLat ?: 0.0,  // Si tienes estas coordenadas
                                    longitude = selectedLng ?: 0.0,
                                    address = eventLocation         // Este es el texto que ya guardas del mapa
                                )
                            )

                            // Llama la navegación a InvitePlayersScreen
                            onInvitePlayersClick(tempEvent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C))
                    ) {
                        Text("+ Agregar jugadores", color = Color.Black)
                    }

                    // 🔹 Mostrar los jugadores invitados (si existen)
                    if (EventSessionManager.invitedUsers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Jugadores invitados:",
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
                    Text("Lista de compras", fontWeight = FontWeight.SemiBold)
                    TextField(
                        value = shoppingList,
                        onValueChange = {
                            shoppingList = it
                            EventSessionManager.shoppingList = it
                        },
                        placeholder = { Text("Ingrese su lista de compras (separada por comas)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFE8E8E8)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón Guardar
                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                val newEvent = Event(
                                    ownerId = userId,
                                    ownerName = userName,
                                    name = EventSessionManager.eventName,
                                    description = EventSessionManager.eventDescription,
                                    date = EventSessionManager.eventDate ?: Date(),
                                    location = EventLocation(
                                        latitude = EventSessionManager.latitude ?: 0.0,
                                        longitude = EventSessionManager.longitude ?: 0.0,
                                        address = EventSessionManager.eventLocation
                                    ),
                                    shoppingList = EventSessionManager.shoppingList
                                        .split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    invitedUsers = EventSessionManager.invitedUsers.mapIndexed { index, userId ->
                                        val name = EventSessionManager.invitedUserNames.getOrNull(index) ?: ""
                                        com.example.forgetshyness.data.InvitedUser(userId = userId, name = name)
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
                    ) {
                        Text("Guardar", color = Color.White)
                    }

                    if (isSaving) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFFFFCB3C))
                    }
                }
            }
        }
    }
}



/** Helper: muestra DatePicker + TimePicker y devuelve Date vía callback */
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
                    EventSessionManager.eventDate = newDate // 🔹 GUARDAMOS PERSISTENTEMENTE
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








