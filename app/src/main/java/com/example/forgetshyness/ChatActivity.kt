package com.example.forgetshyness

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.forgetshyness.data.ChatRepository
import com.example.forgetshyness.recipes.ChatScreen
import com.example.forgetshyness.utils.Constants


class ChatActivity : ComponentActivity() {

    companion object {
        fun newIntent(context: Context, chatId: String?, userId: String, userName: String): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                putExtra("chatId", chatId)
                putExtra(Constants.KEY_USER_ID, userId)
                putExtra(Constants.KEY_USER_NAME, userName)
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val chatId = intent.getStringExtra("chatId") ?: System.currentTimeMillis().toString()
        val userId = intent.getStringExtra(Constants.KEY_USER_ID) ?: ""
        val userName = intent.getStringExtra(Constants.KEY_USER_NAME) ?: ""

        setContent {
            ChatScreen(
                chatId = chatId,
                userId = userId,
                userName = userName,
                repository = ChatRepository(this),
                onMessagesChanged = {},
                onBackClick = {
                    onBackPressedDispatcher.onBackPressed()
                },
            )

        }
    }
}