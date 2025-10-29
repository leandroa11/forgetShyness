package com.example.forgetshyness.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.data.FirestoreRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitePlayersScreen(
    eventId: String,
    repository: FirestoreRepository,
    onBackClick: () -> Unit,
    onInvitationsSent: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var allUsers by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    val selectedUsers = remember { mutableStateListOf<String>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        allUsers = repository.getAllUsers()
        loading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Invitar jugadores", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFC44545)
                )
            )
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
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Selecciona los jugadores a invitar:",
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(allUsers) { user ->
                            val userId = user["id"]!!
                            val userName = user["name"]!!
                            val isSelected = userId in selectedUsers

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedUsers.remove(userId)
                                        else selectedUsers.add(userId)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (isSelected) selectedUsers.remove(userId)
                                        else selectedUsers.add(userId)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFFFCB3C)
                                    )
                                )
                                Text(
                                    text = userName,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                repository.invitePlayersToEvent(eventId, selectedUsers, allUsers)
                                onInvitationsSent()
                            }
                        },
                        enabled = selectedUsers.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFCB3C),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Enviar invitaciones (${selectedUsers.size})")
                    }
                }
            }
        }
    }
}
