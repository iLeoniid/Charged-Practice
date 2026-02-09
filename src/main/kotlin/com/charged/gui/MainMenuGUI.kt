package com.charged.gui

import com.charged.Charged
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class MainMenuGUI(private val plugin: Charged) : GUI("§6§lCharged §8- §7Main Menu", 27) {
    
    override fun setup() {
        // Fill with glass
        for (i in 0 until size) {
            inventory.setItem(i, createItem(Material.STAINED_GLASS_PANE, " ", 7))
        }
        
        // Unranked (slot 11)
        inventory.setItem(11, createItem(
            Material.IRON_SWORD,
            "§a§lUnranked",
            listOf(
                "§7Practice without affecting ELO",
                "",
                "§eClick to select mode"
            )
        ))
        
        // Ranked (slot 13)
        inventory.setItem(13, createItem(
            Material.DIAMOND_SWORD,
            "§6§lRanked",
            listOf(
                "§7Competitive matches",
                "§7Affects your ELO and division",
                "",
                "§eClick to select mode"
            )
        ))
        
        // Stats (slot 15)
        inventory.setItem(15, createItem(
            Material.PAPER,
            "§b§lYour Statistics",
            listOf(
                "§7View your performance",
                "",
                "§eClick to view"
            )
        ))
    }
    
    override fun handleClick(event: InventoryClickEvent) {
        cancel(event)
        
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        
        when (event.slot) {
            11 -> {
                player.closeInventory()
                plugin.plugin.guiManager?.openQueueMenu(player, false)
            }
            13 -> {
                player.closeInventory()
                plugin.plugin.guiManager?.openQueueMenu(player, true)
            }
            15 -> {
                player.closeInventory()
                plugin.plugin.guiManager?.openStatsMenu(player, player.name)
            }
        }
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