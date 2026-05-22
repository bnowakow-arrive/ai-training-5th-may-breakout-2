package com.arrive.ai_training_5th_may_breakout_2.api

import com.arrive.ai_training_5th_may_breakout_2.contracts.OpportunityDto
import com.arrive.ai_training_5th_may_breakout_2.service.OpportunityService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class OpportunityController(
	private val opportunityService: OpportunityService,
) {

	@GetMapping("/opportunities")
	fun opportunities(
		@RequestParam(defaultValue = "10") limit: Int,
	): List<OpportunityDto> = opportunityService.topOpportunities(limit.coerceIn(1, 100))
}
