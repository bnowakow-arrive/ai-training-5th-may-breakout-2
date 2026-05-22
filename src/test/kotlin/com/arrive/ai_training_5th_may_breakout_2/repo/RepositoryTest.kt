package com.arrive.ai_training_5th_may_breakout_2.repo

import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.domain.Competitor
import com.arrive.ai_training_5th_may_breakout_2.domain.DomainMetricsSnapshot
import com.arrive.ai_training_5th_may_breakout_2.domain.KeywordGapRow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class RepositoryTest {

    @Autowired lateinit var competitorRepo: CompetitorRepository
    @Autowired lateinit var metricsRepo: DomainMetricsRepository
    @Autowired lateinit var gapRepo: KeywordGapRepository

    @Test
    fun `save and retrieve competitor`() {
        val saved = competitorRepo.save(Competitor(name = "TestCo", domain = "testco.com", isOwn = false))
        val found = competitorRepo.findById(saved.id)
        assertTrue(found.isPresent)
        assertEquals("testco.com", found.get().domain)
    }

    @Test
    fun `findTopByCompetitorIdOrderByFetchedAtDesc returns latest snapshot`() {
        val c = competitorRepo.save(Competitor(name = "SnapTest", domain = "snaptest.com", isOwn = false))
        metricsRepo.save(snapshot(c, Instant.parse("2026-01-01T00:00:00Z")))
        val newer = metricsRepo.save(snapshot(c, Instant.parse("2026-06-01T00:00:00Z")))

        val result = metricsRepo.findTopByCompetitorIdOrderByFetchedAtDesc(c.id)
        assertNotNull(result)
        assertEquals(newer.id, result!!.id)
    }

    @Test
    fun `findAllByCompetitorIdAndGapType filters by type`() {
        val c = competitorRepo.save(Competitor(name = "GapTest", domain = "gaptest.com", isOwn = false))
        gapRepo.save(gap(c, "kw one", GapType.MISSING))
        gapRepo.save(gap(c, "kw two", GapType.UNTAPPED))
        gapRepo.save(gap(c, "kw three", GapType.MISSING))

        val missing = gapRepo.findAllByCompetitorIdAndGapType(c.id, GapType.MISSING)
        assertEquals(2, missing.size)
        assertTrue(missing.all { it.gapType == GapType.MISSING })
    }

    @Test
    fun `deleteAllByCompetitorId removes only that competitor rows`() {
        val c1 = competitorRepo.save(Competitor(name = "Del1", domain = "del1.com", isOwn = false))
        val c2 = competitorRepo.save(Competitor(name = "Del2", domain = "del2.com", isOwn = false))
        gapRepo.save(gap(c1, "kw c1", GapType.MISSING))
        gapRepo.save(gap(c2, "kw c2", GapType.MISSING))

        gapRepo.deleteAllByCompetitorId(c1.id)

        assertEquals(0, gapRepo.findAllByCompetitorIdAndGapType(c1.id, GapType.MISSING).size)
        assertEquals(1, gapRepo.findAllByCompetitorIdAndGapType(c2.id, GapType.MISSING).size)
    }

    private fun snapshot(c: Competitor, at: Instant) = DomainMetricsSnapshot(
        competitor = c, domain = c.domain,
        organicKeywords = 1000, organicTraffic = 50000,
        organicCost = BigDecimal("3000.00"), top10Keywords = 100, fetchedAt = at,
    )

    private fun gap(c: Competitor, keyword: String, type: GapType) = KeywordGapRow(
        competitor = c, keyword = keyword, gapType = type,
        volume = 1000, kd = 40, positionBase = null, positionCompetitor = 5, cpc = BigDecimal("1.50"),
        fetchedAt = Instant.now(),
    )
}
