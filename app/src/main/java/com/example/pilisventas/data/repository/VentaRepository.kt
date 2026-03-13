package com.example.pilisventas.data.repository

import com.example.pilisventas.data.model.Venta
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class VentaRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getVentasDeHoy(): Flow<List<Venta>> = callbackFlow {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val listener = db.collection("ventas")
            .whereGreaterThanOrEqualTo("fecha", startOfDay)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val ventas = snapshot?.documents?.mapNotNull {
                    it.toObject(Venta::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(ventas)
            }

        awaitClose { listener.remove() }
    }

    suspend fun getVentasPorRango(desde: Long, hasta: Long): Result<List<Venta>> {
        return try {
            val snapshot = db.collection("ventas")
                .whereGreaterThanOrEqualTo("fecha", desde)
                .whereLessThanOrEqualTo("fecha", hasta)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()
            val ventas = snapshot.documents.mapNotNull {
                it.toObject(Venta::class.java)?.copy(id = it.id)
            }
            Result.success(ventas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarVenta(venta: Venta): Result<Unit> {
        return try {
            db.collection("ventas").add(venta).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVentaPorId(ventaId: String): Result<Venta> {
        return try {
            val doc = db.collection("ventas").document(ventaId).get().await()
            val venta = doc.toObject(Venta::class.java)?.copy(id = doc.id)
                ?: throw Exception("Venta no encontrada")
            Result.success(venta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarVenta(ventaId: String, venta: Venta): Result<Unit> {
        return try {
            db.collection("ventas").document(ventaId).set(venta).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
