package com.arrive.ai_training_5th_may_breakout_2.semrush

import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Live integration test — only runs when SEMRUSH_API_KEY is set in the environment.
 * Never run in CI or on a shared machine without understanding the credit cost.
 * Each fetchKeywordGap call consumes ~40 credits PER ROW returned.
 */
@SpringBootTest
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "SEMRUSH_API_KEY", matches = ".+")
class RealSemRushClientIntegrationTest {

    @Autowired
    lateinit var client: SemRushClient

    @Test
    fun `fetchDomainRanks returns non-zero organic keywords for arrive dot com`() {
        val result = client.fetchDomainRanks("arrive.com")
        assertTrue(result.organicKeywords > 0, "Expected non-zero organic keywords, got ${result.organicKeywords}")
    }

    @Test
    fun `fetchKeywordGap returns results for MISSING keywords vs parknow`() {
        val result = client.fetchKeywordGap("arrive.com", "parknow.com", GapType.MISSING)
        assertTrue(result.isNotEmpty(), "Expected at least one MISSING keyword gap row")
        assertTrue(result.all { it.volume >= 0 })
    }
}
