package com.charged.listener

import com.charged.Charged
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val plugin: Charged) : Listener {
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        
        // Save player data
        plugin.plugin.playerManager.savePlayer(player)
        
        // Remove from queue if in queue
        if (plugin.plugin.queueManager.isInQueue(player.uniqueId)) {
            plugin.plugin.queueManager.leaveQueue(player.uniqueId)
        }
        
        // Handle if in match
        val match = plugin.plugin.matchManager.getPlayerMatch(player.uniqueId)
        if (match != null) {
            plugin.plugin.matchManager.handlePlayerQuit(player.uniqueId, match)
        }
        
        // Custom quit message
        event.quitMessage = null
    }
}