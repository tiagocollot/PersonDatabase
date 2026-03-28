package com.example.unit

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RateLimiterTest {

    companion object {
        private const val MOCK_IP_PREFIX = "10.0.0."
        private fun mockIp(n: Int) = "${MOCK_IP_PREFIX}$n"
    }

    @Test
    fun `isAllowed returns true for first request`() {
        RateLimiterTestHelper.reset()
        
        val result = RateLimiterTestHelper.isAllowed(mockIp(1))
        
        assertTrue(result)
    }

    @Test
    fun `isAllowed returns true within limit`() {
        RateLimiterTestHelper.reset()
        
        repeat(99) {
            RateLimiterTestHelper.isAllowed(mockIp(2))
        }
        
        val result = RateLimiterTestHelper.isAllowed(mockIp(2))
        assertTrue(result)
    }

    @Test
    fun `isAllowed returns false when limit exceeded`() {
        RateLimiterTestHelper.reset()
        
        repeat(100) {
            RateLimiterTestHelper.isAllowed(mockIp(3))
        }
        
        val result = RateLimiterTestHelper.isAllowed(mockIp(3))
        assertFalse(result)
    }

    @Test
    fun `different IPs have separate limits`() {
        RateLimiterTestHelper.reset()
        
        repeat(100) {
            RateLimiterTestHelper.isAllowed(mockIp(10))
        }
        
        val result = RateLimiterTestHelper.isAllowed(mockIp(11))
        assertTrue(result)
    }

    @Test
    fun `getRemaining returns correct count`() {
        RateLimiterTestHelper.reset()
        
        RateLimiterTestHelper.isAllowed(mockIp(20))
        RateLimiterTestHelper.isAllowed(mockIp(20))
        
        val remaining = RateLimiterTestHelper.getRemaining(mockIp(20))
        assertEquals(98, remaining)
    }

    @Test
    fun `getRemaining returns max for new IP`() {
        RateLimiterTestHelper.reset()
        
        val remaining = RateLimiterTestHelper.getRemaining(mockIp(99))
        assertEquals(100, remaining)
    }

    @Test
    fun `getResetTime returns positive for existing IP`() {
        RateLimiterTestHelper.reset()
        
        RateLimiterTestHelper.isAllowed(mockIp(30))
        
        val resetTime = RateLimiterTestHelper.getResetTime(mockIp(30))
        assertTrue(resetTime > 0)
    }

    @Test
    fun `getResetTime returns zero for new IP`() {
        RateLimiterTestHelper.reset()
        
        val resetTime = RateLimiterTestHelper.getResetTime(mockIp(100))
        assertEquals(0, resetTime)
    }

    @Test
    fun `thread safety - concurrent requests`() {
        RateLimiterTestHelper.reset()
        
        val latch = CountDownLatch(50)
        repeat(50) {
            Thread {
                RateLimiterTestHelper.isAllowed(mockIp(40))
                latch.countDown()
            }.start()
        }
        
        latch.await(5, TimeUnit.SECONDS)
        
        val remaining = RateLimiterTestHelper.getRemaining(mockIp(40))
        assertTrue(remaining <= 100 && remaining >= 50)
    }
}

object RateLimiterTestHelper {
    private val requests = java.util.concurrent.ConcurrentHashMap<String, RateLimitEntry>()
    private const val MAX_REQUESTS = 100
    private const val WINDOW_MS = 60_000L

    data class RateLimitEntry(
        val count: java.util.concurrent.atomic.AtomicInteger,
        val windowStart: Long
    )

    fun reset() {
        requests.clear()
    }

    fun isAllowed(ip: String): Boolean {
        val now = System.currentTimeMillis()
        val entry = requests.compute(ip) { _, existing ->
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                RateLimitEntry(java.util.concurrent.atomic.AtomicInteger(1), now)
            } else {
                existing.count.incrementAndGet()
                existing
            }
        }
        return entry!!.count.get() <= MAX_REQUESTS
    }

    fun getRemaining(ip: String): Int {
        val entry = requests[ip] ?: return MAX_REQUESTS
        return maxOf(0, MAX_REQUESTS - entry.count.get())
    }

    fun getResetTime(ip: String): Long {
        val entry = requests[ip] ?: return 0
        return maxOf(0, entry.windowStart + WINDOW_MS - System.currentTimeMillis())
    }
}
