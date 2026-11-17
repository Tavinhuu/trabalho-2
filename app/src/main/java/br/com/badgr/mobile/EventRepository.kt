package br.com.badgr.mobile

import android.content.ContentValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class EventRepository(private val helper: BadgrDatabase) {
    suspend fun load(): Catalog = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        val events = db.rawQuery("SELECT * FROM events ORDER BY id", null).use { c -> buildList {
            while(c.moveToNext()) add(Event(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getLong(6),c.getInt(7),c.getDouble(8),c.getDouble(9),c.getString(10),c.getInt(11)))
        } }
        val favorites = db.rawQuery("SELECT event_id FROM favorites",null).use { c -> buildSet { while(c.moveToNext()) add(c.getLong(0)) } }
        val tickets = db.rawQuery("SELECT * FROM tickets ORDER BY created_at DESC",null).use { c -> buildList {
            while(c.moveToNext()) add(Ticket(c.getString(0),c.getLong(1),c.getString(2),c.getLong(3),c.getLong(4),c.getInt(5) == 1))
        } }
        Catalog(events,favorites,tickets)
    }
    suspend fun toggleFavorite(id: Long) = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val removed = db.delete("favorites", "event_id=?", arrayOf(id.toString()))
            if (removed == 0) db.insertOrThrow("favorites",null,ContentValues().apply { put("event_id",id) })
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    /** Estoque consultado e ingresso inserido na mesma transação, sem depender do estado da UI. */
    suspend fun reserve(eventId: Long, holder: String): String = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val (price, capacity) = db.rawQuery("SELECT price,capacity FROM events WHERE id=?",arrayOf(eventId.toString())).use { c ->
                require(c.moveToFirst()) { "Evento não encontrado" }; c.getLong(0) to c.getInt(1)
            }
            val count = db.rawQuery("SELECT COUNT(*) FROM tickets WHERE event_id=?",arrayOf(eventId.toString())).use { c -> c.moveToFirst();c.getInt(0) }
            BookingRules.validate(holder,count,capacity)?.let { throw IllegalArgumentException(it) }
            val id = UUID.randomUUID().toString()
            db.insertOrThrow("tickets",null,ContentValues().apply {
                put("id",id);put("event_id",eventId);put("holder",holder.trim());put("total",Pricing.total(price));put("created_at",System.currentTimeMillis());put("checked_in",0)
            })
            db.setTransactionSuccessful()
            id
        } finally { db.endTransaction() }
    }
    suspend fun checkIn(id: String): Boolean = withContext(Dispatchers.IO) {
        helper.writableDatabase.update("tickets",ContentValues().apply { put("checked_in",1) },"id=? AND checked_in=0",arrayOf(id)) == 1
    }
    suspend fun reset() = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try { db.delete("tickets",null,null);db.delete("favorites",null,null);db.setTransactionSuccessful() } finally { db.endTransaction() }
    }
    fun close() { helper.close() }
}
