package com.arrive.ai_training_5th_may_breakout_2.api

import com.arrive.ai_training_5th_may_breakout_2.contracts.CompetitorDto
import com.arrive.ai_training_5th_may_breakout_2.contracts.CreateCompetitorRequest
import com.arrive.ai_training_5th_may_breakout_2.service.CompetitorService
import com.arrive.ai_training_5th_may_breakout_2.service.RefreshService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/competitors")
class CompetitorController(
	private val competitorService: CompetitorService,
	private val refreshService: RefreshService,
) {

	@GetMapping
	fun list(): List<CompetitorDto> = competitorService.list()

	@PostMapping
	fun create(@RequestBody request: CreateCompetitorRequest): ResponseEntity<CompetitorDto> =
		ResponseEntity.status(HttpStatus.CREATED).body(competitorService.create(request))

	@DeleteMapping("/{id}")
	fun delete(@PathVariable id: Long): ResponseEntity<Void> {
		competitorService.delete(id)
		return ResponseEntity.noContent().build()
	}

	@PostMapping("/{id}/refresh")
	fun refresh(@PathVariable id: Long): ResponseEntity<Void> {
		refreshService.refreshCompetitor(id)
		return ResponseEntity.accepted().build()
	}
}
