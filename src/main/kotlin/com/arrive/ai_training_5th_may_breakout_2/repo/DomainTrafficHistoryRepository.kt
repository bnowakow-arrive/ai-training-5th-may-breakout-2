package com.arrive.ai_training_5th_may_breakout_2.repo

import com.arrive.ai_training_5th_may_breakout_2.domain.DomainTrafficHistorySnapshot
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface DomainTrafficHistoryRepository : JpaRepository<DomainTrafficHistorySnapshot, Long> {
    fun findAllByCompetitorIdOrderByMonthAsc(competitorId: Long): List<DomainTrafficHistorySnapshot>
    fun findByCompetitorIdAndMonth(competitorId: Long, month: LocalDate): DomainTrafficHistorySnapshot?
    fun findTopByCompetitorIdOrderByFetchedAtDesc(competitorId: Long): DomainTrafficHistorySnapshot?
}
