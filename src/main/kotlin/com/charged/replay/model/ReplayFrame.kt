package com.charged.replay.model

import org.bukkit.Location

data class ReplayFrame(
    val tick: Long,
    val player1Location: Location,
    val player2Location: Location,
    val player1Health: Double,
    val player2Health: Double,
    val events: List<ReplayEvent> = emptyList()
)

sealed class ReplayEvent {
    data class HitEvent(val attacker: String, val victim: String) : ReplayEvent()
    data class DeathEvent(val player: String) : ReplayEvent()
}