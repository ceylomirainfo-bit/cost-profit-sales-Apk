package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.OrderNoteDao
import com.example.data.local.ProductDao
import com.example.data.local.SaleDao
import com.example.data.model.OrderNoteEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ProductVariantEntity
import com.example.data.model.ProductWithVariants
import com.example.data.model.SaleEntity
import com.example.data.model.SaleLineItemEntity
import com.example.data.model.SaleWithItems
import com.example.data.remote.CeyvanaFirestoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CeyvanaRepository(
    private val database: AppDatabase,
    private val firestoreRepo: CeyvanaFirestoreRepository? = null
) {
    private val productDao: ProductDao = database.productDao()
    private val saleDao: SaleDao = database.saleDao()
    private val orderNoteDao: OrderNoteDao = database.orderNoteDao()

    val activeProducts: Flow<List<ProductWithVariants>> = productDao.getActiveProductsWithVariants()
    val allProducts: Flow<List<ProductWithVariants>> = productDao.getAllProductsWithVariants()

    val allSales: Flow<List<SaleWithItems>> = saleDao.getAllSales()
    val saleDates: Flow<List<String>> = saleDao.getDistinctSaleDates()

    val allOrderNotes: Flow<List<OrderNoteEntity>> = orderNoteDao.getAllOrderNotes()
    val orderDates: Flow<List<String>> = orderNoteDao.getDistinctOrderDates()

    suspend fun ensureCatalogueInitialized() {
        if (productDao.getProductCount() == 0) {
            AppDatabase.populateInitialCatalogue(database)
        }
    }

    suspend fun syncAllToFirestore() {
        if (firestoreRepo == null) return
        try {
            val products = allProducts.first()
            for (pwv in products) {
                firestoreRepo.syncProduct(pwv.product, pwv.variants)
            }
            val sales = allSales.first()
            for (swi in sales) {
                firestoreRepo.syncSale(swi.sale, swi.items)
            }
            val notes = allOrderNotes.first()
            for (note in notes) {
                firestoreRepo.syncOrderNote(note)
            }
        } catch (_: Exception) {
            // Logged via handleFirestoreError inside CeyvanaFirestoreRepository
        }
    }

    fun getSalesForDate(date: String): Flow<List<SaleWithItems>> {
        return saleDao.getSalesByDate(date)
    }

    fun getOrderNotesForDate(date: String): Flow<List<OrderNoteEntity>> {
        return orderNoteDao.getOrderNotesByDate(date)
    }

    suspend fun addProductWithVariants(
        product: ProductEntity,
        variants: List<ProductVariantEntity>
    ): Long {
        val prodId = productDao.insertProduct(product)
        val linkedVariants = variants.map { it.copy(productId = prodId) }
        productDao.insertVariants(linkedVariants)
        val inserted = product.copy(id = prodId)
        firestoreRepo?.syncProduct(inserted, linkedVariants)
        return prodId
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
        val variants = productDao.getVariantsForProductSync(product.id)
        firestoreRepo?.syncProduct(product, variants)
    }

    suspend fun updateProductAndVariants(
        product: ProductEntity,
        updatedVariants: List<ProductVariantEntity>
    ) {
        productDao.updateProduct(product)
        productDao.deleteVariantsByProductId(product.id)
        val linked = updatedVariants.map { it.copy(id = 0, productId = product.id) }
        productDao.insertVariants(linked)
        firestoreRepo?.syncProduct(product, linked)
    }

    /**
     * Checks if product has historical sales.
     * If yes, safe-archives to preserve historical integrity.
     * If no, permanently deletes.
     * Returns true if deleted, false if archived.
     */
    suspend fun deleteOrArchiveProduct(product: ProductEntity): Boolean {
        val historicalCount = productDao.countHistoricalSalesForProduct(product.id)
        return if (historicalCount > 0) {
            productDao.archiveProduct(product.id)
            val updated = product.copy(isArchived = true)
            val variants = productDao.getVariantsForProductSync(product.id)
            firestoreRepo?.syncProduct(updated, variants)
            false
        } else {
            productDao.deleteVariantsByProductId(product.id)
            productDao.deleteProduct(product)
            firestoreRepo?.deleteProduct(product.id)
            true
        }
    }

    suspend fun addVariant(variant: ProductVariantEntity): Long {
        val id = productDao.insertVariant(variant)
        val product = productDao.getProductById(variant.productId)
        if (product != null) {
            val variants = productDao.getVariantsForProductSync(product.id)
            firestoreRepo?.syncProduct(product, variants)
        }
        return id
    }

    suspend fun updateVariant(variant: ProductVariantEntity) {
        productDao.updateVariant(variant)
        val product = productDao.getProductById(variant.productId)
        if (product != null) {
            val variants = productDao.getVariantsForProductSync(product.id)
            firestoreRepo?.syncProduct(product, variants)
        }
    }

    suspend fun deleteVariant(variant: ProductVariantEntity) {
        productDao.deleteVariant(variant)
        val product = productDao.getProductById(variant.productId)
        if (product != null) {
            val variants = productDao.getVariantsForProductSync(product.id)
            firestoreRepo?.syncProduct(product, variants)
        }
    }

    suspend fun recordSale(
        sale: SaleEntity,
        items: List<SaleLineItemEntity>
    ): Long {
        val saleId = saleDao.insertSale(sale)
        val linkedItems = items.map { it.copy(id = 0, saleId = saleId) }
        saleDao.insertSaleLineItems(linkedItems)
        val insertedSale = sale.copy(id = saleId)
        firestoreRepo?.syncSale(insertedSale, linkedItems)
        return saleId
    }

    suspend fun updateSaleWithItems(
        sale: SaleEntity,
        items: List<SaleLineItemEntity>
    ) {
        saleDao.updateSale(sale)
        saleDao.deleteLineItemsBySaleId(sale.id)
        val linkedItems = items.map { it.copy(id = 0, saleId = sale.id) }
        saleDao.insertSaleLineItems(linkedItems)
        firestoreRepo?.syncSale(sale, linkedItems)
    }

    suspend fun deleteSale(sale: SaleEntity) {
        saleDao.deleteSaleWithItems(sale)
        firestoreRepo?.deleteSale(sale.id)
    }

    suspend fun addOrderNote(note: OrderNoteEntity): Long {
        val noteId = orderNoteDao.insertOrderNote(note)
        val inserted = note.copy(id = noteId)
        firestoreRepo?.syncOrderNote(inserted)
        return noteId
    }

    suspend fun updateOrderNote(note: OrderNoteEntity) {
        orderNoteDao.updateOrderNote(note)
        firestoreRepo?.syncOrderNote(note)
    }

    suspend fun deleteOrderNote(note: OrderNoteEntity) {
        orderNoteDao.deleteOrderNote(note)
        firestoreRepo?.deleteOrderNote(note.id)
    }
}
