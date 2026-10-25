package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

object ProductCategory {
    const val SPICES = "SPICES"
    const val TEA = "TEA"
    const val GROCERY_ITEMS = "GROCERY ITEMS"

    val all = listOf(SPICES, TEA, GROCERY_ITEMS)
}

object VariantSizes {
    val defaults = listOf("25g", "50g", "100g", "250g", "500g", "1kg", "Other")
}

@Entity(
    tableName = "products",
    indices = [Index("category"), Index("isArchived")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val description: String = "",
    val imageUri: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "product_variants",
    indices = [Index("productId"), Index("isActive")]
)
data class ProductVariantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val variantName: String,
    val costPrice: Double,
    val sellingPrice: Double,
    val currency: String = "LKR",
    val isActive: Boolean = true
)

data class ProductWithVariants(
    @Embedded
    val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val variants: List<ProductVariantEntity>
)

@Entity(
    tableName = "sales",
    indices = [Index("saleDate")]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleDate: String, // Format: YYYY-MM-DD (e.g., 2026-10-24)
    val orderReference: String = "",
    val customerName: String = "",
    val notes: String = "",
    val orderDiscount: Double = 0.0,
    val totalGross: Double = 0.0,
    val totalNet: Double = 0.0,
    val totalCost: Double = 0.0,
    val totalGrossProfit: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sale_line_items",
    indices = [Index("saleId"), Index("productId")]
)
data class SaleLineItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val variantId: Long,
    // Snapshots frozen at the time of sale:
    val productName: String,
    val variantName: String,
    val category: String,
    val unitCostPrice: Double,
    val unitSellingPrice: Double,
    val quantity: Int,
    val discount: Double = 0.0,
    val grossAmount: Double,
    val netRevenue: Double,
    val costOfGoodsSold: Double,
    val grossProfit: Double
)

data class SaleWithItems(
    @Embedded
    val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleLineItemEntity>
)

@Entity(
    tableName = "order_notes",
    indices = [Index("orderDate")]
)
data class OrderNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderDate: String, // Format: YYYY-MM-DD
    val orderReference: String = "",
    val customerName: String = "",
    val orderDetails: String = "",
    val notes: String = "",
    val status: String = "Pending", // "Pending", "Confirmed", "Completed", "Cancelled"
    val createdAt: Long = System.currentTimeMillis()
)

data class DailyStats(
    val date: String,
    val revenue: Double,
    val grossProfit: Double,
    val unitsSold: Int,
    val salesCount: Int
)
