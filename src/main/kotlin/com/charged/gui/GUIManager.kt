package com.charged.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.*

class GUIManager {

    private val openGuis = HashMap<UUID, GUI>()

    fun openMainMenu(player: Player) {
        val gui = MainMenuGUI()
        gui.open(player)
        openGuis[player.uniqueId] = gui
    }

    fun openQueueMenu(player: Player, ranked: Boolean) {
        // CORRECCIÓN: Pasar el parámetro ranked al constructor
        val gui = QueueMenuGUI(ranked)  // <-- Aquí estaba el error
        gui.open(player)
        openGuis[player.uniqueId] = gui
    }

    fun openStatsMenu(player: Player, targetName: String) {
        val gui = StatsGUI(targetName)
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

    fun isPlayerInGUI(player: Player): Boolean {
        return openGuis.containsKey(player.uniqueId)
    }
}