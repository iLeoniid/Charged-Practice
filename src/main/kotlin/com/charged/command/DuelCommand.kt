package com.charged.command

import com.charged.Charged
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DuelCommand(private val plugin: Charged) : CommandExecutor {
    
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cSolo jugadores pueden usar este comando.")
            return true
        }
        
        if (args.isEmpty()) {
            sender.sendMessage("§cUso: /duel <jugador>")
            return true
        }
        
        val target = plugin.server.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage(plugin.plugin.languageManager.translate("player-not-found"))
            return true
        }
        
        if (target.uniqueId == sender.uniqueId) {
            sender.sendMessage("§cNo puedes retarte a ti mismo.")
            return true
        }
        
        // Check if already in match
        if (plugin.plugin.matchManager.isInMatch(sender.uniqueId)) {
            sender.sendMessage("§cYa estás en un duelo.")
            return true
        }
        
        if (plugin.plugin.matchManager.isInMatch(target.uniqueId)) {
            sender.sendMessage("§c${target.name} ya está en un duelo.")
            return true
        }
        
        // Send duel request
        sender.sendMessage("§aSolicitud de duelo enviada a §f${target.name}")
        target.sendMessage("§f${sender.name} §7te ha retado a un duelo.")
        
        return true
    }
}