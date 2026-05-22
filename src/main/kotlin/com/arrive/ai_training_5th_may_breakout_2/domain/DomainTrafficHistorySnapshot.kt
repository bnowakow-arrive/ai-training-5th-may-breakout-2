package com.arrive.ai_training_5th_may_breakout_2.domain

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "domain_traffic_history")
class DomainTrafficHistorySnapshot(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competitor_id", nullable = false)
    val competitor: Competitor,
    val month: LocalDate,
    var organicTraffic: Long,
    var organicKeywords: Long,
    var fetchedAt: Instant,
)
