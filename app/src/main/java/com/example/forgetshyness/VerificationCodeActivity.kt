package com.example.forgetshyness

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.res.stringResource
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
        val userPhone = intent.getStringExtra("USER_PHONE")

        if (userId == null || userName == null || userPhone == null) {
            Toast.makeText(this, getString(R.string.error_missing_user_data), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
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

    // Función para enviar el código OTP
    fun sendOtp() {
        // En modo desarrollo, simulamos el envío para no gastar cuota de SMS
        if (AppConfig.IS_DEVELOPMENT_MODE) {
            Toast.makeText(context, context.getString(R.string.otp_simulation_sent), Toast.LENGTH_SHORT).show()
            startResendTimer()
        } else {
            // En modo producción, enviamos el SMS real
            startResendTimer() // Inicia el contador inmediatamente
            isLoading = true   // Bloquea la UI mientras se envía
            otpManager.sendOtp(
                // 💡 Asegura que el formato del número sea E.164 (+códigoPaísNúmero)
                phoneNumber = if (userPhone.startsWith("+")) userPhone else "+57$userPhone",
                activity = activity,
                onCodeSent = {
                    isLoading = false // Desbloquea la UI
                    Toast.makeText(context, context.getString(R.string.otp_sent_to, userPhone), Toast.LENGTH_SHORT).show()
                },
                onVerificationFailed = { exception ->
                    isLoading = false // Desbloquea la UI en caso de error
                    // 💡 Log detallado para depuración
                    Log.e("OTP_ERROR", "Error al enviar OTP a $userPhone", exception)
                    Toast.makeText(context, context.getString(R.string.otp_error_prefix) + " ${exception.message}", Toast.LENGTH_LONG).show()
                    isResendEnabled = true // Permite reintentar inmediatamente si falló
                }
            )
        }
    }

    // 💡 Envía el código OTP automáticamente al entrar en la pantalla
    LaunchedEffect(Unit) {
        sendOtp()
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_3),
            contentDescription = stringResource(R.string.background_desc),
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
                contentDescription = stringResource(R.string.back_button_desc),
                tint = Color.Yellow,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Start)
                    .clickable { activity.finish() }
            )

            Spacer(modifier = Modifier.height(80.dp))

            Text(text = stringResource(R.string.verification_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.verification_instructions, userPhone), fontSize = 14.sp, color = Color.White, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                repeat(6) { index ->
                    OutlinedTextField(
                        value = code[index],
                        onValueChange = { input -> if (input.length <= 1) code = code.toMutableList().also { it[index] = input } },
                        modifier = Modifier.width(48.dp).height(56.dp),
                        singleLine = true,
                        // 💡 Deshabilita los campos mientras se verifica
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, cursorColor = Color.Black,
                            // 💡 Color para estado deshabilitado
                            disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
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
                            onSuccess = { // El código es correcto. Ahora actualizamos la DB.
                                coroutineScope.launch {
                                    // 💡 Llama a la función para cambiar el estado del usuario en Firestore
                                    val (activationSuccess, activationMessage) = firestoreRepository.activateUser(userId)

                                    if (activationSuccess) {
                                        // ✅ ÉXITO: El usuario está activado en Firestore
                                        Toast.makeText(context, context.getString(R.string.verification_success), Toast.LENGTH_SHORT).show()
                                        // Navega a la pantalla de bienvenida
                                        val intent = Intent(context, WelcomeActivity::class.java).apply {
                                            putExtra("USER_NAME", userName)
                                            putExtra("USER_ID", userId)
                                        }
                                        context.startActivity(intent)
                                        activity.finishAffinity() // Cierra esta y las actividades anteriores
                                    } else {
                                        // ❌ FALLO: No se pudo actualizar en Firestore
                                        Toast.makeText(context, activationMessage, Toast.LENGTH_LONG).show()
                                        isLoading = false // Permite al usuario reintentar
                                    }
                                }
                            },
                            onError = { exception ->
                                // ❌ FALLO: El código OTP era incorrecto o expiró
                                Log.e("OTP_VERIFY_ERROR", "Código de verificación incorrecto", exception)
                                Toast.makeText(context, context.getString(R.string.verification_error_code), Toast.LENGTH_LONG).show()
                                isLoading = false
                            }
                        )
                    } else {
                        Toast.makeText(context, context.getString(R.string.verification_code_length_error), Toast.LENGTH_SHORT).show()
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
                    Text(stringResource(R.string.verification_button_text), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.resend_prompt), fontSize = 14.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { sendOtp() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black),
                shape = RoundedCornerShape(50),
                enabled = isResendEnabled && !isLoading,
                modifier = Modifier.wrapContentWidth().height(40.dp)
            ) {
                val text = if (isResendEnabled)
                    stringResource(R.string.resend_button)
                else
                    stringResource(R.string.resend_countdown, countdown)
                Text(text, fontWeight = FontWeight.Bold)
            }
        }
    }
}