package com.arrive.ai_training_5th_may_breakout_2.contracts

import java.time.Instant

data class MetricRow(
	val competitor: CompetitorDto,
	val metrics: DomainMetricsDto?,
	val fetchedAt: Instant?,
)

data class BenchmarkResponse(
	val own: MetricRow?,
	val competitors: List<MetricRow>,
)
