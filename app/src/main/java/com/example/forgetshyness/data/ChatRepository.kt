package com.example.forgetshyness.data

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()

    // ✅ Modelo Gemini (estable)
    private val model = GenerativeModel(
        modelName = "models/gemini-2.5-flash",
        apiKey = "AIzaSyCl29IYAjYQJ0966dYDyqSi-iwAw5gHrUY"
    )

    // 🔹 Obtener los chats de un usuario
    suspend fun getChatsForUser(userId: String): List<Chat> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = db.collection("chats")
                .whereEqualTo("userId", userId)
                .get().await()

            snapshot.documents.mapNotNull { it.toObject(Chat::class.java)?.copy(id = it.id,) }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al obtener chats: ${e.message}")
            emptyList()
        }
    }

    // 🔹 Obtener mensajes de un chat
    suspend fun getMessages(chatId: String): List<MessageModel> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .get().await()

            snapshot.documents.mapNotNull { it.toObject(MessageModel::class.java) }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al obtener mensajes: ${e.message}")
            emptyList()
        }
    }

    // 🔹 Crear un nuevo chat
    suspend fun createNewChat(userId: String, userName: String): String = withContext(Dispatchers.IO) {
        try {
            val chat = Chat(userId = userId, userName = userName, lastMessage = "Nuevo chat")
            val ref = db.collection("chats").add(chat).await()
            ref.id
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al crear chat: ${e.message}")
            ""
        }
    }

    // 🔹 Enviar mensaje a Gemini
    suspend fun sendMessageToGemini(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d("GeminiChat", "Enviando prompt a Gemini: $prompt")

            // ✅ versión correcta para generativeai:0.9.0
            val response = model.generateContent(prompt)

            val output = response.text ?: "Sin respuesta del modelo"
            Log.d("GeminiChat", "Respuesta de Gemini: $output")
            output
        } catch (e: Exception) {
            Log.e("GeminiChat", "Error al conectar con Gemini: ${e.message}", e)
            "Ocurrió un error al conectar con Gemini: ${e.message}"
        }
    }

    // 🔹 Guardar mensaje en Firestore
    suspend fun saveMessage(chatId: String, message: MessageModel) = withContext(Dispatchers.IO) {
        try {
            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .await()

            // Actualiza el último mensaje del chat
            db.collection("chats").document(chatId)
                .update("lastMessage", message.text, "timestamp", message.timestamp)
                .await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al guardar mensaje: ${e.message}")
        }
    }
}