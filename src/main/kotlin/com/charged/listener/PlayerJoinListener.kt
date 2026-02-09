package com.charged.listener

import com.charged.Charged
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Acceder a través de la instancia
        val plugin = Charged.instance
        val chargedPlugin = plugin.chargedPlugin

        // Load player data
        chargedPlugin.playerManager.loadPlayer(player)

        // Teleport to spawn if exists
        val spawn = chargedPlugin.configManager.getSpawn()
        spawn?.let {
            player.teleport(it)
        }

        // Custom join message
        event.joinMessage = "§a▶ §7${player.name} §ajoined"
    }
}