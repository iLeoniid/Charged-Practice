package com.charged.gui

import com.charged.Charged
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class StatsGUI(
    private val plugin: Charged,
    private val targetName: String
) : GUI("§6§lStats §8- §f$targetName", 27) {
    
    override fun setup() {
        val stats = plugin.plugin.playerManager.getStats(targetName)
        
        if (stats == null) {
            inventory.setItem(13, createItem(
                Material.BARRIER,
                "§cJugador no encontrado"
            ))
            return
        }
        
        // Overall stats
        inventory.setItem(10, createItem(
            Material.DIAMOND_SWORD,
            "§6§lOverall Statistics",
            listOf(
                "§7ELO: §f${stats.elo}",
                "§7Wins: §a${stats.wins}",
                "§7Losses: §c${stats.losses}",
                "§7W/L Ratio: §f${String.format("%.2f", stats.wlr)}",
                "§7Win Streak: §f${stats.winstreak}"
            )
        ))
        
        // Division
        inventory.setItem(13, createItem(
            Material.GOLD_BLOCK,
            "§6§lCurrent Division",
            listOf(
                "§7${getDivisionName(stats.elo)}",
                "§7${stats.elo} ELO"
            )
        ))
        
        // Close button
        inventory.setItem(22, createItem(
            Material.BARRIER,
            "§cClose"
        ))
    }
    
    override fun handleClick(event: InventoryClickEvent) {
        cancel(event)
        
        if (event.slot == 22) {
            event.whoClicked.closeInventory()
        }
    }
    
    private fun getDivisionName(elo: Int): String {
        return when {
            elo < 1000 -> "§7Iron"
            elo < 1500 -> "§6Gold"
            elo < 2000 -> "§bDiamond"
            elo < 2400 -> "§5Master"
            elo < 2800 -> "§4Grand Master"
            else -> "§eChampion"
        }
    }
    
    private fun createItem(material: Material, name: String, lore: List<String> = emptyList()): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item
        meta.displayName = name
        if (lore.isNotEmpty()) meta.lore = lore
        item.itemMeta = meta
        return item
    }
}