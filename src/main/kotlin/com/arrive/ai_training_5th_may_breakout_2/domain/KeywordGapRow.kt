package com.arrive.ai_training_5th_may_breakout_2.domain

import com.arrive.ai_training_5th_may_breakout_2.contracts.GapType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "keyword_gap_row")
class KeywordGapRow(
    @Column(name = "competitor_id", nullable = false)
    var competitorId: Long,

    @Column(name = "keyword", nullable = false, length = 512)
    var keyword: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "gap_type", nullable = false, length = 16)
    var gapType: GapType,

    @Column(name = "volume", nullable = false)
    var volume: Long,

    @Column(name = "kd")
    var kd: Int? = null,

    @Column(name = "position_base")
    var positionBase: Int? = null,

    @Column(name = "position_competitor")
    var positionCompetitor: Int? = null,

    @Column(name = "cpc")
    var cpc: BigDecimal? = null,

    @Column(name = "fetched_at", nullable = false)
    var fetchedAt: Instant,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,
)
