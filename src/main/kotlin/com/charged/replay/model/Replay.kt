package com.charged.replay.model

import java.util.UUID

data class Replay(
    val id: String,
    val player1: UUID,
    val player2: UUID,
    val player1Name: String,
    val player2Name: String,
    val mode: String,
    val arena: String,
    val startedAt: Long,
    val endedAt: Long,
    val winnerUuid: UUID?,
    val frames: List<ReplayFrame> = emptyList()
) {
    val durationSeconds: Int
        get() = ((endedAt - startedAt) / 1000).toInt()
}