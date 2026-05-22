package com.arrive.ai_training_5th_may_breakout_2.domain

import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "keyword_gap_row")
class KeywordGapRow(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competitor_id", nullable = false)
    val competitor: Competitor,
    val keyword: String,
    @Enumerated(EnumType.STRING)
    val gapType: GapType,
    val volume: Long,
    val kd: Int?,
    val positionBase: Int?,
    val positionCompetitor: Int?,
    val cpc: BigDecimal?,
)
