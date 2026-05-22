package com.arrive.ai_training_5th_may_breakout_2.api

import com.arrive.ai_training_5th_may_breakout_2.service.RefreshService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class RefreshController(
	private val refreshService: RefreshService,
) {

	@PostMapping("/refresh-all")
	fun refreshAll(): ResponseEntity<Void> {
		refreshService.refreshAll()
		return ResponseEntity.accepted().build()
	}
}
