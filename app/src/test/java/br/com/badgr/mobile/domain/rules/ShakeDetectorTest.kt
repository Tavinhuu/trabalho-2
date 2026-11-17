package br.com.badgr.mobile.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class ShakeDetectorTest {
    @Test
    fun gravityIsNotAShake() {
        val result = ShakeDetector().accept(0f, 0f, 9.80665f, 0)
        assertEquals(false, result)
    }

    @Test
    fun highAccelerationTriggers() {
        val result = ShakeDetector().accept(30f, 0f, 0f, 0)
        assertEquals(true, result)
    }

    @Test
    fun cooldownPreventsDuplicates() {
        val detector = ShakeDetector()
        assertEquals(true, detector.accept(30f, 0f, 0f, 1000))
        assertEquals(false, detector.accept(30f, 0f, 0f, 1500))
    }

    @Test
    fun nextShakeAfterCooldown() {
        val detector = ShakeDetector()
        assertEquals(true, detector.accept(30f, 0f, 0f, 1000))
        assertEquals(true, detector.accept(30f, 0f, 0f, 2800))
    }

    @Test
    fun directionDoesNotMatter() {
        assertEquals(true, ShakeDetector().accept(-30f, 0f, 0f, 0))
    }
}
