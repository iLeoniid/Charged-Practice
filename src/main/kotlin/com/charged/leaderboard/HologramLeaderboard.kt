package com.charged.leaderboard

import com.charged.Charged
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class HologramLeaderboard(private val plugin: Charged) {
    
    private val holograms = ConcurrentHashMap<String, MutableList<ArmorStand>>()
    
    fun createLeaderboard(location: Location, type: LeaderboardType, mode: String? = null) {
        val id = "${type.name}_${mode ?: "global"}"
        
        // Remove existing
        removeLeaderboard(id)
        
        val lines = mutableListOf<ArmorStand>()
        var currentLoc = location.clone().add(0.0, 2.0, 0.0)
        
        // Title
        lines.add(createHologramLine(currentLoc, "§6§l━━━━━━━━━━━━━━━━━━━━"))
        currentLoc.subtract(0.0, 0.3, 0.0)
        
        val title = when (type) {
            LeaderboardType.ELO -> "§e§lTOP ELO${if (mode != null) " - ${mode.uppercase()}" else ""}"
            LeaderboardType.WINS -> "§a§lTOP WINS${if (mode != null) " - ${mode.uppercase()}" else ""}"
            LeaderboardType.WINSTREAK -> "§c§lTOP WINSTREAK${if (mode != null) " - ${mode.uppercase()}" else ""}"
            LeaderboardType.KDR -> "§b§lTOP K/D RATIO${if (mode != null) " - ${mode.uppercase()}" else ""}"
        }
        
        lines.add(createHologramLine(currentLoc, title))
        currentLoc.subtract(0.0, 0.3, 0.0)
        lines.add(createHologramLine(currentLoc, "§6§l━━━━━━━━━━━━━━━━━━━━"))
        currentLoc.subtract(0.0, 0.3, 0.0)
        
        // Get top players
        val topPlayers = getTopPlayers(type, mode, 10)
        
        topPlayers.forEachIndexed { index, entry ->
            val rank = index + 1
            val (name, value) = entry
            
            val rankColor = when (rank) {
                1 -> "§6"
                2 -> "§7"
                3 -> "§c"
                else -> "§f"
            }
            
            val line = "$rankColor#$rank §7- §f$name §8- $rankColor$value"
            lines.add(createHologramLine(currentLoc, line))
            currentLoc.subtract(0.0, 0.25, 0.0)
        }
        
        currentLoc.subtract(0.0, 0.05, 0.0)
        lines.add(createHologramLine(currentLoc, "§6§l━━━━━━━━━━━━━━━━━━━━"))
        currentLoc.subtract(0.0, 0.25, 0.0)
        lines.add(createHologramLine(currentLoc, "§7Updates every §e1 minute"))
        
        holograms[id] = lines
    }
    
    private fun createHologramLine(location: Location, text: String): ArmorStand {
        val stand = location.world?.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
        stand.isVisible = false
        stand.isSmall = true
        stand.setGravity(false)
        stand.isCustomNameVisible = true
        stand.customName = text
        stand.isMarker = true
        return stand
    }
    
    private fun getTopPlayers(type: LeaderboardType, mode: String?, limit: Int): List<Pair<String, Int>> {
        // TODO: Query database for top players
        // This is a placeholder implementation
        
        return listOf(
            "Player1" to 2500,
            "Player2" to 2400,
            "Player3" to 2300,
            "Player4" to 2200,
            "Player5" to 2100,
            "Player6" to 2000,
            "Player7" to 1900,
            "Player8" to 1800,
            "Player9" to 1700,
            "Player10" to 1600
        )
    }
    
    fun removeLeaderboard(id: String) {
        holograms[id]?.forEach { it.remove() }
        holograms.remove(id)
    }
    
    fun updateAll() {
        // Recreate all holograms with fresh data
        val locations = holograms.map { it.key to it.value.first().location }
        
        locations.forEach { (id, loc) ->
            val parts = id.split("_")
            val type = LeaderboardType.valueOf(parts[0])
            val mode = if (parts.size > 1 && parts[1] != "global") parts[1] else null
            
            createLeaderboard(loc, type, mode)
        }
    }
    
    fun startAutoUpdate() {
        plugin.server.scheduler.runTaskRepeatAsynchronously(plugin, Runnable {
            plugin.server.scheduler.runTask(plugin, Runnable {
                updateAll()
            })
        }, 1200L, 1200L) // Every 60 seconds
    }
}

enum class LeaderboardType {
    ELO,
    WINS,
    WINSTREAK,
    KDR
}