package com.example.pilisventas.data.repository

import com.example.pilisventas.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Error de autenticación")
            val doc = db.collection("usuarios").document(uid).get().await()
            val usuario = doc.toObject(Usuario::class.java)?.copy(uid = uid)
                ?: throw Exception("Perfil de usuario no encontrado")
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUsuario(): Usuario? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("usuarios").document(uid).get().await()
            doc.toObject(Usuario::class.java)?.copy(uid = uid)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() = auth.signOut()

    fun isLoggedIn() = auth.currentUser != null
}
