package com.charged.player.model

import java.util.UUID

data class PlayerStats(
    val uuid: UUID,
    val name: String,
    var elo: Int = 1000,
    var wins: Int = 0,
    var losses: Int = 0,
    var winstreak: Int = 0
) {
    val wlr: Double
        get() = if (losses == 0) wins.toDouble() else wins.toDouble() / losses
}