package com.charged.menu

import com.charged.Charged
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MenuManager(private val plugin: Charged) {
    
    private val menus = ConcurrentHashMap<String, Menu>()
    private val openMenus = ConcurrentHashMap<UUID, Menu>()
    private val menuConfig: YamlConfiguration
    private val globalSettings: GlobalSettings
    
    init {
        // Load menu configuration
        val menuFile = File(plugin.dataFolder, "menu.yml")
        menuConfig = YamlConfiguration.loadConfiguration(menuFile)
        
        // Load global settings
        globalSettings = loadGlobalSettings()
        
        // Load all menus
        loadMenus()
        
        plugin.logger.info("§a[MenuManager] Loaded ${menus.size} menus")
    }
    
    private fun loadGlobalSettings(): GlobalSettings {
        val section = menuConfig.getConfigurationSection("global") ?: return GlobalSettings()
        
        return GlobalSettings(
            animationsEnabled = section.getBoolean("animations.enabled", true),
            openAnimation = AnimationType.valueOf(
                section.getString("animations.open-animation", "EXPAND")!!.uppercase()
            ),
            closeAnimation = AnimationType.valueOf(
                section.getString("animations.close-animation", "SHRINK")!!.uppercase()
            ),
            transitionSpeed = section.getInt("animations.transition-speed", 10),
            
            sounds = SoundSettings(
                menuOpen = section.getString("sounds.menu-open", "CHEST_OPEN:1.0:1.0")!!,
                menuClose = section.getString("sounds.menu-close", "CHEST_CLOSE:1.0:1.0")!!,
                clickSuccess = section.getString("sounds.click-success", "CLICK:1.0:1.2")!!,
                clickFail = section.getString("sounds.click-fail", "VILLAGER_NO:1.0:0.8")!!,
                pageTurn = section.getString("sounds.page-turn", "ITEM_BOOK_PAGE_TURN:1.0:1.0")!!
            ),
            
            cooldownEnabled = section.getBoolean("cooldown.enabled", true),
            cooldownTime = section.getInt("cooldown.time", 500),
            cooldownMessage = section.getString("cooldown.message", "§cPlease wait!")!!,
            
            autoRefreshEnabled = section.getBoolean("auto-refresh.enabled", true),
            autoRefreshInterval = section.getInt("auto-refresh.interval", 20),
            autoRefreshOnlyWhenOpen = section.getBoolean("auto-refresh.only-when-open", true)
        )
    }
    
    private fun loadMenus() {
        val menusSection = menuConfig.getConfigurationSection("menus") ?: return
        
        menusSection.getKeys(false).forEach { menuId ->
            val menuSection = menusSection.getConfigurationSection(menuId) ?: return@forEach
            
            val menu = Menu(
                id = menuId,
                title = menuSection.getString("title", "§8Menu")!!,
                size = menuSection.getInt("size", 54),
                updateInterval = menuSection.getInt("update-interval", 20),
                background = loadBackground(menuSection),
                items = loadItems(menuSection),
                autoPopulate = loadAutoPopulate(menuSection),
                globalSettings = globalSettings
            )
            
            menus[menuId] = menu
        }
    }
    
    private fun loadBackground(section: org.bukkit.configuration.ConfigurationSection): Background? {
        val bgSection = section.getConfigurationSection("background") ?: return null
        
        if (!bgSection.getBoolean("enabled", false)) return null
        
        return Background(
            pattern = BackgroundPattern.valueOf(
                bgSection.getString("pattern", "BORDER")!!.uppercase()
            ),
            material = bgSection.getString("material", "STAINED_GLASS_PANE:15")!!,
            borderMaterial = bgSection.getString("border-material", "STAINED_GLASS_PANE:7")
        )
    }
    
    private fun loadItems(section: org.bukkit.configuration.ConfigurationSection): Map<Int, MenuItem> {
        val itemsSection = section.getConfigurationSection("items") ?: return emptyMap()
        val items = mutableMapOf<Int, MenuItem>()
        
        itemsSection.getKeys(false).forEach { itemId ->
            val itemSection = itemsSection.getConfigurationSection(itemId) ?: return@forEach
            
            val slot = itemSection.getInt("slot", -1)
            if (slot < 0) return@forEach
            
            items[slot] = MenuItem(
                id = itemId,
                slot = slot,
                material = itemSection.getString("material", "STONE")!!,
                amount = itemSection.getInt("amount", 1),
                glow = itemSection.getBoolean("glow", false),
                display = loadDisplay(itemSection),
                actions = loadActions(itemSection),
                requirement = loadRequirement(itemSection),
                effects = loadEffects(itemSection),
                toggle = loadToggle(itemSection),
                cycle = loadCycle(itemSection),
                dynamic = itemSection.getBoolean("dynamic.update", false)
            )
        }
        
        return items
    }
    
    private fun loadDisplay(section: org.bukkit.configuration.ConfigurationSection): ItemDisplay {
        val displaySection = section.getConfigurationSection("display") ?: return ItemDisplay("§fItem", emptyList())
        
        return ItemDisplay(
            name = displaySection.getString("name", "§fItem")!!,
            lore = displaySection.getStringList("lore")
        )
    }
    
    private fun loadActions(section: org.bukkit.configuration.ConfigurationSection): Map<ClickType, String> {
        val actionsSection = section.getConfigurationSection("actions") ?: return emptyMap()
        val actions = mutableMapOf<ClickType, String>()
        
        actionsSection.getKeys(false).forEach { clickType ->
            val type = when(clickType.lowercase()) {
                "left-click" -> ClickType.LEFT
                "right-click" -> ClickType.RIGHT
                "shift-click", "shift-left-click" -> ClickType.SHIFT_LEFT
                "shift-right-click" -> ClickType.SHIFT_RIGHT
                "click" -> ClickType.LEFT
                else -> return@forEach
            }
            
            actions[type] = actionsSection.getString(clickType)!!
        }
        
        return actions
    }
    
    private fun loadRequirement(section: org.bukkit.configuration.ConfigurationSection): Requirement? {
        val reqSection = section.getConfigurationSection("requirement") ?: return null
        
        return Requirement(
            type = RequirementType.valueOf(reqSection.getString("type", "NONE")!!.uppercase()),
            value = reqSection.getInt("value", 0),
            message = reqSection.getString("message", "§cRequirement not met!")!!
        )
    }
    
    private fun loadEffects(section: org.bukkit.configuration.ConfigurationSection): ItemEffects? {
        val effectsSection = section.getConfigurationSection("effects") ?: return null
        
        return ItemEffects(
            hoverParticle = effectsSection.getString("hover-particle"),
            clickParticle = effectsSection.getString("click-particle"),
            soundHover = effectsSection.getString("sound-hover")
        )
    }
    
    private fun loadToggle(section: org.bukkit.configuration.ConfigurationSection): ToggleStates? {
        val toggleSection = section.getConfigurationSection("toggle") ?: return null
        val statesSection = toggleSection.getConfigurationSection("states") ?: return null
        
        val states = mutableMapOf<String, ToggleState>()
        statesSection.getKeys(false).forEach { stateName ->
            val stateSection = statesSection.getConfigurationSection(stateName) ?: return@forEach
            states[stateName] = ToggleState(
                material = stateSection.getString("material", "STONE")!!,
                name = stateSection.getString("name", "")!!,
                action = stateSection.getString("action", "")!!
            )
        }
        
        return ToggleStates(states)
    }
    
    private fun loadCycle(section: org.bukkit.configuration.ConfigurationSection): CycleValues? {
        val cycleSection = section.getConfigurationSection("cycle") ?: return null
        
        return CycleValues(
            values = cycleSection.getStringList("values"),
            currentKey = cycleSection.getString("current-key", "")!!,
            wrap = cycleSection.getBoolean("wrap", true)
        )
    }
    
    private fun loadAutoPopulate(section: org.bukkit.configuration.ConfigurationSection): AutoPopulate? {
        val autoSection = section.getConfigurationSection("auto-populate") ?: return null
        
        if (!autoSection.getBoolean("enabled", false)) return null
        
        return AutoPopulate(
            source = autoSection.getString("source", "")!!,
            startSlot = autoSection.getInt("start-slot", 0),
            skipSlots = autoSection.getIntegerList("skip-slots")
        )
    }
    
    /**
     * Open a menu for a player
     */
    fun openMenu(player: Player, menuId: String) {
        val menu = menus[menuId] ?: run {
            player.sendMessage("§cMenu not found: $menuId")
            return
        }
        
        // Close current menu if any
        closeMenu(player)
        
        // Create inventory
        val inventory = menu.createInventory(player, plugin)
        
        // Play open sound
        globalSettings.sounds.playMenuOpen(player)
        
        // Open inventory
        player.openInventory(inventory)
        
        // Track open menu
        openMenus[player.uniqueId] = menu
        
        // Start auto-refresh if enabled
        if (globalSettings.autoRefreshEnabled && menu.updateInterval > 0) {
            startAutoRefresh(player, menu)
        }
    }
    
    /**
     * Close menu for a player
     */
    fun closeMenu(player: Player) {
        openMenus.remove(player.uniqueId)
        player.closeInventory()
        globalSettings.sounds.playMenuClose(player)
    }
    
    /**
     * Handle click in menu
     */
    fun handleClick(player: Player, slot: Int, clickType: ClickType): Boolean {
        val menu = openMenus[player.uniqueId] ?: return false
        
        // Check cooldown
        if (!checkCooldown(player)) {
            player.sendMessage(globalSettings.cooldownMessage)
            return true
        }
        
        // Get item at slot
        val menuItem = menu.items[slot] ?: return true
        
        // Check requirement
        if (!checkRequirement(player, menuItem.requirement)) {
            menuItem.requirement?.let { player.sendMessage(it.message) }
            globalSettings.sounds.playClickFail(player)
            return true
        }
        
        // Get action for click type
        val action = menuItem.actions[clickType] ?: return true
        
        // Execute action
        executeAction(player, action, menuItem)
        
        // Play success sound
        globalSettings.sounds.playClickSuccess(player)
        
        // Play click particle
        menuItem.effects?.clickParticle?.let { playParticle(player, it) }
        
        return true
    }
    
    private fun checkCooldown(player: Player): Boolean {
        if (!globalSettings.cooldownEnabled) return true
        // TODO: Implement cooldown check
        return true
    }
    
    private fun checkRequirement(player: Player, requirement: Requirement?): Boolean {
        requirement ?: return true
        
        return when (requirement.type) {
            RequirementType.LEVEL -> {
                val level = plugin.plugin.playerManager.getStats(player.uniqueId)?.level ?: 0
                level >= requirement.value
            }
            RequirementType.PERMISSION -> {
                player.hasPermission(requirement.value.toString())
            }
            RequirementType.IN_CLAN -> {
                plugin.plugin.clanManager.getPlayerClan(player.uniqueId) != null
            }
            RequirementType.PLACEMENT_MATCHES -> {
                val stats = plugin.plugin.playerManager.getStats(player.uniqueId)
                (stats?.totalMatches ?: 0) >= requirement.value
            }
            RequirementType.ELO_RANGE -> true // TODO
            RequirementType.CUSTOM -> true // TODO
            RequirementType.NONE -> true
        }
    }
    
    private fun executeAction(player: Player, action: String, item: MenuItem) {
        val parts = action.split(":", limit = 2)
        val actionType = parts[0].uppercase()
        val actionValue = parts.getOrNull(1) ?: ""
        
        when (actionType) {
            "OPEN" -> openMenu(player, actionValue)
            "CLOSE" -> closeMenu(player)
            "QUEUE" -> handleQueue(player, actionValue)
            "TELEPORT" -> handleTeleport(player, actionValue)
            "COMMAND" -> player.performCommand(actionValue)
            "PREVIEW" -> handlePreview(player, actionValue)
            "TOGGLE" -> handleToggle(player, item, actionValue)
            "PAGE" -> handlePage(player, actionValue)
            "CYCLE" -> handleCycle(player, item)
            else -> plugin.logger.warning("Unknown action: $actionType")
        }
    }
    
    private fun handleQueue(player: Player, value: String) {
        val parts = value.split(":")
        if (parts.size >= 2) {
            val mode = parts[0]
            val type = parts[1]
            // TODO: Add to queue
            player.sendMessage("§aJoining $mode queue ($type)...")
        }
    }
    
    private fun handleTeleport(player: Player, value: String) {
        // TODO: Implement teleport
        player.sendMessage("§aTeleporting to $value...")
    }
    
    private fun handlePreview(player: Player, value: String) {
        // TODO: Implement preview system
        player.sendMessage("§ePreview: $value")
    }
    
    private fun handleToggle(player: Player, item: MenuItem, value: String) {
        // TODO: Implement toggle system
        player.sendMessage("§eToggled!")
    }
    
    private fun handlePage(player: Player, value: String) {
        // TODO: Implement pagination
        when (value.lowercase()) {
            "next" -> player.sendMessage("§eNext page")
            "previous" -> player.sendMessage("§ePrevious page")
        }
    }
    
    private fun handleCycle(player: Player, item: MenuItem) {
        // TODO: Implement cycle system
        player.sendMessage("§eCycled value!")
    }
    
    private fun playParticle(player: Player, particleType: String) {
        // TODO: Implement particle effects
    }
    
    private fun startAutoRefresh(player: Player, menu: Menu) {
        if (!globalSettings.autoRefreshOnlyWhenOpen) return
        
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (openMenus[player.uniqueId] != menu) return@Runnable
            
            // Refresh inventory
            val inventory = menu.createInventory(player, plugin)
            player.openInventory(inventory)
        }, menu.updateInterval.toLong(), menu.updateInterval.toLong())
        
        // TODO: Track task for cancellation
    }
    
    fun reload() {
        menus.clear()
        loadMenus()
        plugin.logger.info("§a[MenuManager] Reloaded ${menus.size} menus")
    }
}

