package com.charged.player.manager

import com.charged.Charged
import com.charged.database.Database
import com.charged.player.model.PlayerStats
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerManager(
    private val plugin: Charged,
    private val database: Database
) {
    
    private val playerCache = ConcurrentHashMap<UUID, PlayerStats>()
    
    fun loadPlayer(player: Player) {
        val stats = loadStatsFromDatabase(player.uniqueId)
        playerCache[player.uniqueId] = stats
    }
    
    fun savePlayer(player: Player) {
        val stats = playerCache[player.uniqueId] ?: return
        saveStatsToDatabase(stats)
    }
    
    fun saveAll() {
        playerCache.values.forEach { saveStatsToDatabase(it) }
    }
    
    fun getStats(playerName: String): PlayerStats? {
        return playerCache.values.firstOrNull { it.name == playerName }
    }
    
    fun getStats(uuid: UUID): PlayerStats? {
        return playerCache[uuid]
    }
    
    private fun loadStatsFromDatabase(uuid: UUID): PlayerStats {
        database.getConnection().use { conn ->
            conn.prepareStatement(
                "SELECT * FROM charged_players WHERE uuid = ?"
            ).use { stmt ->
                stmt.setString(1, uuid.toString())
                val rs = stmt.executeQuery()
                
                return if (rs.next()) {
                    PlayerStats(
                        uuid = uuid,
                        name = rs.getString("name"),
                        elo = rs.getInt("elo"),
                        wins = rs.getInt("wins"),
                        losses = rs.getInt("losses")
                    )
                } else {
                    // Create new player
                    PlayerStats(
                        uuid = uuid,
                        name = plugin.server.getPlayer(uuid)?.name ?: "Unknown"
                    )
                }
            }
        }
    }
    
    private fun saveStatsToDatabase(stats: PlayerStats) {
        database.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT INTO charged_players (uuid, name, elo, wins, losses)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    elo = VALUES(elo),
                    wins = VALUES(wins),
                    losses = VALUES(losses)
            """).use { stmt ->
                stmt.setString(1, stats.uuid.toString())
                stmt.setString(2, stats.name)
                stmt.setInt(3, stats.elo)
                stmt.setInt(4, stats.wins)
                stmt.setInt(5, stats.losses)
                stmt.executeUpdate()
            }
        }
    }
}