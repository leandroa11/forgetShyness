package com.example.forgetshyness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.forgetshyness.data.Challenge
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.Player
import com.example.forgetshyness.data.Turn
import com.example.forgetshyness.games.VerdadORetoScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VerdadRetoActivity : ComponentActivity() {
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
            var challengesAll by remember { mutableStateOf<List<Challenge>>(emptyList()) }
            val repo = remember { FirestoreRepository() }

            LaunchedEffect(Unit) {
                participantsList = repo.getParticipants(sessionId)
                challengesAll = repo.getAllChallenges()
            }

            val participantNames = participantsList.map { it.name }

            VerdadORetoScreen(
                sessionId = sessionId,
                participants = participantNames,
                allChallenges = challengesAll,
                onFinishTurn = { challenge, idx ->
                    if (idx in participantsList.indices) {
                        val participant = participantsList[idx]
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.addTurn(sessionId, Turn(
                                participantId = participant.id,
                                challengeId = challenge.id,
                                liked = null,
                                timestamp = System.currentTimeMillis()
                            ))
                        }
                    }
                }
            )
        }
    }
}

