package com.charged.clan.model

import java.util.UUID

data class Clan(
    val id: Int,
    val tag: String,
    val name: String,
    val ownerUuid: UUID,
    val createdAt: Long,
    var level: Int = 1,
    var experience: Int = 0,
    var wins: Int = 0,
    var losses: Int = 0,
    val members: MutableList<ClanMember> = mutableListOf()
) {
    val wlr: Double
        get() = if (losses == 0) wins.toDouble() else wins.toDouble() / losses
}