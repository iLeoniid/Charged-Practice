package com.charged.commands

import com.charged.Charged
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class StaffModeCommand(private val plugin: Charged) : CommandExecutor, TabCompleter {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be used by players!")
            return true
        }
        
        when (label.lowercase()) {
            "staffmode", "staff", "sm", "mod" -> {
                plugin.plugin.staffModeManager.toggleStaffMode(sender)
            }
            "vanish", "v" -> {
                if (!plugin.plugin.staffModeManager.isInStaffMode(sender)) {
                    sender.sendMessage("§cYou must be in staff mode to use this!")
                    return true
                }
                plugin.plugin.staffModeManager.toggleVanish(sender)
            }
            "freeze" -> {
                if (args.isEmpty()) {
                    sender.sendMessage("§cUsage: /freeze <player>")
                    return true
                }
                
                val target = plugin.server.getPlayer(args[0])
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!")
                    return true
                }
                
                if (plugin.plugin.staffModeManager.isFrozen(target)) {
                    plugin.plugin.staffModeManager.unfreezePlayer(target, sender)
                } else {
                    plugin.plugin.staffModeManager.freezePlayer(target, sender)
                }
            }
        }
        
        return true
    }
    
    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String> {
        if (label.lowercase() == "freeze" && args.size == 1) {
            return plugin.server.onlinePlayers
                .map { it.name }
                .filter { it.lowercase().startsWith(args[0].lowercase()) }
        }
        return emptyList()
    }
}