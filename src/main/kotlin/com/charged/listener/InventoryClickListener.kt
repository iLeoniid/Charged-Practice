package com.charged.listener

import com.charged.util.PluginAccess
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return

        // Verificar si es una GUI del plugin
        if (event.inventory.holder == null) {
            // Intentar manejar a través del GUIManager
            try {
                PluginAccess.plugin().guiManager.handleClick(player, event)
            } catch (e: Exception) {
                // No es una GUI del plugin
            }
        }
    }
}