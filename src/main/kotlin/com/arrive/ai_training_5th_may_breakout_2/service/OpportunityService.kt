package com.arrive.ai_training_5th_may_breakout_2.service

import com.arrive.ai_training_5th_may_breakout_2.contracts.OpportunityDto
import com.arrive.ai_training_5th_may_breakout_2.domain.KeywordGapRow
import com.arrive.ai_training_5th_may_breakout_2.repo.CompetitorRepository
import com.arrive.ai_training_5th_may_breakout_2.repo.KeywordGapRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OpportunityService(
	private val competitorRepository: CompetitorRepository,
	private val gapRepository: KeywordGapRepository,
) {

	fun topOpportunities(limit: Int = 10): List<OpportunityDto> {
		val competitorById = competitorRepository.findAll().associateBy { it.id }
		return gapRepository.findAll()
			.mapNotNull { row ->
				val competitor = competitorById[row.competitor.id] ?: return@mapNotNull null
				if (competitor.isOwn) return@mapNotNull null
				OpportunityDto(
					keyword = row.keyword,
					gapType = row.gapType,
					volume = row.volume,
					kd = row.kd,
					competitorId = competitor.id,
					competitorName = competitor.name,
					competitorPosition = row.positionCompetitor,
					ourPosition = row.positionBase,
					score = score(row),
				)
			}
			.sortedByDescending { it.score }
			.take(limit)
	}

	/**
	 * volume × competitor-rank-strength × our-weakness ÷ difficulty.
	 * Missing positions: competitor defaults to mid-pack (50), ourPosition (when null) treated as 100.
	 */
	private fun score(row: KeywordGapRow): Long {
		val competitorPos = row.positionCompetitor ?: 50
		val competitorStrength = ((101 - competitorPos).coerceIn(1, 100)) / 100.0
		val ourWeakness = (row.positionBase?.let { (it - competitorPos).coerceAtLeast(0) } ?: 100).toDouble()
		val difficultyFactor = 100.0 / ((row.kd ?: 50) + 10)
		return (row.volume * competitorStrength * ourWeakness * difficultyFactor / 100.0).toLong()
	}
}
