package com.example.forgetshyness

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.forgetshyness.data.FirestoreRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Función de Validación ---
private fun validatePhone(phone: String): String {
    if (!phone.matches(Regex("^[0-9]+$"))) return "Solo se admiten números."
    if (phone.length != 10) return "Debe tener 10 dígitos."
    return ""
}

class MainActivity : ComponentActivity() {
    private val firestoreRepository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var phoneNumber by remember { mutableStateOf("") }
            var phoneError by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }

            WelcomeScreen(
                phoneNumber = phoneNumber,
                phoneError = phoneError,
                isLoading = isLoading,
                onPhoneNumberChange = {
                    phoneNumber = it
                    phoneError = validatePhone(it)
                },
                onEnter = {
                    phoneError = validatePhone(phoneNumber)
                    if (phoneError.isEmpty()) {
                        lifecycleScope.launch {
                            isLoading = true
                            delay(1500) // Simular una pequeña demora
                            val user = firestoreRepository.getUserByPhone(phoneNumber)
                            isLoading = false

                            val intent = if (user != null) {
                                Intent(this@MainActivity, MenuActivity::class.java).apply {
                                    putExtra("USER_NAME", user.name)
                                    putExtra("USER_ID", user.id)
                                }
                            } else {
                                Intent(this@MainActivity, ExperienceActivity::class.java).apply {
                                    putExtra("phoneNumber", phoneNumber)
                                }
                            }
                            startActivity(intent)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    phoneNumber: String,
    phoneError: String,
    isLoading: Boolean,
    onPhoneNumberChange: (String) -> Unit,
    onEnter: () -> Unit
) {

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

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                modifier = Modifier.fillMaxWidth(0.85f),
                placeholder = { Text("Número de teléfono") },
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.telefono),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedPlaceholderColor = Color(0xFFFF6F00),
                    unfocusedPlaceholderColor = Color(0xFFFF6F00),
                    errorBorderColor = Color.Red,
                    errorLeadingIconColor = Color.Red,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = phoneError.isNotEmpty(),
                enabled = !isLoading
            )

            if (phoneError.isNotEmpty()) {
                Text(
                    text = phoneError,
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                )
            } else {
                 Spacer(Modifier.height(24.dp))
            }


            Spacer(Modifier.weight(1f))


            // Botón de entrada
            Button(
                onClick = onEnter,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth(0.85f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFFE65100),
                        strokeWidth = 3.dp
                    )
                } else {
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
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}