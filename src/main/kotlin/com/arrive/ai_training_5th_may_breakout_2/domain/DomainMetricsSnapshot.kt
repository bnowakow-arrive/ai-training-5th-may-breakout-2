package com.arrive.ai_training_5th_may_breakout_2.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "domain_metrics_snapshot")
class DomainMetricsSnapshot(
    @Column(name = "competitor_id", nullable = false)
    var competitorId: Long,

    @Column(name = "domain", nullable = false)
    var domain: String,

    @Column(name = "organic_keywords", nullable = false)
    var organicKeywords: Long,

    @Column(name = "organic_traffic", nullable = false)
    var organicTraffic: Long,

    @Column(name = "organic_cost", nullable = false)
    var organicCost: BigDecimal,

    @Column(name = "top10_keywords", nullable = false)
    var top10Keywords: Long,

    @Column(name = "fetched_at", nullable = false)
    var fetchedAt: Instant,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,
)
