package com.example.forgetshyness.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.forgetshyness.R
import com.example.forgetshyness.data.Challenge
import com.example.forgetshyness.data.Player
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

@Composable
fun RuletaScreen(
    sessionId: String,
    participants: List<Player>,
    challenges: List<Challenge>,
    onSaveTurn: (Challenge, Player, Boolean?) -> Unit,
    onBackClick: () -> Unit
) {
    var currentPlayerIndex by remember { mutableStateOf(0) }
    var selectedChallenge by remember { mutableStateOf<Challenge?>(null) }
    var isDialogVisible by remember { mutableStateOf(false) }

    val currentPlayer = participants.getOrNull(currentPlayerIndex) ?: return

    var rotationAngle by remember { mutableStateOf(0f) }
    val animatedRotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val visibleChallenges = remember(challenges) {
        if (challenges.size > 6) challenges.shuffled().take(6) else challenges
    }

    val segmentColors = listOf(
        Color(0xFFFF007F),
        Color(0xFFFFA500),
        Color(0xFFFFFF00),
        Color(0xFF00FF7F),
        Color(0xFF00BFFF),
        Color(0xFFBA55D3)
    )

    val segmentIcons = listOf(
        ImageBitmap.imageResource(id = R.drawable.beso),
        ImageBitmap.imageResource(id = R.drawable.reto_doble),
        ImageBitmap.imageResource(id = R.drawable.baile),
        ImageBitmap.imageResource(id = R.drawable.confesion),
        ImageBitmap.imageResource(id = R.drawable.prenda),
        ImageBitmap.imageResource(id = R.drawable.shot)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                contentDescription = stringResource(id = R.string.content_desc_back),
                tint = Color.Yellow,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.roulette_title),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentPlayer.name,
                color = Color.White,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(330.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                                center = center,
                                radius = size.minDimension / 1.4f
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(300.dp)
                        .rotate(animatedRotation.value)
                ) {
                    val sweepAngle = 360f / visibleChallenges.size
                    val radius = size.minDimension / 2f

                    visibleChallenges.forEachIndexed { index, challenge ->
                        val startAngle = index * sweepAngle

                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    segmentColors[index % segmentColors.size],
                                    segmentColors[(index + 1) % segmentColors.size]
                                )
                            ),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            style = Fill,
                            size = Size(size.width, size.height)
                        )

                        drawArc(
                            color = Color.White.copy(alpha = 0.8f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 5f),
                            size = Size(size.width, size.height)
                        )

                        val angle = (startAngle + sweepAngle / 2f) * PI / 180f
                        val iconX = (center.x + cos(angle) * radius / 1.6f).toFloat()
                        val iconY = (center.y + sin(angle) * radius / 1.6f).toFloat()

                        drawImage(
                            image = segmentIcons[index % segmentIcons.size],
                            dstOffset = IntOffset((iconX - 50f).toInt(), (iconY - 50f).toInt()),
                            dstSize = IntSize(100, 100),
                            alpha = 0.95f
                        )
                    }

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFFFF4081)),
                            center = center,
                            radius = 90f
                        ),
                        radius = 90f
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = radius,
                        style = Stroke(width = 6f)
                    )
                }

                Canvas(modifier = Modifier.size(330.dp)) {
                    val trianglePath = Path().apply {
                        moveTo(size.width / 2 - 30, 20f)
                        lineTo(size.width / 2 + 30, 20f)
                        lineTo(size.width / 2f, 90f)
                        close()
                    }
                    drawPath(
                        path = trianglePath,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFC107), Color(0xFFFF5722))
                        )
                    )
                    drawPath(
                        path = trianglePath,
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color.Transparent)
                        ),
                        style = Stroke(width = 5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    val newRotation = rotationAngle + 720 + Random.nextInt(0, 360)
                    rotationAngle = newRotation.toFloat()
                    scope.launch {
                        animatedRotation.animateTo(
                            targetValue = rotationAngle,
                            animationSpec = tween(2500, easing = FastOutSlowInEasing)
                        )
                        val chosenIndex =
                            (visibleChallenges.indices.random() + currentPlayerIndex) % visibleChallenges.size
                        selectedChallenge = visibleChallenges[chosenIndex]
                        isDialogVisible = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(55.dp)
            ) {
                Text(
                    text = stringResource(R.string.roulette_spin_button),
                    color = Color(0xFF6C3905),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        if (isDialogVisible && selectedChallenge != null) {
            AlertDialog(
                onDismissRequest = { isDialogVisible = false },
                confirmButton = {},
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.roulette_dialog_challenge_for, currentPlayer.name),
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = selectedChallenge!!.text,
                            color = Color.DarkGray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = {
                                onSaveTurn(selectedChallenge!!, currentPlayer, true)
                                isDialogVisible = false
                                currentPlayerIndex = (currentPlayerIndex + 1) % participants.size
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.like),
                                    contentDescription = stringResource(R.string.content_desc_like),
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                            IconButton(onClick = {
                                onSaveTurn(selectedChallenge!!, currentPlayer, false)
                                isDialogVisible = false
                                currentPlayerIndex = (currentPlayerIndex + 1) % participants.size
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.dislike),
                                    contentDescription = stringResource(R.string.content_desc_dislike),
                                    tint = Color(0xFFB3405F),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}