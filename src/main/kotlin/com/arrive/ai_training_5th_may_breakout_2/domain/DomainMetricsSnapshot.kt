package com.arrive.ai_training_5th_may_breakout_2.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "domain_metrics_snapshot")
class DomainMetricsSnapshot(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competitor_id", nullable = false)
    val competitor: Competitor,
    val domain: String,
    val organicKeywords: Long,
    val organicTraffic: Long,
    val organicCost: BigDecimal,
    val top10Keywords: Long,
    val fetchedAt: Instant,
)
