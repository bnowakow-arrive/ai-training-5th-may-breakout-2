package com.arrive.ai_training_5th_may_breakout_2.semrush

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Phase 0 wiring: if no real SemRushClient bean is registered, fall back to FakeSemRushClient.
 *
 * P2: once RealSemRushClient is implemented as @Component (or @Bean), this fake auto-disables
 * via @ConditionalOnMissingBean — no edits needed here.
 */
@Configuration
class SemRushClientConfig {

	@Bean
	@ConditionalOnMissingBean(SemRushClient::class)
	fun fakeSemRushClient(): SemRushClient = FakeSemRushClient()
}
