package com.arrive.ai_training_5th_may_breakout_2.domain

import jakarta.persistence.*

@Entity
@Table(name = "competitors")
class Competitor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var name: String,
    var domain: String,
    var isOwn: Boolean = false,
)
