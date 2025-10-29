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
import com.example.forgetshyness.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RuletaActivity : ComponentActivity() {
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

            if (participants.isNotEmpty() && challenges.isNotEmpty()) {
                RuletaScreen(
                    sessionId = sessionId,
                    participants = participants,
                    challenges = challenges,
                    onSaveTurn = { challenge, participant, liked ->
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









