package com.example.forgetshyness

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
                Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFFFF80AB)))
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Mostrar saludo con nombre del usuario
            Text(
                text = "Hola, $userName 👋",
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Menú Principal",
                color = Color.White,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuCard(
                title = "Recetas",
                description = "Explora recetas divertidas",
                imageRes = R.drawable.coctel_1,
                onClick = onNavigateToRecipes
            )

            MenuCard(
                title = "Juegos",
                description = "Descubre juegos interactivos",
                imageRes = R.drawable.coctel_2,
                onClick = onNavigateToGames
            )

            MenuCard(
                title = "Eventos",
                description = "Crea o gestiona tus eventos",
                imageRes = R.drawable.calendario,
                onClick = onNavigateToEvents
            )
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    description: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}




