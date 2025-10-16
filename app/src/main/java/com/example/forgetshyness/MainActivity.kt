package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WelcomeScreen(
                onEnter = {
                    startActivity(Intent(this, ExperienceActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun WelcomeScreen(onEnter: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay para mejorar contraste
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xAA000000), Color(0x00000000))
                    )
                )
        )

        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(32.dp))

            // Logo
            Image(
                painter = painterResource(R.drawable.icono_calido_forgetshyness),
                contentDescription = "Logo Forget Shyness",
                modifier = Modifier.height(220.dp)
            )

            Spacer(Modifier.height(28.dp))

            // Textos principales
            Text(
                text = stringResource(R.string.slogan1),
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.slogan2),
                fontSize = 22.sp,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            // Descripción
            Text(
                text = stringResource(R.string.description),
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(Modifier.weight(1f))

            // Botón de entrada
            Button(
                onClick = onEnter,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth(0.85f)
            ) {
                Image(
                    painter = painterResource(R.drawable.icono_boton_juego_coctel_1),
                    contentDescription = "Entrar",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.enter_button),
                    color = Color.Black
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
