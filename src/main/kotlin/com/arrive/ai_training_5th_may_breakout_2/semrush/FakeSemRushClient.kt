package com.arrive.ai_training_5th_may_breakout_2.semrush

import com.arrive.ai_training_5th_may_breakout_2.contracts.DomainMetricsDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.contracts.KeywordGapRowDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.TrafficHistoryDto
import java.math.BigDecimal
import java.time.YearMonth
import kotlin.math.absoluteValue

class FakeSemRushClient : SemRushClient {

	override fun fetchDomainRanks(domain: String): DomainMetricsDto {
		val seed = domain.hashCode().absoluteValue
		return DomainMetricsDto(
			domain = domain,
			organicKeywords = 10_000L + (seed % 90_000L),
			organicTraffic = 50_000L + (seed % 450_000L),
			organicCost = BigDecimal(20_000 + (seed % 80_000)),
			top10Keywords = 200L + (seed % 1_800L),
		)
	}

	override fun fetchKeywordGap(
		baseDomain: String,
		competitorDomain: String,
		gapType: GapType,
	): List<KeywordGapRowDto> {
		val sample = when (gapType) {
			GapType.MISSING -> listOf(
				"fleet leasing comparison",
				"corporate car subscription",
				"ev fleet management software",
				"company car tax calculator",
				"flexible vehicle subscription",
			)
			GapType.UNTAPPED -> listOf(
				"electric van for business",
				"car subscription vs lease",
				"short term company car",
				"corporate mobility platform",
			)
			GapType.SHARED -> listOf(
				"car subscription",
				"flexible car lease",
				"all-inclusive car",
			)
			GapType.UNIQUE -> listOf(
				"$competitorDomain reviews",
				"$competitorDomain pricing",
			)
		}
		return sample.mapIndexed { index, keyword ->
			val seed = (keyword.hashCode() + baseDomain.hashCode()).absoluteValue
			KeywordGapRowDto(
				keyword = keyword,
				gapType = gapType,
				volume = 500L + (seed % 9_500L),
				kd = 10 + (seed % 70).toInt(),
				positionBase = if (gapType == GapType.MISSING) null else (index + 1) * 5,
				positionCompetitor = (index + 1) * 3,
				cpc = BigDecimal("0.${50 + (seed % 450)}"),
			)
		}
	}

	override fun fetchTrafficHistory(domain: String, months: Int): List<TrafficHistoryDto> {
		val seed = domain.hashCode().absoluteValue
		val trafficBase = 50_000L + (seed % 200_000L)
		val keywordsBase = 10_000L + (seed % 60_000L)
		val current = YearMonth.now()
		return (0 until months).map { idx ->
			val month = current.minusMonths((months - 1 - idx).toLong())
			// gentle upward trend (~3% per month compounded) with deterministic ±15% noise per month
			val growth = Math.pow(1.03, idx.toDouble())
			val noise = (((seed shr (idx % 16)) and 0xFF) % 31 - 15) / 100.0
			val traffic = (trafficBase * growth * (1 + noise)).toLong().coerceAtLeast(1_000)
			val keywords = (keywordsBase * growth * (1 + noise)).toLong().coerceAtLeast(100)
			TrafficHistoryDto(
				month = month.toString(),
				organicTraffic = traffic,
				organicKeywords = keywords,
			)
		}
	}
}
