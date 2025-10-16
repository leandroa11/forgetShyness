package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.forgetshyness.data.Challenge
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.Player
import com.example.forgetshyness.data.Turn
import com.example.forgetshyness.games.TruthOrDareScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TruthOrDareActivity : ComponentActivity() {
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

            LaunchedEffect(sessionId) {
                val participants = repo.getParticipants(sessionId)
                val challenges = repo.getAllChallenges()
                participantsList = participants
                challengesAll = challenges
            }

            val participantNames = participantsList.map { it.name }

            TruthOrDareScreen(
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
                },
                onBackClick = {
                    finish()
                }
            )
        }
    }
}
