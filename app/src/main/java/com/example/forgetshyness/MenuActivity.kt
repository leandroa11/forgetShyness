package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.utils.Constants
import androidx.compose.ui.platform.LocalContext

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener datos del Intent usando constantes
        val userName = intent.getStringExtra(Constants.KEY_USER_NAME) ?: "Usuario"
        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""
        Log.d("MenuActivity", "User ID: $userId")

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
                onNavigateToEvents = { /* TODO: ir al módulo eventos */ }
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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        repo.seedChallengesIfEmpty(context)
    }

    HomeScreen(
        userName = userName,
        userId = userId,
        onNavigateToGames = onNavigateToGames,
        onNavigateToRecipes = onNavigateToRecipes,
        onNavigateToEvents = onNavigateToEvents
    )
}

