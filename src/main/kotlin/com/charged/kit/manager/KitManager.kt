package com.charged.kit.manager

import com.charged.Charged
import com.charged.config.ConfigManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class KitManager(
    private val plugin: Charged,
    private val config: ConfigManager
) {
    
    fun applyKit(player: Player, mode: String) {
        player.inventory.clear()
        player.inventory.armorContents = arrayOf()
        
        when (mode.lowercase()) {
            "nodebuff" -> applyNoDebuffKit(player)
            "gapple" -> applyGappleKit(player)
            "sumo" -> applySumoKit(player)
            "builduhc" -> applyBuildUHCKit(player)
            else -> applyNoDebuffKit(player)
        }
        
        player.updateInventory()
    }
    
    fun giveHotbar(player: Player) {
        player.inventory.clear()
        
        // Slot 1: Unranked menu
        player.inventory.setItem(0, createItem(Material.IRON_SWORD, "§a§lUnranked"))
        
        // Slot 2: Ranked menu
        player.inventory.setItem(1, createItem(Material.DIAMOND_SWORD, "§6§lRanked"))
        
        // Slot 3: Replays
        player.inventory.setItem(2, createItem(Material.PAINTING, "§e§lReplays"))
        
        // Slot 8: Stats
        player.inventory.setItem(7, createItem(Material.PAPER, "§7§lStats"))
        
        player.updateInventory()
    }
    
    private fun applyNoDebuffKit(player: Player) {
        // Armor
        player.inventory.helmet = createItem(Material.DIAMOND_HELMET)
        player.inventory.chestplate = createItem(Material.DIAMOND_CHESTPLATE)
        player.inventory.leggings = createItem(Material.DIAMOND_LEGGINGS)
        player.inventory.boots = createItem(Material.DIAMOND_BOOTS)
        
        // Sword
        player.inventory.setItem(0, createItem(Material.DIAMOND_SWORD))
        
        // Potions (slots 1-35)
        val potion = createItem(Material.POTION, "§dPotion")
        for (i in 1..35) {
            player.inventory.setItem(i, potion)
        }
        
        // Ender pearl
        player.inventory.setItem(8, ItemStack(Material.ENDER_PEARL, 16))
        
        // Speed II
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 999999, 1))
    }
    
    private fun applyGappleKit(player: Player) {
        player.inventory.helmet = createItem(Material.DIAMOND_HELMET)
        player.inventory.chestplate = createItem(Material.DIAMOND_CHESTPLATE)
        player.inventory.leggings = createItem(Material.DIAMOND_LEGGINGS)
        player.inventory.boots = createItem(Material.DIAMOND_BOOTS)
        
        player.inventory.setItem(0, createItem(Material.DIAMOND_SWORD))
        player.inventory.setItem(1, ItemStack(Material.GOLDEN_APPLE, 64))
    }
    
    private fun applySumoKit(player: Player) {
        player.inventory.setItem(0, createItem(Material.STICK, "§eKnockback Stick"))
    }
    
    private fun applyBuildUHCKit(player: Player) {
        player.inventory.helmet = createItem(Material.IRON_HELMET)
        player.inventory.chestplate = createItem(Material.IRON_CHESTPLATE)
        player.inventory.leggings = createItem(Material.IRON_LEGGINGS)
        player.inventory.boots = createItem(Material.IRON_BOOTS)
        
        player.inventory.setItem(0, createItem(Material.IRON_AXE))
        player.inventory.setItem(1, ItemStack(Material.COBBLESTONE, 64))
        player.inventory.setItem(2, ItemStack(Material.WOOD, 64))
        player.inventory.setItem(8, ItemStack(Material.GOLDEN_APPLE, 16))
    }
    
    private fun createItem(material: Material, name: String? = null): ItemStack {
        val item = ItemStack(material)
        if (name != null) {
            val meta = item.itemMeta
            meta?.displayName = name
            item.itemMeta = meta
        }
        return item
    }
}