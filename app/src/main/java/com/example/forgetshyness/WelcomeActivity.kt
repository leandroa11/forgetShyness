package com.example.forgetshyness

<<<<<<< HEAD
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
=======
import android.content.Intent
import androidx.compose.ui.res.stringResource
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
>>>>>>> 4681740a56b14f9b3e66dba6e00b18ab7af3c3af
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
<<<<<<< HEAD
=======
import android.os.Bundle
>>>>>>> 4681740a56b14f9b3e66dba6e00b18ab7af3c3af

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userName = intent.getStringExtra("USER_NAME") ?: "Usuario"
<<<<<<< HEAD
        setContent {
            WelcomeScreen(userName = userName)
=======
        val userId = intent.getStringExtra("USER_ID") ?: ""
        setContent {
            WelcomeScreen(
                userName = userName,
                onGoToMenu = {
                    // Aquí lanzas la actividad del menú
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.putExtra("USER_NAME", userName)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                }
            )
>>>>>>> 4681740a56b14f9b3e66dba6e00b18ab7af3c3af
        }
    }
}

@Composable
<<<<<<< HEAD
fun WelcomeScreen(userName: String) {
=======
fun WelcomeScreen(
    userName: String,
    onGoToMenu: () -> Unit
) {
>>>>>>> 4681740a56b14f9b3e66dba6e00b18ab7af3c3af
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
<<<<<<< HEAD
                text = "¡Bienvenido(a),",
=======
                text = stringResource(R.string.welcome_title),
>>>>>>> 4681740a56b14f9b3e66dba6e00b18ab7af3c3af
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
            Text(
                text = userName,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 52.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.coctel_1),
<<<<<<< HEAD
                contentDescription = "Brindis",
=======
                contentDescription = stringResource(R.string.welcome_image_desc),
>>>>>>> 4681740a56b14f9b3e66dba6e00b18ab7af3c3af
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
<<<<<<< HEAD
                text = "¡Es hora de socializar y romper el hielo!",
=======
                text = stringResource(R.string.welcome_message),
>>>>>>> 4681740a56b14f9b3e66dba6e00b18ab7af3c3af
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
<<<<<<< HEAD
        }
    }
}
=======

            Spacer(modifier = Modifier.height(24.dp))

            // — Aquí agregamos el botón —
            Button(
                onClick = onGoToMenu,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
            ) {
                Text(
                    text = stringResource(R.string.go_to_menu),
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }
    }
}

>>>>>>> 4681740a56b14f9b3e66dba6e00b18ab7af3c3af
