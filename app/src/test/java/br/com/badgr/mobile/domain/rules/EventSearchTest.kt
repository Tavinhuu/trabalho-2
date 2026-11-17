package br.com.badgr.mobile.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class EventSearchTest {
    private val event = br.com.badgr.mobile.domain.model.Event(
        1, "Frequência", "21 NOV", "Espaço Aurora", "São Paulo",
        "Techno", 1000, 5, 0.0, 0.0, "", 0
    )

    @Test
    fun ignoresAccentsAndCase() {
        assertEquals(listOf(event), EventSearch.filter(listOf(event), "FREQUENCIA"))
    }

    @Test
    fun searchesCity() {
        assertEquals(listOf(event), EventSearch.filter(listOf(event), "sao paulo"))
    }

    @Test
    fun categoryMustMatch() {
        assertEquals(emptyList<br.com.badgr.mobile.domain.model.Event>(), EventSearch.filter(listOf(event), "", "House"))
    }

    @Test
    fun trimsQuery() {
        assertEquals(listOf(event), EventSearch.filter(listOf(event), "  aurora  "))
    }

    @Test
    fun returnsEmptyWhenNoMatch() {
        assertEquals(emptyList<br.com.badgr.mobile.domain.model.Event>(), EventSearch.filter(listOf(event), "outro"))
    }
}
