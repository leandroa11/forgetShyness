package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.forgetshyness.games.ParticipantsScreen
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.Player
import com.example.forgetshyness.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParticipantsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra(Constants.KEY_USER_NAME) ?: "Usuario"
        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""
        Log.d("ParticipantsActivity", "onCreate: userName=$userName, userId=$userId")

        setContent {
            var players by remember { mutableStateOf<List<Player>>(emptyList()) }
            val repo = remember { FirestoreRepository() }

            LaunchedEffect(Unit) {
                if (userId.isBlank()) {
                    Log.e("ParticipantsActivity", "userId está en blanco, no buscar jugadores")
                } else {
                    try {
                        val loaded = repo.getPlayersByUser(userId)
                        players = loaded
                    } catch (e: Exception) {
                        Log.e("ParticipantsActivity", "Error cargando jugadores", e)
                    }
                }
            }

            ParticipantsScreen(
                userName = userName,
                userId = userId,
                existingPlayers = players,
                onAdd = { name ->
                    Log.d("ParticipantsActivity", "onAdd called with name = $name")
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val newId = repo.addPlayer(Player(name = name, userId = userId))
                            Log.d("ParticipantsActivity", "addPlayer returned id = $newId")
                            val updated = repo.getPlayersByUser(userId)
                            withContext(Dispatchers.Main) {
                                players = updated
                            }
                        } catch (e: Exception) {
                            Log.e("ParticipantsActivity", "Error al agregar jugador", e)
                        }
                    }
                },
                onDelete = { pid ->
                    Log.d("ParticipantsActivity", "onDelete called pid = $pid")
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            repo.deletePlayer(pid)
                            val updated = repo.getPlayersByUser(userId)
                            withContext(Dispatchers.Main) {
                                players = updated
                            }
                        } catch (e: Exception) {
                            Log.e("ParticipantsActivity", "Error eliminando jugador", e)
                        }
                    }
                },
                onSave = {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val sessionId = repo.createGameSession(
                                hostUserId = userId,
                                gameType = Constants.DEFAULT_GAME_TYPE
                            )
                            repo.addParticipantToSession(sessionId, Player(name = userName, userId = userId))
                            players.forEach { p -> repo.addParticipantToSession(sessionId, p) }

                            withContext(Dispatchers.Main) {
                                val intent = Intent(this@ParticipantsActivity, GamesMenuActivity::class.java).apply {
                                    putExtra(Constants.KEY_USER_NAME, userName)
                                    putExtra(Constants.KEY_USER_ID, userId)
                                    putExtra(Constants.KEY_SESSION_ID, sessionId)
                                }
                                startActivity(intent)
                                finish()
                            }
                        } catch (e: Exception) {
                            Log.e("ParticipantsActivity", "Error creando sesión", e)
                        }
                    }
                },
                onBackClick = { finish() }
            )
        }
    }
}
