package com.example.forgetshyness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.media3.common.util.Log
import com.example.forgetshyness.data.Challenge
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.Player
import com.example.forgetshyness.data.Turn
import com.example.forgetshyness.games.RuletaScreen
import com.example.forgetshyness.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RuletaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""
        val sessionId = intent.getStringExtra(Constants.KEY_SESSION_ID) ?: ""

        if (userId.isBlank() || sessionId.isBlank()) {
            finish()
            return
        }

        setContent {
            val repo = remember { FirestoreRepository() }
            var participants by remember { mutableStateOf<List<Player>>(emptyList()) }
            var challenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }

            // Cargar participantes y retos desde Firestore
            LaunchedEffect(sessionId) {
                participants = repo.getParticipants(sessionId)
                challenges = repo.getAllChallenges()
            }

            if (participants.isNotEmpty() && challenges.isNotEmpty()) {
                RuletaScreen(
                    sessionId = sessionId,
                    participants = participants,
                    challenges = challenges,
                    onSaveTurn = { challenge, participant, liked ->
                        /* Log.d("RuletaActivity", "Guardando turno: ${participant.name} → ${challenge.text} | liked: $liked") */
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.addTurn(
                                sessionId,
                                Turn(
                                    participantId = participant.id,
                                    challengeId = challenge.id,
                                    liked = liked,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}





