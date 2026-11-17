package br.com.badgr.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import br.com.badgr.mobile.data.local.entity.TicketEntity

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY created_at DESC")
    suspend fun getAll(): List<TicketEntity>

    @Query("SELECT COUNT(*) FROM tickets WHERE event_id = :eventId")
    suspend fun countForEvent(eventId: Long): Int

    @Insert
    suspend fun insert(ticket: TicketEntity)

    @Query("UPDATE tickets SET checked_in = 1 WHERE id = :id AND checked_in = 0")
    suspend fun checkIn(id: String): Int

    @Query("DELETE FROM tickets")
    suspend fun deleteAll()
}
