package com.arrive.ai_training_5th_may_breakout_2.semrush

import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class RealSemRushClientCsvTest {

    private val client = RealSemRushClient(
        apiKey    = "test-key",
        baseUrl   = "https://api.semrush.com/",
        database  = "us",
        gapRowLimit = 25,
    )

    // ── parseDomainRanks ────────────────────────────────────────────────────────

    @Test
    fun `parseDomainRanks parses a well-formed response`() {
        val csv = """
            Domain;Organic Keywords;Organic Traffic;Organic Cost
            arrive.com;1200;45000;3200.50
        """.trimIndent()

        val result = client.parseDomainRanks("arrive.com", csv)

        assertEquals("arrive.com", result.domain)
        assertEquals(1200L, result.organicKeywords)
        assertEquals(45000L, result.organicTraffic)
        assertEquals(BigDecimal("3200.50"), result.organicCost)
    }

    @Test
    fun `parseDomainRanks returns zero metrics for empty response`() {
        val result = client.parseDomainRanks("unknown.com", "")

        assertEquals("unknown.com", result.domain)
        assertEquals(0L, result.organicKeywords)
        assertEquals(0L, result.organicTraffic)
        assertEquals(BigDecimal.ZERO, result.organicCost)
    }

    // ── parseKeywordGap ─────────────────────────────────────────────────────────

    @Test
    fun `parseKeywordGap parses a well-formed response`() {
        val csv = """
            Keyword;Search Volume;CPC
            parking app uk;9900;4.50
            fleet leasing comparison;3600;2.10
        """.trimIndent()

        val result = client.parseKeywordGap(GapType.MISSING, csv)

        assertEquals(2, result.size)
        assertEquals("parking app uk", result[0].keyword)
        assertEquals(9900L, result[0].volume)
        assertEquals(BigDecimal("4.50"), result[0].cpc)
        assertEquals(GapType.MISSING, result[0].gapType)
        assertEquals("fleet leasing comparison", result[1].keyword)
        assertEquals(3600L, result[1].volume)
    }

    @Test
    fun `parseKeywordGap skips malformed rows and returns valid ones`() {
        val csv = """
            Keyword;Search Volume;CPC
            good-keyword;1000;1.50
            bad-row
            another-good;500;0.80
        """.trimIndent()

        val result = client.parseKeywordGap(GapType.UNTAPPED, csv)

        assertEquals(2, result.size)
        assertEquals("good-keyword", result[0].keyword)
        assertEquals("another-good", result[1].keyword)
    }

    @Test
    fun `parseKeywordGap returns empty list for header-only response`() {
        val csv = "Keyword;Search Volume;CPC"

        val result = client.parseKeywordGap(GapType.MISSING, csv)

        assertTrue(result.isEmpty())
    }
}
