package com.charged.hologram

import com.charged.Charged
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class HologramManager(private val plugin: Charged) {
    
    private val holograms = ConcurrentHashMap<String, Hologram>()
    
    fun createLeaderboard(location: Location, mode: String) {
        val hologram = Hologram(location, mode)
        hologram.spawn()
        holograms["leaderboard_$mode"] = hologram
        
        // Update every 60 seconds
        plugin.server.scheduler.runTaskRepeatAsynchronously(plugin, Runnable {
            updateLeaderboard(mode)
        }, 0L, 1200L)
    }
    
    private fun updateLeaderboard(mode: String) {
        val hologram = holograms["leaderboard_$mode"] ?: return
        
        val lines = mutableListOf<String>()
        lines.add("§6§l━━━━━━━━━━━━━━━━━━━━━━")
        lines.add("§e§lTOP 10 ${mode.uppercase()}")
        lines.add("§6§l━━━━━━━━━━━━━━━━━━━━━━")
        lines.add("")
        
        // Get top 10 players from database
        val topPlayers = getTopPlayers(mode, 10)
        
        topPlayers.forEachIndexed { index, player ->
            val rank = index + 1
            val medal = when (rank) {
                1 -> "§6§l#1 §6"
                2 -> "§7§l#2 §7"
                3 -> "§c§l#3 §c"
                else -> "§f#$rank §f"
            }
            
            lines.add("$medal${player.name} §8- §e${player.elo}")
        }
        
        lines.add("")
        lines.add("§7Updated every minute")
        
        hologram.updateLines(lines)
    }
    
    private fun getTopPlayers(mode: String, limit: Int): List<TopPlayer> {
        // TODO: Query database
        return listOf(
            TopPlayer("Player1", 2500),
            TopPlayer("Player2", 2400),
            TopPlayer("Player3", 2300),
            TopPlayer("Player4", 2200),
            TopPlayer("Player5", 2100),
            TopPlayer("Player6", 2000),
            TopPlayer("Player7", 1900),
            TopPlayer("Player8", 1800),
            TopPlayer("Player9", 1700),
            TopPlayer("Player10", 1600)
        )
    }
    
    fun removeHologram(id: String) {
        holograms[id]?.remove()
        holograms.remove(id)
    }
    
    fun removeAll() {
        holograms.values.forEach { it.remove() }
        holograms.clear()
    }
    
    inner class Hologram(private val location: Location, private val id: String) {
        private val armorStands = mutableListOf<ArmorStand>()
        
        fun spawn() {
            // Spawn initial lines
            updateLines(listOf("§7Loading..."))
        }
        
        fun updateLines(lines: List<String>) {
            // Remove old armor stands
            armorStands.forEach { it.remove() }
            armorStands.clear()
            
            // Spawn new ones
            var yOffset = 0.0
            lines.forEach { line ->
                val stand = location.world?.spawnEntity(
                    location.clone().add(0.0, yOffset, 0.0),
                    EntityType.ARMOR_STAND
                ) as ArmorStand
                
                stand.isVisible = false
                stand.setGravity(false)
                stand.isCustomNameVisible = true
                stand.customName = line
                stand.isMarker = true
                
                armorStands.add(stand)
                yOffset -= 0.25
            }
        }
        
        fun remove() {
            armorStands.forEach { it.remove() }
            armorStands.clear()
        }
    }
    
    data class TopPlayer(val name: String, val elo: Int)
}