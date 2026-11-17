package br.com.badgr.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "favorites", foreignKeys = [ForeignKey(
    entity = EventEntity::class, parentColumns = ["id"], childColumns = ["event_id"],
    onDelete = ForeignKey.CASCADE
)])
data class FavoriteEntity(@PrimaryKey @ColumnInfo(name = "event_id") val eventId: Long)
