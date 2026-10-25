package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.model.OrderNoteEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ProductVariantEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleLineItemEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CeyvanaFirestoreRepository(
    private val firestore: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated with Google before accessing Cloud Firestore.")
    }

    suspend fun syncProduct(product: ProductEntity, variants: List<ProductVariantEntity>) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val path = "users/$uid/products/${product.id}"
        val docRef = firestore.collection("users").document(uid).collection("products").document(product.id.toString())

        val variantMaps = variants.map { v ->
            mapOf(
                "variantName" to v.variantName,
                "costPrice" to v.costPrice,
                "sellingPrice" to v.sellingPrice,
                "currency" to v.currency,
                "isActive" to v.isActive
            )
        }

        val data = hashMapOf(
            "userId" to uid,
            "name" to product.name,
            "category" to product.category,
            "description" to product.description,
            "imageUri" to product.imageUri,
            "isArchived" to product.isArchived,
            "variants" to variantMaps,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
        }
    }

    suspend fun deleteProduct(productId: Long) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val path = "users/$uid/products/$productId"
        try {
            firestore.collection("users").document(uid).collection("products").document(productId.toString()).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
        }
    }

    suspend fun syncSale(sale: SaleEntity, items: List<SaleLineItemEntity>) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val path = "users/$uid/sales/${sale.id}"
        val docRef = firestore.collection("users").document(uid).collection("sales").document(sale.id.toString())

        val itemMaps = items.map { item ->
            mapOf(
                "productId" to item.productId,
                "productName" to item.productName,
                "category" to item.category,
                "variantName" to item.variantName,
                "unitCostPrice" to item.unitCostPrice,
                "unitSellingPrice" to item.unitSellingPrice,
                "quantity" to item.quantity,
                "discount" to item.discount,
                "netRevenue" to item.netRevenue,
                "costOfGoodsSold" to item.costOfGoodsSold,
                "grossProfit" to item.grossProfit
            )
        }

        val data = hashMapOf(
            "userId" to uid,
            "date" to sale.saleDate,
            "orderReference" to sale.orderReference,
            "customerName" to sale.customerName,
            "discount" to sale.orderDiscount,
            "totalGross" to sale.totalGross,
            "totalNet" to sale.totalNet,
            "totalCost" to sale.totalCost,
            "totalGrossProfit" to sale.totalGrossProfit,
            "notes" to sale.notes,
            "items" to itemMaps,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
        }
    }

    suspend fun deleteSale(saleId: Long) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val path = "users/$uid/sales/$saleId"
        try {
            firestore.collection("users").document(uid).collection("sales").document(saleId.toString()).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
        }
    }

    suspend fun syncOrderNote(note: OrderNoteEntity) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val path = "users/$uid/order_notes/${note.id}"
        val docRef = firestore.collection("users").document(uid).collection("order_notes").document(note.id.toString())

        val data = hashMapOf(
            "userId" to uid,
            "date" to note.orderDate,
            "orderReference" to note.orderReference,
            "customerName" to note.customerName,
            "orderDetails" to note.orderDetails,
            "notes" to note.notes,
            "status" to note.status,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
        }
    }

    suspend fun deleteOrderNote(noteId: Long) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val path = "users/$uid/order_notes/$noteId"
        try {
            firestore.collection("users").document(uid).collection("order_notes").document(noteId.toString()).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
        }
    }
}
