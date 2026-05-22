package com.arrive.ai_training_5th_may_breakout_2.service

import com.arrive.ai_training_5th_may_breakout_2.contracts.CompetitorDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.CreateCompetitorRequest
import com.arrive.ai_training_5th_may_breakout_2.domain.Competitor
import com.arrive.ai_training_5th_may_breakout_2.repo.CompetitorRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CompetitorService(
	private val competitorRepository: CompetitorRepository,
) {

	fun list(): List<CompetitorDto> =
		competitorRepository.findAll().map { it.toDto() }

	fun findOwn(): Competitor? = competitorRepository.findByIsOwnTrue()

	@Transactional
	fun create(request: CreateCompetitorRequest): CompetitorDto {
		if (request.isOwn) {
			val existingOwn = competitorRepository.findByIsOwnTrue()
			if (existingOwn != null) {
				throw ResponseStatusException(
					HttpStatus.CONFLICT,
					"There can be only one isOwn=true competitor (existing: ${existingOwn.name}).",
				)
			}
		}
		val saved = competitorRepository.save(
			Competitor(
				name = request.name.trim(),
				domain = request.domain.trim().lowercase(),
				isOwn = request.isOwn,
			),
		)
		return saved.toDto()
	}

	@Transactional
	fun delete(id: Long) {
		if (!competitorRepository.existsById(id)) {
			throw ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor $id not found")
		}
		competitorRepository.deleteById(id)
	}

	fun get(id: Long): Competitor =
		competitorRepository.findById(id).orElseThrow {
			ResponseStatusException(HttpStatus.NOT_FOUND, "Competitor $id not found")
		}
}

internal fun Competitor.toDto(): CompetitorDto =
	CompetitorDto(id = id, name = name, domain = domain, isOwn = isOwn)
