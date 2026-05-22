package com.arrive.ai_training_5th_may_breakout_2.contracts

data class TrafficHistoryDto(
	val month: String,
	val organicTraffic: Long,
	val organicKeywords: Long,
)

data class TrafficHistorySeries(
	val competitor: CompetitorDto,
	val points: List<TrafficHistoryDto>,
)
