package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.forgetshyness.data.Challenge
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.utils.Constants
import com.example.forgetshyness.EventsActivity

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener datos del Intent usando constantes
        val userName = intent.getStringExtra(Constants.KEY_USER_NAME) ?: "Usuario"
        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""
        Log.d("MenuActivity", "User ID: $userId")
        Log.d("MenuActivity", "Usuario: $userName")

        setContent {
            HomeScreenWithSeed(
                userName = userName,
                userId = userId,
                onNavigateToGames = {
                    val intent = Intent(this, ParticipantsActivity::class.java).apply {
                        putExtra(Constants.KEY_USER_NAME, userName)
                        putExtra(Constants.KEY_USER_ID, userId)
                    }
                    startActivity(intent)
                },
                onNavigateToRecipes = { /* TODO: ir al módulo recetas */ },
                onNavigateToEvents = {
                    val intent = Intent(this, EventsActivity::class.java).apply {
                        putExtra(Constants.KEY_USER_ID, userId)
                        putExtra(Constants.KEY_USER_NAME, userName)
                    }
                    Log.d("MenuActivity", "Navegando a EventsActivity con userId=$userId y userName=$userName")
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun HomeScreenWithSeed(
    userName: String,
    userId: String,
    onNavigateToGames: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToEvents: () -> Unit
) {
    val repo = remember { FirestoreRepository() }

    LaunchedEffect(Unit) {
        val defaultChallenges = listOf(
            // VERDADES
            Challenge(id = "v1", type = "verdad", text = "¿Cuál ha sido tu momento más vergonzoso en público?"),
            Challenge(id = "v2", type = "verdad", text = "¿Qué es lo más loco que has hecho por amor?"),
            Challenge(id = "v3", type = "verdad", text = "¿Qué hábito extraño tienes cuando nadie te ve?"),
            Challenge(id = "v4", type = "verdad", text = "¿Cuál fue la última mentira que dijiste?"),
            Challenge(id = "v5", type = "verdad", text = "¿Qué es algo que nunca le has contado a nadie?"),

            // RETOS
            Challenge(id = "r1", type = "reto", text = "Imita a un famoso hasta que alguien adivine quién es."),
            Challenge(id = "r2", type = "reto", text = "Envía un mensaje divertido a la última persona con la que hablaste."),
            Challenge(id = "r3", type = "reto", text = "Habla con acento durante los próximos 2 turnos."),
            Challenge(id = "r4", type = "reto", text = "Haz 10 sentadillas mientras todos te miran."),
            Challenge(id = "r5", type = "reto", text = "Di el abecedario al revés sin equivocarte.")
        )

        repo.initializeChallengesIfEmpty(defaultChallenges)
    }




    HomeScreen(
        userName = userName,
        userId = userId,
        onNavigateToGames = onNavigateToGames,
        onNavigateToRecipes = onNavigateToRecipes,
        onNavigateToEvents = onNavigateToEvents
    )
}
