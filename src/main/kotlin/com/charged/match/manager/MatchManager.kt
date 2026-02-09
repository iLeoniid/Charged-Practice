package com.charged.match.manager

import com.charged.Charged
import com.charged.match.model.*
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MatchManager(private val plugin: Charged) {
    
    private val activeMatches = ConcurrentHashMap<String, Match>()
    private val playerToMatch = ConcurrentHashMap<UUID, String>()
    
    fun createMatch(mode: String, type: MatchType, players: List<UUID>): Match? {
        val arena = plugin.plugin.arenaManager.getAvailableArena(mode)
        if (arena == null) {
            plugin.logger.warning("No available arena for mode: $mode")
            return null
        }
        
        val match = Match(
            mode = mode,
            type = type,
            participants = players
        )
        
        activeMatches[match.id] = match
        players.forEach { playerToMatch[it] = match.id }
        
        plugin.plugin.arenaManager.markInUse(arena)
        
        startMatch(match, arena)
        
        return match
    }
    
    private fun startMatch(match: Match, arena: com.charged.arena.model.Arena) {
        match.state = MatchState.STARTING
        
        // Teleport players
        val players = match.participants.mapNotNull { plugin.server.getPlayer(it) }
        if (players.size < 2) {
            endMatch(match.id)
            return
        }
        
        players[0].teleport(arena.spawn1)
        players[1].teleport(arena.spawn2)
        
        // Give kits
        players.forEach { plugin.plugin.kitManager.applyKit(it, match.mode) }
        
        // Start countdown
        var countdown = 5
        val task = plugin.server.scheduler.runTaskRepeatAsynchronously(plugin, Runnable {
            if (countdown <= 0) {
                match.state = MatchState.ACTIVE
                players.forEach { 
                    it.sendTitle("§a§lFIGHT!", "")
                    it.playSound(it.location, org.bukkit.Sound.LEVEL_UP, 1f, 1f)
                }
                return@Runnable
            }
            
            players.forEach {
                it.sendTitle("§e$countdown", "§7Get ready...")
                it.playSound(it.location, org.bukkit.Sound.CLICK, 1f, 1f)
            }
            countdown--
        }, 0L, 20L)
    }
    
    fun endMatch(matchId: String) {
        val match = activeMatches.remove(matchId) ?: return
        match.state = MatchState.FINISHED
        match.endedAt = System.currentTimeMillis()
        
        match.participants.forEach { playerToMatch.remove(it) }
        
        // Find arena and mark available
        val arena = plugin.plugin.arenaManager.getAvailableArena(match.mode)
        arena?.let { plugin.plugin.arenaManager.markAvailable(it) }
    }
    
    fun getPlayerMatch(uuid: UUID): Match? {
        val matchId = playerToMatch[uuid] ?: return null
        return activeMatches[matchId]
    }
    
    fun isInMatch(uuid: UUID): Boolean = playerToMatch.containsKey(uuid)
    
    fun handlePlayerDeath(victim: Player, killer: Player?, match: Match) {
        // Handle death in match
        if (killer != null) {
            victim.sendMessage("§cFuiste eliminado por §f${killer.name}")
            killer.sendMessage("§aEliminaste a §f${victim.name}")
        }
        
        // End match
        endMatch(match.id)
    }
    
    fun handlePlayerQuit(uuid: UUID, match: Match) {
        endMatch(match.id)
    }
    
    fun trackCombo(damagerUuid: UUID, victimUuid: UUID, match: Match) {
        // Track combo hits
    }
    
    fun stopAll() {
        activeMatches.keys.toList().forEach { endMatch(it) }
    }
}