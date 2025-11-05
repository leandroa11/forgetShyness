package com.example.forgetshyness.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import com.example.forgetshyness.data.Chat
import com.example.forgetshyness.data.ChatRepository
import com.example.forgetshyness.data.MessageModel
import kotlinx.coroutines.launch

/**
 * ChatMasterScreen: panel izquierdo = lista de chats; panel derecho = chat seleccionado.
 * - Si width small: solo lista (y mantiene el comportamiento de navegar a ChatActivity).
 * - Si width large: muestra ambas columnas.
 */
@Composable
fun ChatMasterScreen(
    userId: String,
    userName: String,
    repository: ChatRepository,
    onOpenChatExternally: (String) -> Unit,
    modifier: Modifier = Modifier,
    minTwoPaneWidthDp: Int = 700
) {
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedChatId by remember { mutableStateOf<String?>(null) }
    var messagesForSelected by remember { mutableStateOf<List<MessageModel>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher


    LaunchedEffect(userId) {
        loading = true
        chats = repository.getChatsForUser(userId)
        loading = false
        if (chats.isNotEmpty() && selectedChatId == null) {
            selectedChatId = chats.first().id
            messagesForSelected = repository.getMessages(selectedChatId!!)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val twoPane = maxWidth.value >= minTwoPaneWidthDp

        if (twoPane) {
            Row(modifier = Modifier.fillMaxSize()) {
                ChatListColumn(
                    chats = chats,
                    loading = loading,
                    onCreateChat = {
                        scope.launch {
                            val newId = repository.createNewChat(userId, userName)
                            chats = repository.getChatsForUser(userId)
                            selectedChatId = newId
                            messagesForSelected = repository.getMessages(newId)
                        }
                    },
                    onSelectChat = { chatId ->
                        selectedChatId = chatId
                        scope.launch {
                            messagesForSelected = repository.getMessages(chatId)
                        }
                    },
                    modifier = Modifier.weight(0.36f),
                    repository = repository,
                    userId = userId,
                    onBackClick = {
                        dispatcher?.onBackPressed()
                    },
                )

                Spacer(modifier = Modifier.width(8.dp))

                selectedChatId?.let { chatId ->
                    ChatScreen(
                        chatId = chatId,
                        userId = userId,
                        userName = userName,
                        repository = repository,
                        onMessagesChanged = { msgs -> messagesForSelected = msgs },
                        modifier = Modifier.weight(0.64f),
                        onBackClick = {
                            dispatcher?.onBackPressed()
                        },
                    )
                } ?: Box(modifier = Modifier.weight(0.64f)) {}
            }
        } else {
            ChatListColumn(
                chats = chats,
                loading = loading,
                onCreateChat = {
                    scope.launch {
                        val newId = repository.createNewChat(userId, userName)
                        onOpenChatExternally(newId)
                    }
                },
                onSelectChat = { chatId -> onOpenChatExternally(chatId) },
                modifier = Modifier.fillMaxSize(),
                repository = repository,
                userId = userId,
                onBackClick = {
                    dispatcher?.onBackPressed()
                },
            )
        }
    }
}
