package com.example.forgetshyness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.forgetshyness.data.Chat
import com.example.forgetshyness.data.ChatRepository
import com.example.forgetshyness.utils.Constants
import java.util.Date
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.forgetshyness.recipes.ChatMasterScreen

class RecetaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""
        val userName = intent.getStringExtra(Constants.KEY_USER_NAME) ?: ""

        setContent {
            val repo = ChatRepository(this)
            ChatMasterScreen(
                userId = userId,
                userName = userName,
                repository = repo,
                onOpenChatExternally = { chatId ->
                    // Aquí seguimos tu mismo flujo original
                    startActivity(
                        ChatActivity.newIntent(this, chatId, userId, userName)
                    )
                }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    userId: String,
    userName: String,
    repository: ChatRepository,
    onChatSelected: (String) -> Unit,
    onNewChat: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        chats = repository.getChatsForUser(userId)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bienvenido, $userName 👋", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFC44545)))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewChat, containerColor = Color(0xFFFFCB3C)) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo chat")
            }
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(chats) { chat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onChatSelected(chat.id) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2C2))
                    ) {
                        Text(
                            text = "Chat del ${Date(chat.timestamp)}",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}