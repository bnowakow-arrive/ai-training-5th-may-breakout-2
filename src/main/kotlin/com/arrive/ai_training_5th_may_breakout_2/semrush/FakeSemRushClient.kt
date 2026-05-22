package com.arrive.ai_training_5th_may_breakout_2.semrush

import com.arrive.ai_training_5th_may_breakout_2.contracts.DomainMetricsDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.contracts.KeywordGapRowDto
import java.math.BigDecimal
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
}
