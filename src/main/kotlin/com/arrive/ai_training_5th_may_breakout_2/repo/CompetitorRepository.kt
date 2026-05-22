package com.arrive.ai_training_5th_may_breakout_2.repo

import com.arrive.ai_training_5th_may_breakout_2.domain.Competitor
import org.springframework.data.jpa.repository.JpaRepository

interface CompetitorRepository : JpaRepository<Competitor, Long>
