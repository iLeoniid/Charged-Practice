package com.charged.arena.model

import org.bukkit.Location

data class Arena(
    val name: String,
    val spawn1: Location,
    val spawn2: Location,
    val mode: String,
    var state: ArenaState = ArenaState.AVAILABLE
)