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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.R
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
                text = stringResource(R.string.truth_or_dare_loading),
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
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_3),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.flecha_izquierda),
                contentDescription = stringResource(R.string.content_desc_back),
                tint = Color(0xFFFFCB3C),
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
            Text(
                text = stringResource(R.string.truth_or_dare_title),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Icon(
                painter = painterResource(id = R.drawable.diblo_angel),
                contentDescription = stringResource(R.string.content_desc_angel_devil_icon),
                tint = Color.Black,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.truth_or_dare_subtitle),
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.truth_or_dare_turn_prompt, participants[currentIndex]),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                    text = stringResource(R.string.truth_or_dare_button_truth),
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
                    text = stringResource(R.string.truth_or_dare_button_dare),
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
                    text = stringResource(R.string.truth_or_dare_button_random),
                    color = Color(0xFF6C3905),
                    fontWeight = FontWeight.Bold
                )
            }

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
                            text = stringResource(R.string.truth_or_dare_button_continue),
                            color = Color(0xFF6C3905),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}




