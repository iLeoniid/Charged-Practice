package com.charged.command

import com.charged.Charged
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LeaveQueueCommand(private val plugin: Charged) : CommandExecutor {
    
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
        
        if (!plugin.plugin.queueManager.isInQueue(sender.uniqueId)) {
            sender.sendMessage("§cNo estás en cola.")
            return true
        }
        
        plugin.plugin.queueManager.leaveQueue(sender.uniqueId)
        sender.sendMessage("§c✗ Saliste de la cola")
        
        return true
    }
}