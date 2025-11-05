package com.example.forgetshyness

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.data.FirestoreRepository
import com.example.forgetshyness.data.User
import com.example.forgetshyness.R
import com.example.forgetshyness.utils.Constants
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class ExperienceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialPhone = intent.getStringExtra(Constants.KEY_PHONE_NUMBER) ?: ""
        setContent {
            ExperienceFormScreen(initialPhone)
        }
    }
}

// --- Funciones de Validación (No Composable) ---
private fun validateName(name: String, context: Context): String {
    if (name.length < 3) return context.getString(R.string.validation_name_min_length)
    if (name.length > 40) return context.getString(R.string.validation_name_max_length)
    if (!name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) return context.getString(R.string.validation_name_invalid_chars)
    return ""
}

private fun validatePhone(phone: String, context: Context): String {
    if (!phone.matches(Regex("^[0-9]+$"))) return context.getString(R.string.validation_phone_only_numbers)
    if (phone.length != 10) return context.getString(R.string.validation_phone_ten_digits)
    return ""
}

private fun validateAge(age: String, context: Context): String {
    val ageNum = age.toIntOrNull()
    if (ageNum == null) return context.getString(R.string.validation_age_only_numbers)
    if (ageNum < 18) return context.getString(R.string.validation_age_min_age)
    return ""
}

private fun validateEmail(email: String, context: Context): String {
    val emailPattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    if (!emailPattern.matcher(email).matches()) return context.getString(R.string.validation_email_invalid)
    return ""
}


@Composable
fun ExperienceFormScreen(initialPhone: String) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()
    val firestoreRepository = remember { FirestoreRepository() }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(initialPhone) }
    var age by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var ageError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val isFormValid by remember {
        derivedStateOf {
            validateName(name, context).isEmpty() &&
                    validatePhone(phone, context).isEmpty() &&
                    validateAge(age, context).isEmpty() &&
                    validateEmail(email, context).isEmpty()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Text(stringResource(R.string.experience_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(stringResource(R.string.experience_subtitle), fontSize = 22.sp, color = Color.White)

            Spacer(Modifier.height(32.dp))

            // --- Campos del Formulario ---
            FormField(
                value = name,
                onValueChange = { name = it; nameError = validateName(it, context) },
                label = stringResource(R.string.experience_name_label),
                error = nameError,
                icon = R.drawable.coctel_1,
                enabled = !isLoading
            )

            FormField(
                value = phone,
                onValueChange = { phone = it; phoneError = validatePhone(it, context) },
                label = stringResource(R.string.experience_phone_label),
                error = phoneError,
                icon = R.drawable.telefono,
                keyboardType = KeyboardType.Phone,
                enabled = !isLoading
            )

            FormField(
                value = age,
                onValueChange = { age = it; ageError = validateAge(it, context) },
                label = stringResource(R.string.experience_age_label),
                error = ageError,
                icon = R.drawable.calendario,
                keyboardType = KeyboardType.Number,
                enabled = !isLoading
            )

            FormField(
                value = email,
                onValueChange = { email = it; emailError = validateEmail(it, context) },
                label = stringResource(R.string.experience_email_label),
                error = emailError,
                icon = R.drawable.correo,
                keyboardType = KeyboardType.Email,
                enabled = !isLoading
            )


            Spacer(Modifier.height(36.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val user = User(
                            name = name,
                            phone = phone,
                            age = age.toInt(),
                            email = email,
                            state = false
                        )

                        val (success, message, userId) = firestoreRepository.saveUser(user)

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                        if (success && userId != null) {
                            val intent = Intent(context, VerificationCodeActivity::class.java).apply {
                                putExtra("USER_ID", userId)
                                putExtra("USER_PHONE", phone)
                                putExtra("USER_NAME", name)
                            }
                            context.startActivity(intent)
                        }

                        isLoading = false
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth(0.85f),
                enabled = isFormValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFFE65100),
                        strokeWidth = 3.dp
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.coctel_2),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.experience_submit_button), color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { activity?.finish() }
            ) {
                Image(
                    painter = painterResource(R.drawable.flecha_izquierda),
                    contentDescription = stringResource(R.string.experience_back_button_desc),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.experience_back_button_prompt), color = Color.White)
            }
        }
    }
}

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String,
    icon: Int,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(label) },
            leadingIcon = {
                Image(
                    painter = painterResource(icon),
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
                disabledTextColor = Color.Gray,
                disabledContainerColor = Color(0xFFE0E0E0)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = error.isNotEmpty(),
            enabled = enabled
        )

        if (error.isNotEmpty()) {
            Text(
                text = error,
                fontSize = 12.sp,
                color = Color.Red,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 12.dp)
            )
        } else {
            Spacer(Modifier.height(24.dp))
        }
    }
}
