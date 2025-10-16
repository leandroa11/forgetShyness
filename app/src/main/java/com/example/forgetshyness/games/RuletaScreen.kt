package com.example.forgetshyness.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import com.example.forgetshyness.data.Challenge
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun RuletaScreen(
    sessionId: String,
    participants: List<String>,
    retos: List<Challenge>,
    onFinishTurn: (challenge: Challenge, participantIndex: Int) -> Unit
) {
    var selectedChallenge by remember { mutableStateOf<Challenge?>(null) }
    var currentIndex by remember { mutableStateOf(0) }
    var isSpinning by remember { mutableStateOf(false) }
    var angle by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (participants.isEmpty()) {
            Text("No hay participantes", fontSize = 18.sp)
            return@Column
        }

        Text(text = "Turno de: ${participants[currentIndex]}", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(20.dp))

        var isSpinning by remember { mutableStateOf(false) }

        LaunchedEffect(isSpinning) {
            if (isSpinning && retos.isNotEmpty()) {
                val vueltas = 5 + Random.nextInt(5)
                val finalAngle = 360f * vueltas + Random.nextInt(360)
                val duration = 2000L
                val steps = 60
                for (step in 1..steps) {
                    angle = (finalAngle * step / steps)
                    delay(duration / steps)
                }
                isSpinning = false
                val segmentCount = retos.size
                val segmentAngle = 360f / segmentCount
                val index = ((angle % 360) / segmentAngle).toInt().coerceIn(0, retos.size - 1)
                selectedChallenge = retos[index]
            }
        }

        Button(
            onClick = { if (!isSpinning) isSpinning = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSpinning
        ) {
            Text("Girar Ruleta")
        }


        // Representación visual minimalista de la ruleta (no gráfica completa)
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)) {
            val radius = size.minDimension / 2
            val cx = center.x
            val cy = center.y
            // dibuja sector para cada reto
            val segmentAngleRad = (2 * PI / retos.size).toFloat()
            retos.forEachIndexed { i, reto ->
                // calcular ángulo medio del segmento i
                val midAngle = angle.toDouble() * PI / 180 + i * segmentAngleRad
                // punto para texto (simple)
                val px = cx + radius / 2 * cos(midAngle).toFloat()
                val py = cy + radius / 2 * sin(midAngle).toFloat()
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        reto.text,
                        px,
                        py,
                        android.graphics.Paint().apply {
                            textSize = 24f
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }

        selectedChallenge?.let { challenge ->
            Text(text = "Reto: ${challenge.text}", fontSize = 18.sp, modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                onFinishTurn(challenge, currentIndex)
                currentIndex = (currentIndex + 1) % participants.size
                selectedChallenge = null
                angle = 0f
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Aceptar reto")
            }
        }
    }
}

