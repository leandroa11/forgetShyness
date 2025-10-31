package com.example.forgetshyness.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    repository: FirestoreRepository,
    onBackClick: () -> Unit,
    onEditClick: (Event) -> Unit,
    onEventDeleted: () -> Unit,
    onInviteClick: (Event) -> Unit
) {
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = event.date?.let { dateFormat.format(it) } ?: stringResource(R.string.event_detail_no_date)

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Fondo de pantalla
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 100.dp) // Padding para dejar espacio al botón de volver y la barra de acciones
        ) {

            // --- Título y detalles principales ---
            Text(event.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.event_detail_organizer, event.ownerName), fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
            Text(stringResource(R.string.event_detail_date, formattedDate), fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
            Text(stringResource(R.string.event_card_location, event.location.address), fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(24.dp))

            // --- Descripción ---
            if (event.description.isNotEmpty()) {
                DetailSection(header = stringResource(R.string.event_detail_description_header)) {
                    Text(event.description, color = Color.White.copy(alpha = 0.9f))
                }
            }

            // --- Invitados ---
            if (event.invitedUsers.isNotEmpty()) {
                DetailSection(header = stringResource(R.string.event_detail_guests_header)) {
                    event.invitedUsers.forEach {
                        Text("• ${it.name} (${it.status})", color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            // --- Lista de compras ---
            if (event.shoppingList.isNotEmpty()) {
                DetailSection(header = stringResource(R.string.event_detail_shopping_list_header)) {
                    event.shoppingList.forEach {
                        Text("• $it", color = Color.White.copy(alpha = 0.9f))
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
                .clip(CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.flecha_izquierda),
                contentDescription = stringResource(id = R.string.content_desc_back),
                tint = Color(0xFFFFCB3C)
            )
        }

        // 4. Barra de acciones inferior
        BottomAppBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            containerColor = Color.Black.copy(alpha = 0.4f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { onEditClick(event) }) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.content_desc_edit), tint = Color(0xFFFFCB3C))
                }
                /*IconButton(onClick = { onInviteClick(event) }) {
                    Icon(Icons.Default.GroupAdd, contentDescription = stringResource(R.string.content_desc_invite), tint = Color.White)
                }*/
                IconButton(
                    onClick = {
                        scope.launch {
                            repository.deleteEvent(event)
                            onEventDeleted()
                        }
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.content_desc_delete), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun DetailSection(header: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(header, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        content()
        Spacer(modifier = Modifier.height(16.dp))
    }
}