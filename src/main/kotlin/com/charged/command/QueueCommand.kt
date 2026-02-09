package com.charged.command

import com.charged.Charged
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class QueueCommand(private val plugin: Charged) : CommandExecutor {
    
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
            sender.sendMessage("§cUso: /queue <modo>")
            sender.sendMessage("§7Modos disponibles: nodebuff, gapple, sumo, builduhc")
            return true
        }
        
        val mode = args[0].lowercase()
        
        // Validate mode
        val validModes = listOf("nodebuff", "gapple", "sumo", "builduhc", "combo", "debuff")
        if (mode !in validModes) {
            sender.sendMessage("§cModo inválido: $mode")
            return true
        }
        
        // Check if already in queue
        if (plugin.plugin.queueManager.isInQueue(sender.uniqueId)) {
            sender.sendMessage("§cYa estás en cola. Usa §f/leavequeue§c para salir.")
            return true
        }
        
        // Check if in match
        if (plugin.plugin.matchManager.isInMatch(sender.uniqueId)) {
            sender.sendMessage("§cYa estás en un duelo.")
            return true
        }
        
        // Join queue
        plugin.plugin.queueManager.joinQueue(sender.uniqueId, mode)
        sender.sendMessage("§a✓ Te uniste a la cola de §f$mode")
        
        return true
    }
}