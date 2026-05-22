package com.arrive.ai_training_5th_may_breakout_2.repo

import com.arrive.ai_training_5th_may_breakout_2.domain.DomainMetricsSnapshot
import org.springframework.data.jpa.repository.JpaRepository

interface DomainMetricsRepository : JpaRepository<DomainMetricsSnapshot, Long> {
    fun findTopByCompetitorIdOrderByFetchedAtDesc(competitorId: Long): DomainMetricsSnapshot?
}
