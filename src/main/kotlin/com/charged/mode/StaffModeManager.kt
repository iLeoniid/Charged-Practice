package com.charged.mode

import com.charged.Charged
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class StaffModeManager(private val plugin: Charged) {
    
    private val staffModePlayers = ConcurrentHashMap<UUID, StaffModeSession>()
    private val vanishedPlayers = ConcurrentHashMap<UUID, Boolean>()
    private val frozenPlayers = ConcurrentHashMap<UUID, FreezeData>()
    private val previousInventories = ConcurrentHashMap<UUID, Array<ItemStack?>>()
    
    /**
     * Enable staff mode for player
     */
    fun enableStaffMode(player: Player) {
        if (isInStaffMode(player)) {
            player.sendMessage("§cYou are already in staff mode!")
            return
        }
        
        // Check permission
        if (!player.hasPermission("charged.staff")) {
            player.sendMessage("§cYou don't have permission to use staff mode!")
            return
        }
        
        // Save inventory
        previousInventories[player.uniqueId] = player.inventory.contents.clone()
        
        // Clear inventory
        player.inventory.clear()
        
        // Give staff tools
        giveStaffTools(player)
        
        // Create session
        val session = StaffModeSession(
            player = player.uniqueId,
            startTime = System.currentTimeMillis()
        )
        staffModePlayers[player.uniqueId] = session
        
        // Auto-vanish
        enableVanish(player, silent = true)
        
        // Visual effects
        player.sendTitle("§4§lSTAFF MODE", "§7Enabled")
        player.playSound(player.location, org.bukkit.Sound.ENDERDRAGON_GROWL, 1f, 0.5f)
        player.sendMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("§4§lSTAFF MODE §fenabled!")
        player.sendMessage("")
        player.sendMessage("§7You now have access to:")
        player.sendMessage("§f▸ §7Vanish mode")
        player.sendMessage("§f▸ §7Staff tools")
        player.sendMessage("§f▸ §7Player management")
        player.sendMessage("")
        player.sendMessage("§7Use §c/staffmode §7to exit")
        player.sendMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        // Update nametag
        updateNametag(player, true)
        
        // Broadcast to other staff
        broadcastToStaff("§8[§4Staff§8] §7${player.name} §fjoined staff mode")
    }
    
    /**
     * Disable staff mode
     */
    fun disableStaffMode(player: Player) {
        if (!isInStaffMode(player)) {
            player.sendMessage("§cYou are not in staff mode!")
            return
        }
        
        // Remove session
        staffModePlayers.remove(player.uniqueId)
        
        // Disable vanish
        if (isVanished(player)) {
            disableVanish(player, silent = true)
        }
        
        // Restore inventory
        val previous = previousInventories.remove(player.uniqueId)
        if (previous != null) {
            player.inventory.contents = previous
        } else {
            player.inventory.clear()
        }
        
        // Visual effects
        player.sendTitle("§7§lSTAFF MODE", "§7Disabled")
        player.playSound(player.location, org.bukkit.Sound.FIZZ, 1f, 1f)
        player.sendMessage("§7§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("§7§lSTAFF MODE §fdisabled!")
        player.sendMessage("§7§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        // Update nametag
        updateNametag(player, false)
        
        // Broadcast
        broadcastToStaff("§8[§4Staff§8] §7${player.name} §fleft staff mode")
    }
    
    /**
     * Toggle staff mode
     */
    fun toggleStaffMode(player: Player) {
        if (isInStaffMode(player)) {
            disableStaffMode(player)
        } else {
            enableStaffMode(player)
        }
    }
    
    /**
     * Give staff tools
     */
    private fun giveStaffTools(player: Player) {
        val tools = listOf(
            createTool(Material.INK_SACK, 8, "§7Vanish: §cDisabled", 
                listOf("§7Toggle visibility", "§7to players", "", "§eClick to toggle"), 0),
            createTool(Material.COMPASS, 0, "§bRandom Teleport", 
                listOf("§7Teleport to random player", "", "§eClick to teleport"), 1),
            createTool(Material.BOOK, 0, "§ePlayer Inspector", 
                listOf("§7Inspect player info", "", "§eClick to inspect"), 2),
            createTool(Material.ICE, 0, "§9Freeze Player", 
                listOf("§7Freeze a player", "", "§eClick to freeze"), 3),
            createTool(Material.PAPER, 0, "§cPending Reports", 
                listOf("§7View reports", "", "§cPending: §f0", "", "§eClick to view"), 4),
            createTool(Material.ENDER_PEARL, 0, "§5Teleport Menu", 
                listOf("§7Teleport to players", "", "§eClick to open"), 5),
            createTool(Material.DIAMOND, 0, "§aGamemode Changer", 
                listOf("§7Quick gamemode switch", "", "§eClick to cycle"), 6),
            createTool(Material.BLAZE_ROD, 0, "§6World Editor", 
                listOf("§7Edit world settings", "", "§eClick to open"), 7),
            createTool(Material.NETHER_STAR, 0, "§dStaff Menu", 
                listOf("§7Staff control panel", "", "§eClick to open"), 8)
        )
        
        tools.forEach { (item, slot) ->
            player.inventory.setItem(slot, item)
        }
    }
    
    private fun createTool(material: Material, data: Int, name: String, lore: List<String>, slot: Int): Pair<ItemStack, Int> {
        val item = ItemStack(material, 1, data.toShort())
        val meta = item.itemMeta
        meta.displayName = name
        meta.lore = lore
        meta.spigot().isUnbreakable = true
        item.itemMeta = meta
        return item to slot
    }
    
    /**
     * Enable vanish
     */
    fun enableVanish(player: Player, silent: Boolean = false) {
        vanishedPlayers[player.uniqueId] = true
        
        // Hide from other players
        plugin.server.onlinePlayers.forEach { other ->
            if (other != player && !other.hasPermission("charged.staff.vanish")) {
                other.hidePlayer(player)
            }
        }
        
        // Add invisibility potion
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 0, false, false))
        
        if (!silent) {
            player.sendMessage("§8[§4Staff§8] §7You are now §avanished")
            player.playSound(player.location, org.bukkit.Sound.ORB_PICKUP, 1f, 2f)
        }
        
        // Update vanish tool
        updateVanishTool(player, true)
    }
    
    /**
     * Disable vanish
     */
    fun disableVanish(player: Player, silent: Boolean = false) {
        vanishedPlayers.remove(player.uniqueId)
        
        // Show to other players
        plugin.server.onlinePlayers.forEach { other ->
            other.showPlayer(player)
        }
        
        // Remove invisibility
        player.removePotionEffect(PotionEffectType.INVISIBILITY)
        
        if (!silent) {
            player.sendMessage("§8[§4Staff§8] §7You are now §cvisible")
            player.playSound(player.location, org.bukkit.Sound.ORB_PICKUP, 1f, 0.5f)
        }
        
        // Update vanish tool
        updateVanishTool(player, false)
    }
    
    /**
     * Toggle vanish
     */
    fun toggleVanish(player: Player) {
        if (isVanished(player)) {
            disableVanish(player)
        } else {
            enableVanish(player)
        }
    }
    
    private fun updateVanishTool(player: Player, vanished: Boolean) {
        val item = player.inventory.getItem(0) ?: return
        val meta = item.itemMeta
        
        if (vanished) {
            item.type = Material.INK_SACK
            item.durability = 10.toShort() // Lime dye
            meta.displayName = "§aVanish: §fEnabled"
        } else {
            item.type = Material.INK_SACK
            item.durability = 8.toShort() // Gray dye
            meta.displayName = "§7Vanish: §cDisabled"
        }
        
        item.itemMeta = meta
        player.inventory.setItem(0, item)
    }
    
    /**
     * Freeze player
     */
    fun freezePlayer(target: Player, staff: Player) {
        if (isFrozen(target)) {
            staff.sendMessage("§cThat player is already frozen!")
            return
        }
        
        frozenPlayers[target.uniqueId] = FreezeData(
            frozenBy = staff.uniqueId,
            frozenAt = System.currentTimeMillis()
        )
        
        // Prevent movement
        target.walkSpeed = 0f
        target.flySpeed = 0f
        
        // Send message
        target.sendTitle("§c§lFROZEN", "§7You have been frozen by staff")
        target.playSound(target.location, org.bukkit.Sound.ANVIL_BREAK, 1f, 0.5f)
        target.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        target.sendMessage("§c§lYOU HAVE BEEN FROZEN!")
        target.sendMessage("")
        target.sendMessage("§7You have been frozen by §f${staff.name}")
        target.sendMessage("§7Please wait for staff assistance")
        target.sendMessage("")
        target.sendMessage("§c§lDo not log out or you will be banned!")
        target.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        staff.sendMessage("§8[§4Staff§8] §7You froze §f${target.name}")
    }
    
    /**
     * Unfreeze player
     */
    fun unfreezePlayer(target: Player, staff: Player) {
        if (!isFrozen(target)) {
            staff.sendMessage("§cThat player is not frozen!")
            return
        }
        
        frozenPlayers.remove(target.uniqueId)
        
        // Restore movement
        target.walkSpeed = 0.2f
        target.flySpeed = 0.1f
        
        target.sendTitle("§a§lUNFROZEN", "§7You can move again")
        target.sendMessage("§a§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        target.sendMessage("§a§lYou have been unfrozen!")
        target.sendMessage("§a§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        staff.sendMessage("§8[§4Staff§8] §7You unfroze §f${target.name}")
    }
    
    private fun updateNametag(player: Player, staffMode: Boolean) {
        if (staffMode) {
            player.setDisplayName("§4[STAFF] ${player.name}")
            player.playerListName = "§4[STAFF] ${player.name}"
        } else {
            player.setDisplayName(player.name)
            player.playerListName = player.name
        }
    }
    
    private fun broadcastToStaff(message: String) {
        plugin.server.onlinePlayers
            .filter { it.hasPermission("charged.staff") }
            .forEach { it.sendMessage(message) }
    }
    
    fun isInStaffMode(player: Player): Boolean = staffModePlayers.containsKey(player.uniqueId)
    fun isVanished(player: Player): Boolean = vanishedPlayers.containsKey(player.uniqueId)
    fun isFrozen(player: Player): Boolean = frozenPlayers.containsKey(player.uniqueId)
}

data class StaffModeSession(
    val player: UUID,
    val startTime: Long,
    var actionsPerformed: Int = 0
)

data class FreezeData(
    val frozenBy: UUID,
    val frozenAt: Long
)