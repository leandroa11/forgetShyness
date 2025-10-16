package com.example.forgetshyness.games

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.MenuCard
import com.example.forgetshyness.R


@Composable
fun GamesMenuScreen(
    onAddParticipants: () -> Unit,
    onChooseGame: (gameType: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(text = "Elige un juego", fontSize = 24.sp)
        MenuCard(
            title = "Ruleta Picante",
            description = "Gira la ruleta",
            imageRes = R.drawable.ruleta,  // pon tu ícono
            onClick = { onChooseGame("ruleta") }
        )
        MenuCard(
            title = "Verdad o Reto",
            description = "Elige verdad o reto",
            imageRes = R.drawable.verdadr_reto,
            onClick = { onChooseGame("verdad_o_reto") }
        )
        Button(onClick = onAddParticipants, modifier = Modifier.fillMaxWidth()) {
            Text("Añadir Participantes")
        }
    }
}
