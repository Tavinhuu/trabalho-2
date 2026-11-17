package br.com.badgr.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val date: String,
    val venue: String,
    val city: String,
    val category: String,
    @ColumnInfo(name = "price") val priceCents: Long,
    val capacity: Int,
    @ColumnInfo(name = "lat") val latitude: Double,
    @ColumnInfo(name = "lon") val longitude: Double,
    val description: String,
    val color: Int
)
