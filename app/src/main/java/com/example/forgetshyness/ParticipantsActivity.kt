package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.Player
import com.example.forgetshyness.games.ParticipantsScreen
import com.example.forgetshyness.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParticipantsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra(Constants.KEY_USER_NAME) ?: "Usuario"
        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""
        val incomingSessionId = intent.getStringExtra(Constants.KEY_SESSION_ID)

        Log.d("ParticipantsActivity", "onCreate: userName=$userName, userId=$userId, incomingSession=$incomingSessionId")

        setContent {
            val repo = remember { FirestoreRepository() }
            var players by remember { mutableStateOf<List<Player>>(emptyList()) }
            var sessionId by remember { mutableStateOf<String?>(incomingSessionId) }
            var isLoading by remember { mutableStateOf(true) }

            // 🔹 Cargar o crear sesión
            LaunchedEffect(Unit) {
                try {
                    val sid = sessionId ?: repo.getOrCreateSessionForHost(
                        hostUserId = userId,
                        hostName = userName,
                        gameType = Constants.DEFAULT_GAME_TYPE
                    ).also { sessionId = it }

                    // 🔹 Cargar los participantes actuales
                    players = repo.getParticipants(sid)
                } catch (e: Exception) {
                    Log.e("ParticipantsActivity", "Error cargando sesión: ${e.message}", e)
                } finally {
                    isLoading = false
                }
            }

            // 🔹 Filtramos al host para que no se muestre visualmente
            val visiblePlayers = players.filter { it.userId != userId && it.id != userId }

            if (!isLoading) {
                ParticipantsScreen(
                    userName = userName,
                    userId = userId,
                    existingPlayers = visiblePlayers,
                    onAdd = { name ->
                        if (sessionId.isNullOrBlank()) return@ParticipantsScreen
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val newPlayer = Player(
                                    id = name.lowercase() + "_" + System.currentTimeMillis(),
                                    name = name,
                                    userId = "guest_${System.currentTimeMillis()}" // evitar duplicar host
                                )
                                repo.addParticipantToSession(sessionId!!, newPlayer)
                                val updated = repo.getParticipants(sessionId!!)
                                withContext(Dispatchers.Main) { players = updated }
                            } catch (e: Exception) {
                                Log.e("ParticipantsActivity", "Error al agregar participante: ${e.message}", e)
                            }
                        }
                    },
                    onDelete = { pid ->
                        if (sessionId.isNullOrBlank()) return@ParticipantsScreen
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val player = players.find { it.id == pid }
                                if (player != null && player.userId != userId) {
                                    repo.removeParticipantFromSession(sessionId!!, player)
                                    val updated = repo.getParticipants(sessionId!!)
                                    withContext(Dispatchers.Main) { players = updated }
                                }
                            } catch (e: Exception) {
                                Log.e("ParticipantsActivity", "Error al eliminar participante: ${e.message}", e)
                            }
                        }
                    },
                    onSave = {
                        if (sessionId.isNullOrBlank()) return@ParticipantsScreen
                        lifecycleScope.launch(Dispatchers.Main) {
                            val intent = Intent(this@ParticipantsActivity, GamesMenuActivity::class.java).apply {
                                putExtra(Constants.KEY_USER_NAME, userName)
                                putExtra(Constants.KEY_USER_ID, userId)
                                putExtra(Constants.KEY_SESSION_ID, sessionId)
                            }
                            startActivity(intent)
                            finish()
                        }
                    },
                    onBackClick = { finish() }
                )
            }
        }
    }
}




