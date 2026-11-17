package br.com.badgr.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

@Entity(tableName = "tickets", foreignKeys = [ForeignKey(
    entity = EventEntity::class, parentColumns = ["id"], childColumns = ["event_id"]
)], indices = [Index(value = ["event_id"], name = "tickets_event")])
data class TicketEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "event_id") val eventId: Long,
    val holder: String,
    @ColumnInfo(name = "total") val totalCents: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "checked_in", defaultValue = "0") val checkedIn: Boolean = false
)
