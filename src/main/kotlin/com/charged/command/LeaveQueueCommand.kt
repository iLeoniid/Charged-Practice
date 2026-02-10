package com.charged.command

import com.charged.util.PluginAccess
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LeaveQueueCommand : CommandExecutor {

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

        // val chargedPlugin = PluginAccess.plugin()
        // if (!chargedPlugin.queueManager.isInQueue(sender.uniqueId)) {
        //     sender.sendMessage("§cNo estás en cola.")
        //     return true
        // }

        // chargedPlugin.queueManager.leaveQueue(sender.uniqueId)
        sender.sendMessage("§c✗ Saliste de la cola")

        return true
    }
}