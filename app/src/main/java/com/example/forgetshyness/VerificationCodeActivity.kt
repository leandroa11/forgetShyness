package com.example.forgetshyness

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.config.AppConfig
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.OtpManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VerificationCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra("USER_ID")
        val userName = intent.getStringExtra("USER_NAME")
        // OJO: El número de teléfono debe incluir el prefijo del país, ej: "+521234567890"
        val userPhone = intent.getStringExtra("USER_PHONE")

        // Comprobación de seguridad: si no hay datos, no se puede continuar
        if (userId == null || userName == null || userPhone == null) {
            Toast.makeText(this, "Error: Faltan datos del usuario.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            // Inyectamos las dependencias y los datos a la pantalla principal
            VerificationScreen(
                userId = userId,
                userName = userName,
                userPhone = userPhone,
                activity = this
            )
        }
    }
}

@Composable
fun VerificationScreen(
    userId: String,
    userName: String,
    userPhone: String,
    activity: Activity
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Instancias de nuestros gestores de lógica
    val otpManager = remember { OtpManager(Firebase.auth) }
    val firestoreRepository = remember { FirestoreRepository() }

    var code by remember { mutableStateOf(List(6) { "" }) }
    var isLoading by remember { mutableStateOf(false) }
    var isResendEnabled by remember { mutableStateOf(true) }
    var countdown by remember { mutableStateOf(120) }

    // Función para iniciar el temporizador de reenvío
    fun startResendTimer() {
        isResendEnabled = false
        coroutineScope.launch {
            countdown = 120
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            isResendEnabled = true
        }
    }
    /*
    // Función para enviar el OTP
    fun sendOtp() {
        startResendTimer()
        otpManager.sendOtp(
            // IMPORTANTE: Asegúrate de que el número tenga el prefijo del país. Ej: "+52..."
            phoneNumber = "+57$userPhone",
            activity = activity,
            onCodeSent = {
                Toast.makeText(context, "Código enviado al $userPhone", Toast.LENGTH_SHORT).show()
            },
            onVerificationFailed = { exception ->
                android.util.Log.e("OTP_ERROR", "La verificación del teléfono falló", exception)
                Toast.makeText(context, "Error al enviar código: ${exception.message}", Toast.LENGTH_LONG).show()
                isResendEnabled = true // Permitir reintento si falla
            }
        )
    }

    // Envía el código automáticamente la primera vez que se muestra la pantalla
    LaunchedEffect(Unit) {
        sendOtp()
    } */

    // Simulación para desarrollo
    fun sendOtp() {
        if (AppConfig.IS_DEVELOPMENT_MODE) {
            // Simulación en desarrollo
            Toast.makeText(context, "Simulación: OTP enviado correctamente", Toast.LENGTH_SHORT).show()
            startResendTimer()
        } else {
            // En producción: envía OTP real
            startResendTimer()
            otpManager.sendOtp(
                phoneNumber = "+57$userPhone",
                activity = activity,
                onCodeSent = {
                    Toast.makeText(context, "Código enviado al $userPhone", Toast.LENGTH_SHORT).show()
                },
                onVerificationFailed = { exception ->
                    android.util.Log.e("OTP_ERROR", "Error OTP", exception)
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                    isResendEnabled = true
                }
            )
        }
    }

    // Enviar OTP la primera vez (simulado)
    /*
    LaunchedEffect(Unit) {
        sendOtp()
    } */

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_3),
            contentDescription = "Fondo de pantalla",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.flecha_izquierda),
                contentDescription = "Atrás",
                tint = Color.Yellow,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Start)
                    .clickable { activity.finish() }
            )

            Spacer(modifier = Modifier.height(80.dp))

            Text("Último toque antes del brindis", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Te hemos enviado un código de 6 dígitos para que compruebes que eres tú. Ingresa el código en los siguientes espacios.", fontSize = 14.sp, color = Color.White, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                repeat(6) { index ->
                    OutlinedTextField(
                        value = code[index],
                        onValueChange = { input -> if (input.length <= 1) code = code.toMutableList().also { it[index] = input } },
                        modifier = Modifier.width(48.dp).height(56.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, cursorColor = Color.Black
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val fullCode = code.joinToString("")
                    if (fullCode.length == 6) {
                        isLoading = true
                        otpManager.verifyOtp(
                            code = fullCode,
                            onSuccess = { // El UID de Firebase no se usa aquí, pero confirma el éxito
                                coroutineScope.launch {
                                    val (activationSuccess, activationMessage) = firestoreRepository.activateUser(userId)
                                    if (activationSuccess) {
                                        Toast.makeText(context, "¡Verificación exitosa!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(context, WelcomeActivity::class.java).apply {
                                            putExtra("USER_NAME", userName)
                                        }
                                        context.startActivity(intent)
                                        activity.finishAffinity() // Cierra esta y las actividades anteriores
                                    } else {
                                        Toast.makeText(context, activationMessage, Toast.LENGTH_LONG).show()
                                    }
                                    isLoading = false
                                }
                            },
                            onError = { exception ->
                                Toast.makeText(context, "Código incorrecto. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                                isLoading = false
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black),
                shape = RoundedCornerShape(50),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("¡Que empiece la diversión!", fontWeight = FontWeight.Bold)
                }
            } 

            Spacer(modifier = Modifier.height(24.dp))

            Text("¿No llegó el código?", fontSize = 14.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { sendOtp() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black),
                shape = RoundedCornerShape(50),
                enabled = isResendEnabled && !isLoading,
                modifier = Modifier.wrapContentWidth().height(40.dp)
            ) {
                val text = if (isResendEnabled) "Reenviar código" else "Reenviar en ${countdown}s"
                Text(text, fontWeight = FontWeight.Bold)
            }
        }
    }
}
