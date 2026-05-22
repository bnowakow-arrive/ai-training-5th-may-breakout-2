package com.arrive.ai_training_5th_may_breakout_2.service

import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.domain.Competitor
import com.arrive.ai_training_5th_may_breakout_2.domain.DomainMetricsSnapshot
import com.arrive.ai_training_5th_may_breakout_2.domain.DomainTrafficHistorySnapshot
import com.arrive.ai_training_5th_may_breakout_2.domain.KeywordGapRow
import com.arrive.ai_training_5th_may_breakout_2.repo.CompetitorRepository
import com.arrive.ai_training_5th_may_breakout_2.repo.DomainMetricsRepository
import com.arrive.ai_training_5th_may_breakout_2.repo.DomainTrafficHistoryRepository
import com.arrive.ai_training_5th_may_breakout_2.repo.KeywordGapRepository
import com.arrive.ai_training_5th_may_breakout_2.semrush.SemRushClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

@Service
class RefreshService(
	private val competitorRepository: CompetitorRepository,
	private val metricsRepository: DomainMetricsRepository,
	private val gapRepository: KeywordGapRepository,
	private val trafficHistoryRepository: DomainTrafficHistoryRepository,
	private val semRushClient: SemRushClient,
	@Value("\${semrush.cache-ttl-hours:24}") private val cacheTtlHours: Long,
	@Value("\${semrush.history-months:12}") private val historyMonths: Int,
) {
	private val log = LoggerFactory.getLogger(RefreshService::class.java)

	@Transactional
	fun refreshCompetitor(id: Long) {
		val competitor = competitorRepository.findById(id).orElseThrow {
			ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor $id not found")
		}
		val own = competitorRepository.findByIsOwnTrue()
			?: throw ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"No isOwn=true competitor configured — set one before refreshing.",
			)

		refreshMetricsIfStale(own)
		refreshTrafficHistoryIfStale(own)
		if (own.id != competitor.id) {
			refreshMetricsIfStale(competitor)
			refreshTrafficHistoryIfStale(competitor)
			refreshKeywordGap(base = own, competitor = competitor)
		}
	}

	@Transactional
	fun refreshAll() {
		val all = competitorRepository.findAll()
		val own = all.firstOrNull { it.isOwn }
			?: throw ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"No isOwn=true competitor configured — set one before refreshing.",
			)
		refreshMetricsIfStale(own)
		refreshTrafficHistoryIfStale(own)
		all.filterNot { it.isOwn }.forEach { competitor ->
			refreshMetricsIfStale(competitor)
			refreshTrafficHistoryIfStale(competitor)
			refreshKeywordGap(base = own, competitor = competitor)
		}
	}

	private fun refreshMetricsIfStale(competitor: Competitor) {
		val latest = metricsRepository.findTopByCompetitorIdOrderByFetchedAtDesc(competitor.id)
		if (latest != null && isFresh(latest.fetchedAt)) {
			log.debug("Skipping SEMRush fetch for {} — cache hit ({})", competitor.domain, latest.fetchedAt)
			return
		}
		val dto = semRushClient.fetchDomainRanks(competitor.domain)
		metricsRepository.save(
			DomainMetricsSnapshot(
				competitor = competitor,
				domain = dto.domain,
				organicKeywords = dto.organicKeywords,
				organicTraffic = dto.organicTraffic,
				organicCost = dto.organicCost,
				top10Keywords = dto.top10Keywords,
				fetchedAt = Instant.now(),
			),
		)
	}

	private fun refreshKeywordGap(base: Competitor, competitor: Competitor) {
		gapRepository.deleteAllByCompetitorId(competitor.id)
		val now = Instant.now()
		val rows = listOf(GapType.MISSING, GapType.UNTAPPED).flatMap { type ->
			semRushClient.fetchKeywordGap(base.domain, competitor.domain, type).map { dto ->
				KeywordGapRow(
					competitor = competitor,
					keyword = dto.keyword,
					gapType = dto.gapType,
					volume = dto.volume,
					kd = dto.kd,
					positionBase = dto.positionBase,
					positionCompetitor = dto.positionCompetitor,
					cpc = dto.cpc,
					fetchedAt = now,
				)
			}
		}
		gapRepository.saveAll(rows)
	}

	private fun refreshTrafficHistoryIfStale(competitor: Competitor) {
		val latest = trafficHistoryRepository.findTopByCompetitorIdOrderByFetchedAtDesc(competitor.id)
		if (latest != null && isFresh(latest.fetchedAt)) {
			log.debug("Skipping SEMRush traffic-history fetch for {} — cache hit ({})", competitor.domain, latest.fetchedAt)
			return
		}
		val now = Instant.now()
		semRushClient.fetchTrafficHistory(competitor.domain, historyMonths).forEach { dto ->
			val monthDate = LocalDate.parse("${dto.month}-01")
			val existing = trafficHistoryRepository.findByCompetitorIdAndMonth(competitor.id, monthDate)
			if (existing != null) {
				existing.organicTraffic = dto.organicTraffic
				existing.organicKeywords = dto.organicKeywords
				existing.fetchedAt = now
				trafficHistoryRepository.save(existing)
			} else {
				trafficHistoryRepository.save(
					DomainTrafficHistorySnapshot(
						competitor = competitor,
						month = monthDate,
						organicTraffic = dto.organicTraffic,
						organicKeywords = dto.organicKeywords,
						fetchedAt = now,
					),
				)
			}
		}
	}

	private fun isFresh(fetchedAt: Instant): Boolean =
		Duration.between(fetchedAt, Instant.now()) < Duration.ofHours(cacheTtlHours)
}
