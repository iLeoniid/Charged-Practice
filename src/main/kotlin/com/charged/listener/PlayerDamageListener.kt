package com.charged.listener

import com.charged.Charged
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class PlayerDamageListener(private val plugin: Charged) : Listener {
    
    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity
        val damager = event.damager
        
        if (victim !is Player || damager !is Player) return
        
        // Check if both in same match
        val victimMatch = plugin.plugin.matchManager.getPlayerMatch(victim.uniqueId)
        val damagerMatch = plugin.plugin.matchManager.getPlayerMatch(damager.uniqueId)
        
        // Cancel if not in same match
        if (victimMatch == null || damagerMatch == null || victimMatch.id != damagerMatch.id) {
            event.isCancelled = true
            return
        }
        
        // Cancel if match not active
        if (victimMatch.state != com.charged.match.model.MatchState.ACTIVE) {
            event.isCancelled = true
            return
        }
        
        // Track combo
        plugin.plugin.matchManager.trackCombo(damager.uniqueId, victim.uniqueId, victimMatch)
    }
}