package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OrderNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderNoteDao {
    @Query("SELECT * FROM order_notes ORDER BY createdAt DESC")
    fun getAllOrderNotes(): Flow<List<OrderNoteEntity>>

    @Query("SELECT * FROM order_notes WHERE orderDate = :date ORDER BY createdAt DESC")
    fun getOrderNotesByDate(date: String): Flow<List<OrderNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderNote(note: OrderNoteEntity): Long

    @Update
    suspend fun updateOrderNote(note: OrderNoteEntity)

    @Delete
    suspend fun deleteOrderNote(note: OrderNoteEntity)

    @Query("SELECT DISTINCT orderDate FROM order_notes")
    fun getDistinctOrderDates(): Flow<List<String>>
}
