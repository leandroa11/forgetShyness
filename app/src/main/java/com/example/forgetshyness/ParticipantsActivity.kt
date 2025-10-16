package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.forgetshyness.games.ParticipantsScreen
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ParticipantsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra("USER_NAME") ?: "Usuario"
        val userId = intent.getStringExtra("USER_ID") ?: ""
        Log.d("ParticipantsActivity", "onCreate: userName=$userName, userId=$userId")

        setContent {
            var players by remember { mutableStateOf<List<Player>>(emptyList()) }
            val repo = remember { FirestoreRepository() }

            LaunchedEffect(Unit) {
                if (userId.isBlank()) {
                    Log.e("ParticipantsActivity", "userId está en blanco, no buscar jugadores")
                } else {
                    val loaded = repo.getPlayersByUser(userId)
                    Log.d("ParticipantsActivity", "loaded players size = ${loaded.size}")
                    players = loaded
                }
            }

            ParticipantsScreen(
                userName = userName,
                userId = userId,
                existingPlayers = players,
                onAdd = { name ->
                    Log.d("ParticipantsActivity", "onAdd called with name = $name")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val newId = repo.addPlayer(Player(name = name, userId = userId))
                            Log.d("ParticipantsActivity", "addPlayer returned id = $newId")
                            val updated = repo.getPlayersByUser(userId)
                            Log.d("ParticipantsActivity", "updated list size = ${updated.size}")
                            launch(Dispatchers.Main) {
                                players = updated
                            }
                        } catch (e: Exception) {
                            Log.e("ParticipantsActivity", "Error al agregar jugador", e)
                        }
                    }
                },
                onDelete = { pid ->
                    Log.d("ParticipantsActivity", "onDelete called pid = $pid")
                    CoroutineScope(Dispatchers.IO).launch {
                        repo.deletePlayer(pid)
                        val updated = repo.getPlayersByUser(userId)
                        launch(Dispatchers.Main) {
                            players = updated
                        }
                    }
                },
                onSave = {
                    // Aquí creas la sesión y agregas participantes
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val sessionId = repo.createGameSession(hostUserId = userId, gameType = "pendiente")
                            // Agrega host (usuario principal)
                            repo.addParticipantToSession(sessionId, Player(name = userName, userId = userId))
                            // Agrega otros jugadores
                            players.forEach { p ->
                                repo.addParticipantToSession(sessionId, p)
                            }
                            launch(Dispatchers.Main) {
                                val intent = Intent(this@ParticipantsActivity, GamesMenuActivity::class.java)
                                intent.putExtra("USER_NAME", userName)
                                intent.putExtra("USER_ID", userId)
                                intent.putExtra("SESSION_ID", sessionId)
                                startActivity(intent)
                                finish()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }
    }
}




