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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        // Fondo de burbujas (imagen decorativa)
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_4),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp), // Padding vertical eliminado
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Contenido centrado verticalmente
        ) {
            // Título con saludo
            Text(
                text = "\"¡Hola, $userName!\n¿Listo para romper el hielo?\"",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 30.sp,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Menú de tres tarjetas
            Row(
                horizontalArrangement = Arrangement.Center, // Tarjetas centradas horizontalmente
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {MenuCard(
                backgroundColor = Color(0xFFD24B6C),
                icon = R.drawable.coctel_2_blanco,
                title = "Recetas para la ocasión",
                description = "Mezcla la diversión,\nDescubre cocteles que le ponen un sabor único a tus planes.",
                onClick = onNavigateToRecipes
            )


                Spacer(modifier = Modifier.width(12.dp)) // Espaciador añadido

                MenuCard(
                    backgroundColor = Color(0xFFF68C3F),
                    icon = R.drawable.dado,
                    title = "Juegos interactivos",
                    description = "Pon a prueba tu ingenio.\nDinámicas y juegos para que las conversaciones no paren.",
                    onClick = onNavigateToGames
                )

                Spacer(modifier = Modifier.width(12.dp)) // Espaciador añadido

                MenuCard(
                    backgroundColor = Color(0xFFB06B9A),
                    icon = R.drawable.calendario_blanco,
                    title = "Asistente de planificación",
                    description = "Organiza la reunión.\nEncuentra licorerías y prepara tus eventos sin excusas.",
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
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}




