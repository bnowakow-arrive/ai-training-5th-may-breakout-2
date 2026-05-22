package com.arrive.ai_training_5th_may_breakout_2.semrush

import com.arrive.ai_training_5th_may_breakout_2.contracts.DomainMetricsDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.contracts.KeywordGapRowDto

interface SemRushClient {

	fun fetchDomainRanks(domain: String): DomainMetricsDto

	fun fetchKeywordGap(
		baseDomain: String,
		competitorDomain: String,
		gapType: GapType,
	): List<KeywordGapRowDto>
}
