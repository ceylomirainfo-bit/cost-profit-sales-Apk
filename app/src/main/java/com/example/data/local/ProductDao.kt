package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ProductEntity
import com.example.data.model.ProductVariantEntity
import com.example.data.model.ProductWithVariants
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Transaction
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY id ASC")
    fun getActiveProductsWithVariants(): Flow<List<ProductWithVariants>>

    @Transaction
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProductsWithVariants(): Flow<List<ProductWithVariants>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductWithVariantsById(productId: Long): Flow<ProductWithVariants?>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET isArchived = 1 WHERE id = :productId")
    suspend fun archiveProduct(productId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: ProductVariantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ProductVariantEntity>)

    @Update
    suspend fun updateVariant(variant: ProductVariantEntity)

    @Delete
    suspend fun deleteVariant(variant: ProductVariantEntity)

    @Query("DELETE FROM product_variants WHERE productId = :productId")
    suspend fun deleteVariantsByProductId(productId: Long)

    @Query("SELECT COUNT(*) FROM sale_line_items WHERE productId = :productId")
    suspend fun countHistoricalSalesForProduct(productId: Long): Int

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): ProductEntity?

    @Query("SELECT * FROM product_variants WHERE productId = :productId")
    suspend fun getVariantsForProductSync(productId: Long): List<ProductVariantEntity>
}
