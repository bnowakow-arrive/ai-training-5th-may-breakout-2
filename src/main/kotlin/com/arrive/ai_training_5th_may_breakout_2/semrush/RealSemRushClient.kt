package com.arrive.ai_training_5th_may_breakout_2.semrush

import com.arrive.ai_training_5th_may_breakout_2.contracts.DomainMetricsDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.contracts.KeywordGapRowDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.TrafficHistoryDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.time.YearMonth

/**
 * Live SEMRush client. Active only when semrush.live=true.
 * FakeSemRushClient remains the default so no credits are spent during normal dev/CI runs.
 *
 * CREDIT BUDGET: 50,000/month shared. domain_domains charges ~40 credits PER ROW.
 * Never raise semrush.gap-row-limit without team approval.
 */
@Component
@ConditionalOnProperty(name = ["semrush.live"], havingValue = "true")
class RealSemRushClient(
    @Value("\${semrush.api.key:}") private val apiKey: String,
    @Value("\${semrush.api.base-url:https://api.semrush.com/}") private val baseUrl: String,
    @Value("\${semrush.database:us}") private val database: String,
    @Value("\${semrush.gap-row-limit:25}") private val gapRowLimit: Int,
) : SemRushClient {

    init {
        // If key is blank Spring falls back to FakeSemRushClient via @ConditionalOnMissingBean.
        // This guard makes misconfiguration loud rather than silent.
        require(apiKey.isNotBlank()) {
            "semrush.api.key must be set when semrush.live=true"
        }
    }

    private val http: RestClient = RestClient.builder().baseUrl(baseUrl).build()

    // ── Public API ─────────────────────────────────────────────────────────────

    override fun fetchDomainRanks(domain: String): DomainMetricsDto {
        val csv = get(
            "type=domain_ranks" +
            "&key=$apiKey" +
            "&domain=$domain" +
            "&database=$database" +
            "&export_columns=Dn,Or,Ot,Oc"
        )
        return parseDomainRanks(domain, csv)
    }

    override fun fetchKeywordGap(
        baseDomain: String,
        competitorDomain: String,
        gapType: GapType,
    ): List<KeywordGapRowDto> {
        // Ph=phrase, Nq=volume, Cp=CPC, Kd=keyword difficulty,
        // P0=position of first listed domain (baseDomain), P1=position of second (competitorDomain).
        // Adding Kd/P0/P1 doesn't charge extra credits — credits are per-row, not per-column.
        val csv = get(
            "type=domain_domains" +
            "&key=$apiKey" +
            "&database=$database" +
            "&domain=$baseDomain" +
            "&domains=${gapType.toDomainsParam(baseDomain, competitorDomain)}" +
            "&display_limit=$gapRowLimit" +
            "&export_columns=Ph,Nq,Cp,Kd,P0,P1"
        )
        return parseKeywordGap(gapType, csv)
    }

    override fun fetchTrafficHistory(domain: String, months: Int): List<TrafficHistoryDto> {
        val current = YearMonth.now()
        // No batch endpoint exists for monthly history — one call per month per domain.
        // ~10 credits/call. 12 months × N domains can quickly add up.
        return (0 until months).map { idx ->
            val month = current.minusMonths((months - 1 - idx).toLong())
            val displayDate = "%04d%02d15".format(month.year, month.monthValue)
            val csv = get(
                "type=domain_rank" +
                "&key=$apiKey" +
                "&domain=$domain" +
                "&database=$database" +
                "&display_date=$displayDate" +
                "&export_columns=Dn,Or,Ot,Oc"
            )
            parseMonthlyRanks(month, csv)
        }
    }

    // ── Parsers (internal so unit tests can reach them without a Spring context) ─

    internal fun parseDomainRanks(domain: String, csv: String): DomainMetricsDto {
        val lines = csv.trim().lines()
        // Row 0 is the header; row 1 is the data. Empty / error responses have < 2 lines.
        if (lines.size < 2) return DomainMetricsDto(domain, 0L, 0L, BigDecimal.ZERO, 0L)
        val p = lines[1].split(";")
        return DomainMetricsDto(
            domain = domain,
            organicKeywords = p.getOrNull(1)?.toLongOrNull() ?: 0L,
            organicTraffic   = p.getOrNull(2)?.toLongOrNull() ?: 0L,
            organicCost      = p.getOrNull(3)?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
            // domain_ranks does not expose a top-10 count directly.
            // A separate domain_organic call with display_filter=+|Po|Le|10 would be needed;
            // deferred to avoid the extra credit spend per refresh.
            top10Keywords = 0L,
        )
    }

    internal fun parseKeywordGap(gapType: GapType, csv: String): List<KeywordGapRowDto> {
        val lines = csv.trim().lines()
        if (lines.size < 2) return emptyList()
        return lines.drop(1).mapNotNull { line ->
            val p = line.split(";")
            if (p.size < 3) return@mapNotNull null
            KeywordGapRowDto(
                keyword            = p[0].trim(),
                gapType            = gapType,
                volume             = p.getOrNull(1)?.toLongOrNull() ?: 0L,
                cpc                = p.getOrNull(2)?.toBigDecimalOrNull(),
                kd                 = p.getOrNull(3)?.toIntOrNull(),
                positionBase       = p.getOrNull(4)?.toIntOrNull(),
                positionCompetitor = p.getOrNull(5)?.toIntOrNull(),
            )
        }
    }

    internal fun parseMonthlyRanks(month: YearMonth, csv: String): TrafficHistoryDto {
        val lines = csv.trim().lines()
        if (lines.size < 2) {
            return TrafficHistoryDto(month.toString(), 0L, 0L)
        }
        val p = lines[1].split(";")
        return TrafficHistoryDto(
            month            = month.toString(),
            organicKeywords  = p.getOrNull(1)?.toLongOrNull() ?: 0L,
            organicTraffic   = p.getOrNull(2)?.toLongOrNull() ?: 0L,
        )
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private fun get(query: String): String =
        http.get().uri("?$query").retrieve().body(String::class.java) ?: ""

    // SEMRush domain_domains sign pairs:
    //   +|or|domain  = domain must rank for the keyword
    //   -|or|domain  = domain must NOT rank for the keyword
    private fun GapType.toDomainsParam(base: String, competitor: String) = when (this) {
        GapType.MISSING  -> "-|or|$base|+|or|$competitor"   // competitor has it, base doesn't
        GapType.UNTAPPED -> "+|or|$base|+|or|$competitor"   // both rank; base lags (no position filter via this API)
        GapType.SHARED   -> "+|or|$base|+|or|$competitor"   // both rank
        GapType.UNIQUE   -> "+|or|$base|-|or|$competitor"   // only base ranks
    }
}
