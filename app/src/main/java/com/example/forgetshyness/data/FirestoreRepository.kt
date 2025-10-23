package com.example.forgetshyness.data

import com.example.forgetshyness.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.content.Context

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val playersCollection = db.collection("players")
    private val challengesCollection = db.collection("challenges")
    private val sessionsCollection = db.collection("game_sessions")


    /**
     * Guarda un usuario después de comprobar si el teléfono y el correo electrónico son duplicados.
     * Esta es una función de suspensión, por lo que debe ser llamada desde una corrutina.
     * @return Un Triple que contiene el estado de éxito (Booleano), un mensaje (String), y el ID del usuario (String?).
     */
    suspend fun saveUser(user: User): Triple<Boolean, String, String?> {
        try {
            // 1. Comprueba si existe un usuario con el mismo número de teléfono
            val phoneQuery = usersCollection.whereEqualTo("phone", user.phone).get().await()
            if (!phoneQuery.isEmpty) {
                return Triple(false, "El teléfono ya está registrado.", null)
            }

            // 2. Si el teléfono es único, comprueba si el correo electrónico existe
            val emailQuery = usersCollection.whereEqualTo("email", user.email).get().await()
            if (!emailQuery.isEmpty) {
                return Triple(false, "El correo electrónico ya está registrado.", null)
            }

            // 3. Si ambos son únicos, añade el nuevo usuario y devuelve su ID
            val documentReference = usersCollection.add(user).await()
            return Triple(true, "¡Bienvenido! Información guardada.", documentReference.id)

        } catch (e: Exception) {
            return Triple(false, "Error: ${e.message}", null)
        }
    }

    /**
     * Activa un usuario cambiando su estado a 'true' en la base de datos.
     * @param userId El ID del documento del usuario en Firestore.
     * @return Un Par que contiene el estado de éxito (Booleano) y un mensaje (String).
     */
    suspend fun activateUser(userId: String): Pair<Boolean, String> {
        return try {
            usersCollection.document(userId).update("state", true).await()
            Pair(true, "Usuario activado correctamente.")
        } catch (e: Exception) {
            Pair(false, "Error al activar el usuario: ${e.message}")
        }
    }

    /**
     * Obtiene un usuario por su ID.
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene un usuario por su número de teléfono.
     * @param phone El número de teléfono del usuario a buscar.
     * @return El objeto `User` si se encuentra, o `null` si no existe o hay un error.
     */
    suspend fun getUserByPhone(phone: String): User? {
        return try {
            val snapshot = usersCollection.whereEqualTo("phone", phone).limit(1).get().await()
            if (snapshot.isEmpty) {
                null
            } else {
                val document = snapshot.documents.first()
                val user = document.toObject(User::class.java)
                user?.copy(id = document.id)
            }
        } catch (e: Exception) {
            null
        }
    }

    // ======================
    // JUGADORES
    // ======================

    /**
     * Agrega un jugador asociado al usuario principal.
     */
    suspend fun addPlayer(player: Player): String {
        require(player.userId.isNotBlank()) { "El userId no puede estar vacío" }

        val docRef = playersCollection.add(player).await()
        val generatedId = docRef.id

        playersCollection.document(generatedId)
            .update(mapOf(
                "id" to generatedId,
                "userId" to player.userId
            ))
            .await()

        return generatedId
    }




    /**
     * Obtiene todos los jugadores creados por un usuario.
     */
    suspend fun getPlayersByUser(userId: String): List<Player> {
        val snapshot = playersCollection
            .whereEqualTo("userId", userId)
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(Player::class.java) }
    }

    /**
     * Elimina un jugador específico.
     */
    suspend fun deletePlayer(playerId: String) {
        playersCollection.document(playerId).delete().await()
    }

    // ======================
    // RETOS Y VERDADES
    // ======================

    /**
     * Inserta los retos y verdades por defecto si la colección está vacía.
     */
    suspend fun seedChallengesIfEmpty(context: Context) {
        val snapshot = challengesCollection.get().await()
        if (snapshot.isEmpty) {
            val challenges = listOf(
                Challenge(type = "reto", text = context.getString(R.string.challenge_squats)),
                Challenge(type = "reto", text = context.getString(R.string.challenge_imitation)),
                Challenge(type = "verdad", text = context.getString(R.string.challenge_embarrassing)),
                Challenge(type = "verdad", text = context.getString(R.string.challenge_crazy_love))
            )
            challenges.forEach { challenge ->
                val docRef = challengesCollection.add(challenge).await()
                val generated = docRef.id
                challengesCollection.document(generated)
                    .update("id", generated)
                    .await()
            }
        }
    }

    /**
     * Obtiene los desafíos filtrados por tipo ("reto" o "verdad").
     */
    suspend fun getChallengesByType(type: String): List<Challenge> {
        val snapshot = challengesCollection
            .whereEqualTo("type", type)
            .get().await()
        return snapshot.documents.mapNotNull { it.toObject(Challenge::class.java) }
    }

    suspend fun getAllChallenges(): List<Challenge> {
        val snapshot = challengesCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Challenge::class.java) }
    }

    // Crear sesión de juego con modelo
    suspend fun createGameSession(hostUserId: String, gameType: String): String {
        val session = GameSession(
            id = "", // se asignará después
            hostId = hostUserId,
            gameType = gameType,
            createdAt = System.currentTimeMillis(),
            active = true
        )
        val docRef = sessionsCollection.add(session).await()
        val sessionId = docRef.id
        // actualizar el campo id dentro del documento
        sessionsCollection.document(sessionId).update("id", sessionId).await()
        return sessionId
    }

    suspend fun getGameSession(sessionId: String): GameSession? {
        val doc = sessionsCollection.document(sessionId).get().await()
        return if (doc.exists()) doc.toObject(GameSession::class.java) else null
    }

    suspend fun addParticipantToSession(sessionId: String, participant: Player) {
        sessionsCollection
            .document(sessionId)
            .collection("participants")
            .add(participant)
            .await()
    }

    suspend fun getParticipants(sessionId: String): List<Player> {
        val snap = sessionsCollection
            .document(sessionId)
            .collection("participants")
            .get().await()
        return snap.documents.mapNotNull { it.toObject(Player::class.java) }
    }

    suspend fun deleteParticipant(sessionId: String, participantId: String) {
        sessionsCollection
            .document(sessionId)
            .collection("participants")
            .document(participantId)
            .delete().await()
    }

    suspend fun addTurn(sessionId: String, turn: Turn) {
        sessionsCollection
            .document(sessionId)
            .collection("turns")
            .add(turn)
            .await()
    }

    suspend fun getTurns(sessionId: String): List<Turn> {
        val snap = sessionsCollection
            .document(sessionId)
            .collection("turns")
            .get().await()
        return snap.documents.mapNotNull { it.toObject(Turn::class.java) }
    }


}
