package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.forgetshyness.games.GamesMenuScreen
import com.example.forgetshyness.utils.Constants

class GamesMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra(Constants.KEY_USER_NAME) ?: ""
        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""
        val sessionId = intent.getStringExtra(Constants.KEY_SESSION_ID) ?: ""

        if (userId.isBlank() || sessionId.isBlank()) {
            finish()
            return
        }

        setContent {
            GamesMenuScreen(
                onAddParticipants = {
                    val intent = Intent(this, ParticipantsActivity::class.java).apply {
                        putExtra(Constants.KEY_USER_NAME, userName)
                        putExtra(Constants.KEY_USER_ID, userId)
                        putExtra(Constants.KEY_SESSION_ID, sessionId)
                    }
                    startActivity(intent)
                },
                onChooseGame = { gameType ->
                    when (gameType) {
                        "ruleta" -> {
                            val intent = Intent(this, RuletaActivity::class.java).apply {
                                putExtra(Constants.KEY_USER_NAME, userName) // ✅ agregado
                                putExtra(Constants.KEY_USER_ID, userId)
                                putExtra(Constants.KEY_SESSION_ID, sessionId)
                            }
                            startActivity(intent)
                        }

                        "verdad_o_reto" -> {
                            val intent = Intent(this, TruthOrDareActivity::class.java).apply {
                                putExtra(Constants.KEY_USER_NAME, userName) // ✅ agregado
                                putExtra(Constants.KEY_USER_ID, userId)
                                putExtra(Constants.KEY_SESSION_ID, sessionId)
                            }
                            startActivity(intent)
                        }
                    }
                },
                onBackClick = {
                    val intent = Intent(this, MenuActivity::class.java).apply {
                        putExtra(Constants.KEY_USER_NAME, userName)
                        putExtra(Constants.KEY_USER_ID, userId)
                        putExtra(Constants.KEY_SESSION_ID, sessionId)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}



