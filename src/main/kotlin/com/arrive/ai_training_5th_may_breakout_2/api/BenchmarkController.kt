package com.arrive.ai_training_5th_may_breakout_2.api

import com.arrive.ai_training_5th_may_breakout_2.contracts.BenchmarkResponse
import com.arrive.ai_training_5th_may_breakout_2.contracts.TrafficHistorySeries
import com.arrive.ai_training_5th_may_breakout_2.service.BenchmarkService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class BenchmarkController(
	private val benchmarkService: BenchmarkService,
) {

	@GetMapping("/benchmark")
	fun benchmark(): BenchmarkResponse = benchmarkService.benchmark()

	@GetMapping("/benchmark/traffic-history")
	fun trafficHistory(): List<TrafficHistorySeries> = benchmarkService.trafficHistory()
}
