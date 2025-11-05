package com.example.forgetshyness.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.forgetshyness.R
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()

    // ✅ Modelo Gemini
    private val model by lazy {
        val apiKey = context.getString(R.string.generative_api_key)
        GenerativeModel(
            modelName = "models/gemini-2.5-flash",
            apiKey = apiKey
        )
    }

    // 🔹 Obtener todos los chats de un usuario
    suspend fun getChatsForUser(userId: String): List<Chat> = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("chats")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Chat::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al obtener chats: ${e.message}")
            emptyList()
        }
    }

    // 🔹 Obtener mensajes desde la subcolección "messages"
    suspend fun getMessages(chatId: String): List<MessageModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(MessageModel::class.java) }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al obtener mensajes: ${e.message}")
            emptyList()
        }
    }

    // 🔹 Crear un nuevo chat
    suspend fun createNewChat(userId: String, userName: String): String = withContext(Dispatchers.IO) {
        try {
            val chat = Chat(
                userId = userId,
                userName = userName,
                lastMessage = "Nuevo chat",
                timestamp = System.currentTimeMillis()
            )
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

            val response = model.generateContent(
                """
                Eres un experto bartender y solo debes responder sobre cocteles, recetas, tragos o temas relacionados con bebidas.
                Si el usuario pregunta algo fuera de tema, responde:
                "Solo puedo hablar sobre cocteles, bebidas y mixología 🍸".

                Pregunta del usuario: $prompt
                """.trimIndent()
            )

            val output = response.text ?: "Sin respuesta del modelo"
            Log.d("GeminiChat", "Respuesta de Gemini: $output")
            output

        } catch (e: Exception) {
            Log.e("GeminiChat", "Error al conectar con Gemini: ${e.message}", e)
            "Parece que el bartender está ocupado 🧑‍🍳. Inténtalo más tarde."
        }
    }

    // 🔹 Guardar mensaje correctamente en la subcolección "messages"
    // 🔹 Guardar mensaje correctamente en la subcolección "messages"
    suspend fun saveMessage(chatId: String, message: MessageModel) = withContext(Dispatchers.IO) {
        try {
            val chatRef = db.collection("chats").document(chatId)
            val msgData = message.copy(timestamp = System.currentTimeMillis())

            // 1️⃣ Guardar mensaje en la subcolección
            chatRef.collection("messages").add(msgData).await()

            // 2️⃣ Actualizar el chat principal con resumen y último emisor
            chatRef.update(
                mapOf(
                    "lastMessage" to message.text,
                    "lastSender" to message.sender,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()

        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al guardar mensaje: ${e.message}", e)
        }
    }


    // 🔹 Eliminar chat completo (chat + subcolección)
    suspend fun deleteChat(chatId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val chatRef = db.collection("chats").document(chatId)

            // 1️⃣ Eliminar los mensajes dentro de la subcolección
            val messages = chatRef.collection("messages").get().await()
            for (msg in messages.documents) {
                msg.reference.delete().await()
            }

            // 2️⃣ Eliminar el documento principal
            chatRef.delete().await()
            Log.d("ChatRepository", "Chat $chatId eliminado correctamente.")
            true
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error al eliminar chat: ${e.message}", e)
            false
        }
    }
}

