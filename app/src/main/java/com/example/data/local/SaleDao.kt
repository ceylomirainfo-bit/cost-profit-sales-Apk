package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.SaleEntity
import com.example.data.model.SaleLineItemEntity
import com.example.data.model.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE saleDate = :date ORDER BY createdAt DESC")
    fun getSalesByDate(date: String): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId")
    fun getSaleById(saleId: Long): Flow<SaleWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleLineItems(items: List<SaleLineItemEntity>)

    @Query("DELETE FROM sale_line_items WHERE saleId = :saleId")
    suspend fun deleteLineItemsBySaleId(saleId: Long)

    @Transaction
    suspend fun deleteSaleWithItems(sale: SaleEntity) {
        deleteLineItemsBySaleId(sale.id)
        deleteSale(sale)
    }

    @Query("SELECT DISTINCT saleDate FROM sales")
    fun getDistinctSaleDates(): Flow<List<String>>

    @Query("SELECT * FROM sale_line_items")
    fun getAllLineItems(): Flow<List<SaleLineItemEntity>>
}
