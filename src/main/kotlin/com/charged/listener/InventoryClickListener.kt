package com.charged.listener

import com.charged.Charged
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener(private val plugin: Charged) : Listener {
    
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        
        // Handle GUI clicks
        plugin.plugin.guiManager?.handleClick(player, event)
    }
}