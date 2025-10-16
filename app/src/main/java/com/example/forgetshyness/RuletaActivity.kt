package com.example.forgetshyness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.forgetshyness.data.Challenge
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.Player
import com.example.forgetshyness.data.Turn
import com.example.forgetshyness.games.RuletaScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RuletaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val sessionId = intent.getStringExtra("SESSION_ID") ?: ""
        if (userId.isBlank() || sessionId.isBlank()) {
            finish()
            return
        }

        setContent {
            var participantsList by remember { mutableStateOf<List<Player>>(emptyList()) }
            var retos by remember { mutableStateOf<List<Challenge>>(emptyList()) }
            val repo = remember { FirestoreRepository() }

            LaunchedEffect(Unit) {
                participantsList = repo.getParticipants(sessionId)
                // Supongamos que la ruleta solo usa “reto” tipo
                retos = repo.getChallengesByType("reto")
            }

            val participantNames = participantsList.map { it.name }

            RuletaScreen(
                sessionId = sessionId,
                participants = participantNames,
                retos = retos,
                onFinishTurn = { challenge, idx ->
                    if (idx in participantsList.indices) {
                        val participant = participantsList[idx]
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.addTurn(
                                sessionId,
                                Turn(
                                    participantId = participant.id,
                                    challengeId = challenge.id,
                                    liked = null,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            )
        }
    }
}


