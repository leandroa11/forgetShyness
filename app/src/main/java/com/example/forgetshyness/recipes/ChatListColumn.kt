package com.example.forgetshyness.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.forgetshyness.R
import com.example.forgetshyness.data.Chat
import com.example.forgetshyness.data.ChatRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListColumn(
    chats: List<Chat>,
    loading: Boolean,
    onCreateChat: () -> Unit,
    onSelectChat: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    repository: ChatRepository,
    userId: String
) {
    val scope = rememberCoroutineScope()
    var chatList by remember { mutableStateOf(chats) }
    var isLoading by remember { mutableStateOf(loading) }
    var showDialog by remember { mutableStateOf(false) }
    var chatToDelete by remember { mutableStateOf<Chat?>(null) }

    // --- SOLUCIÓN: Actualiza la lista cada vez que la pantalla se muestra (vuelve a primer plano) ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    isLoading = true
                    chatList = repository.getChatsForUser(userId)
                    isLoading = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_burbujas_3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(Modifier.fillMaxSize()) {
            // Botón de volver y Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.flecha_izquierda),
                        contentDescription = stringResource(id = R.string.content_desc_back),
                        tint = Color.Yellow
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.chat_list_title),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Contenido principal (lista o indicador de carga)
            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (chatList.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.chat_list_no_chats), color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(chatList, key = { it.id }) { c ->
                        ChatItemCard(c, onSelectChat, onDeleteClick = {
                            chatToDelete = c
                            showDialog = true
                        })
                    }
                }
            }

            // Botón inferior para crear nuevo chat
            Button(
                onClick = onCreateChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCB3C)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.chat_list_new_chat), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDialog && chatToDelete != null) {
        DeleteChatDialog(chatToDelete!!, repository, userId, onDismiss = { showDialog = false }) {
            scope.launch {
                chatList = repository.getChatsForUser(userId)
                showDialog = false
            }
        }
    }
}

@Composable
fun ChatItemCard(chat: Chat, onSelectChat: (String) -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onSelectChat(chat.id) },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.chat_list_item_title, chat.userName.ifEmpty { stringResource(R.string.chat_list_default_user_name) }),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (chat.lastMessage.isNotEmpty()) {
                    val prefix = if (chat.lastSender == "user") stringResource(R.string.chat_list_user_prefix) else stringResource(R.string.chat_list_bot_prefix)
                    Text(
                        text = prefix + chat.lastMessage.take(80) + if (chat.lastMessage.length > 80) "…" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.chat_list_delete_description),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun DeleteChatDialog(
    chat: Chat,
    repository: ChatRepository,
    userId: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chat_list_delete_dialog_title)) },
        text = { Text(stringResource(R.string.chat_list_delete_dialog_text)) },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        repository.deleteChat(chat.id)
                        onConfirm()
                    }
                }
            ) {
                Text(stringResource(R.string.chat_list_delete_dialog_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.chat_list_delete_dialog_cancel))
            }
        }
    )
}