// ============================================================================
// DATA CLASSES
// ============================================================================

data class GlobalSettings(
    val animationsEnabled: Boolean = true,
    val openAnimation: AnimationType = AnimationType.EXPAND,
    val closeAnimation: AnimationType = AnimationType.SHRINK,
    val transitionSpeed: Int = 10,
    val sounds: SoundSettings = SoundSettings(),
    val cooldownEnabled: Boolean = true,
    val cooldownTime: Int = 500,
    val cooldownMessage: String = "§cPlease wait!",
    val autoRefreshEnabled: Boolean = true,
    val autoRefreshInterval: Int = 20,
    val autoRefreshOnlyWhenOpen: Boolean = true
)

data class SoundSettings(
    val menuOpen: String = "CHEST_OPEN:1.0:1.0",
    val menuClose: String = "CHEST_CLOSE:1.0:1.0",
    val clickSuccess: String = "CLICK:1.0:1.2",
    val clickFail: String = "VILLAGER_NO:1.0:0.8",
    val pageTurn: String = "ITEM_BOOK_PAGE_TURN:1.0:1.0"
) {
    fun playMenuOpen(player: org.bukkit.entity.Player) = playSound(player, menuOpen)
    fun playMenuClose(player: org.bukkit.entity.Player) = playSound(player, menuClose)
    fun playClickSuccess(player: org.bukkit.entity.Player) = playSound(player, clickSuccess)
    fun playClickFail(player: org.bukkit.entity.Player) = playSound(player, clickFail)
    
    private fun playSound(player: org.bukkit.entity.Player, sound: String) {
        val parts = sound.split(":")
        val soundType = org.bukkit.Sound.valueOf(parts[0])
        val volume = parts.getOrNull(1)?.toFloatOrNull() ?: 1.0f
        val pitch = parts.getOrNull(2)?.toFloatOrNull() ?: 1.0f
        player.playSound(player.location, soundType, volume, pitch)
    }
}

