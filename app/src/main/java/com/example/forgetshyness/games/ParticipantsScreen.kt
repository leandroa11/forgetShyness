package com.example.forgetshyness.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.data.Player

@Composable
fun ParticipantsScreen(
    userName: String,
    userId: String,
    existingPlayers: List<Player>,
    onAdd: (name: String) -> Unit,
    onDelete: (playerId: String) -> Unit,
    onSave: () -> Unit
) {
    var newPlayerName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Participantes", fontSize = 22.sp)
        // Muestra ya el usuario principal como primer participante
        Text(text = "1. $userName")

        LazyColumn {
            items(existingPlayers) { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(player.name)
                    Text(
                        text = "Eliminar",
                        modifier = Modifier.clickable {
                            onDelete(player.id)
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = newPlayerName,
            onValueChange = { newPlayerName = it },
            label = { Text("Nuevo jugador") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            if (newPlayerName.isNotBlank()) {
                onAdd(newPlayerName.trim())
                newPlayerName = ""
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar y jugar")
        }
    }
}
