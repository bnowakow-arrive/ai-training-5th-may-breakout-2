package com.arrive.ai_training_5th_may_breakout_2.contracts

import java.math.BigDecimal

data class KeywordGapRowDto(
	val keyword: String,
	val gapType: GapType,
	val volume: Long,
	val kd: Int?,
	val positionBase: Int?,
	val positionCompetitor: Int?,
	val cpc: BigDecimal?,
)
