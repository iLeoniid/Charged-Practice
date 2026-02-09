package com.charged.gui

import com.charged.Charged
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class GUIManager(private val plugin: Charged) {
    
    private val openGuis = ConcurrentHashMap<UUID, GUI>()
    
    fun openMainMenu(player: Player) {
        val gui = MainMenuGUI(plugin)
        gui.open(player)
        openGuis[player.uniqueId] = gui
    }
    
    fun openQueueMenu(player: Player, ranked: Boolean) {
        val gui = QueueMenuGUI(plugin, ranked)
        gui.open(player)
        openGuis[player.uniqueId] = gui
    }
    
    fun openStatsMenu(player: Player, targetName: String) {
        val gui = StatsGUI(plugin, targetName)
        gui.open(player)
        openGuis[player.uniqueId] = gui
    }
    
    fun handleClick(player: Player, event: InventoryClickEvent) {
        val gui = openGuis[player.uniqueId] ?: return
        gui.handleClick(event)
    }
    
    fun closeGui(player: Player) {
        openGuis.remove(player.uniqueId)
    }
}