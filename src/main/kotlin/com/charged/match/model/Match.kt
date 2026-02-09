package com.charged.match.model

import java.util.UUID

data class Match(
    val id: String = UUID.randomUUID().toString(),
    val mode: String,
    val type: MatchType,
    val participants: List<UUID>,
    var state: MatchState = MatchState.WAITING,
    val startedAt: Long = System.currentTimeMillis(),
    var endedAt: Long = 0
)