data class Background(
    val pattern: BackgroundPattern,
    val material: String,
    val borderMaterial: String?
)

data class MenuItem(
    val id: String,
    val slot: Int,
    val material: String,
    val amount: Int,
    val glow: Boolean,
    val display: ItemDisplay,
    val actions: Map<ClickType, String>,
    val requirement: Requirement?,
    val effects: ItemEffects?,
    val toggle: ToggleStates?,
    val cycle: CycleValues?,
    val dynamic: Boolean
)

data class ItemDisplay(
    val name: String,
    val lore: List<String>
)

data class Requirement(
    val type: RequirementType,
    val value: Int,
    val message: String
)

data class ItemEffects(
    val hoverParticle: String?,
    val clickParticle: String?,
    val soundHover: String?
)

data class ToggleStates(
    val states: Map<String, ToggleState>
)

data class ToggleState(
    val material: String,
    val name: String,
    val action: String
)

data class CycleValues(
    val values: List<String>,
    val currentKey: String,
    val wrap: Boolean
)

data class AutoPopulate(
    val source: String,
    val startSlot: Int,
    val skipSlots: List<Int>
)

enum class AnimationType {
    EXPAND, FADE, SLIDE, SHRINK
}

enum class BackgroundPattern {
    BORDER, CHECKER, FULL, CUSTOM
}

enum class ClickType {
    LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT
}

enum class RequirementType {
    NONE, LEVEL, PERMISSION, IN_CLAN, PLACEMENT_MATCHES, ELO_RANGE, CUSTOM
}