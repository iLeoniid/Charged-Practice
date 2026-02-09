package com.charged.menu

import com.charged.Charged
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class Menu(
    val id: String,
    val title: String,
    val size: Int,
    val updateInterval: Int,
    val background: Background?,
    val items: Map<Int, MenuItem>,
    val autoPopulate: AutoPopulate?,
    val globalSettings: GlobalSettings
) {
    
    /**
     * Create inventory for player
     */
    fun createInventory(player: Player, plugin: Charged): Inventory {
        val inventory = Bukkit.createInventory(null, size, replacePlaceholders(title, player, plugin))
        
        // Apply background
        background?.let { applyBackground(inventory, it) }
        
        // Add items
        items.forEach { (slot, menuItem) ->
            if (slot < size) {
                inventory.setItem(slot, createItemStack(menuItem, player, plugin))
            }
        }
        
        // Handle auto-populate
        autoPopulate?.let { populateItems(inventory, it, player, plugin) }
        
        return inventory
    }
    
    private fun applyBackground(inventory: Inventory, bg: Background) {
        val fillItem = createMaterialItem(bg.material, " ")
        val borderItem = bg.borderMaterial?.let { createMaterialItem(it, "§8▬▬▬") } ?: fillItem
        
        when (bg.pattern) {
            BackgroundPattern.BORDER -> {
                // Fill borders only
                for (i in 0 until size) {
                    val row = i / 9
                    val col = i % 9
                    
                    if (row == 0 || row == (size / 9) - 1 || col == 0 || col == 8) {
                        if (inventory.getItem(i) == null) {
                            inventory.setItem(i, borderItem)
                        }
                    }
                }
            }
            BackgroundPattern.CHECKER -> {
                // Checkered pattern
                for (i in 0 until size) {
                    if (inventory.getItem(i) == null) {
                        val row = i / 9
                        val col = i % 9
                        val item = if ((row + col) % 2 == 0) fillItem else borderItem
                        inventory.setItem(i, item)
                    }
                }
            }
            BackgroundPattern.FULL -> {
                // Fill all empty slots
                for (i in 0 until size) {
                    if (inventory.getItem(i) == null) {
                        inventory.setItem(i, fillItem)
                    }
                }
            }
            BackgroundPattern.CUSTOM -> {
                // Custom pattern - implement as needed
            }
        }
    }
    
    private fun createMaterialItem(material: String, name: String): ItemStack {
        val parts = material.split(":")
        val mat = Material.valueOf(parts[0])
        val data = parts.getOrNull(1)?.toByteOrNull() ?: 0
        
        val item = ItemStack(mat, 1, data.toShort())
        val meta = item.itemMeta
        meta.displayName = name
        item.itemMeta = meta
        return item
    }
    
    private fun createItemStack(menuItem: MenuItem, player: Player, plugin: Charged): ItemStack {
        val parts = menuItem.material.split(":")
        val material = Material.valueOf(parts[0])
        val data = parts.getOrNull(1)?.toByteOrNull() ?: 0
        
        val item = ItemStack(material, menuItem.amount, data.toShort())
        val meta = item.itemMeta
        
        // Set display name
        meta.displayName = replacePlaceholders(menuItem.display.name, player, plugin)
        
        // Set lore
        val lore = menuItem.display.lore.map { replacePlaceholders(it, player, plugin) }
        meta.lore = lore
        
        // Add glow effect
        if (menuItem.glow) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true)
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)
        }
        
        item.itemMeta = meta
        return item
    }
    
    private fun populateItems(inventory: Inventory, autoPopulate: AutoPopulate, player: Player, plugin: Charged) {
        // TODO: Implement auto-populate from kits folder
        // This would load all kits and create items dynamically
    }
    
    private fun replacePlaceholders(text: String, player: Player, plugin: Charged): String {
        var result = text
        
        // Get player stats
        val stats = plugin.plugin.playerManager.getStats(player.uniqueId)
        
        // Player placeholders
        result = result.replace("{player_name}", player.name)
        result = result.replace("{player_level}", (stats?.level ?: 1).toString())
        result = result.replace("{player_elo}", (stats?.elo ?: 1000).toString())
        result = result.replace("{player_wins}", (stats?.wins ?: 0).toString())
        result = result.replace("{player_losses}", (stats?.losses ?: 0).toString())
        
        // Calculate win rate
        val totalMatches = (stats?.wins ?: 0) + (stats?.losses ?: 0)
        val winrate = if (totalMatches > 0) {
            ((stats?.wins ?: 0).toDouble() / totalMatches * 100).toInt()
        } else 0
        result = result.replace("{player_winrate}", winrate.toString())
        
        // Server placeholders
        result = result.replace("{online_players}", Bukkit.getOnlinePlayers().size.toString())
        result = result.replace("{max_players}", Bukkit.getMaxPlayers().toString())
        
        // Color codes
        result = result.replace("&", "§")
        
        return result
    }
}