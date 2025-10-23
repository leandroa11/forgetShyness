package com.example.forgetshyness.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.data.Challenge

@Composable
fun TruthOrDareScreen(
    sessionId: String,
    participants: List<String>,
    allChallenges: List<Challenge>,
    onFinishTurn: (challenge: Challenge, participantIndex: Int) -> Unit,
    onBackClick: () -> Unit,
) {
    var selectedChallenge by remember { mutableStateOf<Challenge?>(null) }
    var currentIndex by remember { mutableStateOf(0) }

    // ✅ Evitar crash si aún no hay datos
    if (participants.isEmpty() || allChallenges.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFB3405F),
                            Color(0xFFDD6D47)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cargando jugadores...",
                color = Color.White,
                fontSize = 18.sp
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB3405F),
                        Color(0xFFDD6D47)
                    )
                )
            )
    ) {
        // Fondo decorativo (burbujas)
        Image(
            painter = painterResource(id = com.example.forgetshyness.R.drawable.fondo_burbujas_3),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Flecha para volver
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = com.example.forgetshyness.R.drawable.flecha_izquierda),
                contentDescription = "Volver",
                tint = Color.Yellow,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Título principal
            Text(
                text = "Verdad o Reto",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Ícono central
            Icon(
                painter = painterResource(id = com.example.forgetshyness.R.drawable.diblo_angel), // cambia por tu recurso
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Texto de descripción
            Text(
                text = "¿Confesar tus secretos o aceptar un reto picante?",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Nombre del participante actual
            Text(
                text = "${participants[currentIndex]} ¿Qué eliges?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones principales
            Button(
                onClick = {
                    val verdadList = allChallenges.filter { it.type == "verdad" }
                    if (verdadList.isNotEmpty()) selectedChallenge = verdadList.random()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD24C)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Elijo verdad 😇",
                    color = Color(0xFF6C3905),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val retoList = allChallenges.filter { it.type == "reto" }
                    if (retoList.isNotEmpty()) selectedChallenge = retoList.random()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD24C)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Elijo reto 😈",
                    color = Color(0xFF6C3905),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (allChallenges.isNotEmpty()) selectedChallenge = allChallenges.random()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD24C)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Que sea aleatorio 🔀",
                    color = Color(0xFF6C3905),
                    fontWeight = FontWeight.Bold
                )
            }

            // Resultado del desafío elegido
            selectedChallenge?.let { challenge ->
                Spacer(modifier = Modifier.height(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = challenge.text,
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onFinishTurn(challenge, currentIndex)
                            currentIndex = (currentIndex + 1) % participants.size
                            selectedChallenge = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD24C)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Continuar",
                            color = Color(0xFF6C3905),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


