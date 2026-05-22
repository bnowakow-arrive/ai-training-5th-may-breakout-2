package com.arrive.ai_training_5th_may_breakout_2.repo

import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import com.arrive.ai_training_5th_may_breakout_2.domain.KeywordGapRow
import org.springframework.data.jpa.repository.JpaRepository

interface KeywordGapRepository : JpaRepository<KeywordGapRow, Long> {
    fun findAllByCompetitorIdAndGapType(competitorId: Long, gapType: GapType): List<KeywordGapRow>
    fun deleteAllByCompetitorId(competitorId: Long)
}
