package com.example.forgetshyness.recipes

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
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier,
    repository: ChatRepository,
    userId: String
) {
    val scope = rememberCoroutineScope()
    var chatList by remember { mutableStateOf(chats) }
    var isLoading by remember { mutableStateOf(loading) }
    var showDialog by remember { mutableStateOf(false) }
    var chatToDelete by remember { mutableStateOf<Chat?>(null) }

    // 🔹 Cargar chats de Firestore al iniciar
    LaunchedEffect(userId) {
        isLoading = true
        chatList = repository.getChatsForUser(userId)
        isLoading = false
    }

    Column(modifier = modifier.padding(8.dp)) {
        TopAppBar(
            title = { Text("Recetas / Chat") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFC44545))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onCreateChat, modifier = Modifier.fillMaxWidth()) {
            Text("Nuevo chat")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (chatList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay chats aún 🍹", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chatList, key = { it.id }) { c ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onSelectChat(c.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chat de ${c.userName.ifEmpty { "Usuario" }}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (c.lastMessage.isNotEmpty()) {
                                    val prefix = if (c.lastSender == "user") "Tú: " else "Bot: "
                                    Text(
                                        text = prefix + c.lastMessage.take(80) + if (c.lastMessage.length > 80) "…" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    chatToDelete = c
                                    showDialog = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar chat",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 🔸 Diálogo de confirmación de eliminación
    if (showDialog && chatToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar chat") },
            text = { Text("¿Seguro que deseas eliminar este chat y todos sus mensajes? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val eliminado = repository.deleteChat(chatToDelete!!.id)
                            if (eliminado) {
                                chatList = repository.getChatsForUser(userId)
                            }
                            showDialog = false
                            chatToDelete = null
                        }
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    chatToDelete = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
