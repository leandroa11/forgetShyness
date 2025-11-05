package com.example.forgetshyness.recipes

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgetshyness.R
import com.example.forgetshyness.data.ChatRepository
import com.example.forgetshyness.data.MessageModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    userId: String,
    userName: String,
    repository: ChatRepository,
    modifier: Modifier = Modifier,
    onMessagesChanged: (List<MessageModel>) -> Unit,
    onBackClick: () -> Unit
){
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<MessageModel>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Cargar mensajes y hacer scroll al último
    LaunchedEffect(messages.size, loading) {
        if (loading || messages.isNotEmpty()) {
            val targetIndex = if (loading) messages.size else messages.size - 1
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }
    
    // Carga inicial de mensajes
    LaunchedEffect(chatId) {
        messages = repository.getMessages(chatId)

        if (messages.isEmpty()) {
            val saludo = MessageModel(
                sender = "bot",
                text = context.getString(R.string.chat_bot_greeting, userName)
            )
            repository.saveMessage(chatId, saludo)
            messages = repository.getMessages(chatId)
        }
        onMessagesChanged(messages)
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_1),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0,0,0,0)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) 
            ) {
                // Botón de volver flotante
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.flecha_izquierda),
                        contentDescription = stringResource(id = R.string.content_desc_back),
                        tint = Color.Yellow
                    )
                }
                
                // Lista de mensajes
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        MessageBubble(msg)
                    }
                    if (loading) {
                        item {
                            TypingIndicator()
                        }
                    }
                }

                // Barra de input
                ChatInputBar(
                    inputText = inputText,
                    onTextChange = { inputText = it },
                    onSendClick = {
                        val text = inputText.trim()
                        if (text.isNotEmpty() && !loading) {
                            if (chatId.isBlank()) {
                                Log.e("ChatScreen", "⚠️ ChatID vacío: no se puede guardar mensaje")
                                return@ChatInputBar
                            }

                            scope.launch {
                                loading = true
                                try {
                                    val userMsg = MessageModel(sender = "user", text = text)
                                    repository.saveMessage(chatId, userMsg)
                                    messages = repository.getMessages(chatId)
                                    inputText = ""

                                    val botReply = repository.sendMessageToGemini(text)
                                    val botMsg = MessageModel(sender = "bot", text = botReply)
                                    repository.saveMessage(chatId, botMsg)
                                    
                                    messages = repository.getMessages(chatId)
                                    onMessagesChanged(messages)
                                } catch (e: Exception) {
                                    val errorMsg = when {
                                        e.message?.contains("403") == true -> context.getString(R.string.chat_error_403)
                                        e.message?.contains("503") == true -> context.getString(R.string.chat_error_503)
                                        else -> context.getString(R.string.chat_error_generic)
                                    }
                                    val errorResponse = MessageModel(sender = "bot", text = errorMsg)
                                    repository.saveMessage(chatId, errorResponse)
                                    messages = repository.getMessages(chatId)
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    },
                    isLoading = loading
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: MessageModel) {
    val isUser = message.sender == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isUser) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.3f)
    val shape = if(isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = backgroundColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = Color.White
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            modifier = Modifier.widthIn(max = 100.dp)
        ) {
            Text(
                text = "...",
                modifier = Modifier.padding(12.dp),
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    inputText: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        color = Color.Black.copy(alpha = 0.3f),
        modifier = Modifier.navigationBarsPadding().padding(8.dp),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.chat_input_placeholder), color = Color.White.copy(0.7f)) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFFFCB3C),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).padding(horizontal = 12.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onSendClick, enabled = inputText.isNotBlank()) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = stringResource(R.string.chat_send_button),
                        tint = if (inputText.isNotBlank()) Color(0xFFFFCB3C) else Color.Gray
                    )
                }
            }
        }
    }
}
