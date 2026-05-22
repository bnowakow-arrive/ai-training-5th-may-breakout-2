package com.arrive.ai_training_5th_may_breakout_2.contracts

data class OpportunityDto(
	val keyword: String,
	val gapType: GapType,
	val volume: Long,
	val kd: Int?,
	val competitorId: Long,
	val competitorName: String,
	val competitorPosition: Int?,
	val ourPosition: Int?,
	val score: Long,
)
