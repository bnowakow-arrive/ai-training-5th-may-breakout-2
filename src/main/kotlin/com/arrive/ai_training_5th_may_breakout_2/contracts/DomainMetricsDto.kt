package com.arrive.ai_training_5th_may_breakout_2.contracts

import java.math.BigDecimal

data class DomainMetricsDto(
	val domain: String,
	val organicKeywords: Long,
	val organicTraffic: Long,
	val organicCost: BigDecimal,
	val top10Keywords: Long,
)
