package com.example.forgetshyness.events

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var currentEvent by remember { mutableStateOf(event) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.event_details_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_description_back), tint = Color.White)
                    }
                },
                actions = {
                    Row {
                        IconButton(onClick = { onEditClick(currentEvent) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.content_description_edit), tint = Color(0xFFFFCB3C))
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    repository.deleteEvent(currentEvent)
                                    onEventDeleted()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.content_description_delete), tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { onInviteClick(currentEvent) }) {
                            Icon(Icons.Default.GroupAdd, contentDescription = stringResource(R.string.content_description_invite), tint = Color.White)
                        }

                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFC44545)
                )
            )
        }
    ) { padding ->
        EventDetailContent(currentEvent, Modifier.padding(padding))
    }
}

@Composable
fun EventDetailContent(event: Event, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = event.date?.let { dateFormat.format(it) } ?: "Sin fecha"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(event.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC44545))
        Text(stringResource(R.string.organizer_l, event.ownerName), fontSize = 16.sp, color = Color.DarkGray)
        Text(stringResource(R.string.date_l, formattedDate), fontSize = 16.sp)
        Text(stringResource(R.string.location_l, event.location.address), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (event.description.isNotEmpty()) {
            Text(stringResource(R.string.description_l), fontWeight = FontWeight.Bold)
            Text(event.description)
        }

        if (event.invitedUsers.isNotEmpty()) {
            Text(stringResource(R.string.guests_label), fontWeight = FontWeight.Bold)
            for (user in event.invitedUsers) {
                Text("• ${user.name} (${user.status})")
            }
        }

        if (event.shoppingList.isNotEmpty()) {
            Text(stringResource(R.string.shopping_list_label), fontWeight = FontWeight.Bold)
            for (item in event.shoppingList) {
                Text("• $item")
            }
        }
    }
}



