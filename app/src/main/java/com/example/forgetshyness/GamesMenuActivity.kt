package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.forgetshyness.games.GamesMenuScreen

class GamesMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userName = intent.getStringExtra("USER_NAME") ?: ""
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val sessionId = intent.getStringExtra("SESSION_ID") ?: ""

        // Validación
        if (userId.isBlank() || sessionId.isBlank()) {
            finish()
            return
        }

        setContent {
            GamesMenuScreen(
                onAddParticipants = {
                    // Lógica para volver a agregar participantes (opcional)
                    val intent = Intent(this, ParticipantsActivity::class.java)
                    intent.putExtra("USER_NAME", userName)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                },
                onChooseGame = { gameType ->
                    if (gameType == "ruleta") {
                        val intent = Intent(this, RuletaActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        intent.putExtra("SESSION_ID", sessionId)
                        startActivity(intent)
                    } else if (gameType == "verdad_o_reto") {
                        val intent = Intent(this, VerdadRetoActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        intent.putExtra("SESSION_ID", sessionId)
                        startActivity(intent)
                    }
                }
            )
        }
    }
}


