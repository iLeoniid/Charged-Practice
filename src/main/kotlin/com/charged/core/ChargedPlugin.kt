package com.charged.core

import com.charged.Charged
import com.charged.achievement.manager.AchievementManager
import com.charged.arena.manager.ArenaManager
import com.charged.clan.ClanManager
import com.charged.command.CommandManager
import com.charged.config.ConfigManager
import com.charged.database.impl.SQLiteDatabase
import com.charged.gui.GUIManager
import com.charged.match.manager.MatchManager
import com.charged.menu.MenuManager
import com.charged.player.manager.PlayerManager
import com.charged.queue.QueueManager
import com.charged.scoreboard.ScoreboardManager
import com.charged.util.PluginAccess.plugin
import com.charged.web.WebMatchManager
import org.bukkit.Bukkit
import java.io.File

class ChargedPlugin(val javaPlugin: Charged) {

    // TODOS los managers necesarios
    lateinit var configManager: ConfigManager
    lateinit var database: SQLiteDatabase
    lateinit var playerManager: PlayerManager
    lateinit var arenaManager: ArenaManager
    lateinit var guiManager: GUIManager
    lateinit var scoreboardManager: ScoreboardManager
    lateinit var matchManager: MatchManager
    lateinit var queueManager: QueueManager
    lateinit var menuManager: MenuManager
    lateinit var clanManager: ClanManager
    lateinit var webMatchManager: WebMatchManager
    lateinit var achievementManager: AchievementManager

    // Comentar temporalmente si no existen
    // lateinit var hologramManager: com.charged.hologram.HologramManager
    // lateinit var hologramLeaderboard: HologramLeaderboard

    fun enable() {
        javaPlugin.logger.info("§e[1/8] Loading configurations...")
        configManager = ConfigManager(javaPlugin)
        configManager.loadAll()

        javaPlugin.logger.info("§e[2/8] Connecting to database...")
        database = SQLiteDatabase(File(javaPlugin.dataFolder.parentFile, "charged"))
        database.connect()
        database.setupTables()

        javaPlugin.logger.info("§e[3/8] Initializing managers...")
        playerManager = PlayerManager(javaPlugin, database)
        arenaManager = ArenaManager(javaPlugin, configManager)
        guiManager = GUIManager()
        scoreboardManager = ScoreboardManager()
        matchManager = MatchManager()
        queueManager = QueueManager()
        menuManager = com.charged.menu.MenuManager(plugin())
        clanManager = ClanManager(javaPlugin, database)
        webMatchManager = WebMatchManager(javaPlugin, database)
        achievementManager = AchievementManager(javaPlugin, database)
        // hologramLeaderboard = HologramLeaderboard()

        javaPlugin.logger.info("§e[4/8] Registering commands...")
        CommandManager(javaPlugin).registerAll()

        javaPlugin.logger.info("§e[5/8] Registering listeners...")
        registerListeners()

        javaPlugin.logger.info("§e[6/8] Starting systems...")
        startSystems()

        javaPlugin.logger.info("§e[7/8] Setting up leaderboards...")
        // setupLeaderboards()

        javaPlugin.logger.info("§e[8/8] Plugin ready!")
    }

    private fun registerListeners() {
        val pm = Bukkit.getPluginManager()
        pm.registerEvents(com.charged.listener.PlayerJoinListener(), javaPlugin)
        pm.registerEvents(com.charged.listener.PlayerQuitListener(), javaPlugin)
        pm.registerEvents(com.charged.listener.PlayerDeathListener(), javaPlugin)
        pm.registerEvents(com.charged.listener.PlayerDamageListener(), javaPlugin)
        pm.registerEvents(com.charged.listener.InventoryClickListener(), javaPlugin)
        pm.registerEvents(com.charged.listener.PlayerInteractListener(), javaPlugin)
        pm.registerEvents(com.charged.menu.MenuListener(plugin()), javaPlugin)
    }

    private fun startSystems() {
        // Iniciar sistemas que necesitan correr en segundo plano
        // queueManager ya se inicia automáticamente en su init
    }

    private fun setupLeaderboards() {
        // Opcional: Crear leaderboards automáticamente
        // val spawn = configManager.getSpawn()
        // spawn?.let {
        //     val leaderboardLoc = it.clone().add(10.0, 5.0, 0.0)
        //     hologramLeaderboard.createLeaderboard(leaderboardLoc, HologramLeaderboard.LeaderboardType.ELO)
        // }
    }

    fun disable() {
        javaPlugin.logger.info("§cShutting down...")
        playerManager.saveAll()
        matchManager.stopAll()
        queueManager.clearAll()
        database.disconnect()
        javaPlugin.logger.info("§c✓ Shutdown complete")
    }
}