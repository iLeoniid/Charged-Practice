package com.charged.commands

import com.charged.Charged
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MenuCommand(private val plugin: Charged) : CommandExecutor {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be used by players!")
            return true
        }
        
        when (label.lowercase()) {
            "menu" -> {
                // Open main menu
                plugin.plugin.menuManager.openMenu(sender, "main-menu")
            }
            "play", "queue" -> {
                // Open unranked menu
                plugin.plugin.menuManager.openMenu(sender, "unranked-menu")
            }
            "ranked" -> {
                // Open ranked menu
                plugin.plugin.menuManager.openMenu(sender, "ranked-menu")
            }
            "settings" -> {
                // Open settings menu
                plugin.plugin.menuManager.openMenu(sender, "settings-menu")
            }
        }
        
        return true
    }
}