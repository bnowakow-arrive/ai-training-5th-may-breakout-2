package com.arrive.ai_training_5th_may_breakout_2.service

import com.arrive.ai_training_5th_may_breakout_2.contracts.BenchmarkResponse
import com.arrive.ai_training_5th_may_breakout_2.contracts.CompetitorDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.DomainMetricsDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.contracts.KeywordGapRowDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.MetricRow
import com.arrive.ai_training_5th_may_breakout_2.contracts.TrafficHistoryDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.TrafficHistorySeries
import com.arrive.ai_training_5th_may_breakout_2.domain.Competitor
import com.arrive.ai_training_5th_may_breakout_2.domain.DomainMetricsSnapshot
import com.arrive.ai_training_5th_may_breakout_2.domain.DomainTrafficHistorySnapshot
import com.arrive.ai_training_5th_may_breakout_2.domain.KeywordGapRow
import com.arrive.ai_training_5th_may_breakout_2.repo.CompetitorRepository
import com.arrive.ai_training_5th_may_breakout_2.repo.DomainMetricsRepository
import com.arrive.ai_training_5th_may_breakout_2.repo.DomainTrafficHistoryRepository
import com.arrive.ai_training_5th_may_breakout_2.repo.KeywordGapRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth

@Service
@Transactional(readOnly = true)
class BenchmarkService(
	private val competitorRepository: CompetitorRepository,
	private val metricsRepository: DomainMetricsRepository,
	private val gapRepository: KeywordGapRepository,
	private val trafficHistoryRepository: DomainTrafficHistoryRepository,
) {

	fun benchmark(): BenchmarkResponse {
		val all = competitorRepository.findAll()
		val own = all.firstOrNull { it.isOwn }?.let(::toMetricRow)
		val competitors = all.filterNot { it.isOwn }.map(::toMetricRow)
		return BenchmarkResponse(own = own, competitors = competitors)
	}

	fun keywordGap(competitorId: Long, gapType: GapType): List<KeywordGapRowDto> =
		gapRepository.findAllByCompetitorIdAndGapType(competitorId, gapType).map { it.toDto() }

	fun trafficHistory(): List<TrafficHistorySeries> {
		val all = competitorRepository.findAll()
		// Arrive first (so its line draws on top of competitors in the chart legend).
		val ordered = all.filter { it.isOwn } + all.filterNot { it.isOwn }
		return ordered.map { competitor ->
			val points = trafficHistoryRepository.findAllByCompetitorIdOrderByMonthAsc(competitor.id)
				.map { it.toDto() }
			TrafficHistorySeries(
				competitor = competitor.toCompetitorDto(),
				points = points,
			)
		}
	}

	private fun toMetricRow(competitor: Competitor): MetricRow {
		val snapshot = metricsRepository.findTopByCompetitorIdOrderByFetchedAtDesc(competitor.id)
		return MetricRow(
			competitor = competitor.toCompetitorDto(),
			metrics = snapshot?.toDto(),
			fetchedAt = snapshot?.fetchedAt,
		)
	}
}

private fun Competitor.toCompetitorDto() =
	CompetitorDto(id = id, name = name, domain = domain, isOwn = isOwn)

private fun DomainMetricsSnapshot.toDto() =
	DomainMetricsDto(
		domain = domain,
		organicKeywords = organicKeywords,
		organicTraffic = organicTraffic,
		organicCost = organicCost,
		top10Keywords = top10Keywords,
	)

private fun KeywordGapRow.toDto() =
	KeywordGapRowDto(
		keyword = keyword,
		gapType = gapType,
		volume = volume,
		kd = kd,
		positionBase = positionBase,
		positionCompetitor = positionCompetitor,
		cpc = cpc,
	)

private fun DomainTrafficHistorySnapshot.toDto() =
	TrafficHistoryDto(
		month = YearMonth.from(month).toString(),
		organicTraffic = organicTraffic,
		organicKeywords = organicKeywords,
	)
