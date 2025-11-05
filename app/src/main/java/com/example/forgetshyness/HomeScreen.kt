package com.example.forgetshyness

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun HomeScreen(
    userName: String,
    userId: String,
    onNavigateToGames: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToEvents: () -> Unit
) {
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
            painter = painterResource(id = R.drawable.fondo_burbujas_4),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Saludo
            Text(
                text = stringResource(R.string.home_greeting, userName),
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 30.sp,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Menú principal en vertical
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp), // Espacio uniforme entre elementos
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                MenuCard(
                    backgroundColor = Color(0xFFD24B6C),
                    icon = R.drawable.coctel_2_blanco,
                    title = stringResource(R.string.recipes_title),
                    description = stringResource(R.string.recipes_description),
                    onClick = onNavigateToRecipes
                )

                MenuCard(
                    backgroundColor = Color(0xFFF68C3F),
                    icon = R.drawable.dado,
                    title = stringResource(R.string.games_title),
                    description = stringResource(R.string.games_description),
                    onClick = onNavigateToGames
                )

                MenuCard(
                    backgroundColor = Color(0xFFB06B9A),
                    icon = R.drawable.calendario_blanco,
                    title = stringResource(R.string.events_title),
                    description = stringResource(R.string.events_description),
                    onClick = onNavigateToEvents
                )
            }
        }
    }
}

@Composable
fun MenuCard(
    backgroundColor: Color,
    icon: Int,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth() // Ocupa todo el ancho
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp, // Un poco más grande para mejorar legibilidad
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp, // Un poco más grande
            )
        }
    }
}