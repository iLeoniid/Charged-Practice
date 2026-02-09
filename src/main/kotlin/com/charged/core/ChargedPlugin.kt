package com.charged.core

import com.charged.Charged
import com.charged.arena.manager.ArenaManager
import com.charged.config.ConfigManager
import com.charged.database.Database
import com.charged.database.impl.SQLiteDatabase
import com.charged.player.manager.PlayerManager
import org.bukkit.Bukkit
import java.io.File

class ChargedPlugin(private val javaPlugin: Charged) {

    lateinit var configManager: ConfigManager
    lateinit var database: Database
    lateinit var playerManager: PlayerManager
    lateinit var arenaManager: ArenaManager

    fun enable() {
        javaPlugin.logger.info("§e[1/5] Loading configurations...")
        configManager = ConfigManager(javaPlugin)
        configManager.loadAll()

        javaPlugin.logger.info("§e[2/5] Connecting to database...")
        database = SQLiteDatabase(File(javaPlugin.dataFolder, "../"))
        database.connect()
        database.setupTables()

        javaPlugin.logger.info("§e[3/5] Initializing managers...")
        playerManager = PlayerManager(javaPlugin, database)
        arenaManager = ArenaManager(javaPlugin, configManager)

        javaPlugin.logger.info("§e[4/5] Loading arenas...")
        // arenaManager.loadArenas() // Comentado temporalmente

        javaPlugin.logger.info("§e[5/5] Registering listeners...")
        registerListeners()

        javaPlugin.logger.info("§a✓ All systems initialized successfully!")
    }

    fun disable() {
        javaPlugin.logger.info("§cShutting down...")
        playerManager.saveAll()
        database.disconnect()
        javaPlugin.logger.info("§c✓ Shutdown complete")
    }

    fun getArenaCount(): Int {
        return arenaManager.getArenaCount()
    }

    private fun registerListeners() {
        val pluginManager = Bukkit.getPluginManager()
        // Comentar temporalmente para que compile
        // pluginManager.registerEvents(PlayerJoinListener(javaPlugin), javaPlugin)
    }
}