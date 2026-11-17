package br.com.badgr.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.badgr.mobile.data.local.entity.FavoriteEntity

@Dao
interface FavoriteDao {
    @Query("SELECT event_id FROM favorites ORDER BY event_id")
    suspend fun getEventIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE event_id = :eventId")
    suspend fun delete(eventId: Long): Int

    @Query("DELETE FROM favorites")
    suspend fun deleteAll()
}
