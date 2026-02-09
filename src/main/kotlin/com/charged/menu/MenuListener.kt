package com.charged.menu

import com.charged.Charged
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerQuitEvent

class MenuListener(private val plugin: Charged) : Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        
        // Check if player has menu open
        if (!plugin.plugin.menuManager.hasMenuOpen(player)) return
        
        event.isCancelled = true
        
        val slot = event.rawSlot
        
        // Determine click type
        val clickType = when {
            event.isShiftClick && event.isRightClick -> ClickType.SHIFT_RIGHT
            event.isShiftClick -> ClickType.SHIFT_LEFT
            event.isRightClick -> ClickType.RIGHT
            else -> ClickType.LEFT
        }
        
        // Handle click
        plugin.plugin.menuManager.handleClick(player, slot, clickType)
    }
    
    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        
        if (plugin.plugin.menuManager.hasMenuOpen(player)) {
            event.isCancelled = true
        }
    }
    
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        plugin.plugin.menuManager.onMenuClose(player)
    }
    
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.plugin.menuManager.onMenuClose(event.player)
    }
}