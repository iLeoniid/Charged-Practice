package com.charged

import com.charged.commands.*
import com.charged.menu.*
import com.charged.mode.*

/**
 * This file shows the additions needed in Charged.kt main class
 * Add these to your main Charged class
 */

class ChargedIntegration {
    
    // Add to class properties:
    lateinit var menuManager: MenuManager
    lateinit var placeholderManager: PlaceholderManager
    lateinit var buildModeManager: BuildModeManager
    lateinit var staffModeManager: StaffModeManager
    
    // Add to onEnable():
    fun initializeNewSystems(plugin: Charged) {
        plugin.logger.info("§a[Charged] Initializing menu system...")
        
        // Initialize managers
        menuManager = MenuManager(plugin)
        placeholderManager = PlaceholderManager(plugin)
        buildModeManager = BuildModeManager(plugin)
        staffModeManager = StaffModeManager(plugin)
        
        // Register listeners
        plugin.server.pluginManager.registerEvents(MenuListener(plugin), plugin)
        
        // Register commands
        plugin.getCommand("menu")?.setExecutor(MenuCommand(plugin))
        plugin.getCommand("play")?.setExecutor(MenuCommand(plugin))
        plugin.getCommand("queue")?.setExecutor(MenuCommand(plugin))
        plugin.getCommand("ranked")?.setExecutor(MenuCommand(plugin))
        plugin.getCommand("settings")?.setExecutor(MenuCommand(plugin))
        
        plugin.getCommand("buildmode")?.setExecutor(BuildModeCommand(plugin))
        plugin.getCommand("build")?.setExecutor(BuildModeCommand(plugin))
        plugin.getCommand("bm")?.setExecutor(BuildModeCommand(plugin))
        
        val staffCmd = StaffModeCommand(plugin)
        plugin.getCommand("staffmode")?.setExecutor(staffCmd)
        plugin.getCommand("staff")?.setExecutor(staffCmd)
        plugin.getCommand("sm")?.setExecutor(staffCmd)
        plugin.getCommand("mod")?.setExecutor(staffCmd)
        plugin.getCommand("vanish")?.setExecutor(staffCmd)
        plugin.getCommand("v")?.setExecutor(staffCmd)
        plugin.getCommand("freeze")?.setExecutor(staffCmd)
        
        plugin.logger.info("§a[Charged] Menu system initialized!")
        plugin.logger.info("§a[Charged] ✓ MenuManager loaded")
        plugin.logger.info("§a[Charged] ✓ PlaceholderManager (50+ placeholders)")
        plugin.logger.info("§a[Charged] ✓ BuildModeManager ready")
        plugin.logger.info("§a[Charged] ✓ StaffModeManager ready")
    }
}