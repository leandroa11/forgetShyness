package com.example.forgetshyness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.forgetshyness.data.Challenge
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.Player
import com.example.forgetshyness.data.Turn
import com.example.forgetshyness.games.TruthOrDareScreen
import com.example.forgetshyness.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TruthOrDareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""
        val sessionId = intent.getStringExtra(Constants.KEY_SESSION_ID) ?: ""
        val userName = intent.getStringExtra(Constants.KEY_USER_NAME) ?: "Usuario"

        if (userId.isBlank() || sessionId.isBlank()) {
            finish()
            return
        }

        setContent {
            val repo = remember { FirestoreRepository() }
            var participants by remember { mutableStateOf<List<Player>>(emptyList()) }
            var challenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }

            LaunchedEffect(sessionId) {
                val list = repo.getParticipants(sessionId).toMutableList()

                // ✅ Aseguramos que el host esté presente
                val hostAlreadyIncluded = list.any { it.userId == userId }
                if (!hostAlreadyIncluded) {
                    list.add(0, Player(id = userId, name = userName, userId = userId))
                }

                participants = list
                challenges = repo.getAllChallenges()
            }

            val participantNames = participants.map { it.name }

            TruthOrDareScreen(
                sessionId = sessionId,
                participants = participantNames,
                allChallenges = challenges,
                onFinishTurn = { challenge, idx ->
                    if (idx in participants.indices) {
                        val participant = participants[idx]
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
                },
                onBackClick = { finish() }
            )
        }
    }
}



