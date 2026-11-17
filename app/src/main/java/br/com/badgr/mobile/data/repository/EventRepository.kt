package br.com.badgr.mobile.data.repository

import androidx.room.withTransaction
import br.com.badgr.mobile.data.local.BadgrDatabase
import br.com.badgr.mobile.data.local.DemoEvents
import br.com.badgr.mobile.data.local.entity.FavoriteEntity
import br.com.badgr.mobile.data.local.entity.TicketEntity
import br.com.badgr.mobile.data.mapper.toDomain
import br.com.badgr.mobile.data.mapper.toEntity
import br.com.badgr.mobile.domain.model.Catalog
import br.com.badgr.mobile.domain.rules.BookingRules
import br.com.badgr.mobile.domain.rules.Pricing
import java.util.UUID
class EventRepository(private val database: BadgrDatabase) {
    private val events = database.eventDao()
    private val favorites = database.favoriteDao()
    private val tickets = database.ticketDao()

    suspend fun load(): Catalog = database.withTransaction {
        if (events.count() == 0) events.insertAll(DemoEvents.all.map { it.toEntity() })
        Catalog(events.getAll().map { it.toDomain() }, favorites.getEventIds().toSet(), tickets.getAll().map { it.toDomain() })
    }

    suspend fun toggleFavorite(id: Long) = database.withTransaction {
        requireNotNull(events.getById(id)) { "Evento não encontrado" }
        if (favorites.delete(id) == 0) favorites.insert(FavoriteEntity(id))
    }

    suspend fun reserve(eventId: Long, holder: String): String = database.withTransaction {
        val event = requireNotNull(events.getById(eventId)) { "Evento não encontrado" }
        BookingRules.validate(holder, tickets.countForEvent(eventId), event.capacity)?.let {
            throw IllegalArgumentException(it)
        }
        val id = UUID.randomUUID().toString()
        tickets.insert(TicketEntity(id, eventId, holder.trim(), Pricing.total(event.priceCents), System.currentTimeMillis()))
        id
    }

    suspend fun checkIn(id: String): Boolean = tickets.checkIn(id) == 1

    suspend fun reset() = database.withTransaction {
        tickets.deleteAll()
        favorites.deleteAll()
    }
}
