package com.example.forgetshyness.events

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.forgetshyness.data.EventSessionManager
import com.example.forgetshyness.data.FirestoreRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitePlayersScreen(
    eventId: String,
    userId: String,
    repository: FirestoreRepository,
    onBackClick: () -> Unit,
    onInvitationsSent: () -> Unit
) {
    var allUsers by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    val selectedUsers = remember { mutableStateListOf<String>().apply { addAll(EventSessionManager.invitedUsers) } }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val users = repository.getAllUsers()
        allUsers = users.filter { it["id"] != userId }
        loading = false
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            Text(
                text = stringResource(R.string.invite_players_title),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.invite_players_subtitle),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color.White)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(allUsers) { user ->
                        val uId = user["id"]!!
                        val uName = user["name"]!!
                        val isSelected = uId in selectedUsers

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { if (isSelected) selectedUsers.remove(uId) else selectedUsers.add(uId) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { if (isSelected) selectedUsers.remove(uId) else selectedUsers.add(uId) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFCB3C), checkmarkColor = Color.Black)
                            )
                            Text(text = uName, fontSize = 16.sp, color = Color.White, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    // 1. Actualizar la lista de IDs en la sesión
                    EventSessionManager.invitedUsers.clear()
                    EventSessionManager.invitedUsers.addAll(selectedUsers)

                    // 2. Reconstruir la lista de NOMBRES a partir de la lista de IDs actualizada
                    EventSessionManager.invitedUserNames.clear()
                    val selectedNames = allUsers
                        .filter { it["id"] in EventSessionManager.invitedUsers }
                        .mapNotNull { it["name"] }
                    EventSessionManager.invitedUserNames.addAll(selectedNames)

                    // 3. Log de depuración para verificar el estado ANTES de volver
                    Log.d("InvitePlayersScreen", "Actualizando SessionManager. IDs: ${EventSessionManager.invitedUsers}, Nombres: ${EventSessionManager.invitedUserNames}")

                    // 4. Volver a la pantalla anterior
                    onInvitationsSent()
                },
                enabled = selectedUsers.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C), contentColor = Color.Black)
            ) {
                Text(stringResource(R.string.button_send_invitations, selectedUsers.size))
            }
        }

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
                tint = Color.Yellow
            )
        }
    }
}