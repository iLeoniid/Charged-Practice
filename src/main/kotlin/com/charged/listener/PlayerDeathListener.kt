package com.charged.listener

import com.charged.Charged
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener(private val plugin: Charged) : Listener {
    
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity
        val killer = victim.killer
        
        // Check if in match
        val match = plugin.plugin.matchManager.getPlayerMatch(victim.uniqueId) ?: return
        
        // Custom death message
        event.deathMessage = null
        
        // Handle match death
        plugin.plugin.matchManager.handlePlayerDeath(victim, killer, match)
        
        // Keep inventory in match
        event.keepInventory = true
        event.drops.clear()
        
        // Respawn player
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            victim.spigot().respawn()
        }, 1L)
    }
}