package br.com.badgr.mobile.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class PricingTest {
    @Test
    fun freeTicketHasNoFee() {
        assertEquals(0L, Pricing.fee(0))
    }

    @Test
    fun minimumFeeApplies() {
        assertEquals(150L, Pricing.fee(1000))
    }

    @Test
    fun tenPercentForPaidTicket() {
        assertEquals(350L, Pricing.fee(3500))
    }

    @Test
    fun totalIncludesFee() {
        assertEquals(3850L, Pricing.total(3500))
    }

    @Test
    fun centsRoundHalfUp() {
        assertEquals(251L, Pricing.fee(2505))
    }

    @Test
    fun negativePriceRejected() {
        val error = runCatching { Pricing.fee(-1) }.exceptionOrNull()
        assertEquals(IllegalArgumentException::class.java, error?.javaClass)
        assertEquals("Preço inválido", error?.message)
    }
}
