package br.com.badgr.mobile.data.mapper

import br.com.badgr.mobile.data.local.entity.EventEntity
import br.com.badgr.mobile.data.local.entity.TicketEntity
import br.com.badgr.mobile.domain.model.Event
import br.com.badgr.mobile.domain.model.Ticket

fun EventEntity.toDomain() = Event(id, title, date, venue, city, category, priceCents, capacity, latitude, longitude, description, color)
fun Event.toEntity() = EventEntity(id, title, date, venue, city, category, priceCents, capacity, latitude, longitude, description, color)
fun TicketEntity.toDomain() = Ticket(id, eventId, holder, totalCents, createdAt, checkedIn)
