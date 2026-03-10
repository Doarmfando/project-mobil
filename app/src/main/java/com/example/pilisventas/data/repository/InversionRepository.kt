package com.example.pilisventas.data.repository

import com.example.pilisventas.data.model.Inversion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class InversionRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getInversionDelMes(mes: String): Result<Inversion?> {
        return try {
            val snapshot = db.collection("inversiones")
                .whereEqualTo("mes", mes)
                .limit(1)
                .get()
                .await()
            val inv = snapshot.documents.firstOrNull()?.let {
                it.toObject(Inversion::class.java)?.copy(id = it.id)
            }
            Result.success(inv)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setInversion(inversion: Inversion): Result<Unit> {
        return try {
            if (inversion.id.isEmpty()) {
                db.collection("inversiones").add(inversion).await()
            } else {
                db.collection("inversiones").document(inversion.id).set(inversion).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
