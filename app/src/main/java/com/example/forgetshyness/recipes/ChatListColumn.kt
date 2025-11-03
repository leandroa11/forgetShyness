package com.example.forgetshyness.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.forgetshyness.data.Chat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListColumn(
    chats: List<Chat>,
    loading: Boolean,
    onCreateChat: () -> Unit,
    onSelectChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        TopAppBar(title = { Text("Recetas / Chat") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFC44545)))
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onCreateChat, modifier = Modifier.fillMaxWidth()) {
            Text("Nuevo chat")
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chats) { c ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onSelectChat(c.id) }) {
                        Text(
                            text = "Chat de ${c.userName.ifEmpty { "Usuario" }} - ${Date(c.timestamp)}",
                            modifier = Modifier.padding(12.dp)
                        )

                    }
                }
            }
        }
    }
}