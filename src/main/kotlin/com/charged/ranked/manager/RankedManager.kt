package com.charged.ranked.manager

import com.charged.Charged
import com.charged.database.Database
import java.util.UUID

class RankedManager(
    private val plugin: Charged,
    private val database: Database
) {
    
    fun getElo(uuid: UUID, mode: String): Int = 1000
}