package br.com.badgr.mobile.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class BookingRulesTest {
    @Test
    fun rejectsEmptyName() {
        val result = BookingRules.validate("  ", 0, 10)
        assertEquals("Informe seu nome com pelo menos 3 caracteres.", result)
    }

    @Test
    fun rejectsShortTrimmedName() {
        val result = BookingRules.validate(" A ", 0, 10)
        assertEquals("Informe seu nome com pelo menos 3 caracteres.", result)
    }

    @Test
    fun rejectsLongName() {
        val result = BookingRules.validate("A".repeat(81), 0, 10)
        assertEquals("O nome deve ter até 80 caracteres.", result)
    }

    @Test
    fun rejectsSoldOut() {
        assertEquals("Este lote está esgotado.", BookingRules.validate("Gustavo", 10, 10))
    }

    @Test
    fun rejectsOversold() {
        assertEquals("Este lote está esgotado.", BookingRules.validate("Gustavo", 11, 10))
    }

    @Test
    fun permitsLastTicket() {
        assertEquals(null, BookingRules.validate("Gustavo", 9, 10))
    }
}
