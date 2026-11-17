package br.com.badgr.mobile

import br.com.badgr.mobile.domain.model.Event
import br.com.badgr.mobile.domain.rules.*

import org.junit.Assert.*
import org.junit.Test

class PricingTest {
    @Test fun freeTicketHasNoFee() { assertEquals(0L,Pricing.fee(0)) }
    @Test fun minimumFeeApplies() { assertEquals(150L,Pricing.fee(1000)) }
    @Test fun tenPercentForPaidTicket() { assertEquals(350L,Pricing.fee(3500)) }
    @Test fun totalIncludesFee() { assertEquals(3850L,Pricing.total(3500)) }
    @Test fun centsRoundHalfUp() { assertEquals(251L,Pricing.fee(2505)) }
    @Test(expected=IllegalArgumentException::class) fun negativePriceRejected() { Pricing.fee(-1) }
}
class BookingRulesTest {
    @Test fun rejectsEmptyName() { assertNotNull(BookingRules.validate("  ",0,10)) }
    @Test fun rejectsShortTrimmedName() { assertNotNull(BookingRules.validate(" A ",0,10)) }
    @Test fun rejectsLongName() { assertNotNull(BookingRules.validate("A".repeat(81),0,10)) }
    @Test fun rejectsSoldOut() { assertNotNull(BookingRules.validate("Gustavo",10,10)) }
    @Test fun rejectsOversold() { assertNotNull(BookingRules.validate("Gustavo",11,10)) }
    @Test fun permitsLastTicket() { assertNull(BookingRules.validate("Gustavo",9,10)) }
}
class ShakeDetectorTest {
    @Test fun gravityIsNotAShake() { assertFalse(ShakeDetector().accept(0f,0f,9.80665f,0)) }
    @Test fun highAccelerationTriggers() { assertTrue(ShakeDetector().accept(30f,0f,0f,0)) }
    @Test fun cooldownPreventsDuplicates() {
        val detector=ShakeDetector();assertTrue(detector.accept(30f,0f,0f,1000));assertFalse(detector.accept(30f,0f,0f,1500))
    }
    @Test fun nextShakeAfterCooldown() {
        val detector=ShakeDetector();detector.accept(30f,0f,0f,1000);assertTrue(detector.accept(30f,0f,0f,2800))
    }
    @Test fun directionDoesNotMatter() { assertTrue(ShakeDetector().accept(-30f,0f,0f,0)) }
}
class EventSearchTest {
    private val event=Event(1,"Frequência", "21 NOV", "Espaço Aurora", "São Paulo", "Techno",1000,5,0.0,0.0,"",0)
    @Test fun ignoresAccentsAndCase() { assertEquals(1,EventSearch.filter(listOf(event),"FREQUENCIA").size) }
    @Test fun searchesCity() { assertEquals(1,EventSearch.filter(listOf(event),"sao paulo").size) }
    @Test fun categoryMustMatch() { assertTrue(EventSearch.filter(listOf(event),"","House").isEmpty()) }
    @Test fun trimsQuery() { assertEquals(1,EventSearch.filter(listOf(event),"  aurora  ").size) }
    @Test fun returnsEmptyWhenNoMatch() { assertTrue(EventSearch.filter(listOf(event),"outro").isEmpty()) }
}
