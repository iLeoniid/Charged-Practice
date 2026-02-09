package com.charged

import com.charged.core.ChargedPlugin
import org.bukkit.plugin.java.JavaPlugin

class Charged : JavaPlugin() {

    companion object {
        @JvmStatic
        lateinit var instance: Charged
            private set
    }

    lateinit var chargedPlugin: ChargedPlugin

    override fun onLoad() {
        instance = this
        logger.info("§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.info("§e  CHARGED PvP §7- §6Loading...")
        logger.info("§7  Version: §f${description.version}")
        logger.info("§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    override fun onEnable() {
        try {
            chargedPlugin = ChargedPlugin(this)
            chargedPlugin.enable()

            logger.info("§a━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.info("§a  ✓ CHARGED PvP §7- §aEnabled!")
            logger.info("§a━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        } catch (e: Exception) {
            logger.severe("§c━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.severe("§c  ✗ FAILED TO ENABLE!")
            logger.severe("§c  ${e.message}")
            logger.severe("§c━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        if (::chargedPlugin.isInitialized) {
            chargedPlugin.disable()
        }
        logger.info("§c  ✓ CHARGED PvP Disabled")
    }
}