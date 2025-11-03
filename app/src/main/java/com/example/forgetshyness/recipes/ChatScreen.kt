package com.example.forgetshyness.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.forgetshyness.data.ChatRepository
import com.example.forgetshyness.data.MessageModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    chatId: String,
    userId: String,
    userName: String,
    repository: ChatRepository,
    modifier: Modifier = Modifier,
    onMessagesChanged: (List<MessageModel>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<MessageModel>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // 🔹 Cargar mensajes al iniciar o cambiar chat
    LaunchedEffect(chatId) {
        messages = repository.getMessages(chatId)

        // Si no hay mensajes, agregar saludo inicial del bot
        if (messages.isEmpty()) {
            val saludo = MessageModel(
                sender = "bot",
                text = "Hola $userName 👋 soy tu bartender virtual. ¿Qué tipo de cóctel te gustaría hoy?"
            )
            repository.saveMessage(chatId, saludo)
            messages = repository.getMessages(chatId)
        }

        onMessagesChanged(messages)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text("Bienvenido $userName 🍸", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = msg.text,
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe tu mensaje...") }
            )

            Button(
                onClick = {
                    val text = inputText.trim()
                    if (text.isNotEmpty()) {
                        scope.launch {
                            loading = true

                            // 1️⃣ Guardar mensaje del usuario
                            val userMsg = MessageModel(sender = "user", text = text)
                            repository.saveMessage(chatId, userMsg)

                            // 2️⃣ Generar respuesta del bot usando Gemini
                            val botReply = repository.sendMessageToGemini(text)

                            // 3️⃣ Guardar mensaje del bot
                            val botMsg = MessageModel(sender = "bot", text = botReply)
                            repository.saveMessage(chatId, botMsg)

                            // 4️⃣ Actualizar mensajes en pantalla
                            messages = repository.getMessages(chatId)
                            onMessagesChanged(messages)

                            inputText = ""
                            loading = false
                        }
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Enviar")
            }
        }
    }
}