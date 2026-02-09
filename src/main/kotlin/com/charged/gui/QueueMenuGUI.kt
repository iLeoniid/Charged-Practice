package com.charged.gui

import com.charged.Charged
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class QueueMenuGUI(
    private val plugin: Charged,
    private val ranked: Boolean
) : GUI(if (ranked) "§6§lRanked Queue" else "§a§lUnranked Queue", 27) {
    
    override fun setup() {
        // Fill with glass
        for (i in 0 until size) {
            inventory.setItem(i, createItem(Material.STAINED_GLASS_PANE, " ", 7))
        }
        
        val modes = mapOf(
            10 to Triple("nodebuff", Material.POTION, "§d§lNoDebuff"),
            11 to Triple("gapple", Material.GOLDEN_APPLE, "§6§lGapple"),
            12 to Triple("sumo", Material.STICK, "§e§lSumo"),
            13 to Triple("builduhc", Material.WOOD, "§2§lBuildUHC"),
            14 to Triple("combo", Material.BLAZE_ROD, "§c§lCombo"),
            15 to Triple("debuff", Material.SPIDER_EYE, "§5§lDebuff"),
            16 to Triple("soup", Material.MUSHROOM_SOUP, "§a§lSoup")
        )
        
        modes.forEach { (slot, data) ->
            val (mode, material, displayName) = data
            inventory.setItem(slot, createItem(
                material,
                displayName,
                listOf(
                    "§7Mode: §f$mode",
                    if (ranked) "§7Type: §6Ranked" else "§7Type: §aUnranked",
                    "",
                    "§eClick to join queue"
                )
            ))
        }
        
        // Back button
        inventory.setItem(22, createItem(
            Material.ARROW,
            "§7← Back",
            listOf("§7Return to main menu")
        ))
    }
    
    override fun handleClick(event: InventoryClickEvent) {
        cancel(event)
        
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        
        when (event.slot) {
            10 -> joinQueue(player, "nodebuff")
            11 -> joinQueue(player, "gapple")
            12 -> joinQueue(player, "sumo")
            13 -> joinQueue(player, "builduhc")
            14 -> joinQueue(player, "combo")
            15 -> joinQueue(player, "debuff")
            16 -> joinQueue(player, "soup")
            22 -> {
                player.closeInventory()
                plugin.plugin.guiManager?.openMainMenu(player)
            }
        }
    }
    
    private fun joinQueue(player: org.bukkit.entity.Player, mode: String) {
        player.closeInventory()
        
        if (plugin.plugin.queueManager.isInQueue(player.uniqueId)) {
            player.sendMessage("§cYa estás en cola.")
            return
        }
        
        if (plugin.plugin.matchManager.isInMatch(player.uniqueId)) {
            player.sendMessage("§cYa estás en un duelo.")
            return
        }
        
        plugin.plugin.queueManager.joinQueue(player.uniqueId, mode)
        player.sendMessage("§a✓ Te uniste a la cola de §f$mode")
    }
    
    private fun createItem(material: Material, name: String, lore: List<String> = emptyList(), data: Short = 0): ItemStack {
        val item = ItemStack(material, 1, data)
        val meta = item.itemMeta ?: return item
        meta.displayName = name
        if (lore.isNotEmpty()) meta.lore = lore
        item.itemMeta = meta
        return item
    }
}