package com.arrive.ai_training_5th_may_breakout_2.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "competitor")
class Competitor(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "domain", nullable = false, unique = true)
    var domain: String,

    @Column(name = "is_own", nullable = false)
    var isOwn: Boolean = false,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,
)
