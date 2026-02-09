package com.charged.listener

import com.charged.Charged
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action

class PlayerInteractListener(private val plugin: Charged) : Listener {
    
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }
        
        val player = event.player
        val item = event.item ?: return
        
        // Check if in match - don't handle hotbar items in match
        if (plugin.plugin.matchManager.isInMatch(player.uniqueId)) {
            return
        }
        
        when (item.type) {
            Material.IRON_SWORD -> {
                // Unranked menu
                if (item.hasItemMeta() && item.itemMeta?.displayName?.contains("Unranked") == true) {
                    plugin.plugin.guiManager.openQueueMenu(player, false)
                    event.isCancelled = true
                }
            }
            Material.DIAMOND_SWORD -> {
                // Ranked menu
                if (item.hasItemMeta() && item.itemMeta?.displayName?.contains("Ranked") == true) {
                    plugin.plugin.guiManager.openQueueMenu(player, true)
                    event.isCancelled = true
                }
            }
            Material.PAINTING -> {
                // Replays (placeholder)
                player.sendMessage("§cReplays system coming soon!")
                event.isCancelled = true
            }
            Material.PAPER -> {
                // Stats
                plugin.plugin.guiManager.openStatsMenu(player, player.name)
                event.isCancelled = true
            }
            else -> {}
        }
    }
}
