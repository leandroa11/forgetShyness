package com.example.forgetshyness.games

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.data.Challenge
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun VerdadORetoScreen(
    sessionId: String,
    participants: List<String>,
    allChallenges: List<Challenge>,
    onFinishTurn: (challenge: Challenge, participantIndex: Int) -> Unit
) {
    var selectedChallenge by remember { mutableStateOf<Challenge?>(null) }
    var currentIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (participants.isEmpty()) {
            Text("No hay participantes", fontSize = 18.sp)
            return@Column
        }

        Text("Turno de: ${participants[currentIndex]}", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                // Filtrar solo verdades del conjunto
                val verdadList = allChallenges.filter { it.type == "verdad" }
                if (verdadList.isNotEmpty()) {
                    selectedChallenge = verdadList.random()
                }
            }) {
                Text("Verdad")
            }
            Button(onClick = {
                val retoList = allChallenges.filter { it.type == "reto" }
                if (retoList.isNotEmpty()) {
                    selectedChallenge = retoList.random()
                }
            }) {
                Text("Reto")
            }
            Button(onClick = {
                if (allChallenges.isNotEmpty()) {
                    selectedChallenge = allChallenges.random()
                }
            }) {
                Text("Aleatorio")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        selectedChallenge?.let { challenge ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Elección: ${challenge.text}", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    onFinishTurn(challenge, currentIndex)
                    currentIndex = (currentIndex + 1) % participants.size
                    selectedChallenge = null
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Continuar")
                }
            }
        }
    }
}


