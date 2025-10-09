package com.example.forgetshyness.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

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
}
