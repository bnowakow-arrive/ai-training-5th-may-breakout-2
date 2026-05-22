package com.arrive.ai_training_5th_may_breakout_2.semrush

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Default to the fake client. P2: register `RealSemRushClient` as @Component with
 *   @ConditionalOnProperty(name = "semrush.live", havingValue = "true")
 * so the real one only kicks in when explicitly flipped on. The 50k/month credit budget
 * means we keep the fake as the default everywhere — local dev, CI, and the bulk of demo prep.
 */
@Configuration
class SemRushClientConfig {

	@Bean
	@ConditionalOnMissingBean(SemRushClient::class)
	fun fakeSemRushClient(): SemRushClient = FakeSemRushClient()
}
