package com.charged.mode

import com.charged.Charged
import com.charged.util.PluginAccess
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BuildModeManager(private val plugin: Charged) {

    private val buildModePlayers = ConcurrentHashMap<UUID, BuildModeSession>()
    private val previousInventories = ConcurrentHashMap<UUID, Array<ItemStack?>>()
    private val previousArmor = ConcurrentHashMap<UUID, Array<ItemStack?>>()
    private val previousGameModes = ConcurrentHashMap<UUID, GameMode>()
    private val previousLocations = ConcurrentHashMap<UUID, org.bukkit.Location>()
    private val previousHealth = ConcurrentHashMap<UUID, Double>()
    private val previousFood = ConcurrentHashMap<UUID, Int>()
    private val previousExp = ConcurrentHashMap<UUID, Float>()

    companion object {
        val BUILD_TOOLS = mapOf(
            0 to ToolData(Material.WOOD_AXE, "§6§lWorldEdit Wand",
                listOf("§7Left click: §eFirst position", "§7Right click: §eSecond position", "§7Shift + Right: §eClear selection")),
            1 to ToolData(Material.WOOD_SPADE, "§e§lSelection Tool",
                listOf("§7§lSHIFT + SCROLL:", "§7Expand/contract selection", "§7§oUse shift + mouse wheel")),
            2 to ToolData(Material.BOOK, "§b§lBuild Commands",
                listOf("§7/set <block>", "§7/replace <from> <to>", "§7/move <x> <y> <z>", "§7/stack <times>")),
            3 to ToolData(Material.GRASS, "§a§lBlocks Menu",
                listOf("§7Right click to open", "§7Quick block selection", "§7§oShift + Right: Favorites")),
            4 to ToolData(Material.SKULL_ITEM, "§d§lCopy/Paste Tool",
                listOf("§7/copy", "§7/paste", "§7/cut", "§7/schematic save <name>"), 3),
            5 to ToolData(Material.BLAZE_ROD, "§c§lBuild Settings",
                listOf("§7/mask <pattern>", "§7/pattern <block>", "§7/fast", "§7/speed <1-10>")),
            6 to ToolData(Material.ENDER_PEARL, "§5§lTeleport Tool",
                listOf("§7/teleport <x> <y> <z>", "§7/jumpto", "§7/ascend", "§7/descend")),
            7 to ToolData(Material.WATCH, "§3§lHistory Tools",
                listOf("§7/undo", "§7/redo", "§7/clearhistory", "§7/undos <amount>")),
            8 to ToolData(Material.BARRIER, "§4§lExit Build Mode",
                listOf("§c§lCLICK TO EXIT!", "§7Or use §c/buildmode", "§7§oExit safely"))
        )
    }

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

        // Save player state
        savePlayerState(player)

        // Create session
        val session = BuildModeSession(
            player = player.uniqueId,
            startTime = System.currentTimeMillis(),
            originLocation = player.location.clone()
        )
        buildModePlayers[player.uniqueId] = session

        // Setup build mode
        setupBuildMode(player)

        // Give tools
        giveBuildTools(player)

        // Visual feedback
        sendEnableMessage(player, session)

        // Register event listener if needed
        registerEvents(player)
    }

    /**
     * Disable build mode for player
     */
    fun disableBuildMode(player: Player) {
        if (!isInBuildMode(player)) {
            player.sendMessage("§cYou are not in build mode!")
            return
        }

        // Get session for stats
        val session = buildModePlayers.remove(player.uniqueId)

        // Restore player state
        restorePlayerState(player)

        // Visual feedback
        sendDisableMessage(player, session)

        // Remove effects
        removeBuildModeEffects(player)

        // Unregister events
        unregisterEvents(player)
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
     * Save player's original state
     */
    private fun savePlayerState(player: Player) {
        // Save inventory
        previousInventories[player.uniqueId] = player.inventory.contents.clone()
        previousArmor[player.uniqueId] = player.inventory.armorContents.clone()

        // Save other properties
        previousGameModes[player.uniqueId] = player.gameMode
        previousLocations[player.uniqueId] = player.location.clone()
        previousHealth[player.uniqueId] = player.health
        previousFood[player.uniqueId] = player.foodLevel
        previousExp[player.uniqueId] = player.exp
    }

    /**
     * Restore player's original state
     */
    private fun restorePlayerState(player: Player) {
        // Restore inventory
        previousInventories.remove(player.uniqueId)?.let {
            player.inventory.contents = it
        }
        previousArmor.remove(player.uniqueId)?.let {
            player.inventory.armorContents = it
        }

        // Restore other properties
        previousGameModes.remove(player.uniqueId)?.let { player.gameMode = it }
        previousLocations.remove(player.uniqueId)?.let { player.teleport(it) }
        previousHealth.remove(player.uniqueId)?.let { player.health = it }
        previousFood.remove(player.uniqueId)?.let { player.foodLevel = it }
        previousExp.remove(player.uniqueId)?.let { player.exp = it }
    }

    /**
     * Setup build mode properties
     */
    private fun setupBuildMode(player: Player) {
        // Set creative mode
        player.gameMode = GameMode.CREATIVE

        // Enable flight
        player.allowFlight = true
        player.isFlying = true

        // Clear inventory
        player.inventory.clear()
        player.inventory.armorContents = arrayOfNulls(4)

        // Add build effects
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 1, false, false))

        // Set health and food to max
        player.health = 20.0
        player.foodLevel = 20

        // Disable damage
        player.noDamageTicks = Integer.MAX_VALUE
    }

    /**
     * Give build tools to player
     */
    private fun giveBuildTools(player: Player) {
        BUILD_TOOLS.forEach { (slot, toolData) ->
            val item = createToolItem(toolData)
            player.inventory.setItem(slot, item)
        }

        // Give some building blocks
        val buildingBlocks = listOf(
            ItemStack(Material.STONE, 64),
            ItemStack(Material.GRASS, 64),
            ItemStack(Material.WOOD, 64),
            ItemStack(Material.GLASS, 64),
            ItemStack(Material.WOOL, 64),
            ItemStack(Material.SANDSTONE, 64),
            ItemStack(Material.BRICK, 64),
            ItemStack(Material.SMOOTH_BRICK, 64)
        )

        buildingBlocks.forEachIndexed { index, item ->
            player.inventory.setItem(9 + index, item)
        }

        // Update inventory
        player.updateInventory()
    }

    /**
     * Create a tool item with metadata
     */
    private fun createToolItem(toolData: ToolData): ItemStack {
        val item = ItemStack(toolData.material, 1, toolData.data.toShort())
        val meta = item.itemMeta

        meta.displayName = toolData.name
        meta.lore = toolData.lore

        // Special handling for skull
        if (toolData.material == Material.SKULL_ITEM && toolData.data == 3) {
            val skullMeta = meta as SkullMeta
            skullMeta.owner = "MHF_Question"
        }

        // Make unbreakable
        meta.spigot().isUnbreakable = true

        item.itemMeta = meta
        return item
    }

    /**
     * Send enable message with session info
     */
    private fun sendEnableMessage(player: Player, session: BuildModeSession) {
        player.sendMessage(" ")
        player.sendMessage("§6§l» §e§lBUILD MODE §6§lACTIVATED!")
        player.sendMessage("§7  You now have access to build tools and commands")
        player.sendMessage(" ")
        player.sendMessage("§8§l⚒ §7Tools §8(§f0-8§8)")
        player.sendMessage("  §7▸ §fWorldEdit Wand §8- §7Selection tool")
        player.sendMessage("  §7▸ §fBlocks Menu §8- §7Right click for blocks")
        player.sendMessage("  §7▸ §fExit Tool §8- §cSlot 8 to exit")
        player.sendMessage(" ")
        player.sendMessage("§8§l⚡ §7Useful Commands")
        player.sendMessage("  §7▸ §f/pos1§7, §f/pos2 §8- §7Set positions")
        player.sendMessage("  §7▸ §f/set §8- §7Fill selection")
        player.sendMessage("  §7▸ §f/undo §8- §7Undo last action")
        player.sendMessage(" ")
        player.sendMessage("§8§lℹ §7Info")
        player.sendMessage("  §7▸ §fSession: §e#${session.id}")
        player.sendMessage("  §7▸ §fLocation: §e${player.world.name}")
        player.sendMessage("  §7▸ §fTime: §e${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
        player.sendMessage(" ")
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // Title and sound
        player.sendTitle("§a§lBUILD MODE", "§7Tools activated")
        player.playSound(player.location, org.bukkit.Sound.LEVEL_UP, 1f, 1f)

        // Update scoreboard
        updateBuildScoreboard(player, session)

        // Update nametag
        updateNametag(player, true)
    }

    /**
     * Send disable message with session stats
     */
    private fun sendDisableMessage(player: Player, session: BuildModeSession?) {
        val duration = session?.let {
            val minutes = (System.currentTimeMillis() - it.startTime) / 60000
            "${minutes}m"
        } ?: "?"

        val blocks = session?.let {
            "§7Placed: §e${it.blocksPlaced} §8| §7Destroyed: §e${it.blocksDestroyed}"
        } ?: ""

        player.sendMessage(" ")
        player.sendMessage("§c§l» §4§lBUILD MODE §c§lDEACTIVATED!")
        player.sendMessage("§7  Your inventory has been restored")
        player.sendMessage(" ")
        player.sendMessage("§8§l📊 §7Session Stats")
        if (session != null) {
            player.sendMessage("  §7▸ §fDuration: §e$duration")
            player.sendMessage("  §7▸ $blocks")
            player.sendMessage("  §7▸ §fSession: §e#${session.id}")
        }
        player.sendMessage(" ")
        player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // Title and sound
        player.sendTitle("§c§lBUILD MODE", "§7Disabled")
        player.playSound(player.location, org.bukkit.Sound.FIZZ, 0.5f, 1f)

        // Update nametag
        updateNametag(player, false)

        // Remove scoreboard
        removeScoreboard(player)
    }

    /**
     * Update player nametag
     */
    private fun updateNametag(player: Player, buildMode: Boolean) {
        if (buildMode) {
            player.setDisplayName("§e[BUILD] §f${player.name}")
            player.playerListName = "§e[BUILD] §f${player.name}"
        } else {
            player.setDisplayName(player.name)
            player.playerListName = player.name
        }
    }

    /**
     * Update build mode scoreboard
     */
    private fun updateBuildScoreboard(player: Player, session: BuildModeSession) {
        val scoreboard = plugin.server.scoreboardManager.newScoreboard
        val objective = scoreboard.registerNewObjective("buildmode", "dummy")

        objective.displayName = "§6§lBUILD MODE"
        objective.displaySlot = org.bukkit.scoreboard.DisplaySlot.SIDEBAR

        // Add lines
        fun addLine(text: String, score: Int) {
            val entry = org.bukkit.ChatColor.values()[score].toString() + org.bukkit.ChatColor.RESET
            val team = scoreboard.registerNewTeam("line$score")
            team.prefix = text
            team.addEntry(entry)
            objective.getScore(entry).score = score
        }

        addLine("§7Session: §e#${session.id}", 15)
        addLine("§7", 14)
        addLine("§8§l⚒ §7Tools", 13)
        addLine("§7▸ Wand: §fSlot 0", 12)
        addLine("§7▸ Blocks: §fSlot 3", 11)
        addLine("§7▸ Exit: §cSlot 8", 10)
        addLine("§7", 9)
        addLine("§8§l📊 §7Stats", 8)
        addLine("§7Placed: §e${session.blocksPlaced}", 7)
        addLine("§7Destroyed: §e${session.blocksDestroyed}", 6)
        addLine("§7", 5)
        addLine("§8§l⚡ §7Commands", 4)
        addLine("§7/pos1§7, /pos2", 3)
        addLine("§7/set, /undo", 2)
        addLine("§7", 1)
        addLine("§e§lcharged.gg", 0)

        player.scoreboard = scoreboard
    }

    /**
     * Remove scoreboard
     */
    private fun removeScoreboard(player: Player) {
        player.scoreboard = plugin.server.scoreboardManager.mainScoreboard
    }

    /**
     * Remove build mode effects
     */
    private fun removeBuildModeEffects(player: Player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION)
        player.removePotionEffect(PotionEffectType.SPEED)
        player.noDamageTicks = 0
    }

    /**
     * Register event listeners for build mode
     */
    private fun registerEvents(player: Player) {
        // This would be handled by a separate BuildModeListener class
        // For now, we just track in manager
    }

    /**
     * Unregister event listeners
     */
    private fun unregisterEvents(player: Player) {
        // Cleanup
    }

    /**
     * Handle block placement in build mode
     */
    fun handleBlockPlace(player: Player) {
        val session = buildModePlayers[player.uniqueId]
        session?.blocksPlaced = (session?.blocksPlaced ?: 0) + 1
        updateStatsOnScoreboard(player, session)
    }

    /**
     * Handle block break in build mode
     */
    fun handleBlockBreak(player: Player) {
        val session = buildModePlayers[player.uniqueId]
        session?.blocksDestroyed = (session?.blocksDestroyed ?: 0) + 1
        updateStatsOnScoreboard(player, session)
    }

    /**
     * Update stats on scoreboard
     */
    private fun updateStatsOnScoreboard(player: Player, session: BuildModeSession?) {
        // Update the scoreboard if it exists
        val scoreboard = player.scoreboard
        val objective = scoreboard.getObjective("buildmode")

        if (objective != null && session != null) {
            // Find and update the lines
            scoreboard.teams.forEach { team ->
                if (team.prefix.contains("Placed: ")) {
                    team.prefix = "§7Placed: §e${session.blocksPlaced}"
                } else if (team.prefix.contains("Destroyed: ")) {
                    team.prefix = "§7Destroyed: §e${session.blocksDestroyed}"
                }
            }
        }
    }

    /**
     * Get build mode session info
     */
    fun getSessionInfo(player: Player): String {
        val session = buildModePlayers[player.uniqueId] ?: return "Not in build mode"

        val duration = (System.currentTimeMillis() - session.startTime) / 1000
        val minutes = duration / 60
        val seconds = duration % 60

        return """
            §6§lBuild Mode Session
            §7ID: §e#${session.id}
            §7Duration: §e${minutes}m ${seconds}s
            §7Blocks Placed: §e${session.blocksPlaced}
            §7Blocks Destroyed: §e${session.blocksDestroyed}
            §7Origin: §e${session.originLocation?.world?.name ?: "Unknown"}
            §7Location: §e${session.originLocation?.blockX ?: 0}, ${session.originLocation?.blockY ?: 0}, ${session.originLocation?.blockZ ?: 0}
        """.trimIndent()
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

    /**
     * Get all players in build mode
     */
    fun getPlayersInBuildMode(): List<Player> {
        return buildModePlayers.keys.mapNotNull { plugin.server.getPlayer(it) }
    }

    /**
     * Force exit all build mode sessions
     */
    fun forceExitAll() {
        getPlayersInBuildMode().forEach { disableBuildMode(it) }
        plugin.logger.info("Force exited all build mode sessions")
    }
}

data class BuildModeSession(
    val player: UUID,
    val startTime: Long,
    val id: String = UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
    var blocksPlaced: Int = 0,
    var blocksDestroyed: Int = 0,
    val originLocation: org.bukkit.Location? = null
)

data class ToolData(
    val material: Material,
    val name: String,
    val lore: List<String>,
    val data: Int = 0
)