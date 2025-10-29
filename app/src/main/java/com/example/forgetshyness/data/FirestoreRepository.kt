package com.example.forgetshyness.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    private val usersCollection = db.collection("users")
    private val challengesCollection = db.collection("challenges")
    private val sessionsCollection = db.collection("game_sessions")

    private val eventsCollection = db.collection("events")


    // -----------------------------
    // USERS
    // -----------------------------
    suspend fun addUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        val snapshot = usersCollection.document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }

    /**
     * Activa al usuario después de verificar su código OTP.
     * Retorna Pair(éxito, mensaje)
     */
    suspend fun activateUser(userId: String): Pair<Boolean, String> {
        return try {
            val userRef = usersCollection.document(userId)
            val snapshot = userRef.get().await()

            if (!snapshot.exists()) {
                Pair(false, "El usuario no existe en la base de datos.")
            } else {
                userRef.update("active", true).await()
                Pair(true, "Usuario activado correctamente.")
            }
        } catch (e: Exception) {
            Pair(false, "Error al activar el usuario: ${e.message}")
        }
    }

    suspend fun getUserByPhone(phone: String): User? {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("phone", phone)
                .get()
                .await()

            if (snapshot.isEmpty) null
            else snapshot.documents.first().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUser(user: User): Triple<Boolean, String, String?> {
        return try {
            // Verificar si ya existe un usuario con el mismo teléfono
            val existingUserSnapshot = usersCollection
                .whereEqualTo("phone", user.phone)
                .get()
                .await()

            if (!existingUserSnapshot.isEmpty) {
                // Ya existe un usuario con ese teléfono
                val existingUser = existingUserSnapshot.documents.first().toObject(User::class.java)
                return Triple(true, "Usuario ya registrado", existingUser?.id)
            }

            // Si no existe, crear nuevo usuario
            val userId = user.id.ifEmpty { usersCollection.document().id }
            val userToSave = user.copy(id = userId)

            usersCollection.document(userId).set(userToSave).await()

            Triple(true, "Usuario registrado correctamente", userId)

        } catch (e: Exception) {
            Triple(false, "Error al guardar usuario: ${e.message}", null)
        }
    }

    // -----------------------------
    // CHALLENGES
    // -----------------------------
    suspend fun initializeChallengesIfEmpty(defaultChallenges: List<Challenge>) {
        val snapshot = challengesCollection.get().await()
        if (snapshot.isEmpty) {
            for (challenge in defaultChallenges) {
                challengesCollection.add(challenge).await()
            }
        }
    }

    suspend fun getAllChallenges(): List<Challenge> {
        val snapshot = challengesCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Challenge::class.java) }
    }

    // -----------------------------
    // GAME SESSIONS
    // -----------------------------
    /**
     * Busca una sesión activa para el host (hostUserId). Si existe devuelve su id,
     * si no existe crea una nueva sesión y la devuelve.
     *
     * La nueva sesión, cuando se crea, incluye por defecto al host dentro del array participants.
     */
    suspend fun getOrCreateSessionForHost(
        hostUserId: String,
        hostName: String,
        gameType: String
    ): String {
        // 1️⃣ Buscar si ya existe una sesión activa del host
        val querySnapshot = sessionsCollection
            .whereEqualTo("hostId", hostUserId)
            .whereEqualTo("active", true)
            .limit(1)
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            val existingSession = querySnapshot.documents.first()
            val sessionId = existingSession.id

            // 2️⃣ Asegurar que el host esté incluido en "participants"
            val participants = existingSession.get("participants") as? List<Map<String, Any>> ?: emptyList()
            val hostAlreadyIn = participants.any { it["id"] == hostUserId }

            if (!hostAlreadyIn) {
                val updated = participants + mapOf(
                    "id" to hostUserId,
                    "name" to hostName,
                    "userId" to hostUserId
                )
                sessionsCollection.document(sessionId).update("participants", updated).await()
            }

            return sessionId
        }

        // 3️⃣ No existe: crear nueva sesión con el host incluido como participante
        val hostParticipant = mapOf(
            "id" to hostUserId,
            "name" to hostName,
            "userId" to hostUserId
        )

        val session = GameSession(
            id = "",
            hostId = hostUserId,
            gameType = gameType,
            createdAt = System.currentTimeMillis(),
            active = true,
            participants = listOf(hostParticipant)
        )

        val docRef = sessionsCollection.add(session).await()
        val sessionId = docRef.id

        sessionsCollection.document(sessionId).update("id", sessionId).await()

        return sessionId
    }


    /**
     * Añade un participante a la sesión, evitando duplicados.
     * Operación segura con transaction.
     */
    suspend fun addParticipantToSession(sessionId: String, player: Player) {
        val sessionRef = sessionsCollection.document(sessionId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(sessionRef)
            val participants = snapshot.get("participants") as? List<Map<String, Any>> ?: emptyList()

            // Si ya existe (mismo id), no añadimos
            val exists = participants.any { it["id"] == player.id }
            if (!exists) {
                val updated = participants + mapOf(
                    "id" to player.id,
                    "name" to player.name,
                    "userId" to player.userId
                )
                transaction.update(sessionRef, "participants", updated)
            }
        }.await()
    }

    /**
     * Elimina participante por id (transaction).
     */
    suspend fun removeParticipantFromSession(sessionId: String, player: Player) {
        val sessionRef = sessionsCollection.document(sessionId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(sessionRef)
            val participants = snapshot.get("participants") as? List<Map<String, Any>> ?: emptyList()
            val updated = participants.filterNot { it["id"] == player.id }
            transaction.update(sessionRef, "participants", updated)
        }.await()
    }

    /**
     * Lee el array participants del documento de sesión y lo transforma a List<Player>.
     */
    suspend fun getParticipants(sessionId: String): List<Player> {
        val doc = sessionsCollection.document(sessionId).get().await()
        if (!doc.exists()) return emptyList()
        val participants = doc.get("participants") as? List<Map<String, Any>> ?: emptyList()
        return participants.map {
            Player(
                id = it["id"] as? String ?: "",
                name = it["name"] as? String ?: "",
                userId = it["userId"] as? String ?: ""
            )
        }
    }
    // -----------------------------
    // TURNS (Me gusta / No me gusta)
    // -----------------------------
    suspend fun addTurn(sessionId: String, turn: Turn) {
        val turnsRef = db.collection("game_sessions")
            .document(sessionId)
            .collection("turns")

        // 🔹 Datos base (siempre necesarios)
        val turnData = mutableMapOf<String, Any>(
            "participantId" to turn.participantId,
            "challengeId" to turn.challengeId,
            "timestamp" to turn.timestamp
        )

        // 🔹 Solo incluir 'liked' si no es null
        turn.liked?.let { turnData["liked"] = it }

        // 🔹 Guardar el turno (no crea ninguna colección vacía)
        turnsRef.add(turnData).await()

        // 🔹 Solo actualizar estadísticas si hay un valor para 'liked'
        if (turn.liked != null) {
            incrementChallengeStats(turn.challengeId, turn.liked)
        }
    }


    suspend fun getTurns(sessionId: String): List<Turn> {
        val snapshot = sessionsCollection
            .document(sessionId)
            .collection("turns")
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Turn::class.java) }
    }

    // -----------------------------
    // ESTADÍSTICAS DE RETOS
    // -----------------------------
    private suspend fun incrementChallengeStats(challengeId: String, liked: Boolean?) {
        val statsRef = db.collection("challenge_stats").document(challengeId)
        val incrementField = when (liked) {
            true -> mapOf("likes" to FieldValue.increment(1))
            false -> mapOf("dislikes" to FieldValue.increment(1))
            else -> mapOf("calificaciones" to FieldValue.increment(1))
        }
        statsRef.set(incrementField, SetOptions.merge()).await()
    }

    // --- EVENTOS ---

    // Crear evento
    suspend fun createEvent(event: Event): String? {
        return try {
            val docRef = eventsCollection.document()
            val eventToSave = event.copy(id = docRef.id)
            docRef.set(eventToSave).await()
            docRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Obtener eventos del usuario (creados o invitados)
    suspend fun getEventsForUser(userId: String): List<Event> {
        val results = mutableListOf<Event>()
        try {
            val ownerQuery = eventsCollection.whereEqualTo("ownerId", userId).get().await()
            results.addAll(ownerQuery.toObjects(Event::class.java))

            val invitedQuery = eventsCollection.get().await()
            invitedQuery.documents.forEach { doc ->
                val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
                if (event != null && event.invitedUsers.any { it.userId == userId }) {
                    if (!results.any { it.id == event.id }) results.add(event)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return results
    }

    // Actualizar estado de invitación
    suspend fun updateInvitationStatus(eventId: String, userId: String, newStatus: String): Boolean {
        return try {
            val docRef = eventsCollection.document(eventId)
            val snapshot = docRef.get().await()
            val event = snapshot.toObject(Event::class.java) ?: return false

            val updatedInvited = event.invitedUsers.map {
                if (it.userId == userId) it.copy(status = newStatus) else it
            }

            docRef.update("invitedUsers", updatedInvited).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Obtener todos los usuarios (para invitar)
    suspend fun getAllUsers(): List<Map<String, String>> {
        val snapshot = db.collection("users").get().await()
        return snapshot.documents.mapNotNull {
            val id = it.id
            val name = it.getString("name") ?: return@mapNotNull null
            mapOf("id" to id, "name" to name)
        }
    }

    // Invitar jugadores
    suspend fun invitePlayersToEvent(eventId: String, invitedIds: List<String>, users: List<Map<String, String>>) {
        try {
            val docRef = eventsCollection.document(eventId)
            val snapshot = docRef.get().await()
            val event = snapshot.toObject(Event::class.java) ?: return
            val existingInvited = event.invitedUsers.toMutableList()

            invitedIds.forEach { id ->
                val user = users.find { it["id"] == id }
                if (user != null && existingInvited.none { it.userId == id }) {
                    existingInvited.add(InvitedUser(id, user["name"] ?: "", "pending"))
                }
            }

            docRef.update("invitedUsers", existingInvited).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Obtener evento por ID
    suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = eventsCollection.document(eventId).get().await()
            doc.toObject(Event::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Eliminar evento
    suspend fun deleteEvent(event: Event) {
        try {
            eventsCollection.document(event.id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

