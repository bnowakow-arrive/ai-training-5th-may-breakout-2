package com.arrive.ai_training_5th_may_breakout_2.api

import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.contracts.KeywordGapRowDto
import com.arrive.ai_training_5th_may_breakout_2.service.BenchmarkService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/competitors")
class KeywordGapController(
	private val benchmarkService: BenchmarkService,
) {

	@GetMapping("/{id}/keyword-gap")
	fun gap(
		@PathVariable id: Long,
		@RequestParam(name = "type", defaultValue = "MISSING") type: GapType,
	): List<KeywordGapRowDto> = benchmarkService.keywordGap(id, type)
}
