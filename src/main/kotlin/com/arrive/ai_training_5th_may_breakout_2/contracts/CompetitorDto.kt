package com.arrive.ai_training_5th_may_breakout_2.contracts

data class CompetitorDto(
	val id: Long?,
	val name: String,
	val domain: String,
	val isOwn: Boolean,
)

data class CreateCompetitorRequest(
	val name: String,
	val domain: String,
	val isOwn: Boolean = false,
)
