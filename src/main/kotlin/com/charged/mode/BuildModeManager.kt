package com.charged.mode

import com.charged.Charged
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BuildModeManager(private val plugin: Charged) {
    
    private val buildModePlayers = ConcurrentHashMap<UUID, BuildModeSession>()
    private val previousInventories = ConcurrentHashMap<UUID, Array<ItemStack?>>()
    
    /**
     * Enable build mode for player
     */
    fun enableBuildMode(player: Player) {
        if (isInBuildMode(player)) {
            player.sendMessage("§cYou are already in build mode!")
            return
        }
        
        // Check permission
        if (!player.hasPermission("charged.build")) {
            player.sendMessage("§cYou don't have permission to use build mode!")
            return
        }
        
        // Save current inventory
        previousInventories[player.uniqueId] = player.inventory.contents.clone()
        
        // Clear inventory
        player.inventory.clear()
        
        // Set gamemode
        player.gameMode = GameMode.CREATIVE
        
        // Give build tools
        giveBuildTools(player)
        
        // Create session
        val session = BuildModeSession(
            player = player.uniqueId,
            startTime = System.currentTimeMillis()
        )
        buildModePlayers[player.uniqueId] = session
        
        // Visual effects
        player.sendTitle("§a§lBUILD MODE", "§7Tools have been given")
        player.playSound(player.location, org.bukkit.Sound.LEVEL_UP, 1f, 1f)
        player.sendMessage("§a§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("§a§lBUILD MODE §fenabled!")
        player.sendMessage("")
        player.sendMessage("§7You now have access to:")
        player.sendMessage("§f▸ §7WorldEdit commands")
        player.sendMessage("§f▸ §7Building tools")
        player.sendMessage("§f▸ §7Creative mode")
        player.sendMessage("")
        player.sendMessage("§7Use §c/buildmode §7to exit")
        player.sendMessage("§a§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        // Update nametag
        updateNametag(player, true)
    }
    
    /**
     * Disable build mode for player
     */
    fun disableBuildMode(player: Player) {
        if (!isInBuildMode(player)) {
            player.sendMessage("§cYou are not in build mode!")
            return
        }
        
        // Remove session
        buildModePlayers.remove(player.uniqueId)
        
        // Restore inventory
        val previous = previousInventories.remove(player.uniqueId)
        if (previous != null) {
            player.inventory.contents = previous
        } else {
            player.inventory.clear()
        }
        
        // Set gamemode back
        player.gameMode = GameMode.SURVIVAL
        
        // Visual effects
        player.sendTitle("§c§lBUILD MODE", "§7Disabled")
        player.playSound(player.location, org.bukkit.Sound.FIZZ, 1f, 1f)
        player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("§c§lBUILD MODE §fdisabled!")
        player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        // Update nametag
        updateNametag(player, false)
    }
    
    /**
     * Toggle build mode
     */
    fun toggleBuildMode(player: Player) {
        if (isInBuildMode(player)) {
            disableBuildMode(player)
        } else {
            enableBuildMode(player)
        }
    }
    
    /**
     * Give build tools to player
     */
    private fun giveBuildTools(player: Player) {
        val tools = listOf(
            createTool(Material.WOOD_AXE, "§6WorldEdit Wand", 
                listOf("§7Left click: First position", "§7Right click: Second position"), 0),
            createTool(Material.WOOD_SPADE, "§eSelection Tool", 
                listOf("§7Expand/contract selection"), 1),
            createTool(Material.BOOK, "§bBuild Commands", 
                listOf("§7View all build commands"), 2),
            createTool(Material.GRASS, "§aBlocks Menu", 
                listOf("§7Open block selection"), 3),
            createTool(Material.SKULL_ITEM, "§dCopy/Paste", 
                listOf("§7Copy and paste structures"), 4),
            createTool(Material.BLAZE_ROD, "§cBuild Settings", 
                listOf("§7Configure build options"), 5),
            createTool(Material.ENDER_PEARL, "§5Teleport Tool", 
                listOf("§7Teleport to locations"), 6),
            createTool(Material.WATCH, "§3Undo/Redo", 
                listOf("§7Undo: §e/undo", "§7Redo: §e/redo"), 7),
            createTool(Material.BARRIER, "§cExit Build Mode", 
                listOf("§7Exit build mode", "§c§lClick to exit!"), 8)
        )
        
        tools.forEach { (item, slot) ->
            player.inventory.setItem(slot, item)
        }
    }
    
    private fun createTool(material: Material, name: String, lore: List<String>, slot: Int): Pair<ItemStack, Int> {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName = name
        meta.lore = lore
        meta.spigot().isUnbreakable = true
        item.itemMeta = meta
        return item to slot
    }
    
    /**
     * Update player nametag
     */
    private fun updateNametag(player: Player, buildMode: Boolean) {
        if (buildMode) {
            player.setDisplayName("§e[BUILD] ${player.name}")
            player.playerListName = "§e[BUILD] ${player.name}"
        } else {
            player.setDisplayName(player.name)
            player.playerListName = player.name
        }
    }
    
    /**
     * Check if player is in build mode
     */
    fun isInBuildMode(player: Player): Boolean {
        return buildModePlayers.containsKey(player.uniqueId)
    }
    
    /**
     * Get build mode session
     */
    fun getSession(player: Player): BuildModeSession? {
        return buildModePlayers[player.uniqueId]
    }
}

data class BuildModeSession(
    val player: UUID,
    val startTime: Long,
    var blocksPlaced: Int = 0,
    var blocksDestroyed: Int = 0
)