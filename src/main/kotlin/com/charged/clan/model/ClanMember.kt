package com.charged.clan.model

import java.util.UUID

data class ClanMember(
    val uuid: UUID,
    val name: String,
    val role: ClanRole,
    val joinedAt: Long
)

enum class ClanRole {
    OWNER,
    ADMIN,
    MEMBER
}