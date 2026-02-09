package com.charged.command

import com.charged.Charged
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StatsCommand(private val plugin: Charged) : CommandExecutor {
    
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val targetName = if (args.isEmpty()) {
            if (sender is Player) sender.name else {
                sender.sendMessage("§cEspecifica un jugador.")
                return true
            }
        } else {
            args[0]
        }
        
        // Get stats from database
        val stats = plugin.plugin.playerManager.getStats(targetName)
        
        if (stats == null) {
            sender.sendMessage("§cJugador no encontrado.")
            return true
        }
        
        // Display stats
        sender.sendMessage("§6§l━━━ Estadísticas de $targetName ━━━")
        sender.sendMessage("§7ELO: §f${stats.elo}")
        sender.sendMessage("§7Victorias: §a${stats.wins}")
        sender.sendMessage("§7Derrotas: §c${stats.losses}")
        sender.sendMessage("§7WLR: §f${stats.wlr}")
        sender.sendMessage("§7Racha: §f${stats.winstreak}")
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        return true
    }
}