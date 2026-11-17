package br.com.badgr.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.badgr.mobile.data.local.dao.EventDao
import br.com.badgr.mobile.data.local.dao.FavoriteDao
import br.com.badgr.mobile.data.local.dao.TicketDao
import br.com.badgr.mobile.data.local.entity.EventEntity
import br.com.badgr.mobile.data.local.entity.FavoriteEntity
import br.com.badgr.mobile.data.local.entity.TicketEntity

@Database(entities = [EventEntity::class, FavoriteEntity::class, TicketEntity::class], version = 2, exportSchema = true)
abstract class BadgrDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile private var instance: BadgrDatabase? = null

        fun getInstance(context: Context): BadgrDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, BadgrDatabase::class.java, "badgr-demo.db")
                .addMigrations(DatabaseMigrations.FROM_1_TO_2)
                .build().also { instance = it }
        }
    }
}
