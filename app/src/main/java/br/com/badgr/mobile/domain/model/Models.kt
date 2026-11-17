package br.com.badgr.mobile.domain.model

data class Event(val id: Long, val title: String, val date: String, val venue: String,
    val city: String, val category: String, val priceCents: Long, val capacity: Int,
    val latitude: Double, val longitude: Double, val description: String, val color: Int)
data class Ticket(val id: String, val eventId: Long, val holder: String, val totalCents: Long,
    val createdAt: Long, val checkedIn: Boolean = false)
data class Catalog(val events: List<Event> = emptyList(), val favorites: Set<Long> = emptySet(),
    val tickets: List<Ticket> = emptyList())

